package dev.zerosum.example.receiver

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

object Bootstrap extends App {

  implicit val system = ActorSystem("receiver")
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:32797")

  val done: Future[Done] =
    Source(1 to 100)
      .map(_.toString)
      .map(value => new ProducerRecord[String, String]("front", value))
      .runWith(Producer.plainSink(producerSettings))
}
