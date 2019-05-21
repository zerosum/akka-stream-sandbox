package dev.zerosum.example.receiver

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream._
import akka.stream.scaladsl._
import dev.zerosum.example.message.{Person, Score}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent._
import scala.util.Random

object Bootstrap extends App {

  implicit val system = ActorSystem("receiver")
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  def firstNameStream: Stream[String] = Person.generateFirstName #:: firstNameStream
  def lastNameStream: Stream[String] = Person.generateLastName #:: lastNameStream

  def scoreStream: Stream[Int] = (Random.nextInt(50) + 50) #:: scoreStream

  val firstNameSource = Source(firstNameStream.take(10))
  val lastNameSource = Source(lastNameStream.take(10))
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
      .map(score => new ProducerRecord[String, String]("front", score.id, score.marshal))
      .runWith(Producer.plainSink(producerSettings))
}
