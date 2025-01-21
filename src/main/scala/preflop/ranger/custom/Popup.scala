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

import preflop.ranger.PreflopRanger
import scalafx.stage.Stage

class Popup extends Stage {
  resizable = false
  alwaysOnTop = false

  override def showAndWait(): Unit = if (!PreflopRanger.popupOpen.value) {
    PreflopRanger.popupOpen.value = true
    super.showAndWait()
  }

  def onClose(): Unit = ()

  onCloseRequest = { _ =>
    PreflopRanger.popupOpen.value = false
    onClose()
  }

  override def close(): Unit = {
    PreflopRanger.popupOpen.value = false
    super.close()
  }

}
