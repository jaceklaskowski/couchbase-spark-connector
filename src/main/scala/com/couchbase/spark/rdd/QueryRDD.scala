/*
 * Copyright (c) 2015 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.spark.rdd

import java.util.concurrent.TimeUnit

import com.couchbase.client.core.BackpressureException
import com.couchbase.client.core.time.Delay
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.error.{CouchbaseOutOfMemoryException, TemporaryFailureException}
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.util.retry.RetryBuilder
import com.couchbase.spark.connection.{CouchbaseConfig, CouchbaseConnection, QueryAccessor}
import com.couchbase.spark.internal.LazyIterator
import org.apache.spark.{Logging, Partition, SparkContext, TaskContext}
import org.apache.spark.rdd.RDD
import rx.lang.scala.JavaConversions._

case class CouchbaseQueryRow(value: JsonObject)

class QueryRDD(@transient sc: SparkContext, query: N1qlQuery, bucketName: String = null)
  extends RDD[CouchbaseQueryRow](sc, Nil)
  with Logging {

  private val cbConfig = CouchbaseConfig(sc.getConf)

  override def compute(split: Partition, context: TaskContext): Iterator[CouchbaseQueryRow] =
    new QueryAccessor(cbConfig, Seq(query), bucketName).compute()

  override protected def getPartitions: Array[Partition] = Array(new CouchbasePartition(0))
}

object QueryRDD {

  def apply(sc: SparkContext, bucketName: String, query: N1qlQuery) =
    new QueryRDD(sc, query, bucketName)

}
