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

import preflop.ranger.PreflopRanger
import scalafx.geometry.Pos.TopRight
import scalafx.scene.Scene
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

import scala.collection.mutable

final case class HandAction(r: Int, j: Int, c: Int, l: Int, f: Int, n: Int, variableRaiseSizes: Map[String, Int]) {
  def render: String =
    (if (r == 0) "" else s"r$r") +
      (if (j == 0) "" else s"j$j") +
      (if (c == 0) "" else s"c$c") +
      (if (l == 0) "" else s"l$l") +
      (if (f == 0) "" else s"f$f") +
      (if (n == 0) "" else s"n$n") +
      variableRaiseSizes.map { case (size, pc) =>
        if (pc == 0) "" else s"r($size)$pc"
      }.mkString

  private def rect(
      id: String,
      percentageFrom: Int,
      _fill: Color,
      base: Rectangle
  ): Rectangle = {
    val centile            = base.widthProperty().divide(100.0)
    val rectRelativeHeight = base.heightProperty()
    val rectRelativeWidth  = centile.multiply(100.0 - percentageFrom)
    val r = new Rectangle() {
      fill = _fill
      hgrow = Always
      vgrow = Always
      width = rectRelativeWidth.get()
      height = rectRelativeHeight.get()
    }
    SettingsMenu.actions.onChange { (_, _, change) =>
      change.get(id).foreach(d => r.fill = d.colour)
    }
    r.widthProperty().bind(rectRelativeWidth)
    r.heightProperty().bind(rectRelativeHeight)
    r
  }

  def draw(scene: Scene): StackPane = {
    val sceneWidth = scene.width
      .subtract(2 * PreflopRanger.borderInset)
      .divide(13.0)
      .subtract(0.5)
    val baseRect = new Rectangle() {
      hgrow = Always
      vgrow = Always
      this.width.bind(sceneWidth)
      this.height.bind(sceneWidth)
      fill = SettingsMenu.actions.get()("n").colour
    }
    val all: List[(String, Int)] = List("r" -> r) ++ variableRaiseSizes
      .map { case (k, v) => k -> v } ++ List("j" -> j, "c" -> c, "l" -> l, "f" -> f, "n" -> n)
    var cumulativePc = 0
    val rectangles = all.flatMap { case (id, pc) =>
      if (pc == 0) None
      else {
        val res = Some(rect(id, cumulativePc, SettingsMenu.actions.get()(id).colour, baseRect))
        cumulativePc += pc
        res
      }
    }
    new StackPane() {
      alignment = TopRight
      hgrow = Always
      vgrow = Always
      children = baseRect :: rectangles
    }
  }

  def *(i: Int): HandAction = copy(
    r = r * i,
    j = j * i,
    c = c * i,
    f = f * i,
    n = n * i,
    l = l * i,
    variableRaiseSizes = variableRaiseSizes.view.mapValues(_ * i).toMap
  )
}

object HandAction {
  def combineAll(has: Seq[HandAction]): HandAction = {
    var r                               = 0
    var j                               = 0
    var c                               = 0
    var l                               = 0
    var f                               = 0
    var n                               = 0
    val other: mutable.Map[String, Int] = mutable.Map.empty
    has.foreach { ha =>
      r += ha.r
      j += ha.j
      c += ha.c
      l += ha.l
      f += ha.f
      n += ha.n
      ha.variableRaiseSizes.keySet.foreach { key =>
        other.updateWith(key) {
          case Some(value) => Some(value + ha.variableRaiseSizes(key))
          case None        => Some(ha.variableRaiseSizes(key))
        }
      }
    }
    HandAction(r = r, j = j, c = c, l = l, f = f, n = n, variableRaiseSizes = other.toMap)
  }
}
