package http2.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by johnson on 16/1/11.
 */
public class Http2ResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
  static Logger logger = LoggerFactory.getLogger(Http2ResponseHandler.class);

  private SortedMap<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;

  public Http2ResponseHandler() {
    streamidPromiseMap = new TreeMap<>();
  }

  /**
   * Create an association between an anticipated response stream id and a {@link io.netty.channel.ChannelPromise}
   *
   * @param streamId The stream for which a response is expected
   * @param writeFuture A future that represent the request write operation
   * @param promise The promise object that will be used to wait/notify events
   * @return The previous objec associated with {@code streamId}
   * @see Http2ResponseHandler#awaitResponses(long, java.util.concurrent.TimeUnit)
   */
  public Map.Entry<ChannelFuture, ChannelPromise> put(int streamId, ChannelFuture writeFuture, ChannelPromise promise) {
    return streamidPromiseMap.put(streamId, new AbstractMap.SimpleEntry<>(writeFuture, promise));
  }

  /**
   * Wait (sequentially) for a time duration for each anticipated response
   *
   * @param timeout Value of time to wait for each response
   * @param unit Units associated with {@code timeout}
   */
  public void awaitResponses(long timeout, TimeUnit unit) {
    Iterator<Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
    while (itr.hasNext()) {
      Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
      ChannelFuture writeFuture = entry.getValue().getKey();
      if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
        throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
      }
      if (!writeFuture.isSuccess()) {
        throw new RuntimeException(writeFuture.cause());
      }
      ChannelPromise promise = entry.getValue().getValue();
      if (!promise.awaitUninterruptibly(timeout, unit)) {
        throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
      }
      if (!promise.isSuccess()) {
        throw new RuntimeException(promise.cause());
      }
      logger.info("---Stream id: " + entry.getKey() + " received---");
      itr.remove();
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
    Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
    if (streamId == null) {
      logger.error("HttpResponseHandler unexpected message received: " + msg);
      return;
    }

    Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap.get(streamId);
    if (entry == null) {
      logger.error("Message received for unknown stream id " + streamId);
    } else {
      // Do stuff with the message (for now just print it)
      ByteBuf content = msg.content();
      if (content.isReadable()) {
        int contentLength = content.readableBytes();
        byte[] arr = new byte[contentLength];
        content.readBytes(arr);
        logger.info(String.valueOf(new String(arr, 0, contentLength, CharsetUtil.UTF_8).length()));
      }
      entry.getValue().setSuccess();
    }
  }
}

