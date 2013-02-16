package net.moioli.packaging.example

import java.util.concurrent.TimeUnit

import org.openqa.selenium.By
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.hamcrest.CoreMatchers

object Main {

  def main(args: Array[String]): Unit = {
    val driver = new HtmlUnitDriver()
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)
    driver.get("http://docs.seleniumhq.org/")
    driver.findElement(By.linkText("Projects")).click()
    driver.findElement(By.linkText("Download")).click()
    driver.findElement(By.linkText("Source Code")).click()
    driver.findElement(By.xpath("//a[contains(text(),'Browse\n        SVN')]")).click()
    System.out.println(driver.getCurrentUrl())
  }

}