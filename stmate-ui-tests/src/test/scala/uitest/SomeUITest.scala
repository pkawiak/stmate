package uitest

import org.fest.assertions.Assertions

class SomeUITest extends BootzookaUITest {
  test("some test") {
    Assertions.assertThat(true).isTrue()
  }

}
