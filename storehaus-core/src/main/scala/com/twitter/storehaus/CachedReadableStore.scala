/*
 * Copyright 2013 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.storehaus

import com.twitter.util.Future

// TODO: Should we throw some special exception about a value that
// never made it into the cache vs Future.None?

class CachedReadableStore[-K, V](store: ReadableStore[K, V], cache: Cache[K, Future[Option[V]]]) extends ReadableStore[K, V] {
  override def get(k: K): Future[Option[V]] = {
    cache.touch(k)(store.get(_))
      .get(k).getOrElse(Future.None)
  }
  override def multiGet[K1 <: K](keys: Set[K1]): Map[K1, Future[Option[V]]] = {
    val pinged = cache.multiTouch(keys)(store.multiGet(_))
    CollectionOps.zipWith(keys) { pinged.get(_).getOrElse(Future.None) }
  }
}