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

import preflop.ranger.PreflopRanger
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Hyperlink
import scalafx.scene.layout.{Background, Border, BorderPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.scene.text.TextAlignment.Center

class BuyMeACoffeePopup extends Popup {
  title = "Buy Me A Coffee"

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new Text(
        "I don't and never will charge for Preflop Ranger.\n" +
          "However, if you would like to say thanks you can get me a long black."
      ) {
        textAlignment = Center
      }
      center = new Hyperlink("buymeacoffee.com/jbwheatley") {
        border = Border.Empty
        onAction = _ => PreflopRanger.hostServices.showDocument("https://buymeacoffee.com/jbwheatley")
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Close") {
            override def onLeftClick(): Unit = close()
          }
        )
      )
    }
  }
}
