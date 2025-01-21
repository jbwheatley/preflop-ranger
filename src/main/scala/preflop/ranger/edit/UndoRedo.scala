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

import scalafx.beans.property.{BooleanProperty, StringProperty}

import scala.collection.mutable

object UndoRedo {
  case class Change(app: () => Unit, undo: () => Unit, appDescription: String)

  val hasChanges: BooleanProperty = BooleanProperty(false)
  val hasRedos: BooleanProperty   = BooleanProperty(false)

  val latestChangeDescription: StringProperty = StringProperty("")
  val nextRedoDescription: StringProperty     = StringProperty("")

  private var changes: mutable.Stack[Change] = mutable.Stack.empty
  private var undone: mutable.Stack[Change]  = mutable.Stack.empty

  def add(change: Change): Unit = {
    changes.push(change)
    undone = mutable.Stack.empty
    latestChangeDescription.value = change.appDescription
    hasRedos.value = false
    hasChanges.value = true
  }

  def undo(): Unit = if (changes.nonEmpty) {
    val changeToUndo = changes.pop()
    changeToUndo.undo()
    undone.push(changeToUndo)
    if (changes.isEmpty) {
      hasChanges.value = false
      latestChangeDescription.value = ""
    } else {
      latestChangeDescription.value = changes.head.appDescription
    }
    nextRedoDescription.value = changeToUndo.appDescription
    hasRedos.value = true
  }

  def redo(): Unit = if (undone.nonEmpty) {
    val changeToRedo = undone.pop()
    changeToRedo.app()
    changes.push(changeToRedo)
    if (undone.isEmpty) {
      hasRedos.value = false
      nextRedoDescription.value = ""
    } else {
      nextRedoDescription.value = undone.head.appDescription
    }
    latestChangeDescription.value = changeToRedo.appDescription
    hasChanges.value = true
  }

  def reset(): Unit = {
    hasChanges.value = false
    hasRedos.value = false
    changes = mutable.Stack.empty
    undone = mutable.Stack.empty
  }
}
