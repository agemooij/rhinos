Rhinos (or RhinoS) is a tiny Scala wrapper around Mozilla's Rhino Javascript runtime for the JVM. It allows you to run Javascript code and to extract return values as native Scala objects.

Internally it uses [Spray Json](https://github.com/spray/spray-json) and its excellent support for mapping Javascript values (i.e. Json) to Scala classes.


### Status
This project is brand new and hasn't been tested in production yet.

Version 0.1 is out now and contains more than 90% of all features I can currently imagine.

All comments, suggestions, feature requests, pull requests, etc. and very welcome.


### Purpose
Run Javascript code and act on its output from Scala.

The use case that started this project was a __programmable fake REST server__ to support functional testing of mobile (iOS, Android) apps. It needed to run on the JVM because the _real_ REST server used heavy encryption written in Java but the behavior of the server (the _scenarios_ to run when responding to REST calls) needed to be programmed in Javascript because that was the one language all our testers knew.


### Downloads
No downloads yet. Version 0.1 will be uploaded to a Maven/SBT repo near you ASAP!


### Requirements
Rhinos is built using SBT 0.11.x and depends on:

- org.mozilla.rhino 1.7R3
- spray-json 1.1.0
- slf4j-api 1.6.4
- specs2 1.8.2 (only for running the tests of course)

You will have to provide your own SLF4J implementation, even though everybody knows you should use Logback :)


### Roadmap
Rough ideas for 0.2 and later:

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

You can also load or run existing libraries and then use them in subsequent calls. The below snippet shows how this works.

```scala
"eval the file and return the converted return value" in {
  val result = rhino[Int] { context =>
    context.evalFileOnClasspath("scripts/script-with-return-value.js")
  }
  
  result must beSome[Int]
  result.get must beEqualTo(42)
}

"eval 3rd party JS lib from a file and make it available to later calls to eval()" in {
  val url = this.getClass.getClassLoader.getResource("scripts/underscore.js")
  val file = new File(url.toURI)
  
  val result = rhino[List[Int]] { context =>
    context.evalFile(file)
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

### License
<a rel="license" href="http://creativecommons.org/licenses/by/3.0/">
    <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" />
</a>
<br />
This work is licensed under a 
<a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>.

### Acknowledgements
Versions 0.1 and 0.2 of Rhinos were written on top of a mountain in Austria. Many thanks to Georg Ribitsch for making this possible by letting me borrow his Austrian 3G dongle so I could download some dependencies and lookup some documentation once in a while.
