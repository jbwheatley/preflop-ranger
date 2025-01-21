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
import preflop.ranger.PreflopRanger.{basePath, saveProfiles}
import preflop.ranger.custom.Bindings.matches
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import preflop.ranger.model.{FileData, Profile}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{ChoiceBox, TextField}
import scalafx.scene.layout.{Background, BorderPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

import java.nio.file.{Files, Path, StandardOpenOption}

class CreateProfilePopup(refreshProfileMenu: () => Unit) extends Popup { stage =>
  title = "Create Profile"
  scene = new Scene {
    root = new BorderPane() {
      private val textField: TextField = new TextField()
      private val seedProfile: ChoiceBox[String] =
        new ChoiceBox[String](new ObservableBuffer[String]().addAll("" +: PreflopRanger.allProfiles.map(_.name)))
      private val noOfPlayers: ChoiceBox[Int] =
        new ChoiceBox[Int](new ObservableBuffer[Int]().addAll(List(3, 4, 5, 6, 7, 8, 9)))
      background = Background.fill(Color.White)
      padding = Insets(20)
      center = {
        val grid = new GridPane(4, 4)
        grid.addRow(0, new Text("Name:"), textField)
        grid.addRow(1, new Text("Default no. of players:"), noOfPlayers)
        grid.addRow(2, new Text("Copy from (optional):"), seedProfile)
        grid
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit =
              close()
          },
          new LeftClickButton("Create") {
            disable.bind(matches(textField.text, """\s*""").or(noOfPlayers.value.isNull))
            override def onLeftClick(): Unit =
              if (PreflopRanger.allProfiles.exists(_.name == textField.text.value)) {
                showTooltip(stage)(s"Profile named '${textField.text.value}' already exists")
              } else {
                val name = textField.text.get()
                PreflopRanger.allProfiles =
                  PreflopRanger.allProfiles.appended(Profile(name, selected = false)).sortBy(_.name)
                val profileToWrite =
                  if (seedProfile.value.value == "") {
                    upickle.default.read[FileData](Files.readString(Path.of("src/main/resources/default.json")))
                  } else
                    upickle.default.read[FileData](
                      Files.readString(
                        basePath.resolve(s"profiles/${seedProfile.value.value.replaceAll(" ", "_")}.json")
                      )
                    )
                Files.writeString(
                  basePath.resolve(s"profiles/${textField.text.get().replaceAll(" ", "_")}.json"),
                  upickle.default.write[FileData](
                    profileToWrite.copy(
                      settings = profileToWrite.settings.copy(defaultPlayers = noOfPlayers.value.value)
                    ),
                    indent = 2
                  ),
                  StandardOpenOption.CREATE,
                  StandardOpenOption.WRITE,
                  StandardOpenOption.TRUNCATE_EXISTING
                )
                saveProfiles()
                refreshProfileMenu()
                close()
              }
          }
        )
      )
    }
  }
}
