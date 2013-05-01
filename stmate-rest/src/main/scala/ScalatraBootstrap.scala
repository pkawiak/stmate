import org.a29.stmate.rest.HelloServlet
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle {
  val Prefix = "/rest"

  override def init(context: ServletContext) {
    context.mount(new HelloServlet, Prefix + "/hello")
  }

}
