package util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Johnson on 16/6/14.
 */
public class ShellUtilTest {

  @Test
  public void testExec() throws Exception {
    ShellUtil shellUtil = new ShellUtil();
    Assert.assertFalse(shellUtil.exec("hostname").isEmpty());
  }
}
