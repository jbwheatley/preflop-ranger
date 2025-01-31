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

import scalafx.beans.property.BooleanProperty
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control.{Menu, MenuItem}
import scalafx.scene.input.{KeyCharacterCombination, KeyCombination}

class DisappearingMenuItem(
    name: String,
    show: BooleanProperty,
    hasShortcut: Boolean,
    shortcutKey: Char,
    shift: Boolean,
    parent: Menu,
    idxInParent: Int,
    action: => Unit,
    conditionalNameUpdate: MenuItem => Subscription = _ => () => ()
) extends MenuItem { self =>
  conditionalNameUpdate(self)
  text = name
  show.onChange {
    parent.items(idxInParent) = new DisappearingMenuItem(
      self.text.get(),
      show,
      hasShortcut,
      shortcutKey,
      shift,
      parent,
      idxInParent,
      action,
      conditionalNameUpdate
    )
  }
  if (!show.get()) styleClass.addOne("nohover")
  onAction = _ => action
  style = if (show.get()) "-fx-text-color: black;" else "-fx-text-fill: lightgray;"
  private val modifiers: List[KeyCombination.Modifier] =
    if (shift) List(KeyCombination.ShortcutDown, KeyCombination.ShiftDown) else List(KeyCombination.ShortcutDown)
  if (hasShortcut) accelerator = new KeyCharacterCombination(shortcutKey.toString, modifiers.map(_.delegate): _*)
}
