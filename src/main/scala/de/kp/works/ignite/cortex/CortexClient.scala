package de.kp.works.ignite.cortex
/*
 * Copyright (c) 2020 - 2021 Dr. Krusche & Partner PartG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Stefan Krusche, Dr. Krusche & Partner PartG
 *
 */

import com.google.gson._
import com.typesafe.config.Config
import de.kp.works.cortex.AnalyzerRegistry
import de.kp.works.http.HttpConnect

import scala.collection.JavaConversions._

class CortexClient(cfg:Config) extends HttpConnect {

  private val apiKey = cfg.getString("apiKey")
  /**
   * Extract the endpoint configuration
   * parameters and build the base url
   */
  private val baseUrl = {

    /* The host of the Cortex Server */
    val host = cfg.getString("host")

    /* The port of the Cortex Server */
    val port = cfg.getInt("port")

    /* The protocol of the Cortex Server */
    val protocol = cfg.getString("protocol")
    s"$protocol://$host:$port/api"

  }
  /**
   * This methods allows a user to retrieve all enabled
   * analyzers within his organization.
   *
   * The result is used to populate the [CortexRegistry],
   * a prerequisite step to enable analyzer requests.
   *
   * POST /api/analyzer/_search
   *
   * Authorization requires the API_KEY
   */
  def analyzers:Boolean = {

    val endpoint = s"$baseUrl/analyzer/_search"
    /*
     * It is expected that the Cortex server determines
     * the current user and its roles from the respective
     * API_KEY
     */
    val headers = Map(
      "Authorization" -> s"Bearer ${apiKey}",
      "Content-Type"-> "application/json"
    )
    /*
     * The request body specifies the query and
     * respective range (all analyzers):
     *
     * {
     *    "query" : {}, "range" : "all"
     * }
     */
    val body = new JsonObject
    body.add("query", new JsonObject)
    body.addProperty("range", "all")
    /*
     * Send POST request to Cortex Server
     * and extract the result (which is a
     * JSON Array
     */
    val result = try {

      val source = post(endpoint, headers, body.toString)
      extractJsonBody(source).getAsJsonArray

    } catch {
      case _:Throwable => new JsonArray
    }

    if (result.isEmpty) return false
    AnalyzerRegistry.register(result)

    true

  }

  def close():Unit = ???
  /**
   * This method is used to validate whether the
   * provided user name & password exists. This is
   * used as an initial check.
   *
   * Most other request require authentication and
   * there the configured API_KEY must be used
   *
   * Note, all other request even with API_KEY run
   * within an authenticated session
   */
  def isLogin:Boolean = {

    val endpoint = s"$baseUrl/login"

    val user = cfg.getString("user")
    val password = cfg.getString("password")

    val headers = Map.empty[String,String]

    val body = new JsonObject
    body.addProperty("user", user)
    body.addProperty("password", password)

    val result = try {

      val source = post(endpoint, headers, body.toString)
      extractJsonBody(source).getAsJsonObject

    } catch {
      case _:Throwable => new JsonObject
    }

    if (result.keySet().isEmpty) return false

    if (result.has("type")) {
      /*
       * We do not have to check whether the `type`
       * parameter is specified as `AuthenticationError`
       */
      false

    } else {

      val name = result.get("name").getAsString
      if (name != user) return false

      val roles = result.get("roles")
        .getAsJsonArray.map(_.getAsString)
        .toList
      /*
       * Login is successful, if the configured user
       * has the respective roles assigned to access
       * the analyzer API.
       */
      if (roles.contains("analyze") || roles.contains("orgadmin"))
        true

      else false

    }
  }
  /**
   * This method allows a user with an `analyze` or `orgAdmin`
   * role to run analyzers on observables of different data
   * types.
   *
   * This request creates an analyzer job and returns the
   * respective job description
   */
  def run:Unit = ???

  private def buildRequestBody(data:String, dataType:String):JsonObject = {

    val body = new JsonObject

    body.addProperty("data", data)
    body.addProperty("dataType", dataType)
    body

  }
}
