package de.kp.works.ignite.service
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

import com.typesafe.config.Config
import org.apache.ignite.Ignite
import org.apache.ignite.services.ServiceConfiguration

object CortexProvider {

  val SERVICE_NAME:String = "CortexService"

}
/**
 * This class is responsible for providing
 * (installing) the CortexService on the
 * current local node
 */
class CortexProvider(ignite:Ignite, cortexCfg:Config) {

  import CortexProvider._
  /*
   * The services are not restricted to a
   * certain cluster group
   */
  private val services = ignite.services()

  def getOrCreateService():CortexService = {

    var service:CortexService = null
    try {
      /*
       * Try to retrieve an already existing Cortex service;
       * note, the service class must be an interface
       */
      service = services.serviceProxy(SERVICE_NAME, classOf[AbstractService], true)

     } catch {
      case _:Throwable => /* Do nothing */
    }

    if (service == null) {
      try {
        /*
         * Initialize [CortexService]
         */
        service = new CortexService(ignite, cortexCfg)

        val serviceCfg = new ServiceConfiguration()
        serviceCfg.setName(SERVICE_NAME)
        /*
         * Sets maximum number of deployed service
         * instances on each node, 0 for unlimited.
         */
        val maxPerNodeCount = cortexCfg.getInt("maxPerNodeCount")
        serviceCfg.setMaxPerNodeCount(maxPerNodeCount)
        /*
         * Sets total number of deployed service
         * instances in the cluster, 0 for unlimited.
         */
        val totalCount = cortexCfg.getInt("totalCount")
        serviceCfg.setTotalCount(totalCount)

        services.deploy(serviceCfg)
        service

      } catch {
        case _:Throwable => null
      }

    } else service

  }
}
