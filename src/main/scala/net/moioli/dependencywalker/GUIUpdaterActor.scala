package net.moioli.dependencywalker

import java.awt.EventQueue

import scala.actors.Actor
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.swing.Button

import edu.uci.ics.jung.algorithms.layout.ISOMLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.BasicVisualizationServer


/**
 * Updates the GUI when new dependencies are received.
 */
class GUIUpdaterActor(graph: Graph[Package, Dependency], panel: BasicVisualizationServer[Package, Dependency], button: Button) extends Actor {

  /**
   * @see scala.actors.Actor#act()
   */
  override def act(): Unit = {

    while (true) {
      receive {
        case ClearGraph =>
          (graph getEdges).toList.foreach(graph removeEdge _)
          (graph getVertices).toList.foreach(graph removeVertex _)
        case result: Dependency =>
          runOnSwingThread(() => {
            graph addVertex result.from
            graph addVertex result.to
            graph addEdge (result, result.from, result.to)

            val layout = new ISOMLayout(graph)

            panel.setGraphLayout(layout)
          })
        case Finished => runOnSwingThread(() => button.enabled = true)
      }
    }
  }
    
  /** Runs a function on the GUI thread. */
  def runOnSwingThread(function: () => Unit) = {
    EventQueue.invokeLater(new Runnable {
      def run() {
        function()
      }
    })
  }
}