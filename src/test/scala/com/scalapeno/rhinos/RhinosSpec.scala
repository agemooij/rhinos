package com.scalapeno.rhinos

import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._

import org.specs2.mutable._


class RhinosSpec extends Specification {
  import Rhinos._

  "RhinosContext[T].eval(...)" should {
    "return None when the script is empty" in {
      val result = rhino[Int](_.eval(""""""))
      
      result must beNone
    }
    
    "return None when the script does not return a result" in {
      val result = rhino[Double](_.eval("""var x = 42;"""))
      
      result must beNone
    }

    "return None when the script returns a Javascript null" in {
      val result = rhino[String](_.eval("""var b = null; b;"""))
      
      result must beNone
    }

    "return None when the result cannot be converted to T" in {
      val result = rhino[Boolean](_.eval("""var s = "some string!"; s;"""))
      
      result must beNone
    }

    "return Some(true) when T =:= Boolean and the script returns a Javascript true" in {
      val result = rhino[Boolean](_.eval("""var b = true; b;"""))
      
      result must beSome[Boolean]
      result.get must beTrue
    }
    
    "return Some(false) when T =:= Boolean and the script returns a Javascript false" in {
      val result = rhino[Boolean](_.eval("""var b = false; b;"""))
      
      result must beSome[Boolean]
      result.get must beFalse
    }

    "return Some[Int] when T =:= Int and the script returns a Javascript number" in {
      val result = rhino[Int](_.eval("""var n = 3; n;"""))
    
      result must beSome[Int]
      result.get must beEqualTo(3)
    }
    
    "return Some[Long] when T =:= Long and the script returns a Javascript number" in {
      val result = rhino[Long](_.eval("""var n = 3141521424; n;"""))
    
      result must beSome[Long]
      result.get must beEqualTo(3141521424L)
    }

    "return Some[Float] when T =:= Float and the script returns a Javascript number" in {
      val result = rhino[Float](_.eval("""var n = 3.1415; n;"""))
    
      result must beSome[Float]
      result.get must beEqualTo(3.1415f)
    }
    
    "return Some[Double] when T =:= Double and the script returns a Javascript number" in {
      val result = rhino[Double](_.eval("""var n = 1313133.141586193338; n;"""))
    
      result must beSome[Double]
      result.get must beEqualTo(1313133.141586193338)
    }
    
    // TODO: add tests for the Javascript 52bit number boundary cases
    
    "return Some[Int] when T =:= Int and the script returns a Javascript number (auto-converted from Double to Int)" in {
      val result = rhino[Int](_.eval("""var n = 3.1415; n;"""))
    
      result must beSome[Int]
      result.get must beEqualTo(3)
    }
    
    "return Some[Byte] when T =:= Byte and the script returns a Javascript number" in {
      val result = rhino[Char](_.eval("""var c = 'c'; c;"""))
    
      result must beSome[Char]
      result.get must beEqualTo('c')
    }
    
    "return Some[Char] when T =:= Char and the script returns a Javascript character" in {
      val result = rhino[Char](_.eval("""var c = 'c'; c;"""))
    
      result must beSome[Char]
      result.get must beEqualTo('c')
    }

    "return Some[String] when T =:= String and the script returns a Javascript string" in {
      val result = rhino[String](_.eval("""var s = "some string!"; s;"""))
      
      result must beSome[String]
      result.get must beEqualTo("some string!")
    }

    "return Some(Some[String]) when T =:= Option[String] and the script returns a Javascript string" in {
      val result = rhino[Option[String]](_.eval("""var a = "An optional String"; a;"""))
      
      result must beSome[Option[String]]
      result.get must beEqualTo(Some("An optional String"))
    }
    
    "return None when T =:= Option[String] and the script returns a Javascript null" in {
      val result = rhino[Option[String]](_.eval("""var a = null; a;"""))
      
      result must beNone
    }

    "return Some[List[Int]] whenT =:= List[Int] and the script returns a Javascript array of numbers" in {
      val result = rhino[List[Int]](_.eval("""var a = [1,2,3]; a;"""))
      
      result must beSome[List[Int]]
      result.get must beEqualTo(List(1,2,3))
    }
    
    "return None when T =:= List[String] and the script returns a Javascript array of numbers" in {
      val result = rhino[List[String]](_.eval("""var a = [1,2,3]; a;"""))
      
      result must beNone
    }
    
    "return Some[Map[String, Int]] when T =:= Map[String, Int] and the script returns a compatible Javascript object" in {
      val result = rhino[Map[String, Int]](_.eval("""var o = {"name1": 40, "name2": 2}; o;"""))
      
      result must beSome[Map[String, Int]]
      result.get must beEqualTo(Map("name1" -> 40, "name2" -> 2))
    }
    
    "return Some(CustomObject) when T =:= CustomObject and the script returns a Javascript object" in {
      case class CustomObject(name1: String, name2:Boolean)
      implicit val customObjectFormat = jsonFormat2(CustomObject)
      
      val result = rhino[CustomObject](_.eval("""var o = {"name1": "value", "name2": true}; o;"""))
      
      result must beSome[CustomObject]
      result.get must beEqualTo(CustomObject("value", true))
    }
    
    "return Some(CompoundObject) when T =:= CompoundObject and the script returns a Javascript object with nested objects" in {
      case class CompoundObject(a: String, b:Int, c:Double, d:Boolean, e:Boolean, f:Option[String], g:List[Int], h:NestedObject)
      case class NestedObject(h1:Long, h2:DoublyNestedObject)
      case class DoublyNestedObject(h2a:List[String])
      
      implicit val doublyNestedFormat = jsonFormat1(DoublyNestedObject)
      implicit val nestedFormat = jsonFormat2(NestedObject)
      implicit val compoundFormat = jsonFormat(CompoundObject, "a", "b", "c", "d", "e", "f", "g", "h")
      
      val result = rhino[CompoundObject] { context =>
        context.eval("""
            var func = function(a, b) {return a + b;};
  
            var bla = {
              "a": "string",
              "b": func(15, 27),
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
              }
            };
  
            bla
          """
        )
      }
      
      result must beSome[CompoundObject]
      result.get must beEqualTo(
        CompoundObject(
          a = "string",
          b = 42, 
          c = 3.1415,
          d = false, 
          e = true,
          f = None,
          g = List(1, 2, 3),
          h = NestedObject(
            h1 = 1,
            h2 = DoublyNestedObject(
              h2a = List("4", "5", "6"))))
      )
    }
  }
  
  "RhinosContext[T].loadFromClasspath()" should {
    "not throw an exception when the path doesn't exist" in {
      val result = rhino[Unit] { context =>
        context.loadFromClasspath("non-existing-path.js")
        
        None
      }
      
      result must beNone
    }
    
    "load functions from a file and make them available to eval()" in {
      val result = rhino[Double] { context =>
        context.loadFromClasspath("scripts/test-functions.js")
        context.eval("""var r = add2(add(10, 30)); r;""")
      }
      
      result must beSome[Double]
      result.get must beEqualTo(42.0)
    }
    
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
  }
  
  
  // "RhinosContext.eval()" should {
  //   
  //   "return None when the script is empty" in {
  //     val result = rhino(_.eval(""""""))
  //     
  //     result must beNone
  //   }
  //   
  //   "return None when the script does not return a result" in {
  //     val result = rhino(_.eval("""var x = 42;"""))
  //     
  //     result must beNone
  //   }
  // 
  //   "return Some(JsNull) when the script returns a Javascript null" in {
  //     val result = rhino(_.eval("""var b = null; b;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsNull)
  //   }
  // 
  //   "return Some(JsBoolean(true)) when the script returns a Javascript true" in {
  //     val result = rhino(_.eval("""var b = true; b;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsBoolean(true))
  //   }
  //   
  //   "return Some(JsBoolean(false)) when the script returns a Javascript false" in {
  //     val result = rhino(_.eval("""var b = false; b;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsBoolean(false))
  //   }
  // 
  //   "return Some(JsNumber) when the script returns a Javascript number" in {
  //     val result = rhino(_.eval("""var n = 3.1415; n;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsNumber(3.1415))
  //   }
  // 
  //   "return Some(JsString) when the script returns a Javascript string" in {
  //     val result = rhino(_.eval("""var s = "some string!"; s;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsString("some string!"))
  //   }
  // 
  //   "return Some(JsArray) when the script returns a Javascript array" in {
  //     val result = rhino(_.eval("""var a = [1,2,3]; a;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(JsArray(JsNumber(1), JsNumber(2), JsNumber(3)))
  //     result.get.convertTo[List[Int]] must beEqualTo(List(1,2,3))
  //   }
  //   
  //   "return Some(JsObject) when the script returns a Javascript object" in {
  //     val result = rhino(_.eval("""var o = {"name1": "value", "name2": true}; o;"""))
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(
  //       JsObject("name1" -> JsString("value"), 
  //                "name2" -> JsBoolean(true))
  //     )
  //   }
  //   
  //   "return Some(JsObject) when the script returns a Javascript object with nested objects" in {
  //     val result = rhino { context =>
  //       context.eval("""
  //           var func = function(a, b) {return a + b;};
  // 
  //           var bla = {
  //             "a": "string",
  //             "b": 1,
  //             "c": 3.1415,
  //             "d": false,
  //             "e": true,
  //             "f": null,
  //             "g": [1, 2, 3],
  //             "h": {
  //               "h1": 1,
  //               "h2": {
  //                 "h2a": ["4", "5", "6"]
  //               }
  //             },
  //             "i": func(15, 27)
  //           };
  // 
  //           bla
  //         """
  //       )
  //     }
  //     
  //     result must beSome[JsValue]
  //     result.get must beEqualTo(
  //       JsObject("a" -> JsString("string"),
  //                "b" -> JsNumber(1), 
  //                "c" -> JsNumber(3.1415),
  //                "d" -> JsBoolean(false), 
  //                "e" -> JsBoolean(true),
  //                "f" -> JsNull,
  //                "g" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3)),
  //                "h" -> JsObject("h1" -> JsNumber(1),
  //                                "h2" -> JsObject("h2a" -> JsArray(JsString("4"), JsString("5"), JsString("6")))),
  //                "i" -> JsNumber(42))
  //     )
  //   }
  // }
  
}
