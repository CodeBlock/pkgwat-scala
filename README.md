# pkgwat (scala)

A port of [pkgwat.api](https://github.com/fedora-infra/pkgwat.api) to Scala.

The API works similarly, but is slightly different in that rather than
returning hashes, we return objects that contain the information.

We use Lift-JSON for dealing with JSON parsing and conversion.

# Example usage

```scala
scala> import scala.concurrent._
import scala.concurrent._

scala> import scala.concurrent.duration._
import scala.concurrent.duration._

scala> val pkgwat = new Pkgwat("https://apps.fedoraproject.org/packages")
pkgwat: Pkgwat = Pkgwat@50b7dd89

scala> val httpie = Await.result(pkgwat.search("httpie"), 5.seconds)
httpie: SearchResults = ...

scala> httpie.rows.head.develOwner
res4: Option[String] = Some(codeblock)

scala> httpie.rows.head.summary
res5: String = A Curl-like tool for humans
```

# License

(c) 2013 Red Hat, Inc.

License: Apache 2. See LICENSE and NOTICE.
