package com.scalapeno.rhinos

import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author <a href="mailto:jmahowald@angel.com">Josh Mahowald</a> 
 */


import org.specs2.mock.Mockito
import org.mockito.Matchers._  // to use matchers like anyInt()

import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._

class RhinosCallbackSupportSpec extends SpecificationWithJUnit with Mockito {
  "RhinosRuntime.eval[T](...)" should {
    var rhinos: RhinosRuntime = null

    step {
      rhinos = new RhinosRuntime()
    }


    "Allow for simple callbacks" in {
      val mockCallback = mock[SimpleCallback]

      //Stubbed call.  Don't know if the values need to match up here  or below when we assert
      mockCallback.callback(anyInt, anyString) returns "bar"
      rhinos.addObject("testobj", mockCallback)
      val result = rhinos.eval[String](
        """
          var x = testobj.callback(1, "foo");
          var y = 'bar' + x;
          y;
        """)
      result must beEqualTo(Some("barbar"))
      there was mockCallback.callback(1,"foo")

    }

//    "Allow to call arbitraty things" in {
//         val mockCallback = mock[SimpleCallback]
//
//         //Stubbed call.  Don't know if the values need to match up here  or below when we assert
//         mockCallback.complexCallBack(any[CustomObject])  returns "bar"
//         rhinos.addObject("testobj", mockCallback)
//         val result = rhinos.eval[String](
//           """
//             var x = testobj.callBackComplex("foo", true);
//             var y = 'bar' + x;
//             y;
//           """)
//
//         there was one(mockCallback.complexCallBack(CustomObject("foo", true)))
//
//       }
  }


  trait SimpleCallback {
    def callback(i:Int, s:String) : String
   // def complexCallBack(obj:CustomObject) : String
  }

  case class CustomObject(name1: String, name2:Boolean)
  implicit val customObjectFormat = jsonFormat2(CustomObject)

}
