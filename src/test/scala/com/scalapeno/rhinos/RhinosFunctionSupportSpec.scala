package com.scalapeno.rhinos

import org.specs2.mutable._

import spray.json._
import spray.json.DefaultJsonProtocol._


class RhinosFunctionSupportSpec extends SpecificationWithJUnit {
  
  "RhinosFunctionSupport.callFunction[T](...)" should {
    var rhinos: RhinosRuntime = null
    
    step {
      rhinos = new RhinosRuntime()
    }
    
    "return None when the script is empty" in {
      val result = rhinos.eval[Int]("""""")
      
      result must beNone
    }
  }
}