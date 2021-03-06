package org.sunbird.job.searchindexer.functions

import com.google.gson.reflect.TypeToken
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.slf4j.LoggerFactory
import org.sunbird.job.searchindexer.compositesearch.domain.Event
import org.sunbird.job.searchindexer.task.SearchIndexerConfig
import org.sunbird.job.{BaseProcessKeyedFunction, Metrics}

import java.lang.reflect.Type

class TransactionEventRouter(config: SearchIndexerConfig)
  extends BaseProcessKeyedFunction[String, Event, String](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[TransactionEventRouter])
  val mapType: Type = new TypeToken[java.util.Map[String, AnyRef]]() {}.getType

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
  }

  override def close(): Unit = {
    super.close()
  }

  override def processElement(event: Event, context: KeyedProcessFunction[String, Event, String]#Context, metrics: Metrics): Unit = {
    metrics.incCounter(config.totalEventsCount)
    if (event.validEvent(config.restrictObjectTypes)) {
      event.nodeType match {
        case "SET" | "DATA_NODE" => context.output(config.compositeSearchDataOutTag, event)
        case "EXTERNAL" => context.output(config.dialCodeExternalOutTag, event)
        case "DIALCODE_METRICS" => context.output(config.dialCodeMetricOutTag, event)
        case _ => {
          logger.info(s"UNKNOWN EVENT NODETYPE : ${event.nodeType} for Identifier : ${event.id}.")
          metrics.incCounter(config.skippedEventCount)
        }
      }
    } else {
      metrics.incCounter(config.skippedEventCount)
      logger.info(s"Event not qualified for indexing for Identifier : ${event.id}.")
    }
  }

  override def metricsList(): List[String] = {
    List(config.totalEventsCount, config.skippedEventCount)
  }

}