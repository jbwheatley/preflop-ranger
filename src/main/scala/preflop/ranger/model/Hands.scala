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

object Hands {
  private val cards = Array("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2").zipWithIndex
  val hands: Array[Array[String]] = cards.map { case (c1, i1) =>
    cards.map {
      case (c2, i2) if i2 == i1 => c1 + c2
      case (c2, i2) if i2 > i1  => c1 + c2 + "s"
      case (c2, i2) if i2 < i1  => c2 + c1 + "o"
      case (_, _)               => ???
    }
  }
}
