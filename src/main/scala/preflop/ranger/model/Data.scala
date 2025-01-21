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

package preflop.ranger.model

import preflop.ranger.edit.EditRegistry
import preflop.ranger.model.ChartMenu.{ChartMenuLeaf, ChartMenuNode0, ChartMenuNode1}
import preflop.ranger.model.FileData.{MenuLabelNode0, MenuLabelNode1, Settings, StoredChart}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

case class Data(
    profile: String,
    menu: ChartMenu,
    settings: Settings
) {

  def toFileData: FileData = {
    val cs: ListBuffer[(Chart, Array[Byte])] = ListBuffer.empty
    val labels = menu.children.zipWithIndex.map { case (n0, priority0) =>
      MenuLabelNode0(
        n0.label,
        n0.children.zipWithIndex.map { case (n1, priority1) =>
          n1.chart.foreach(c => cs.addOne(c -> Array(priority0, priority1).map(_.toByte)))
          MenuLabelNode1(
            n1.label,
            n1.children.zipWithIndex.map { case (n2, priority2) =>
              cs.addOne(n2.chart -> Array(priority0, priority1, priority2).map(_.toByte))
              n2.label
            }.toList
          )
        }.toList
      )
    }
    FileData(
      cs.map { case (c, ms) =>
        StoredChart(
          c.name.value,
          ms,
          c.actions.map(_.map(_.render).mkString(",")).mkString(","),
          c.subset,
          c.noOfPlayers
        )
      }.toList
        .sortBy(_.menu)(Data.ord),
      labels.toList,
      Settings(SettingsMenu.noOfPlayers.value, SettingsMenu.showPercentages.value, SettingsMenu.actions.value)
    )
  }
  def charts: List[Chart] = {
    val cs: ListBuffer[Chart] = ListBuffer.empty
    menu.children.foreach(_.children.foreach { n =>
      n.chart.foreach(cs.addOne)
      cs.addAll(n.children.map(_.chart))
    })
    cs.toList
  }
  def saveInMemoryModel(): Unit = {
    EditRegistry.empty()
    charts.foreach(_.save())
    SettingsMenu.save()
  }
}

object Data {
  val ord: Ordering[Array[Byte]] = (x: Array[Byte], y: Array[Byte]) =>
    if (x(0) < y(0)) -1
    else if (x(0) > y(0)) 1
    else {
      if (x(1) < y(1)) -1
      else if (x(1) > y(1)) 1
      else {
        if (x.isDefinedAt(2) && y.isDefinedAt(2)) {
          if (x(2) < y(2)) -1 else if (x(2) > y(2)) 1 else 0
        } else 0
      }
    }

  def fromFileData(name: String, fileData: FileData): Data = {
    val menuLabels = fileData.menus
    val menu = ChartMenu(
      children = ArrayBuffer.from(
        fileData.charts
          .groupBy(_.menu(0))
          .toList
          .sortBy(_._1)
          .map { case (priority0: Byte, charts) =>
            ChartMenuNode0(
              label = menuLabels(priority0.toInt).label,
              children = ArrayBuffer.from(
                charts
                  .groupBy(_.menu(1))
                  .toList
                  .sortBy(_._1)
                  .map { case (priority1, charts) =>
                    val label = menuLabels(priority0.toInt).children(priority1.toInt).label
                    if (charts.length == 1) {
                      ChartMenuNode1(label = label, chart = charts.headOption.map(_.toChart), ArrayBuffer.empty)
                    } else
                      ChartMenuNode1(
                        label = label,
                        chart = None,
                        children = ArrayBuffer.from(
                          charts
                            .sortBy(_.menu(2))
                            .map { chart =>
                              ChartMenuLeaf(
                                label =
                                  menuLabels(priority0.toInt).children(priority1.toInt).children(chart.menu(2).toInt),
                                chart.toChart
                              )
                            }
                        )
                      )
                  }
              )
            )
          }
      )
    )
    Data(name, menu, fileData.settings)
  }
}
