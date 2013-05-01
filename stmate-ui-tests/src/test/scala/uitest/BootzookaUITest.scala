package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit

class BootzookaUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter {

  var driver: FirefoxDriver = _

  override def beforeAll() {
    startJetty()
  }

  before {
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
  }

  after {
    driver.close
    driver = null
  }

  override def afterAll() {
    stopJetty()
  }
}

trait EmbeddedJetty {
  protected var jetty: Server = null
  protected var context: ServletContext = null

  def startJetty() {
    jetty = new Server(8080)
    jetty setHandler prepareContext
    jetty.start()
  }

  private def prepareContext() = {
    val context = new WebAppContext()
    context setContextPath "/"
    context setResourceBase "stmate-ui/src/main/webapp"
    this.context = context.getServletContext
    context
  }


  def stopJetty() {
    jetty.stop()
  }
}