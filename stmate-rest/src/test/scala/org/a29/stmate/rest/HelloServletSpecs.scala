package org.a29.stmate.rest

import com.softwaremill.bootzooka.BootzookaServletSpec

class HelloServletSpecs extends BootzookaServletSpec {
  addServlet(new HelloServlet(), "/*")

  "GET /World" should "return status 200" in {
    get("/World") {
      status should be (200)
    }
  }

  "GET /World" should "return JSON content type" in {
    get("/World") {
      header.get("Content-Type").get should include ("application/json")
    }
  }

  "GET /World" should "contain value Hello World in body" in {
    get("/World") {
      body should include ("{\"value\":\"Hello World\"}")
    }
  }
}
