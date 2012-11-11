package com.scalapeno.rhinos

import org.specs2.mutable._

import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._


class RhinosObjectSupportSpec extends SpecificationWithJUnit {
  
  trait RhinosObjectSupport { self: RhinosJsonSupport =>
    val scope: RhinosScope
    
    
    
    
  }
  
  
  
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