package preflop.ranger

import scalafx.scene.paint.Color

sealed trait HandAction

sealed abstract class _100(val color: Color) extends HandAction

sealed trait Mixed extends HandAction {
  def color1: Color
  def ratio1: Double
  def color2: Color
  def ratio2: Double
}

sealed abstract class _50_50(val color1: Color, val color2: Color) extends Mixed {
  val ratio1 = 0.5
  val ratio2 = 0.5
}
sealed abstract class _75_25(val color1: Color, val color2: Color) extends Mixed {
  val ratio1 = 0.75
  val ratio2 = 0.25
}
sealed abstract class _25_75(val color1: Color, val color2: Color) extends Mixed {
  val ratio1 = 0.25
  val ratio2 = 0.75
}

object HandAction {
  val rColor  = Color.DarkSalmon
  val fColor  = Color.DeepSkyBlue
  val cColor  = Color.LightGreen
  val noColor = Color.Gray

  case object R_100 extends _100(rColor)
  case object F_100 extends _100(fColor)
  case object C_100 extends _100(cColor)
  case object N_100 extends _100(noColor)

  case object R_50_F_50 extends _50_50(rColor, fColor)
  case object C_50_F_50 extends _50_50(cColor, fColor)
  case object R_50_C_50 extends _50_50(rColor, cColor)
  case object R_50_N_50 extends _50_50(rColor, noColor)
  case object C_50_N_50 extends _50_50(cColor, noColor)
  case object F_50_N_50 extends _50_50(fColor, noColor)

  case object R_75_F_25 extends _75_25(rColor, fColor)
  case object C_75_F_25 extends _75_25(cColor, fColor)
  case object R_75_C_25 extends _75_25(rColor, cColor)

  case object R_25_F_75 extends _25_75(rColor, fColor)
  case object C_25_F_75 extends _25_75(cColor, fColor)
  case object R_25_C_75 extends _25_75(rColor, cColor)
}
