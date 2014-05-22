Spray Extensions
================

spray-extensions its a simple library with useful tools for develop web apps with Spray.


### Repository

Stable [OSS Sonatype](https://oss.sonatype.org/content/repositories/releases/com/github/jarlakxen/)

    "com.github.jarlakxen" %% "spray-extensions" % "1.0"

### Changelog

1.0
- Add [scalate](http://scalate.fusesource.org/) support.
- Add json filtering.
- Add pagination support.


## Pagination Support

The trait PaginationDirectives offers some helpers for handling pagination:

```
path("filter-test") {
  pagination { page =>
    complete {
      page match {
        case Some(page) => ... // A page was requested
        case None => ... // No page was requested
      }
    }
  }
}
```

The page object has this format

```
sealed trait Order

object Order {
  case object Asc extends Order
  case object Desc extends Order
}

case class PageRequest(index: Int, size: Int, sort: Map[String, Order])
```

This is an example of url: `/filter-test?page=1&size=10` or `/filter-test?page=1&size=10&sort=name,asc;age,desc`

The name of the parameters can be configured through [Typesafe Config](https://github.com/typesafehub/config):

```

spray {
    extensions {
        rest{
            pagination{
                index-param-name = "page"
                size-param-name  = "size"
                sort-param-name  = "sort"
                asc-param-name   = "asc"
                desc-param-name  = "desc"
                sorting-separator = ";"
                order-separator  = ","
                totalelements-header-name = "total_elements"
            }
        }
    }
}

```

When you replay a page, you sometime needs to send te total number of elements. This is usefull for the client for knowing how many pages they are. For this scenario there is:

```

path("filter-test") {
  pagination { page =>
    complete {
      page match {
        case Some(page) => {
            val response = PageResponse(List("test"), 10)
            completePage(response)
        }
        case None => ... // No page was requested
      }
    }
  }
}

```

The total number of elements is sended in the header `total_elements`

## JSON filtering Support

The trait FiltersDirectives offers some helpers for filtering the json response:

```
val JsonArrayFull = """[
    {
        "name":"user1",
        "password":"1234", 
        "meta":{"email":"user1@email.com", "age": 12}
    }, 
    {
        "name":"user2", 
        "password":"0000",
        "meta":{"email":"user2@email.com", "age": 20}
    }
  ]"""

path("filter-test") {
  pagination { page =>
    filterResponse(resolver.json4s) {
        complete { HttpEntity(ContentTypes.`application/json`, JsonArrayFull) }
    }
  }
}
```

The filterResponse directive required a resolver, depending of the json parser:
* resolver.json4s
* resolver.'spray-json'

You need to add the dependency to the json parser in you build.sbt ( "spray-json" or "json4s-jackson")

Using this url: `/filter-test?filter=name`. The result is: `[{"name":"user1"},{"name":"user2"}]`

Using this url: `/filter-test?filter=name;meta;!meta.email`. The result is: `[{"name":"user1","meta":{"age":12}},{"name":"user2","meta":{"age":20}}]`

In the last example the operator `!` is used to exclude fields.

You can customize the filter separator, changing the property `spray.extensions.rest.filter.separator`.

## Scalate Support

The trait ScalateSupport offers some helpers for using the [scalate](http://scalate.fusesource.org/) template engine. This is usefull if you, for example, want to use mustache.

```

path("filter-test") {
  pagination { page =>
    mustache("index", Map("USER_NAME" -> "Test"))
  }
}
```

In this example the file /templates/index.mustache is loaded by scalate. The base path can be changed, by the property `spray.extensions.view.templates-path`.


