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

import com.google.gson.JsonArray

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * The [AnalyzerRegistry] contains all registered
 * analyzers and their respective configurations
 */
object AnalyzerRegistry {

  private val registry = mutable.HashMap.empty[String, Analyzer]

  private val dataTypeIndex = mutable.HashMap.empty[String, Seq[Analyzer]]
  private val nameIndex = mutable.HashMap.empty[String, Analyzer]

  def register(analyzers: JsonArray): Unit = {

    analyzers.foreach(json => {
      /*
       * STEP #1: Plain registration of the retrieved
       * analyzer
       */
      val analyzer = new Analyzer(json.getAsJsonObject)
      registry += analyzer.id -> analyzer
      /*
       * STEP #2: The analyzer registry provides analyzer
       * indices for the name and the data types of the
       * analyzers
       */
      nameIndex += analyzer.name -> analyzer

      val dataTypes = analyzer.dataTypes
      dataTypes.foreach(dataType => {

        if (!dataTypeIndex.contains(dataType))
          dataTypeIndex += dataType -> Seq.empty[Analyzer]

        val values = dataTypeIndex(dataType) ++ Seq(analyzer)
        dataTypeIndex += dataType -> values

      })
    })

  }

  def getByName(name:String): Option[Analyzer] =
    nameIndex.get(name)

  def getByType(dataType:String):Option[Seq[Analyzer]] =
    dataTypeIndex.get(dataType)

}
