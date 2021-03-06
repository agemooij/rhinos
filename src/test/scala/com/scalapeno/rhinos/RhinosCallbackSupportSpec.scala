package com.scalapeno.rhinos

import org.specs2.mutable.SpecificationWithJUnit
import org.mozilla.javascript.NativeObject

/**
 * @author <a href="mailto:jmahowald@angel.com">Josh Mahowald</a> 
 */


import org.specs2.mock.Mockito
import org.mockito.Matchers._  // to use matchers like anyInt()

import spray.json._
import spray.json.DefaultJsonProtocol._

class RhinosCallbackSupportSpec extends SpecificationWithJUnit with Mockito {
  "RhinosRuntime.eval[T](...)" should {
    var rhinos: RhinosRuntime = null

    step {
      rhinos = new RhinosRuntime()

      //Seems kind of hacky, but it's pretty useful to be able to debug from within the context of the
      //java script.
      //The actual print command suggestion came from http://community.jedit.org/?q=node/view/3849
      rhinos.eval[String](
            """
              function print(message) {
                    java.lang.System.out.println(message);
              }
            """)



    }



    "Allow for simple callbacks" in {
      val mockCallback = mock[SimpleCallback]

      //Stubbed call.  Don't know if the values need to match up here  or below when we assert
      mockCallback.callback(anyInt, anyString) returns "bar"
      rhinos.addObject("testobj", mockCallback)
      val result = rhinos.eval[String](
        """
          var x = testobj.callback(1, "foo");
          print("Got back:" + x);
          var y = 'bar' + x;
          y;
        """)
      there was mockCallback.callback(1,"foo")
      result must beEqualTo(Some("barbar"))

    }

    "Allow to call arbitraty things" in {
      val callback = new ComplexCallback
      val mockCallback = spy(callback)
         rhinos.addObject("complexobj", mockCallback)
         val result = rhinos.eval[CustomObject](
           """
              var o = {"name": "foo", "used" : false }
              var x = complexobj.complexCallback(o);
              print('got back:' + x);
              x;
           """)

         there was one(mockCallback).complexCallback(CustomObject("foo", false))
         result must beEqualTo(Some(CustomObject("foofoo", false)))

       }

//
//    "Substitute a function " in {
//      failure("Not yet implented")
//    }
  }



  trait SimpleCallback {
    def callback(i:Int, s:String) : String
  }

  class ComplexCallback extends RhinosJsonSupport {
    def complexCallback(obj:CustomObject) = CustomObject(obj.name + obj.name, obj.used)
    def complexCallback(obj:NativeObject):CustomObject = {
      val convert = toScala[CustomObject](obj)
      convert match {
        case Some(cust) =>complexCallback(cust)
        case None =>  deserializationError("Expected object not returned")
      }
    }
  }

  case class CustomObject(name: String, used:Boolean)

  implicit val customObjectFormat = jsonFormat2(CustomObject)

}
