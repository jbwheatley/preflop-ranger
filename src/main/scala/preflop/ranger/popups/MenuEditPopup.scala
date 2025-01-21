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

///*
// * Copyright 2025 io.github.jbwheatley
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package preflop.ranger.popups
//
//import preflop.ranger.custom.Popup
//import preflop.ranger.model.ChartMenu
//import preflop.ranger.model.ChartMenu.{ChartMenuLeaf, ChartMenuNode0, ChartMenuNode1}
//import preflop.ranger.popups.MenuEditPopup.MenuEditScene
//import scalafx.scene.control.{TreeItem, TreeView}
//import scalafx.scene.input.{ClipboardContent, Dragboard, TransferMode}
//import scalafx.scene.layout.{Border, BorderStroke, BorderStrokeStyle, BorderWidths}
//import scalafx.scene.paint.Color
//import scalafx.scene.text.Text
//import scalafx.scene.{Scene, SnapshotParameters}
//import scalafx.stage.Stage
//
//import scala.collection.mutable.ArrayBuffer
//
//class MenuEditPopup(menu: ChartMenu) extends Popup { self =>
//  scene = new MenuEditScene(menu, self)
//}
//
//object MenuEditPopup {
//  // TODO needs cancel, apply, ok buttons
//  // TODO edit labels
//  class MenuEditScene(menu: ChartMenu, popupStage: Stage) extends Scene {
//    def makeTreeView(menu: ChartMenu): TreeView[ChartMenu.Node] =
//      new TreeView[ChartMenu.Node](new TreeItem[ChartMenu.Node](menu) {
//        expanded = true
//        children = menu.children.map { case n @ ChartMenuNode0(_, _children) =>
//          new TreeItem[ChartMenu.Node](n) {
//            children = _children.map { case n @ ChartMenuNode1(label, chart, _children) =>
//              new TreeItem[ChartMenu.Node](n) {
//                children = _children.map { n =>
//                  new TreeItem[ChartMenu.Node](n)
//                }.toList
//              }
//            }.toList
//          }
//        }.toList
//      }) { self =>
//        cellFactory = (cell, value) => {
//          cell.text = value.label
//
//          cell.onDragDetected = e => {
//            value match {
//              case _: ChartMenu => ()
//              case _ =>
//                val db: Dragboard = cell.startDragAndDrop(TransferMode.Move)
//                db.dragView = new Text(value.label).snapshot(new SnapshotParameters(), null)
//                val content = new ClipboardContent()
//                content.putString("")
//                db.setContent(content)
//            }
//            e.consume()
//          }
//
//          cell.onDragOver = event => {
//            if (event.getGestureSource != cell) {
//              event.acceptTransferModes(TransferMode.Move)
//            }
//            event.consume()
//          }
//
//          def insertAbove(source: TreeItem[ChartMenu.Node], target: TreeItem[ChartMenu.Node]): Unit = {
//            // move in model
//            val moved: Boolean = (source.getValue, target.getValue) match {
//              case (_: ChartMenu, _) => false
//              case (_, _: ChartMenu) => false
//              case (x: ChartMenuNode0, y: ChartMenuNode0) =>
//                menu.children -= x
//                menu.children.insert(menu.children.indexOf(y), x)
//                true
//              case (x: ChartMenuNode0, y: ChartMenuNode1) =>
//                val shifted = ChartMenuNode1(
//                  x.label,
//                  None,
//                  x.children.collect { case ChartMenuNode1(label, Some(chart), _) => ChartMenuLeaf(label, chart) }
//                )
//                val yList = y.menuParent.get.asInstanceOf[ChartMenuNode0].children
//                yList.insert(yList.indexOf(y), shifted)
//                true
//              case (_: ChartMenuNode0, _: ChartMenuLeaf) => false
//              case (x @ ChartMenuNode1(label, None, children), y: ChartMenuNode0) =>
//                x.menuParent.foreach(_.asInstanceOf[ChartMenuNode0].children -= x)
//                val shifted =
//                  ChartMenuNode0(label, children.map(l => ChartMenuNode1(l.label, Some(l.chart), ArrayBuffer.empty)))
//                menu.children.insert(menu.children.indexOf(y), shifted)
//                true
//              case (_: ChartMenuNode1, _: ChartMenuNode0) => false
//              case (x: ChartMenuNode1, y: ChartMenuNode1) =>
//                x.menuParent.foreach(_.asInstanceOf[ChartMenuNode0].children -= x)
//                val yList = y.menuParent.get.asInstanceOf[ChartMenuNode0].children
//                yList.insert(yList.indexOf(y), x)
//                true
//              case (ChartMenuNode1(label, Some(chart), _), y: ChartMenuLeaf) =>
//                val shifted = ChartMenuLeaf(
//                  label,
//                  chart
//                )
//                val yList = y.menuParent.get.asInstanceOf[ChartMenuNode1].children
//                yList.insert(yList.indexOf(y), shifted)
//                true
//              case (_: ChartMenuNode1, _: ChartMenuLeaf) => false
//              case (_: ChartMenuLeaf, _: ChartMenuNode0) => false
//              case (x @ ChartMenuLeaf(label, chart), y: ChartMenuNode1) =>
//                x.menuParent.foreach(_.asInstanceOf[ChartMenuNode1].children -= x)
//                val shifted =
//                  ChartMenuNode1(label, Some(chart), ArrayBuffer.empty)
//                val yList = y.menuParent.get.asInstanceOf[ChartMenuNode0].children
//                yList.insert(yList.indexOf(y), shifted)
//                true
//              case (x: ChartMenuLeaf, y: ChartMenuLeaf) =>
//                x.menuParent.foreach(_.asInstanceOf[ChartMenuNode1].children -= x)
//                val yList = y.menuParent.get.asInstanceOf[ChartMenuNode1].children
//                yList.insert(yList.indexOf(y), x)
//                true
//            }
//            // move in tree view
//            if (moved) {
//              source.getParent.getChildren.remove(source)
//              val indexOfTarget = target.getParent.getChildren.indexOf(target)
//              target.getParent.getChildren.add(indexOfTarget, source)
//            }
//          }
//
//          cell.onDragDropped = event => {
//            // the one moving
//            val source =
//              event.getGestureSource.asInstanceOf[javafx.scene.control.TreeCell[ChartMenu.Node]]
//            // the one being hovered over
//            val target = cell.delegate
//            if (source != target && source.getTreeItem.isLeaf && target.getTreeItem.isLeaf) {
//              insertAbove(
//                new TreeItem[ChartMenu.Node](source.getTreeItem),
//                new TreeItem[ChartMenu.Node](target.getTreeItem)
//              )
//            }
//            event.setDropCompleted(true)
//            event.consume()
//          }
//
//          cell.onDragDone = _.consume()
//
//          cell.onDragEntered = event => {
//            val source: javafx.scene.control.TreeCell[ChartMenu.Node] = event.getGestureSource
//              .asInstanceOf[javafx.scene.control.TreeCell[ChartMenu.Node]]
//            if (event.getGestureSource != cell.delegate) {
//              if (value.isLeaf && source.getTreeItem.isLeaf) {
//                cell.setBorder(
//                  new Border(
//                    new BorderStroke(
//                      topStroke = Color.Blue,
//                      null,
//                      null,
//                      null,
//                      topStyle = BorderStrokeStyle.Solid,
//                      rightStyle = BorderStrokeStyle.None,
//                      bottomStyle = BorderStrokeStyle.None,
//                      leftStyle = BorderStrokeStyle.None,
//                      null,
//                      widths = new BorderWidths(2),
//                      null
//                    )
//                  )
//                )
//                cell.opacity = 0.3
//              }
//            }
//            event.consume()
//          }
//
//          cell.onDragExited = event => {
//            if (
//              event.getGestureSource != cell.delegate &&
//              event.getDragboard.hasString
//            ) {
//              cell.opacity = 1
//              cell.setBorder(Border.Empty)
//            }
//            event.consume()
//          }
//        }
//      }
//
//    content = makeTreeView(menu)
//  }
//}
