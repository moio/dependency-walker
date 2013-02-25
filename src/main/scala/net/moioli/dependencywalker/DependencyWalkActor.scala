package net.moioli.dependencywalker

import scala.runtime.Nothing$
import scala.collection.immutable.List
import scala.actors.Actor

/**
 * Uses DependencyWalker in a separate thread, notifying a
 * receiving actor when each dependency gets discovered.
 *
 * @author Silvio Moioli, silvio@moioli.net
 */
class DependencyWalkActor(receiver: Actor) extends Actor {

  /**
   * @see scala.actors.Actor#act()
   */
  override def act(): Unit = {
    val luke = new DependencyWalker

    while (true) {
      receive {
        case query: String =>
          val firstPackage = luke.search(query)

          luke walkThroughAllDependenciesOf (firstPackage, doing = { receiver ! _ })
          
          receiver ! Finished
      }
    }
  }
}

case object Finished