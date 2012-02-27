package com.agemooij.rhinos

import cc.spray.json._
import org.specs2.mutable._

class RhinosSpec extends Specification {
  import Rhinos._

  "RhinosContext.eval() " should {
    
    "return None when the script produces undefined output" in {
      val result = rhino(_.eval(""""""))
      
      result must beNone
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
}
