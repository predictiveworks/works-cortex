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
import de.kp.works.ignite.cortex.CortexClient
import de.kp.works.ignite.listener.PutListener
import org.apache.ignite.Ignite
import org.apache.ignite.services.ServiceContext

class CortexService(ignite:Ignite, cfg:Config) extends AbstractService {

  private var client:CortexClient = _
  private var listener:PutListener = _
  /**
   * This method cancels the service execution
   * and closes all associated resources
   */
  override def cancel(serviceContext: ServiceContext): Unit = {
    listener.unsubscribe()
    client.close()
  }
  /**
   * This method is called by Ignite before the service is
   * deployed (and before the execute() method is called).
   *
   * It is used to build the Change (PUT) listener that
   * monitors the observable cache. Note, this service
   * expects that the respective cache is built already.
   */
  override def init(serviceContext: ServiceContext): Unit = {

    /* STEP #1: Check whether the configured cache
     * name exists
     */
    val cacheNames = ignite.cacheNames()
    val cacheName = cfg.getString("cacheName")

    if (!cacheNames.contains(cacheName))
      throw new Exception(
        s"[CortexService] Cyber observable cache does not exist.")
    /*
     * STEP #2 Connect to external (TheHive) Cortex
     * server
     */
    val receiverCfg = cfg.getConfig("receiver")
    client = new CortexClient(receiverCfg)

    if (!client.isLogin)
      throw new Exception(
        s"[CortexService] The configured user name & password is not accepted.")

    /*
     * STEP #3: Build the Change (PUT) listener to send
     * cyber observable to the (TheHive) Cortex server
     */
    listener = new PutListener(ignite, cacheName, client)
    listener.subscribe()

  }
  /**
   * This method starts execution of the service;
   * this is performed by Ignite and no further
   * action must be taken.
   */
  override def execute(serviceContext: ServiceContext): Unit = {/* Do nothing */}

}
