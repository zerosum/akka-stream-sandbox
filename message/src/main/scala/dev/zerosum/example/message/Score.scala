package dev.zerosum.example.message

import java.util.UUID
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._


case class Score(id: String, person: Person, value: Int)

object Score {
  def create(person: Person, value: Int): Score = {
    Score(UUID.randomUUID().toString, person, value)
  }

  private def _marshal(score: Score): String = score.asJson.noSpaces

  def unmarshal(json: String): Score = {
      decode[Score](json).fold(
        e => throw e,
        score => score
      )
  }

  implicit class ScoreMarshaller(val value: Score) {
    def marshal: String = _marshal(value)
  }

}

