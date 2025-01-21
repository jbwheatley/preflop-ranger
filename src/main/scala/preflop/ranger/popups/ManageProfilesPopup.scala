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

import javafx.scene.input.MouseButton
import javafx.scene.layout
import preflop.ranger.PreflopRanger
import preflop.ranger.PreflopRanger.basePath
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup, RectangleStack}
import preflop.ranger.model.Profile
import preflop.ranger.model.SettingsMenu.switchProfile
import scalafx.geometry.Insets
import scalafx.geometry.Pos.CenterLeft
import scalafx.scene.Scene
import scalafx.scene.control.TextField
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

import java.nio.file.Files
import scala.collection.mutable

class ManageProfilesPopup(refreshProfileMenu: () => Unit) extends Popup {
  stage =>
  title = "Manage Profiles"

  private val deleted: mutable.Map[String, Boolean] =
    mutable.Map(PreflopRanger.allProfiles.toList.map(_.name -> false): _*)

  private val renamed: mutable.Map[String, String] =
    mutable.Map(PreflopRanger.allProfiles.toList.map(p => p.name -> p.name): _*)

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new RectangleStack(50, 30, new Text("Rename or delete profiles.")) {
        alignment = CenterLeft
      }
      center = {
        val grid = new GridPane(4, 4)
        PreflopRanger.allProfiles.zipWithIndex.foreach { case (p, idx) =>
          val tf = new TextField() {
            text = p.name
            text.onChange { (_, _, newName) =>
              renamed.update(p.name, newName.trim)
            }
          }
          grid.addRow(
            idx,
            new Text(p.name + ":"),
            tf,
            deleteButton(grid, p, tf, idx)
          )
        }
        grid
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit =
              close()
          },
          new LeftClickButton("OK") {
            override def onLeftClick(): Unit = {
              val newNames = renamed.values.toList
              if (newNames.distinct == newNames) {
                deleteProfiles()
                renameProfiles()
                PreflopRanger.saveProfiles()
                refreshProfileMenu()
                close()
              } else {
                showTooltip(stage)("Profile names must be distinct")
              }
            }
          }
        )
      )
    }
  }

  private def deleteProfiles(): Unit = {
    val deletedProfiles: List[String] = deleted.collect { case (p, true) => p }.toList
    PreflopRanger.allProfiles = PreflopRanger.allProfiles.filter(p => !deleted(p.name))
    if (deletedProfiles.contains(PreflopRanger.selectedProfile.profile)) {
      switchProfile(deleted.collectFirst { case (p, false) => p }.get)
    }
    deletedProfiles.foreach { p =>
      Files.delete(
        basePath.resolve(s"profiles/${p.replaceAll(" ", "_")}.json")
      )
    }
  }

  private def renameProfiles(): Unit = {
    val renamedProfiles: List[(String, String)] = renamed.filter { case (p, r) => p != r }.toList
    renamedProfiles.foreach { case (oldName, newName) =>
      Files.move(
        basePath.resolve(s"profiles/${oldName.replaceAll(" ", "_")}.json"),
        basePath.resolve(s"profiles/${newName.replaceAll(" ", "_")}.json")
      )
    }
    PreflopRanger.allProfiles = PreflopRanger.allProfiles.map(p =>
      renamed.get(p.name) match {
        case Some(value) => p.copy(name = value)
        case None        => p
      }
    )
  }

  private def deleteButton(grid: GridPane, profile: Profile, tf: TextField, idx: Int): RectangleStack =
    new RectangleStack(
      18,
      18,
      new ImageView(new Image("/delete-icon.png")) {
        fitHeight = 18
        fitWidth = 18
      }
    ) { self =>
      onMouseClicked = o =>
        if (o.getButton == MouseButton.PRIMARY) {
          if (deleted.values.count(!_) > 1) {
            grid.children.remove(tf)
            grid.children.remove(self)
            val deletedTf: TextField = new TextField() {
              text = "Deleted"
              val bg: layout.Background        = tf.backgroundProperty().get()
              val fill1: layout.BackgroundFill = bg.getFills.get(0)
              val fill2: layout.BackgroundFill = bg.getFills.get(1)
              val newBg = new Background(
                fills = Array(
                  new BackgroundFill(
                    Color.LightGrey,
                    new CornerRadii(fill1.getRadii),
                    new Insets(fill1.getInsets)
                  ),
                  new BackgroundFill(
                    Color.LightGrey,
                    new CornerRadii(fill2.getRadii),
                    new Insets(fill2.getInsets)
                  )
                )
              )
              this.backgroundProperty().setValue(newBg)
              editable = false
            }
            grid.add(
              deletedTf,
              1,
              idx
            )
            grid.add(
              new RectangleStack(
                18,
                18,
                new ImageView(new Image("/undo-icon.png")) {
                  fitHeight = 18
                  fitWidth = 18
                }
              ) {
                onMouseClicked = o =>
                  if (o.getButton == MouseButton.PRIMARY) {
                    grid.add(tf, 1, idx)
                    grid.add(deleteButton(grid, profile, tf, idx), 2, idx)
                    deleted.update(profile.name, false)
                    renamed.remove(profile.name)
                    ()
                  }
              },
              2,
              idx
            )
            deleted.update(profile.name, true)
            renamed.addOne(profile.name -> profile.name)
          } else {
            showTooltip(stage)("At least one profile must exist")
          }
        }
    }
}
