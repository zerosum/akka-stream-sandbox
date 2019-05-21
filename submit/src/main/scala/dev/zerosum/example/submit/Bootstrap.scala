package dev.zerosum.example.submit

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{GraphDSL, Source, ZipWith}
import akka.stream.{ActorMaterializer, SourceShape}
import dev.zerosum.example.message.{Person, Score}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import scala.util.Random

object Bootstrap extends App {

  implicit val system = ActorSystem("submit")
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  def firstNameStream: Stream[String] = Person.generateFirstName #:: firstNameStream
  def lastNameStream: Stream[String] = Person.generateLastName #:: lastNameStream

  def scoreStream: Stream[Int] = (Random.nextInt(50) + 50) #:: scoreStream

  val firstNameSource = Source(firstNameStream)
  val lastNameSource = Source(lastNameStream)
  val randomScoreSource = Source(scoreStream)

  val scores = Source.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zipPerson = b.add(ZipWith[String, String, Person](Person(_, _)))
    val zipScore = b.add(ZipWith[Person, Int, Score](Score.create))

    firstNameSource ~> zipPerson.in0
    lastNameSource ~> zipPerson.in1

    zipPerson.out ~> zipScore.in0
    randomScoreSource ~> zipScore.in1

    SourceShape(zipScore.out)
  })

  import Score.ScoreMarshaller
  val done: Future[Done] = scores
      .map { score =>
        println(s"====== scoring result: $score")
        new ProducerRecord[String, String]("submitted", score.id, score.marshal)
      }
      .runWith(Producer.plainSink(producerSettings))
}
