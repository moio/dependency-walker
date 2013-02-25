package net.moioli.dependencywalker

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.HashSet
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import java.util.concurrent.TimeUnit

/**
 * Walks across Maven repositories on mvnrepository.com to find out
 * Java packages and dependencies.
 *
 * Limitation: only the latest versions are fetched at the moment
 *
 * @author Silvio Moioli, silvio@moioli.net
 */
class DependencyWalker {

  /** Selenium driver instance. */
  val driver = new HtmlUnitDriver

  /** Set of dependencies that were already visited (to avoid cycles). */
  private val visited = new HashSet[Dependency]()

  /**
   * Runs the specified query in mvnrepository.com
   * and returns the first result package's home page URL
   *
   * @author Silvio Moioli, silvio@moioli.net
   */
  def search(name: String): Package = {
    driver.get("http://mvnrepository.com/")

    driver.findElement(By id "query").clear
    driver.findElement(By id "query").sendKeys(name)
    driver.findElement(By cssSelector "input.button").click

    driver.findElement(By cssSelector "a.result-title").click
    driver.findElement(By cssSelector "a.versionbutton").click

    new Package(name, driver getCurrentUrl)
  }

  /**
   * Walks through all the dependencies of a package, calling a
   * function every time one is found.
   *
   * @param aPackage package to start visiting
   * @param doing function to be called when a dependency is found
   */
  def walkThroughAllDependenciesOf(aPackage: Package, doing: (Dependency) => Unit): Unit = {
    walkThroughDependenciesOf(aPackage, doing = (dependency) => {
      if ((visited contains dependency) == false) {
        doing(dependency)
        visited add dependency
        walkThroughAllDependenciesOf(dependency.to, doing)
      }
    })

    visited clear
  }

  /**
   * Walks through the direct dependencies of a package, calling a
   * function every time one is found.
   *
   * @param aPackage package to start visiting
   * @param doing function to be called when a dependency is found
   */
  def walkThroughDependenciesOf(aPackage: Package, doing: (Dependency) => Unit) = {
    driver get aPackage.url

    val links = driver.findElements(By xpath "//div[@id='maincontent']/table[2]/tbody//td[2]/a")
    val unsolvedDependencies = links.map(link => {
      new UnsolvedPackage(link getText, link getAttribute "href")
    })

    unsolvedDependencies foreach (unsolvedPackage => {
      driver get unsolvedPackage.url
      try {
        val completeUrl = driver.findElement(By cssSelector "a.versionbutton").getAttribute("href")
        val dependentPackage = new Package(unsolvedPackage.name, completeUrl)

        doing(new Dependency(aPackage, dependentPackage));
      } catch {
        case e: NoSuchElementException => Unit
      }
    })
  }
}

/** A mvnrepository.com package */
case class Package(name: String, url: String)
/** A dependency between packages */
case class Dependency(from: Package, to: Package)
/** Class to store temporary (name, url) pairs to avoid reusing Selenium elements between requests */
private case class UnsolvedPackage(name: String, url: String)
