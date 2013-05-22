package tests

import org.scalatest.{ FunSpec, ParallelTestExecution }
import org.scalatest.matchers.ShouldMatchers._

import scala.concurrent._
import scala.concurrent.duration._

import org.fedoraproject.pkgwat._

class PkgwatSpec extends FunSpec with ParallelTestExecution {
  val pkgwat = new Pkgwat("https://apps.fedoraproject.org/packages")

  describe("The search method") {
    it("should be able to search Fedora Packages") {
      val result = Await.result(pkgwat.search("httpie"), 5.seconds)
      result.totalRows should equal (result.rows.length)
      result.rows.head.summary should equal ("A Curl-like tool for humans")
      result.rows.head.name should equal ("httpie")
      result.rows.head.subPackages.head.name should equal ("python3-httpie")
    }
  }
}
