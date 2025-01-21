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

package preflop.ranger.edit

import scalafx.beans.property.BooleanProperty

import scala.collection.mutable

object EditRegistry {
  private var registry: mutable.HashSet[Any] = mutable.HashSet.empty

  val hasEdits: BooleanProperty = BooleanProperty(false)

  def register(a: Any): Unit = {
    registry.add(a)
    hasEdits.value = true
  }

  def deregister(a: Any): Unit = {
    registry.remove(a)
    hasEdits.value = registry.nonEmpty
  }

  def empty(): Unit = {
    registry = mutable.HashSet.empty
    hasEdits.value = false
  }
}
