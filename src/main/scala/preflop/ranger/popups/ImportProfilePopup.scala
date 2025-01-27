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
import preflop.ranger.edit.RangerFiles.{saveProfile, saveProfileList}
import preflop.ranger.custom.Bindings.matches
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import preflop.ranger.model.{FileData, Profile}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TextArea, TextField}
import scalafx.scene.layout.{Background, BorderPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.nio.file.Files
import scala.util.{Failure, Success, Try}

class ImportProfilePopup(refreshProfileMenu: () => Unit) extends Popup { self =>
  title = "Import Profile"

  private val nameText = new TextField()
  private val textBox = new TextArea() {
    resizable = true
  }

  val grid = new GridPane(4, 4)

  private lazy val fileChooser: FileChooser = new FileChooser() {
    title = "Import Profile"
    import scalafx.Includes._
    extensionFilters ++=
      List(
        new ExtensionFilter("Text Files", List("*.txt", "*.json"))
      )
  }

  grid.addRow(
    0,
    new Text("Choose file:"),
    new Button("...") {
      onAction = _ => {
        val file = fileChooser.showOpenDialog(self)
        textBox.text = Files.readString(file.toPath)
        if (nameText.text.value.isBlank) {
          nameText.text = file.getName.split('.')(0).replaceAll("_", " ")
        }
      }
    }
  )
  grid.addRow(1, new Text("Name:"), nameText)
  grid.add(textBox, 0, 2, 2, 1)

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      center = grid
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit =
              close()
          },
          new LeftClickButton("OK") {
            disable.bind(matches(textBox.text, """\s*""").or(matches(nameText.text, """\s*""")))
            override def onLeftClick(): Unit = {
              val newName = nameText.text.value.trim
              if (PreflopRanger.allProfiles.exists(_.name == newName)) {
                showTooltip(self)(s"Profile named '$newName' already exists")
              } else {
                Try(upickle.default.read[FileData](textBox.text.value)) match {
                  case Failure(error) =>
                    grid.addRow(
                      3,
                      new Text(s"Invalid JSON input: ${error.getMessage}") {
                        fill = Color.Red
                      }
                    )
                  case Success(fd) =>
                    saveProfile(newName, fd)
                    PreflopRanger.allProfiles =
                      PreflopRanger.allProfiles.appended(Profile(newName, selected = false)).sortBy(_.name)
                    saveProfileList()
                    refreshProfileMenu()
                    close()
                }
              }
            }
          }
        )
      )
    }
  }
}
