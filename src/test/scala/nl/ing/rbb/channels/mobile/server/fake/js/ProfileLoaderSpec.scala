package nl.ing.rbb.channels.mobile.server.fake.js

import org.specs2.mutable._


class ProfileLoaderSpec extends Specification {

    "The profile loader" should {
        "be able to load a profile from the classpath" in {
            
            /*
            
            Approach: profile gets:
                - loaded from file
                - executed in Rhino
                    - with all standard support functions loaded
                - translated to JsObject
                - translated to instance of Profile
            
            
            rhino { context =>
                context.load(...)
                context.eval("...")
            }
            
            
            */
            
            true must beTrue
        }
    }
}
