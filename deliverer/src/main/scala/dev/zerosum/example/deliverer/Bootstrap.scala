package dev.zerosum.example.deliverer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl._
import org.apache.kafka.common.serialization.StringDeserializer

object Bootstrap extends App {

  implicit val system = ActorSystem("deliverer")
  implicit val materializer = ActorMaterializer()

  val kafkaConfig = system.settings.config.getConfig("akka.kafka")

  val bootstrapServers = "localhost:32797"

  val consumerConfig = kafkaConfig.getConfig("consumer")
  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withGroupId("group2")
      .withBootstrapServers(bootstrapServers)

  val control = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("rear"))
    .map( msg => println(s"========== ${msg.value}"))
    .toMat(Sink.seq)(Keep.both)
    .run()
}
