package multi;

import fullweb.FullWebHttpsHandler;
import fullweb.TraceController;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import util.ChannelPoolUtil;

import java.net.URL;

/**
 * Created by Johnson on 16/8/8.
 */
@ChannelHandler.Sharable
public class MultiHttpsHandler extends FullWebHttpsHandler {
  public MultiHttpsHandler(TraceController traceController) {
    super(traceController);
  }

  @Override
  protected void sendRequest(URL url, ChannelHandlerContext ctx) {
    traceController.visitUrl(url, null);
    ChannelPoolUtil.sendRequest(ChannelPoolUtil.getChannelPool(ctx.channel()), url);
  }
}
