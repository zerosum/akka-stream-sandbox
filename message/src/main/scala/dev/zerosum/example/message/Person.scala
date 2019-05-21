package dev.zerosum.example.message

import scala.util.Random

case class Person(firstName: String, lastName: String) {

  lazy val fullName: String = s"$firstName $lastName"
}

object Person {

  private val firstNames = Seq("Alice", "Bob", "Cathy", "Denis", "Emma")

  private val lastNames = Seq("Foster", "Gibson", "Hammond", "Irvine", "Jefferson")

  def generateFirstName: String = firstNames(Random.nextInt(firstNames.length))

  def generateLastName: String = lastNames(Random.nextInt(lastNames.length))
}
