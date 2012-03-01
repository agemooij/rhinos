Rhinos (or RhinoS) is a tiny Scala wrapper around Mozilla's Rhino Javascript runtime for the JVM. It allows you to run Javascript code and to extract return values as native Scala objects 

Internally it uses [Spray Json](https://github.com/spray/spray-json) and its excellent support for mapping Json structures to Scala classes.


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
- Add `context.runFromClasspath(path: String): Option[T]`
- Add `context.runFromFile(path: String): Option[T]`
- Add `context.loadFromFile(path: String): None`
- Better Javascript error handling

Rough ideas for later:

- add support for injecting values into the Rhino context


### How Does it Work?
To give you an idea of how it works, here's a snippet from one of the unit tests:

```scala
"return Some[Int] when T =:= Int and the script returns a Javascript number" in {
  val result = rhino[Int](_.eval("""var n = 3; n;"""))

  result must beSome[Int]
  result.get must beEqualTo(3)
}

"return Some[String] when T =:= String and the script returns a Javascript string" in {
  val result = rhino[String](_.eval("""var s = "some string!"; s;"""))
  
  result must beSome[String]
  result.get must beEqualTo("some string!")
}

"return Some(CustomObject) when T =:= CustomObject and the script returns a Javascript object" in {
  case class CustomObject(name1: String, name2:Boolean)
  implicit val customObjectFormat = jsonFormat2(CustomObject)
  
  val result = rhino[CustomObject](_.eval("""var o = {"name1": "value", "name2": true}; o;"""))
  
  result must beSome[CustomObject]
  result.get must beEqualTo(CustomObject("value", true))
}
```

You can also load or run existing libraries and then use them. The below snippet shows how this works.

```scala
"load 3rd party JS lib from a file and make it available to eval()" in {
  val result = rhino[List[Int]] { context =>
    context.loadFromClasspath("scripts/underscore.js")
    context.eval("""
      var mapped = _.map([1, 2, 3], function(num) { return num * 3; });
      
      mapped;
    """)
  }
  
  result must beSome[List[Int]]
  result.get must beEqualTo(List(3, 6, 9))
}
```

For more examples, take a look at the [unit tests](https://github.com/agemooij/rhinos/blob/master/src/test/scala/com/scalapeno/rhinos/RhinosSpec.scala)

