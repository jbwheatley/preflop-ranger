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

import preflop.ranger.PreflopRanger.saveData
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.{Background, BorderPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

//TODO list changes summary?
class OnCloseUnsavedPopup(closeRequest: javafx.stage.WindowEvent) extends Popup {
  title = "Quit"

  scene = new Scene {
    root = new BorderPane() {
      padding = Insets(20)
      background = Background.fill(Color.White)
      top = new Text("There are unsaved changes to this profile, would you still like to exit?") {
        wrappingWidth = 300
      }
      center = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit = {
              closeRequest.consume()
              close()
            }
          },
          new LeftClickButton("Discard") {
            override def onLeftClick(): Unit = close()
          },
          new LeftClickButton("Save and Quit") {
            override def onLeftClick(): Unit = {
              saveData()
              close()
            }
          }
        )
      )
    }
  }
  override def onClose(): Unit = closeRequest.consume()

}
