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

package preflop.ranger.popups

import preflop.ranger.PreflopRanger.resetStage
import preflop.ranger.edit.RangerFiles.{loadProfile, saveSelectedProfile}
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import preflop.ranger.edit.UndoRedo
import preflop.ranger.model.SettingsMenu.switchProfile
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.{Background, BorderPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

class SwitchProfileConfirmationPopup(
    toggles: javafx.scene.control.ToggleGroup,
    old: javafx.scene.control.Toggle,
    selected: String
) extends Popup {

  title = "Switch Profile"

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new Text("There are unsaved changes to this profile, would you still like to switch?") {
        wrappingWidth = 300
      }
      center = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit = {
              toggles.selectToggle(old)
              close()
            }
          },
          new LeftClickButton("Discard") {
            override def onLeftClick(): Unit = {
              UndoRedo.reset()
              loadProfile(selected, startup = false)
              resetStage()
              close()
            }
          },
          new LeftClickButton("Save and Switch") {
            override def onLeftClick(): Unit = {
              saveSelectedProfile()
              switchProfile(selected)
              close()
            }
          }
        )
      )
    }
  }
  override def onClose(): Unit = toggles.selectToggle(old)

}
