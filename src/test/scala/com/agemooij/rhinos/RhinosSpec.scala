package com.agemooij.rhinos

import cc.spray.json._
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

    "convert a Javascript String return value to a JsString" in {
      val result = rhino(_.eval("""var s = "some string!"; s;"""))
      
      result must beSome[JsValue]
      
      val jsString = result.get.asInstanceOf[JsString]
  
      jsString.value must beEqualTo("some string!")
    }

    "convert a Javascript Array return value to a JsArray" in {
      val result = rhino(_.eval("""var a = [1,2,3]; a;"""))
      
      result must beSome[JsValue]
      
      val jsArray = result.get.asInstanceOf[JsArray]
  
      jsArray.elements must have size(3)
      jsArray.elements(0) === JsNumber(1)
      jsArray.elements(1) === JsNumber(2)
      jsArray.elements(2) === JsNumber(3)
    }
    
    "convert a Javascript Object return value to a JsObject" in {
      val result = rhino(_.eval("""var o = {"name": "value"}; o;"""))
      
      result must beSome[JsValue]
      
      val jsObject = result.get.asInstanceOf[JsObject]
  
      jsObject.fields must have size(1)
      jsObject.fields("name") must beEqualTo(JsString("value"))
    }
    
    
    "convert a Javascript nested Object tree with calculated values to a JsObject" in {
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
              "i": func(1, 1)
            };

            bla
          """
        )
      }
      
      result must beSome[JsValue]
      
      val jsObject = result.get.asInstanceOf[JsObject]
  
      jsObject.fields must have size(9)
      jsObject.fields("a") must beEqualTo(JsString("string"))
      jsObject.fields("b") must beEqualTo(JsNumber(1))
      jsObject.fields("c") must beEqualTo(JsNumber(3.1415))
      jsObject.fields("d") must beEqualTo(JsBoolean(false))
      jsObject.fields("e") must beEqualTo(JsBoolean(true))
      jsObject.fields("f") must beEqualTo(JsNull)
      
      jsObject.fields("g") must beEqualTo(JsArray(JsNumber(1), JsNumber(2), JsNumber(3)))
      
      val h = jsObject.fields("h").asInstanceOf[JsObject]
      
      h.fields must have size(2)
      h.fields("h1") must beEqualTo(JsNumber(1))
      
      val h2 = h.fields("h2").asInstanceOf[JsObject]
      
      h2.fields must have size(1)
      h2.fields("h2a") must beEqualTo(JsArray(JsString("4"), JsString("5"), JsString("6")))
      
      jsObject.fields("i") must beEqualTo(JsNumber(2))
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
      
      val js = result.get.asInstanceOf[JsNumber]
  
      js.value must beEqualTo(42)
    }
    
    "load standard JS libs from a file and make them available to eval()" in {
      val result = rhino { context =>
        context.loadFromClasspath("scripts/underscore.js")
        context.eval("""
          var mapped = _.map([1, 2, 3], function(num){ return num * 3; });
          
          mapped;
        """)
      }
      
      result must beSome[JsValue]
      
      val jsArray = result.get.asInstanceOf[JsArray]
  
      jsArray.elements must have size(3)
      jsArray.elements(0) === JsNumber(3)
      jsArray.elements(1) === JsNumber(6)
      jsArray.elements(2) === JsNumber(9)
    }
  }
}
