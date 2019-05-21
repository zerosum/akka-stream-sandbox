package dev.zerosum.example.transferer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object Bootstrap extends App {

  implicit val system = ActorSystem("transferer")
  implicit val materializer = ActorMaterializer()

  val kafkaConfig = system.settings.config.getConfig("akka.kafka")

  val bootstrapServers = "localhost:32774"

  val consumerConfig = kafkaConfig.getConfig("consumer")
  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withGroupId("group1")
      .withBootstrapServers(bootstrapServers)

  val producerConfig = kafkaConfig.getConfig("producer")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val control = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("front"))
    .filter(msg => msg.value.toInt % 2 != 0)
    .map(msg => new ProducerRecord[String, String]("rear", msg.value))
    .toMat(Producer.plainSink(producerSettings))(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()
}
