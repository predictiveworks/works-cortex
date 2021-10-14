package de.kp.works.cortex
/*
 * Copyright (c) 2019 - 2021 Dr. Krusche & Partner PartG. All rights reserved.
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

import com.google.gson.{JsonArray, JsonObject}

import scala.collection.JavaConversions._
/**
 * A wrapper for a JSON analyzer specification that can used to
 * assign analyzer identifier to data types and vice versa.
 *
 * The parameters defined below are extracted from the available
 * Cortex API documentation
 */
class Analyzer(json:JsonObject) {

  /**
   * METADATA SPECIFICATION
   *
   * Received from Cortex Server with analyzer/_search
   * request; note, subset of parameters is visible to
   * users with `orgadmin` role.
   */

  /*
   * Analyzer definition name	(readonly)
   */
  val analyzerDefinitionId:String = json.get("analyzerDefinitionId")
    .getAsString
  /*
   * Author of the analyzer (readonly)
   */
  val author:String = json.get("author")
    .getAsString
  /*
   * Name of the analyzer	(readonly)
   */
  val name:String = json.get("name")
    .getAsString
  /*
   * Version of the analyzer (readonly)
   */
  val version:String = json.get("version")
    .getAsString
  /*
   * Description of the analyzer (readonly)
   */
  val description:String = json.get("description")
    .getAsString
  /*
   * URL where the analyzer has been published (readonly)
   */
  val url:String = json.get("url")
    .getAsString
  /*
   * License of the analyzer (readonly)
   */
  val license:String = json.get("license")
    .getAsString
  /*
    * Base configuration name. This identifies the shared
    * set of configuration with all the analyzer's flavors
    * (readonly)
    */
  val baseConfig:String = json.get("baseConfig")
    .getAsString
  /*
   * User who enabled the analyzer (computed)
   */
  val createdBy:String = json.get("createdBy")
    .getAsString
  /*
   * User who last updated the analyzer (computed)
   */
  val updatedBy:String = json.get("updatedBy")
    .getAsString
  /*
   * Last update date (computed)
   */
  val updatedAt:String = try {
    json.get("updatedAt").getAsString

  } catch {
    case _:Throwable =>
      json.get("updatedAt").getAsLong.toString
  }
  /*
   * Report cache timeout in minutes, visible for orgAdmin
   * users
   */
  val jobCache:Int =
    if (json.has("jobCache")) {
      try {
        json.get("jobCache").getAsInt

      } catch {
        case _:Throwable =>
          json.get("jobCache").getAsString.toInt
      }

    }
    else 0
  /*
   * Numeric amount of analyzer calls authorized for
   * the specified rateUnit, visible for orgAdmin users
   * only
   */
  val rate:Int = {
    if (json.has("rate"))
      json.get("rate").getAsInt

    else -1
  }
  /*
   * Period of availability of the rate limit: Day or Month,
   * visible for orgAdmin users only
   */
  val rateUnit:String = {
    if (json.has("rateUnit"))
      json.get("rateUnit").getAsString

    else ""
  }

  /**
   * ANALYZING SUPPORT SPECIFICATION
   *
   * Received from Cortex Server with analyzer/_search
   * request; note, subset of parameters is visible to
   * users with `orgadmin` role.
   */

  /*
   * Analyzer ID once enabled within an organization (readonly).
   * Note, every analyzer request requires a specific `id`.
   *
   * Therefore, before initiating an analyzer job, the list of
   * enabled analyzers must be retrieved.
   */
  val id:String = json.get("id")
    .getAsString
  /*
   * Allowed data types, e.g. "ip"
   */
  val dataTypes: List[String] = json.get("dataTypeList")
    .getAsJsonArray.map(_.getAsString).toList
  /*
   * A JSON object where key/value pairs represent the config names,
   * and their values. It includes the default properties proxy_http,
   * proxy_https, auto_extract_artifacts, check_tlp, and max_tlp,
   * visible for orgAdmin users only
   *
   * Note, as we want to initiate analyzer jobs for users other
   * than those that are `orgadmin` users, the respective interface
   * parameters must be provided via default values.
   */
  val configuration: JsonObject = getConfiguration
  /*
   * This analyzer field is not part of the API documentation, but
   * it is referenced in the Cortex4Py implementation; therefore,
   * we also check whether this field is available
   */
  val configurationItems: JsonArray =
    if (json.has("configurationItems"))
      json.get("configurationItems").getAsJsonArray
    else
      new JsonArray

  private def getConfiguration:JsonObject = {
    /*
     * TheHive provides misleading implementations
     * about the configuration parameter name
     */
    var fname:String = ""
    List("config", "configuration").foreach(name => {
      if (json.has(name)) fname = name
    })

    if (name.isEmpty) return new JsonObject
    json.get(name).getAsJsonObject

  }
}
