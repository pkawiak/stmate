package org.a29.stmate.rest

import com.softwaremill.bootzooka.common.JsonWrapper

class HelloServlet extends JsonServlet {

  get("/:who") {
    JsonWrapper("Hello " + params("who"))
  }

}
