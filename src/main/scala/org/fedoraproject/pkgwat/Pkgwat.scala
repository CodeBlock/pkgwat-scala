package me.elrod.pkgwat

import spray.json._
import DefaultJsonProtocol._

import dispatch._, Defaults._

import scala.concurrent._
import scala.concurrent.duration._

import java.net.URLEncoder

case class APIResults[T](
  visibleRows: Int,
  totalRows: Int,
  rowsPerPage: Int,
  startRow: Int,
  rows: List[T])

case class Package(
  icon: String,
  description: String,
  link: String,
  subPackages: Option[List[Package]],
  summary: String,
  name: String,
  upstreamURL: Option[String] = None,
  develOwner: Option[String] = None)

case class Release(
  release: String,
  stableVersion: String,
  testingVersion: String)

case class Build(
  buildID: Long,
  completionTime: String, // TODO: DateTime this?
  completionTimeDisplay: Map[String, String],
  completionTimestamp: Double,
  creationEventID: Long,
  creationTime: String, //TODO: DateTime this?
  creationTimesstamp: Double,
  epoch: Option[Int],
  name: String,
  nvr: String,
  ownerID: Long,
  ownerName: String,
  packageID: Long,
  packageName: String,
  release: String,
  state: Int,
  stateString: String,
  taskID: Long,
  version: String,
  volumeID: Long,
  volumeName: String)

case class Bug(
  bugClass: String,
  description: String,
  id: Long,
  lastModified: String,
  release: String,
  status: String)

class Pkgwat(baseURL: String = "https://apps.fedoraproject.org/packages") {

  case class FilteredQuery(rowsPerPage: Int, startRow: Int, filters: Map[String, String])

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val filteredQueryFormat = jsonFormat(FilteredQuery, "rows_per_page", "start_row", "filters")

    implicit val releaseFormat = jsonFormat(Release, "release", "stable_version", "testing_version")
    implicit val releaseResultFormat = jsonFormat(APIResults[Release], "visible_rows", "total_rows", "rows_per_page", "start_row", "rows")

    implicit val packageFormat: JsonFormat[Package] = lazyFormat(jsonFormat(Package, "icon", "description", "link", "sub_pkgs", "summary", "name", "upstream_url", "devel_owner"))
    implicit val packageResultFormat = jsonFormat(APIResults[Package], "visible_rows", "total_rows", "rows_per_page", "start_row", "rows")

    implicit val buildFormat = jsonFormat(Build, "build_id", "completion_time", "completion_time_display", "completion_ts", "creation_event_id", "creation_time", "creation_ts", "epoch", "name", "nvr", "owner_id", "owner_name", "package_id", "package_name", "release", "state", "state_str", "task_id", "version", "volume_id", "volume_name")
    implicit val buildResultFormat = jsonFormat(APIResults[Build], "visible_rows", "total_rows", "rows_per_page", "start_row", "rows")

    implicit val bugFormat = jsonFormat(Bug, "bug_class", "description", "id", "last_modified", "release", "status")
    implicit val bugResultFormat = jsonFormat(APIResults[Bug], "visible_rows", "total_rows", "rows_per_page", "start_row", "rows")
  }

  import MyJsonProtocol._

  /** Returns an encoded URL ready to pass to Dispatch. */
  private def constructURL(path: String, query: FilteredQuery): String = {
    val json = query.toJson.compactPrint
    Seq(
      baseURL.replaceAll("/$", ""),
      "fcomm_connector",
      path,
      URLEncoder.encode(json, "utf8")).mkString("/")
  }

  /** Returns a [[Future[APIResults]]] after searching for a package.
    *
    * @param query The package to search for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def search(query: String, rowsPerPage: Int = 10, startRow: Int = 0) = {
    val jsonURL = constructURL(
      "xapian/query/search_packages",
      FilteredQuery(
        rowsPerPage,
        startRow,
        Map("search" -> query)))

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => JsonParser(content.replaceAll("""<\/?.*?>""", "")).convertTo[APIResults[Package]]
        case Left(error) => throw error
      }
    }
  }

  /** Returns a [[Future[Option[Package]]]] after searching Fedora Packages for it.
    *
    * @param query The package to search for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def get(query: String, rowsPerPage: Int = 10, startRow: Int = 0) = {
    val jsonURL = constructURL(
      "xapian/query/search_packages",
      FilteredQuery(
        rowsPerPage,
        startRow,
        Map("search" -> query)))

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => {
          val results = JsonParser(content.replaceAll("""<\/?.*?>""", "")).convertTo[APIResults[Package]]
          val subPackages = results.rows.map(_.subPackages).flatten.flatten
          val pkg = results.rows.filter(_.name == query) ++
            subPackages.filter(_.name == query)
          if (pkg.length == 1) Some(pkg.head) else None
        }
        case Left(error) => throw error
      }
    }
  }

  /** Returns a [[Future[Option[List[Release]]]]] after looking up package releases.
    *
    * @param pkg The package to search for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def releases(pkg: String, rowsPerPage: Int = 10, startRow: Int = 0) = {
    val jsonURL = constructURL(
      "bodhi/query/query_active_releases",
      FilteredQuery(
        rowsPerPage,
        startRow,
        Map("package" -> pkg)))

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => JsonParser(content.replaceAll("""<\/?.*?>""", "")).convertTo[APIResults[Release]]
        case Left(error) => throw error
      }
    }
  }

  /** Returns a [[Future[APIResults[Build]]]] after looking up builds.
    *
    * @param pkg The package to search for.
    * @param state The build state to search for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def builds(pkg: String, state: Option[String] = None, rowsPerPage: Int = 10, startRow: Int = 0) = {
    val stateEnum = state match {
      case Some("building") => "0"
      case Some("success") => "1"
      case Some("deleted") => "2"
      case Some("failed") => "3"
      case Some("cancelled") => "4"
      case _ => ""
    }
    val jsonURL = constructURL(
      "koji/query/query_builds",
      FilteredQuery(
        rowsPerPage,
        startRow,
        Map(
          "package" -> pkg,
          "state" -> stateEnum
        )))

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => JsonParser(content.replaceAll("""<\/?.*?>""", "")).convertTo[APIResults[Build]]
        case Left(error) => throw error
      }
    }
  }

  /** Returns a [[Future[APIResults[Bug]]]] after looking up package bugs.
    *
    * @param pkg The package to search for bugs for.
    * @param version The package version to filter for.
    * @param startRow The result row number to start at.
    * @param rowsPerPage How many rows should be returned at a time.
    */
  def bugs(pkg: String, version: String = "all", rowsPerPage: Int = 10, startRow: Int = 0) = {
    val versionEnum = version match {
      case "el5" => "5"
      case "el6" => "6"
      case "f15" => "15"
      case "f16" => "16"
      case "f17" => "17"
      case "f18" => "18"
      case "f19" => "19"
      case _ => ""
    }
    val jsonURL = constructURL(
      "bugzilla/query/query_bugs",
      FilteredQuery(
        rowsPerPage,
        startRow,
        Map(
          "package" -> pkg,
          "version" -> versionEnum
        )))

    for (result <- Http(url(jsonURL) OK as.String).either) yield {
      result match {
        case Right(content) => JsonParser(content.replaceAll("""<\/?.*?>""", "")).convertTo[APIResults[Bug]]
        case Left(error) => throw error
      }
    }
  }

}
