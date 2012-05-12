Rhinos (or RhinoS) is a tiny Scala wrapper around Mozilla's Rhino Javascript runtime for the JVM. It allows you to run Javascript code and to extract return values as native Scala objects.

Internally it uses [Spray Json](https://github.com/spray/spray-json) and its excellent support for mapping Javascript values (i.e. Json) to Scala classes.


### Purpose
Run Javascript code and act on its output from Scala.

The use case that started this project was a __programmable fake REST server__ to support functional testing of mobile (iOS, Android) apps. It needed to run on the JVM because the _real_ REST server used heavy encryption written in Java but the behavior of the server (the _scenarios_ to run when responding to REST calls) needed to be programmed in Javascript because that was the one language all our testers knew.


### Status
This project is brand new and hasn't been tested in production yet.

Version 0.3.0 is out now and work is already underway on version 0.4. See the feature roadmap below for where it's all going.

All comments, suggestions, feature requests, pull requests, etc. and very welcome.


### Feature Roadmap
Rough ideas for the future:

- ![Done](https://github.com/agemooij/rhinos/raw/master/project/images/accept.png) re-enable direct access to the underlying Spray Json AST for apps that just need to output Json produced by Javascript, like REST web services. Basically make it possible for T in rhino[T] to be the unconverted JsObject or JsArray
- add support for calling Javascript functions, passing in native Scala arguments (using spray-json formatters)
- ![Done](https://github.com/agemooij/rhinos/raw/master/project/images/accept.png) add support for creating a global scope outside of the current rhino[T] block so a pre-loaded (sealed) scope can be reused
- ![Done](https://github.com/agemooij/rhinos/raw/master/project/images/accept.png) make the typing of rhino[T] more flexible so not all calls within the block have to produce a T
- add support for injecting values (global variables) into the Rhino scope
- add support for CommonJS modules
- add solid docs to the wiki
- put the scaladocs online
- Release artifacts into the SBT/Maven repo system


## Getting started
Things are still a bit chaotic but if you want to start playing with Rhinos, here how:

### Requirements
Rhinos is written in Scala (2.9.1/2.9.2), built using [SBT](https://github.com/harrah/xsbt/wiki) 0.11.x, and it depends on:

- [org.mozilla.rhino](http://www.mozilla.org/rhino/) 1.7R3
- [spray-json](https://github.com/spray/spray-json) 1.1.1
- [slf4j-api](http://www.slf4j.org/) 1.6.4
- [specs2](http://etorreborre.github.com/specs2/) 1.9 (only for testing)

I haven't hardcoded a dependency on a specific SLF4J implementation so you can choose your own, even though everybody knows you should use [Logback](http://logback.qos.ch/) :)


### Downloading Rhinos
As long as Rhinos has not been uploaded to an SBT/Maven repository, you'll have to download the archive, which includes all published artefacts for both Scala 2.9.1 and 2.9.2. 
Just extract it into your local SBT (~/.ivy2/local) or Maven (~/.m2/repository) repository.

### Building Rhinos
Or just build it yourself using [SBT](https://github.com/harrah/xsbt/wiki) 0.11.3. Just clone the Git repository and run the following command:

    sbt test publish-local

This puts the current version of Rhinos in your local SBT repository, for version 0.3.0 this would be in `~/.ivy2/local/com.scalapeno/rhinos_2.9.2/0.3.0/`
If you need a version for Scala 2.9.1, you can run a cross build using:

    sbt test + publish-local


### SBT Project Configuration
Add the following two lines to your list of dependencies:

```scala
libraryDependencies ++= Seq(
  ...
  "com.scalapeno" %% "rhinos" % "0.3.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0",
  ...
)
```


### Examples
To give you an idea of how Rhinos works, here's a snippet from one of the unit tests:

```scala
"return Some[Int] when T = Int and the script returns a Javascript number" in {
  val result = rhinos.eval[Int]("""var n = 3; n;""")

  result must beSome[Int]
  result.get must beEqualTo(3)
}

"return Some[String] when T = String and the script returns a Javascript string" in {
  val result = rhinos.eval[String]("""var s = "some string!"; s;""")
  
  result must beSome[String]
  result.get must beEqualTo("some string!")
}

"return Some(CustomObject) when T = CustomObject and the script returns a compatible Javascript object" in {
  case class CustomObject(name1: String, name2:Boolean)
  implicit val customObjectFormat = jsonFormat2(CustomObject)
  
  val result = rhinos.eval[CustomObject]("""var o = {"name1": "value", "name2": true}; o;""")
  
  result must beSome[CustomObject]
  result.get must beEqualTo(CustomObject("value", true))
}
```

You can also load or run existing libraries and then use them in subsequent calls. The below snippet shows how this works.

```scala
"eval the file and return the converted return value" in {
  val result = rhinos.evalFileOnClasspath[Int]("scripts/script-with-return-value.js")
  
  result must beSome[Int]
  result.get must beEqualTo(42)
}

"eval functions from a file and make them available to later calls to eval()" in {
  rhinos.evalFileOnClasspath[Unit]("scripts/test-functions.js")
  
  val result = rhinos.eval[Double]("""var r = add2(add(10, 30)); r;""")
  
  result must beSome[Double]
  result.get must beEqualTo(42.0)
}

"eval 3rd party JS lib from a file and make it available to later calls to eval()" in {
  rhinos.evalFileOnClasspath[Unit]("scripts/underscore.js")
  
  val result = rhinos.eval[List[Int]]("""
    var mapped = _.map([1, 2, 3], function(num) { return num * 3; });
    
    mapped;
  """)
  
  result must beSome[List[Int]]
  result.get must beEqualTo(List(3, 6, 9))
}
```

For more examples, take a look at the [unit tests](https://github.com/agemooij/rhinos/blob/master/src/test/scala/com/scalapeno/rhinos/RhinosRuntimeSpec.scala)


## License
<a rel="license" href="http://creativecommons.org/licenses/by/3.0/">
    <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" />
</a>
<br />
This work is licensed under a 
<a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>.


## Acknowledgements
Versions 0.1 and 0.2 of Rhinos were written on top of a mountain in Austria while "normal" people were out snowboarding. Many thanks to Georg Ribitsch for making this possible by letting me borrow his Austrian 3G dongle so I could download some dependencies and lookup some documentation once in a while.
