package preflop.ranger

object Hands {
  private val cards = Array("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2").zipWithIndex
  val hands: Array[Array[String]] = cards.map { case (c1, i1) =>
    cards.map {
      case (c2, i2) if i2 == i1 => c1 + c2
      case (c2, i2) if i2 > i1  => c1 + c2 + "s"
      case (c2, i2) if i2 < i1  => c2 + c1 + "o"
    }
  }
}
