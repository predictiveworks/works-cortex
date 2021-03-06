package de.kp.works.http
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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.google.gson._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

trait HttpConnect {
      
  implicit val system: ActorSystem = ActorSystem("http-cortex-system")

  implicit lazy val context: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  /*
   * Common timeout for all Akka connection  
   */
  val duration: FiniteDuration = 30.seconds
  implicit val timeout: Timeout = Timeout(duration)

  def getJson(endpoint:String):JsonElement = {
    extractJsonBody(get(endpoint))    
  }

  def extractJsonBody(source:Source[ByteString,Any]):JsonElement = {
    
    /* Extract body as String from request entity */
    val future = source.runFold(ByteString(""))(_ ++ _)
    /*
     * We do not expect to retrieve large messages 
     * and accept a blocking wait
     */
    val bytes = Await.result(future, timeout.duration)

    val body = bytes.decodeString("UTF-8")      
    JsonParser.parseString(body)
 
  }

  def post(endpoint:String, headers:Map[String,String], query:String):Source[ByteString,Any] = {

    try {
      
      val reqEntity = HttpEntity(`application/json`, ByteString(query))
      val reqHeaders = headers.map{case(k,v) => RawHeader(k, v)}.toList
      
      val request = HttpRequest(HttpMethods.POST, endpoint, entity=reqEntity, headers=reqHeaders)
      val future: Future[HttpResponse] = Http(system).singleRequest(request)

      val response = Await.result(future, duration)

      val status = response.status
      if (status != StatusCodes.OK)
        throw new Exception(s"Request to Http endpoint returns with: ${status.value}. Reason: ${status.reason}")

      response.entity.dataBytes

    } catch {
      case t:Throwable => 
        throw new Exception(t.getLocalizedMessage)
    }
  
  }

  def get(endpoint:String, headers:Map[String,String]=Map.empty[String,String]):Source[ByteString,Any] = {

    try {
      
      val request = {
        
        if (headers.isEmpty)        
          HttpRequest(HttpMethods.GET, endpoint)
          
        else
          HttpRequest(HttpMethods.GET, endpoint, headers=headers.map{case(k,v) => RawHeader(k, v)}.toList)

      }

      val future: Future[HttpResponse] = Http(system).singleRequest(request)

      val response = Await.result(future, duration)

      val status = response.status
      if (status == StatusCodes.OK)
        return response.entity.dataBytes
      /*
       * YFinance returns 422, if the combination of 'interval'
       * and 'range' is not supported. The result, however, is
       * a valid JSON element
       */
      if (status == StatusCodes.UnprocessableEntity)
        return response.entity.dataBytes
        
      throw new Exception(s"Request to Http endpoint returns with: ${status.value}. Reason: ${status.reason}")

    } catch {
      case t:Throwable => 
        throw new Exception(t.getLocalizedMessage)
    }

  }

}