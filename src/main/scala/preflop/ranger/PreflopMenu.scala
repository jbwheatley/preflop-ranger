package preflop.ranger

import javafx.event.{ActionEvent, EventHandler}
import scalafx.geometry.Insets
import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.scene.paint.Color

object PreflopMenu {
  def menu(
      allCharts: Map[String, Array[Array[HandAction]]],
      callback: (String, Array[Array[HandAction]]) => Unit
  ): MenuBar = {
    def click(chart: String): EventHandler[ActionEvent] = (_: ActionEvent) =>
      allCharts.get(chart).fold(())(callback(chart, _))

    def menuItem(text: String, chartName: String): MenuItem =
      new MenuItem(text = text) {
        onAction = click(chartName)
      }

    new MenuBar {
      centerShape = true
      style = "-fx-font: normal bold 13pt sans-serif"
      background = new Background(
        Array(
          new BackgroundFill(
            fill = Color.OrangeRed,
            radii = new CornerRadii(5.0),
            insets = Insets.Empty
          )
        )
      )
      menus = List(
        new Menu(
          label = "RFI"
        ) {
          items = List(
            menuItem("Small Blind", ChartNames.RFI.SB),
            menuItem("Button", ChartNames.RFI.BTN),
            menuItem("Cut Off", ChartNames.RFI.CO),
            menuItem("Hijack", ChartNames.RFI.HJ),
            menuItem("Lojack", ChartNames.RFI.LJ)
          )
        },
        new Menu(
          label = "RFI VS 3-BET"
        ) {
          items = List(
            new Menu(label = "Small Blind") {
              items = List(
                menuItem("vs Big Blind", ChartNames.VS3BET.SB.vsBB)
              )
            },
            new Menu(label = "Button") {
              items = List(
                menuItem("vs Small Blind", ChartNames.VS3BET.BTN.vsSB),
                menuItem("vs Big Blind", ChartNames.VS3BET.BTN.vsBB)
              )
            },
            new Menu(label = "Cut Off") {
              items = List(
                menuItem("vs Button", ChartNames.VS3BET.CO.vsBTN),
                menuItem("vs Small Blind", ChartNames.VS3BET.CO.vsSB),
                menuItem("vs Big Blind", ChartNames.VS3BET.CO.vsBB)
              )
            },
            new Menu(label = "Hijack") {
              items = List(
                menuItem("vs Cut Off", ChartNames.VS3BET.HJ.vsCO),
                menuItem("vs Button", ChartNames.VS3BET.HJ.vsBTN),
                menuItem("vs Small Blind", ChartNames.VS3BET.HJ.vsSB),
                menuItem("vs Big Blind", ChartNames.VS3BET.HJ.vsBB)
              )
            },
            new Menu(label = "Lojack") {
              items = List(
                menuItem("vs Hijack", ChartNames.VS3BET.LJ.vsHJ),
                menuItem("vs Cut Off", ChartNames.VS3BET.LJ.vsCO),
                menuItem("vs Button", ChartNames.VS3BET.LJ.vsBTN),
                menuItem("vs Small Blind", ChartNames.VS3BET.LJ.vsSB),
                menuItem("vs Big Blind", ChartNames.VS3BET.LJ.vsBB)
              )
            }
          )
        },
        new Menu(
          label = "VS RFI"
        ) {
          items = List(
            new Menu(label = "Big Blind") {
              items = List(
                menuItem("vs Small Blind", ChartNames.VSRFI.BB.vsSB),
                menuItem("vs Button", ChartNames.VSRFI.BB.vsBTN),
                menuItem("vs Cut Off", ChartNames.VSRFI.BB.vsCO),
                menuItem("vs Hijack", ChartNames.VSRFI.BB.vsHJ),
                menuItem("vs Lojack", ChartNames.VSRFI.BB.vsLJ)
              )
            },
            new Menu(label = "Small Blind") {
              items = List(
                menuItem("vs Button", ChartNames.VSRFI.SB.vsBTN),
                menuItem("vs Cut Off", ChartNames.VSRFI.SB.vsCO),
                menuItem("vs Hijack", ChartNames.VSRFI.SB.vsHJ),
                menuItem("vs Lojack", ChartNames.VSRFI.SB.vsLJ)
              )
            },
            new Menu(label = "Button") {
              items = List(
                menuItem("vs Cut Off", ChartNames.VSRFI.BTN.vsCO),
                menuItem("vs Hijack", ChartNames.VSRFI.BTN.vsHJ),
                menuItem("vs Lojack", ChartNames.VSRFI.BTN.vsLJ)
              )
            },
            new Menu(label = "Cut Off") {
              items = List(
                menuItem("vs Hijack", ChartNames.VSRFI.CO.vsHJ),
                menuItem("vs Lojack", ChartNames.VSRFI.CO.vsLJ)
              )
            },
            new Menu(label = "Hijack") {
              items = List(
                menuItem("Lojack", ChartNames.VSRFI.HJ.vsLJ)
              )
            }
          )
        },
        new Menu(
          label = "COLD 4-BET"
        ) {
          items = List(
            new Menu(label = "BB vs SB 3-Bet and") {
              items = List(
                menuItem("Button open", ChartNames.COLD4BET.BBvsSB3Bet.andBTN),
                menuItem("Cut Off open", ChartNames.COLD4BET.BBvsSB3Bet.andCO),
                menuItem("Early open", ChartNames.COLD4BET.BBvsSB3Bet.andEarly)
              )
            },
            new Menu(label = "Big Blind") {
              items = List(
                menuItem("vs Button 3-Bet", ChartNames.COLD4BET.BB.vsBTN3Bet),
                menuItem("vs Cut Off 3-Bet", ChartNames.COLD4BET.BB.vsCO3Bet),
                menuItem("vs Hijack 3-Bet", ChartNames.COLD4BET.BB.vsHJ3Bet)
              )
            },
            new Menu(label = "Small Blind") {
              items = List(
                menuItem("vs Button 3-Bet", ChartNames.COLD4BET.SB.vsBTN3Bet),
                menuItem("vs Cut Off 3-Bet", ChartNames.COLD4BET.SB.vsCO3Bet),
                menuItem("vs Hijack 3-Bet", ChartNames.COLD4BET.SB.vsHJ3Bet)
              )
            },
            new Menu(label = "Button") {
              items = List(
                menuItem("vs Cut Off 3-Bet", ChartNames.COLD4BET.BTN.vsCO3Bet),
                menuItem("vs Hijack 3-Bet", ChartNames.COLD4BET.BTN.vsHJ3Bet)
              )
            },
            new Menu(label = "Cut Off") {
              items = List(
                menuItem("vs Hijack 3-Bet", ChartNames.COLD4BET.CO.vsHJ3Bet)
              )
            }
          )
        },
        new Menu(label = "VS 4-BET") {
          items = List(
            new Menu(label = "Big Blind") {
              items = List(
                menuItem("vs Small Blind 3-Bet", ChartNames.VS4BET.BB.vsSB4bet),
                menuItem("vs Button 3-Bet", ChartNames.VS4BET.BB.vsBTN4bet),
                menuItem("vs Cut Off 3-Bet", ChartNames.VS4BET.BB.vsCO4bet),
                menuItem("vs Hijack 3-Bet", ChartNames.VS4BET.BB.vsHJ4Bet),
                menuItem("vs Lojack 3-Bet", ChartNames.VS4BET.BB.vsLJ4Bet)
              )
            },
            new Menu(label = "Small Blind") {
              items = List(
                menuItem("vs Button 3-Bet", ChartNames.VS4BET.SB.vsBTN4bet),
                menuItem("vs Cut Off 3-Bet", ChartNames.VS4BET.SB.vsCO4bet),
                menuItem("vs Hijack 3-Bet", ChartNames.VS4BET.SB.vsHJ4Bet),
                menuItem("vs Lojack 3-Bet", ChartNames.VS4BET.SB.vsLJ4Bet)
              )
            },
            new Menu(label = "Button") {
              items = List(
                menuItem("vs Cut Off 3-Bet", ChartNames.VS4BET.BTN.vsCO4bet),
                menuItem("vs Hijack 3-Bet", ChartNames.VS4BET.BTN.vsHJ4Bet),
                menuItem("vs Lojack 3-Bet", ChartNames.VS4BET.BTN.vsLJ4Bet)
              )
            },
            new Menu(label = "Cut Off") {
              items = List(
                menuItem("vs Hijack 3-Bet", ChartNames.VS4BET.CO.vsHJ4Bet),
                menuItem("vs Lojack 3-Bet", ChartNames.VS4BET.CO.vsLJ4Bet)
              )
            },
            new Menu(label = "Hijack") {
              items = List(menuItem("vs Lojack 3-Bet", ChartNames.VS4BET.HJ.vsLJ4Bet))
            }
          )
        }
      )
    }
  }
}
