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

import javafx.beans.value.ObservableStringValue
import javafx.collections.{FXCollections, ObservableList}
import scalafx.beans.binding.BooleanBinding

object Bindings {
  val trueBinding: BooleanBinding  = new BooleanBinding(() => true)
  val falseBinding: BooleanBinding = new BooleanBinding(() => false)

  def matches(var0: ObservableStringValue, var1: String): BooleanBinding = new BooleanBinding(
    new javafx.beans.binding.BooleanBinding() {
      super.bind(var0)

      override def dispose(): Unit =
        super.unbind(var0)

      override protected def computeValue: Boolean =
        var0.get().matches(var1)

      override def getDependencies: ObservableList[_] = FXCollections.singletonObservableList(var0)
    }
  )
}
