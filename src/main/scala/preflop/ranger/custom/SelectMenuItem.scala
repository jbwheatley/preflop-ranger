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

import javafx.beans.value.ObservableValue
import preflop.ranger.edit.UndoRedo.Change
import preflop.ranger.edit.{EditRegistry, UndoRedo}
import scalafx.beans.property.{Property, ReadOnlyProperty}
import scalafx.scene.control.MenuItem

trait SelectMenuItem[M <: MenuItem, T1, T2, S1, S2] { self: M =>
  def memoryValue: Property[T1, T2]

  private var undoRedoAction = false
  private var resetting      = false
  private var initOnLoad: T1 = _
  def changeValue: S2 => Unit
  def selectionProperty: M => ReadOnlyProperty[S1, S2]
  def bindingFunction: ReadOnlyProperty[S1, S2] => ObservableValue[T2]
  private def bind(): Unit = memoryValue.bind(bindingFunction(selectionProperty(self)))
  def changeActionName: S2 => String
  def editRegistryName: String
  def initialiseProperty: T1 => Unit

  def initialise: () => Unit = () => ()

  def save(): Unit = initOnLoad = memoryValue.value

  def reset(newValue: T1): Unit = {
    resetting = true
    initOnLoad = newValue
    memoryValue.unbind()
    memoryValue.value = newValue
    initialiseProperty(memoryValue.value)
    bind()
    resetting = false
  }

  initialise()
  initialiseProperty(memoryValue.value)
  bind()
  selectionProperty(self).onChange { (_, old, selected) =>
    if (!resetting) {
      val change = () => {
        undoRedoAction = true
        changeValue(selected)
        undoRedoAction = false
      }
      val undo = () => {
        undoRedoAction = true
        changeValue(old)
        undoRedoAction = false
      }
      // don't add change to the redo stack if we're acting inside the undo/redo action
      if (!undoRedoAction) UndoRedo.add(Change(change, undo, changeActionName(selected)))
      if (memoryValue.value != initOnLoad) EditRegistry.register(editRegistryName)
      else EditRegistry.deregister(editRegistryName)
    }
  }
}
