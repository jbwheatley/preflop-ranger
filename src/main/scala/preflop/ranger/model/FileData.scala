/*
 * Copyright (C) 2025  io.github.jbwheatley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package preflop.ranger.model

import preflop.ranger.model.FileData.{MenuLabelNode0, Settings, StoredChart}
import scalafx.scene.paint.Color
import upickle.default.{macroRW, ReadWriter => RW}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

case class FileData(
    charts: List[StoredChart],
    menus: List[MenuLabelNode0],
    settings: Settings
)

object FileData {
  case class StoredChart(
      name: String,
      menu: Array[Byte],
      actions: String,
      subset: Boolean,
      numberOfPlayers: Int
  ) {
    private def strToAction(str: String): HandAction = {
      def get(char: Char): Int = str.indexOf(char.toInt) match {
        case -1    => 0
        case other => str.drop(other + 1).takeWhile(!_.isLetter).toInt
      }
      var r: Int                        = 0
      val rs: ListBuffer[(String, Int)] = new ListBuffer[(String, Int)]()
      @tailrec
      def allRaises(remaining: String): Unit =
        remaining.indexOf('r') match {
          case -1 => ()
          case other =>
            val rest      = remaining.drop(other + 1)
            val idxOfNext = rest.indexWhere(_.isLetter)
            val (l, rem)  = if (idxOfNext >= 0) rest.splitAt(idxOfNext) else (rest, "")
            if (l.startsWith("(")) {
              val (size, pc) = l.splitAt(l.indexOf(")") + 1)
              rs.addOne(size.tail.init -> pc.toInt)
            } else {
              r = l.toInt
            }
            allRaises(rem)
        }

      allRaises(str)
      HandAction(r, get('j'), get('c'), get('l'), get('f'), get('n'), rs.toMap)
    }

    def toChart: Chart = Chart(name, actions.split(",").map(strToAction).grouped(13).toArray, subset, numberOfPlayers)
  }

  case class MenuLabelNode0(label: String, children: List[MenuLabelNode1])

  case class MenuLabelNode1(label: String, children: List[String])

  case class Settings(
      defaultPlayers: Int,
      showPercentages: Boolean,
      actions: Map[String, ActionData]
  )

  case class ActionData(colour: Color)

  implicit val node0RW: RW[MenuLabelNode0]  = macroRW
  implicit val node1RW: RW[MenuLabelNode1]  = macroRW
  implicit val fileDataRW: RW[FileData]     = macroRW
  implicit val chartRW: RW[StoredChart]     = macroRW
  implicit val settingsRW: RW[Settings]     = macroRW
  implicit val actionDataRW: RW[ActionData] = macroRW
  implicit val colorRW: RW[Color]           = implicitly[RW[String]].bimap(_.delegate.toString(), Color.web)
}
