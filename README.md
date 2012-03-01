Rhinos (or RhinoS) is a tiny Scala wrapper around Mozilla's Rhino Javascript runtime for the JVM. It allows you to run Javascript code and to extract return values as native Scala objects, using the [Spray JSON](https://github.com/spray/spray-json) AST as an intermediate format. Spray JSON then allows you to easily convert that AST to normal Scala objects and/or instances of your own (case) classes.


### Status
__WARNING:__ Work in progress!    
This project is a few days old and still actively under construction. It has not been used in production.... yet.

All comments, suggestions, feature requests, pull requests, etc. and very welcome.


### Purpose
Run Javascript code and act on its output from Scala.

The use case that started this project was a __programmable fake REST server__ to support functional testing of mobile (iOS, Android) apps. It needed to run on the JVM because the _real_ REST server used heavy encryption written in Java but the behavior of the server (the _scenarios_ to run when responding to REST calls) needed to be programmed in Javascript because that was the one language all our testers knew.


### Roadmap
For version 0.1, the following features are under construction:

- Change return type of `context.loadFromClasspath(path: String)` to `None` so all context methods return a value
- Add `context.runFromClasspath(path: String): Option[JsValue]`
- Add `context.runFromFile(path: String): Option[JsValue]`
- Add `context.loadFromFile(path: String): None`
- Better Javascript error handling

Rough ideas for later:

- add support for injecting values into the Rhino context


### How Does it Work?
To give you an idea of how it works, here's a snippet from one of the unit tests:

```scala
"return Some(JsObject) when the script returns a Javascript object with nested objects" in {
  val result = rhino { context =>
    context.eval("""
        var func = function(a, b) {return a + b;};

        var bla = {
          "a": "string",
          "b": 1,
          "c": 3.1415,
          "d": false,
          "e": true,
          "f": null,
          "g": [1, 2, 3],
          "h": {
            "h1": 1,
            "h2": {
              "h2a": ["4", "5", "6"]
            }
          },
          "i": func(15, 27)
        };

        bla
      """
    )
  }

  result must beSome[JsValue]
  result.get must beEqualTo(
    JsObject("a" -> JsString("string"),
             "b" -> JsNumber(1), 
             "c" -> JsNumber(3.1415),
             "d" -> JsBoolean(false), 
             "e" -> JsBoolean(true),
             "f" -> JsNull,
             "g" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3)),
             "h" -> JsObject("h1" -> JsNumber(1),
                             "h2" -> JsObject("h2a" -> JsArray(JsString("4"), JsString("5"), JsString("6")))),
             "i" -> JsNumber(42))
  )
}
```

You can also load or run existing libraries and then use them. The below snippet shows that in action and it also shows how easy it is to convert the AST to native Scala objects.

```scala
    "load 3rd party JS lib from a file and make it available to eval()" in {
        val result = rhino { context =>
          context.loadFromClasspath("scripts/underscore.js")
          context.eval("""
            var mapped = _.map([1, 2, 3], function(num) { return num * 3; });
        
            mapped;
          """)
        }
    
        result must beSome[JsValue]
        result.get must beEqualTo(JsArray(JsNumber(3), JsNumber(6), JsNumber(9)))
        result.get.convertTo[List[Int]] must beEqualTo(List(3, 6, 9))
    }
```

For more examples, look at the [unit tests](https://github.com/agemooij/rhinos/blob/master/src/test/scala/com/scalapeno/rhinos/RhinosSpec.scala)

