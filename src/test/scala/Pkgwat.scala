package tests

import org.scalatest.{ FunSpec, ParallelTestExecution }
import org.scalatest.matchers.ShouldMatchers._

import scala.concurrent._
import scala.concurrent.duration._

import me.elrod.pkgwat._

class PkgwatSpec extends FunSpec with ParallelTestExecution {
  val pkgwat = new Pkgwat

  describe("The search method") {
    it("should be able to search Fedora Packages") {
      val result = Await.result(pkgwat.search("httpie"), 5.seconds)
      result.totalRows should equal (result.rows.length)
      result.rows.head.summary should equal ("A Curl-like tool for humans")
      result.rows.head.name should equal ("httpie")
      result.rows.head.subPackages should not be (None)
      result.rows.head.subPackages.get.head.name should equal ("python3-httpie")
    }
  }

  describe("The get method") {
    it("should work with subpackages") {
      val p = Await.result(pkgwat.get("gcc-go"), 5.seconds)
      p should not be (None)
      p.get.name should equal("gcc-go")
      p.get.summary should equal("Go support")
    }

    it("should work with main packages") {
      val p = Await.result(pkgwat.get("gcc"), 5.seconds)
      p should not be (None)
      p.get.name should equal("gcc")
      p.get.summary should equal("Various compilers (C, C++, Objective-C, Java, ...)")
    }

    it("should bail out gracefully on no results") {
      val p = Await.result(pkgwat.get("nonexistant-package-foo"), 5.seconds)
      p should be (None)
    }
  }

  describe("The releases method") {
    it("should list all releases for a given package") {
      val httpieReleases = Await.result(pkgwat.releases("httpie"), 5.seconds)
      httpieReleases.rows.length should be > (4)
      httpieReleases.rows.length should be < (10)
      httpieReleases.rows.map(_.release) should contain ("Fedora EPEL 6")
    }
  }

  describe("The builds method") {
    it("should list all builds for a given package") {
      val httpieBuilds = Await.result(pkgwat.builds("httpie"), 5.seconds)
      httpieBuilds.rows.length should be > (4)
      httpieBuilds.rows.length should be < (100)
      httpieBuilds.rows.head.packageName should equal ("httpie")
    }
  }

  describe("The bugs method") {
    it("should list bugs for a given package") {
      val httpieBuilds = Await.result(pkgwat.bugs("firefox"), 5.seconds)
      httpieBuilds.rows.length should be > (5)
      httpieBuilds.rows.length should be < (500)
    }
  }
}
