package poseidon;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;


/**
 * Created by Johnson on 16/9/10.
 */
public class HandshakeManager {
  private static final String HANDSHAKE_ATTR = "handshake";
  private static final AttributeKey<ChannelPromise> HANDSHAKE_ATTRIBUTE_KEY = AttributeKey.newInstance(HANDSHAKE_ATTR);
  private static final Logger logger = LoggerFactory.getLogger(HandshakeManager.class);
  private static HandshakeManager ourInstance;
  private Context context = null;
  private SettableFuture<Void> initFuture = SettableFuture.create();
  private Set<Channel> handshakeInProgressChannels = new ConcurrentSet<>();

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

  public synchronized void initHandshakeContext(Channel channel) {
    if (!channel.hasAttr(HANDSHAKE_ATTRIBUTE_KEY) || channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get() == null) {
      channel.attr(HANDSHAKE_ATTRIBUTE_KEY).set(channel.newPromise());
    } else {
      logger.debug("duplicate handshake initiation");
    }
  }

  public void waitHandshake(Channel channel, GenericFutureListener<Future<Void>> listener) {
    initHandshakeContext(channel);
    handshakeInProgressChannels.add(channel);
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().addListeners(listener,
      future -> handshakeInProgressChannels.remove(channel), future -> initFuture.set(null));
  }

  public boolean hasHandshakeInProgress() {
    return !handshakeInProgressChannels.isEmpty();
  }

  public void waitHandshake() throws ExecutionException, InterruptedException {
    initFuture.get();
  }

  public void failHandshake(Channel channel, Throwable cause) {
    initHandshakeContext(channel);
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().tryFailure(cause);
  }

  public void completeHandshake(Channel channel) {
    initHandshakeContext(channel);
    channel.attr(HANDSHAKE_ATTRIBUTE_KEY).get().setSuccess();
  }
}
