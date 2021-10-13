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

import com.google.gson.JsonObject
import com.typesafe.config.Config

class CortexClient(cfg:Config) {
  /**
   * Extract the endpoint configuration
   * parameters and build the base url
   */
  private val endpoint = {

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
   */
  def analyzers:Boolean = ???

  def close():Unit = ???
  /*
   * This method is used to validate whether the
   * provided user name & password exists. This is
   * used as an initial check.
   *
   * Most other request require authentication and
   * there the configured API_KEY must be used
   */
  def isLogin:Boolean = ???
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
