package poseidon;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Johnson on 16/9/10.
 */
public class HandshakeManager {
  private static final String HANDSHAKE_ATTR = "handshake";
  private static final AttributeKey<ChannelPromise> HANDSHAKE_ATTRIBUTE_KEY = AttributeKey.newInstance(HANDSHAKE_ATTR);
  private static final Logger logger = LoggerFactory.getLogger(HandshakeManager.class);
  private static HandshakeManager ourInstance;
  private Context context = null;

  private HandshakeManager(Context context) {
    this.context = context;
  }

  public static synchronized HandshakeManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new HandshakeManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new HandshakeManager(context);
    }
    return ourInstance;
  }

  public boolean handshakeCompleted(Channel channel) {
    return channel.hasAttr(HANDSHAKE_ATTRIBUTE_KEY);
  }

  public void initHandshakeContext(Channel channel) {
    if (!channel.hasAttr(HANDSHAKE_ATTRIBUTE_KEY)) {
      channel.attr(HANDSHAKE_ATTRIBUTE_KEY).set(channel.newPromise());
    } else {
      logger.warn("duplicate handshake initiation");
    }
  }

  public void waitHandshake(Channel channel, GenericFutureListener<Future<Void>> listener) {
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().addListener(listener);
  }

  public void waitHandshake(Channel channel) throws InterruptedException {
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().await();
  }

  public void failHandshake(Channel channel, Throwable cause) {
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().setFailure(cause);
  }

  public void completeHandshake(Channel channel) {
    logger.warn(channel.toString());
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().setSuccess();
  }
}
