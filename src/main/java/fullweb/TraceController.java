package fullweb;

import io.netty.channel.Channel;

import java.net.URL;

/**
 * Created by Johnson on 16/7/21.
 */
public interface TraceController {
  boolean urlVisited(URL url);

  void visitUrl(URL url, Channel channel);

  void completeVisit(Channel channel, URL url);
}
