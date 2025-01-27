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

import preflop.ranger.PreflopRanger.{allProfiles, selectedProfile}
import preflop.ranger.model.{Data, FileData, Profile, SettingsMenu}
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}

import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}

object RangerFiles {
  val basePath: Path            = Path.of(System.getProperty("user.home") + File.separator + ".preflop-ranger")
  private val profilePath: Path = basePath.resolve("profiles.json")

  def load(startup: Boolean): Unit =
    if (Files.exists(profilePath)) {
      val profiles = upickle.default.read[Array[Profile]](Files.readString(profilePath))
      allProfiles = profiles.sortBy(_.name)
      val name = profiles.find(_.selected).getOrElse(profiles.head).name
      loadProfile(name, startup)
    } else {
      firstOpenLoad()
    }

  private def firstOpenLoad(): Unit = {
    Files.createDirectories(basePath.resolve("profiles"))
    val profiles = Array(Profile("empty", selected = false), Profile("sample", selected = true))
    Files.writeString(
      profilePath,
      upickle.default.write[Array[Profile]](profiles, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
    Files.copy(
      this.getClass.getResource("/default.json").openStream(),
      basePath.resolve("profiles/empty.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    Files.copy(
      this.getClass.getResource("/sample.json").openStream(),
      basePath.resolve("profiles/sample.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    allProfiles = profiles
    loadProfile("sample", startup = true)
  }

  def loadProfile(name: String, startup: Boolean): Unit =
    if (Files.exists(basePath.resolve(s"profiles/${name.replaceAll(" ", "_")}.json"))) {
      val data = readProfile(name)
      putDataInMemory(name, Data.fromFileData(name, data), startup)
    }

  def readProfile(name: String): FileData = upickle.default.read[FileData](
    Files.readString(basePath.resolve(s"profiles/${name.replaceAll(" ", "_")}.json"))
  )

  private def putDataInMemory(name: String, data: Data, startup: Boolean): Unit = {
    selectedProfile = data
    allProfiles.foreach(_.selected = false)
    allProfiles.find(_.name == name).get.selected = true

    if (startup) {
      SettingsMenu.noOfPlayers = IntegerProperty(data.settings.defaultPlayers)
      SettingsMenu.showPercentages = BooleanProperty(data.settings.showPercentages)
    }

    SettingsMenu.showPercentagesMenuItem.reset(data.settings.showPercentages)
    SettingsMenu.noOfPlayersMenu.reset(data.settings.defaultPlayers)

    SettingsMenu.actionsInit = data.settings.actions
    SettingsMenu.actions = ObjectProperty(data.settings.actions)
  }

  def saveProfileList(): Unit = {
    Files.writeString(
      profilePath,
      upickle.default.write[Array[Profile]](allProfiles, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    ()
  }

  def saveSelectedProfile(): Unit = {
    saveProfile(selectedProfile.profile, selectedProfile.toFileData)
    selectedProfile.saveInMemoryModel()
  }

  def saveProfile(name: String, data: FileData): Unit = {
    Files.writeString(
      basePath.resolve(s"profiles/${name.replaceAll(" ", "_")}.json"),
      upickle.default.write[FileData](data, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    ()
  }

  def writeErrorLog(e: Throwable): Unit = {
    if (Files.notExists(basePath.resolve("logs")))
      Files.createDirectories(basePath.resolve("logs"))
    Files.writeString(
      basePath.resolve(s"logs/${System.currentTimeMillis() / 1000}.log"),
      e.toString + "\n  " + e.getStackTrace.map(_.toString).mkString("\n  "),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
    ()
  }
}
