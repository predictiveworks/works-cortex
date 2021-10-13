package de.kp.works.ignite.listener
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

import de.kp.works.ignite.cortex.CortexClient
import org.apache.ignite.Ignite
import org.apache.ignite.events.{CacheEvent, EventType}
import org.apache.ignite.lang.{IgniteBiPredicate, IgnitePredicate}

import java.util.UUID

trait PutReactor {

  def eventArrived(event:CacheEvent):Unit

}
/**
 * Event handling:
 *
 * It is highly recommended to enable only those events that
 * the application logic requires. This is achieved by using
 *
 * IgniteConfiguration.getIncludeEventTypes() method in Ignite
 * configuration.
 *
 */
class PutListener(ignite:Ignite, cacheName:String, client:CortexClient) extends PutReactor {

  private var uuid:UUID = _

  def eventArrived(event:CacheEvent):Unit = {
    // TODO
  }

  def subscribe(): Unit = {

    /*
     * Cache PUT event: Remote subscription, defined by remoteListen,
     * will add an event listener for specified events on all nodes
     * in the cluster group. All cluster group nodes will then be
     * notified of the subscribed events.
     */
    val filter: IgnitePredicate[CacheEvent] = null

    val listener: IgniteBiPredicate[UUID, CacheEvent] = new IgniteBiPredicate[UUID, CacheEvent] {
      override def apply(uuid: UUID, event: CacheEvent): Boolean = {
        /*
         * If the listener returns `false`, subscription to the
         * specified event stops. Therefore, this method always
         * returns true.
         *
         * Restrict the received events to those that refer to the
         * specified cache name
         */
        if (event.cacheName() == cacheName)
          eventArrived(event)

        true
      }
    }
    /*
     * The IgniteEvents.remoteListen(localListener, filter, types)
     * method can be used to register a listener that listens for
     * both remote and local events.
     *
     * It accepts a local listener, a filter, and a list of event
     * types you want to listen to.
     *
     * We do not leverage a remote event filter, i.e. all events are
     * sent to the (local) listener.
     */
    uuid = ignite.events()
      .remoteListen(listener, filter, EventType.EVT_CACHE_OBJECT_PUT)

  }

  def unsubscribe():Unit =
    ignite.events().stopRemoteListen(uuid)

}
