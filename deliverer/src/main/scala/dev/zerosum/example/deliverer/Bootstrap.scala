package dev.zerosum.example.deliverer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl._
import org.apache.kafka.common.serialization.StringDeserializer
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.{Mailer, MailerBuilder}

object Bootstrap extends App {

  implicit val system = ActorSystem("deliverer")
  implicit val materializer = ActorMaterializer()

  val kafkaConfig = system.settings.config.getConfig("akka.kafka")

  val bootstrapServers = "localhost:9092"

  val consumerConfig = kafkaConfig.getConfig("consumer")
  val consumerSettings =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withGroupId("group2")
      .withBootstrapServers(bootstrapServers)

  val mailer: Mailer = MailerBuilder
    .withSMTPServer("localhost", 30025)
    .buildMailer

  val control = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("rear"))
    .map { msg =>

      val email = EmailBuilder.startingBlank()
        .from("from@example.com")
        .to("to@example.com")
        .withSubject(s"========== ${msg.value}")
        .withPlainText("body")
        .buildEmail()

      mailer.sendMail(email, true)
    }
    .toMat(Sink.seq)(Keep.both)
    .run()
}
