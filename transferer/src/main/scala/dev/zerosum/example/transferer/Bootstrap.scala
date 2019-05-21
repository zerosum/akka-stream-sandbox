package dev.zerosum.example.transferer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl._
import dev.zerosum.example.message.Score
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object Bootstrap extends App {

  implicit val system = ActorSystem("transferer")
  implicit val materializer = ActorMaterializer()

  val kafkaConfig = system.settings.config.getConfig("akka.kafka")

  val bootstrapServers = "localhost:9092"

  val consumerConfig = kafkaConfig.getConfig("consumer")
  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withGroupId("group1")
      .withBootstrapServers(bootstrapServers)

  val producerConfig = kafkaConfig.getConfig("producer")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  import Score.ScoreMarshaller
  val control = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("front"))
    .map(msg => Score.unmarshal(msg.value()))
    .filter(_.value >= 80)
    .map(score => new ProducerRecord[String, String]("rear", score.id, score.marshal))
    .toMat(Producer.plainSink(producerSettings))(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()
}
