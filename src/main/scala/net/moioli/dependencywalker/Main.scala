package net.moioli.dependencywalker

import java.awt.Color
import java.awt.Dimension
import java.awt.Paint

import scala.swing.event.ButtonClicked
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import scala.swing.Swing
import scala.swing.TextField

import org.apache.commons.collections15.Transformer

import edu.uci.ics.jung.algorithms.layout.CircleLayout
import edu.uci.ics.jung.graph.DirectedSparseGraph
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller
import edu.uci.ics.jung.visualization.BasicVisualizationServer

/**
 * A Swing application wrapper for DependencyWalker.
 * 
 * Admittedly, not the best-looking code ever written, but
 * quite nice if compared to Java Swing standards.
 */
object Main extends SimpleSwingApplication {

  def top = new MainFrame {

    // lay out the GUI
    title = "Java Maven dependency graphs"
    val searchLabel = new Label {
      text = "Draw dependency graph for package:"
      border = Swing.EmptyBorder(5, 5, 5, 5)
    }
    val searchStringTextField = new TextField {
      text = "spring"
      columns = 20
    }
    val goButton = new Button {
      text = "Generate graph"
    }

    val graph = new DirectedSparseGraph[Package, Dependency]
    val layout = new CircleLayout(graph)
    val graphPanel = new BasicVisualizationServer(layout, new Dimension(800,600))
    setGraphStyle(graphPanel)

    contents = new BorderPanel() {
      layout(new FlowPanel() {
        contents += searchLabel
        contents += searchStringTextField
        contents += goButton
      }) = BorderPanel.Position.North

      layout(Component.wrap(graphPanel)) = BorderPanel.Position.Center

      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    // start background actors
    val guiActor = new GUIUpdaterActor(graph, graphPanel, goButton)
    guiActor start

    val luke = new DependencyWalkActor(guiActor)
    luke start

    // add GUI listeners
    listenTo(goButton)
    reactions += {
      case ButtonClicked(_) =>
        goButton.enabled = false
        luke ! searchStringTextField.text
    }
  }

  /** Add labels and colors. */
  def setGraphStyle(graphPanel: BasicVisualizationServer[Package, Dependency]) = {
    graphPanel setBackground(Color WHITE)
    
    val context = graphPanel getRenderContext
    
    context setVertexLabelTransformer(new ToStringLabeller[Package]() {
      override def transform(aPackage: Package) = {
        aPackage name
      }
    })
    
    context setVertexFillPaintTransformer(new Transformer[Package, Paint]() {
        def transform(aPackage: Package) = {
            new Color(0x88, 0xD8, 0x47)
        }
    })

    context setVertexDrawPaintTransformer(new Transformer[Package, Paint]() {
        def transform(aPackage: Package) = {
            new Color(0x4B, 0x90, 0x37)
        }
    })
  }
}
