package com.agemooij.rhinos

import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._

import org.specs2.mutable._


class RhinosSpec extends Specification {
  import Rhinos._

  "RhinosContext.eval()" should {
    
    "return None when the script is empty" in {
      val result = rhino(_.eval(""""""))
      
      result must beNone
    }
    
    "return None when the script does not return a result" in {
      val result = rhino(_.eval("""var x = 42;"""))
      
      result must beNone
    }

    "return Some(JsNull) when the script returns a Javascript null" in {
      val result = rhino(_.eval("""var b = null; b;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsNull)
    }

    "return Some(JsBoolean(true)) when the script returns a Javascript true" in {
      val result = rhino(_.eval("""var b = true; b;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsBoolean(true))
    }
    
    "return Some(JsBoolean(false)) when the script returns a Javascript false" in {
      val result = rhino(_.eval("""var b = false; b;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsBoolean(false))
    }

    "return Some(JsNumber) when the script returns a Javascript number" in {
      val result = rhino(_.eval("""var n = 3.1415; n;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsNumber(3.1415))
    }

    "return Some(JsString) when the script returns a Javascript string" in {
      val result = rhino(_.eval("""var s = "some string!"; s;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsString("some string!"))
    }

    "return Some(JsArray) when the script returns a Javascript array" in {
      val result = rhino(_.eval("""var a = [1,2,3]; a;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsArray(JsNumber(1), JsNumber(2), JsNumber(3)))
      result.get.convertTo[List[Int]] must beEqualTo(List(1,2,3))
    }
    
    "return Some(JsObject) when the script returns a Javascript object" in {
      val result = rhino(_.eval("""var o = {"name1": "value", "name2": true}; o;"""))
      
      result must beSome[JsValue]
      result.get must beEqualTo(
        JsObject("name1" -> JsString("value"), 
                 "name2" -> JsBoolean(true))
      )
    }
    
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
  }
  
  "RhinosContext.loadFromClasspath()" should {
    "not throw an exception when the path doesn't exist" in {
      val result = rhino { context =>
        context.loadFromClasspath("non-existing-path.js")
         
        // return None because the block should return an Option[JsValue]
        // but loadFromClasspath does not return a value
        None
      }
      
      result must beNone
    }
    
    "load functions from a file and make them available to eval()" in {
      val result = rhino { context =>
        context.loadFromClasspath("scripts/test-functions.js")
        context.eval("""var r = add2(add(10, 30)); r;""")
      }
      
      result must beSome[JsValue]
      result.get must beEqualTo(JsNumber(42))
    }
    
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
  }
}
