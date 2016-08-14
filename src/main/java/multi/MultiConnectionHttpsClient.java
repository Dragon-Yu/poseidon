package multi;

import config.BaseTestConfig;
import fullweb.TraceInfo;
import https.client.HttpsClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ChannelPoolUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Johnson on 16/7/29.
 */
public class MultiConnectionHttpsClient {

  static long startTime, endTime;
  static Logger logger = LoggerFactory.getLogger(HttpsClient.class);
  static AtomicInteger counter = new AtomicInteger(1);
  static int CHANNEL_POOL_SIZE = 10;
  protected int requestTimes = BaseTestConfig.REQUEST_TIMES;
  EventLoopGroup group = new NioEventLoopGroup();
  SocketAddress remoteAddress;
  SocketAddress localAddress;
  private ChannelPoolHandler channelPoolHandler;
  private Map<URL, TraceInfo> traces;

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    logger.info("uri redirected to: " + uri);
    new MultiConnectionHttpsClient().run(uri);
  }

  public void run(URI uri) throws Exception {
    int port = uri.getPort() > 0 ? uri.getPort() : BaseTestConfig.HTTPS_PORT;
    String host = uri.getHost();

    SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

    // Configure the client.
    try {
      Bootstrap b = new Bootstrap();
      b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
      b.group(group)
        .channel(NioSocketChannel.class)
        .remoteAddress(host, port);
      channelPoolHandler = new MultiConnectionHandler(sslCtx);
      ChannelPool channelPool = new FixedChannelPool(b, channelPoolHandler, CHANNEL_POOL_SIZE);
      ChannelPoolUtil.setChannelPool(channelPool);

      startTime = System.nanoTime();
      sendRequest(uri.toURL(), channelPool);

      // Wait for the server to close the connection.
      traces = ContextManager.getInstance().getTraces(60 * 3);
      endTime = System.nanoTime();
      long duration = endTime - startTime;
      logger.info(String.format("connection duration: %,dns (%d)", duration, duration));

      additionalInfo(channelPool);
      logger.info("request size: " + getRequestSize() + ", response size: " + getResponseSize());
    } finally {
      // Shut down executor threads to exit.
      group.shutdownGracefully();
    }
  }

  protected void additionalInfo(ChannelPool channelPool) throws Exception {
    Channel channel = channelPool.acquire().get();
    localAddress = channel.localAddress();
    remoteAddress = channel.remoteAddress();
  }

  protected void sendRequest(URL url, ChannelPool channelPool) {
    ChannelPoolUtil.sendRequest(channelPool, url);
  }

  public InetSocketAddress getRemoteAddress() {
    return (InetSocketAddress) remoteAddress;
  }

  public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) localAddress;
  }

  public long getTimeElapsed() {
    return endTime - startTime;
  }

  public Map<URL, TraceInfo> getTraces() {
    return traces;
  }

  public long getRequestSize() {
    long res = 0;
    Map<Channel, Long> map = ContextManager.getInstance().getRequestSize();
    for (Channel channel : map.keySet()) {
      res += map.get(channel);
    }
    return res;
  }

  public long getResponseSize() {
    long res = 0;
    Map<Channel, Long> map = ContextManager.getInstance().getResponseSize();
    for (Channel channel : map.keySet()) {
      res += map.get(channel);
    }
    return res;
  }
}
