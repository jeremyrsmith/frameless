import frameless._

object TestHelpers {
  def checkAnswer[A <: Product](tf: TypedDataFrame[A], seq: Seq[A])(implicit t: TypeableRow[A]): Unit =
    assert(tf.collect() == seq)

  def checkAnswer[A <: Product](tf: TypedDataFrame[A], set: Set[A])(implicit t: TypeableRow[A]): Unit =
    assert(tf.collect().toSet == set)
}
