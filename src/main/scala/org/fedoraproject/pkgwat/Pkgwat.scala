package org.fedoraproject.pkgwat

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

import dispatch._, Defaults._

import scala.concurrent._
import scala.concurrent.duration._

import java.net.URLEncoder

case class Package(
  icon: String,
  description: String,
  link: String,
  subPackages: List[Package],
  summary: String,
  name: String,
  upstreamURL: Option[String] = None,
  develOwner: Option[String] = None)

case class SearchResults(
  visibleRows: Int,
  totalRows: Int,
  rowsPerPage: Int,
  startRow: Int,
  rows: List[Package])

class Pkgwat(baseURL: String) {
  /** JSON Magic. */
  implicit val formats = DefaultFormats

  /** Returns an encoded URL ready to pass to Dispatch. */
  private def constructURL(path: String, query: Map[String, Any]): String = {
    val json = write(query)
    Seq(
      baseURL.replaceAll("/$", ""),
      "fcomm_connector",
      path,
      URLEncoder.encode(json, "utf8")).mkString("/")
  }

  /** Returns a [[SearchResults]] after searching the Fedora Packages app for a package.
    *
    * @param query The package to search for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def search(query: String, rowsPerPage: Int = 10, startRow: Int = 0) = {
    val jsonURL = constructURL(
      "xapian/query/search_packages",
      Map(
        "filters" -> Map("search" -> query),
        "rows_per_page" -> rowsPerPage,
        "start_row" -> startRow
      )
    )

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => {
          // Strip tags and slurp into lift-json's magic.
          val json = parse(content.replaceAll("""<\/?.*?>""", ""))

          // Transform so that we match our case classes above.
          json.transform {
            case JField("upstream_url", x) => JField("upstreamURL", x)
            case JField("sub_pkgs", x) => JField("subPackages", x)
            case JField("devel_owner", x) => JField("develOwner", x)
            case JField("visible_rows", x) => JField("visibleRows", x)
            case JField("start_row", x) => JField("startRow", x)
            case JField("rows_per_page", x) => JField("rowsPerPage", x)
            case JField("total_rows", x) => JField("totalRows", x)
          }.extract[SearchResults]
        }
        case Left(error) => throw error
      }
    }
  }
}
