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

package preflop.ranger.custom

import scalafx.scene.Node
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

class RectangleStack(_width: Int, _height: Int, node: Node) extends StackPane { self =>
  val colour: Color = Color.White
  private val rect = new Rectangle() {
    height = _height.toDouble
    width = _width.toDouble
    fill = colour
  }

  def map: Rectangle => Rectangle = identity

  def replaceNode(node: Node): Unit                     = children(1) = node
  def highlight(colour: Color = Color.DarkSalmon): Unit = rect.setFill(colour)
  def unhighlight(): Unit                               = rect.setFill(colour)

  children = List(
    map(rect),
    node
  )
}
