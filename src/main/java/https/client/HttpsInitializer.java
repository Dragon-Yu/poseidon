package https.client;/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsInitializer extends ChannelInitializer<SocketChannel> {

  private final SslContext sslCtx;
  protected HttpsHandler httpsHandler = new HttpsHandler();
  Logger logger = LoggerFactory.getLogger(HttpsInitializer.class);
  ChannelTrafficShapingHandler channelTrafficShapingHandler = new ChannelTrafficShapingHandler(10);

  public HttpsInitializer(SslContext sslCtx) {
    this.sslCtx = sslCtx;
  }

  @Override
  public void initChannel(SocketChannel ch) {
    ChannelPipeline p = ch.pipeline();

    p.addLast(channelTrafficShapingHandler);

    // Enable HTTPS if necessary.
    if (sslCtx != null) {
      p.addLast(sslCtx.newHandler(ch.alloc()));
    }

    p.addLast(new HttpClientCodec());

    p.addLast(new ChannelOutboundHandlerAdapter() {
      @Override
      public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof DefaultFullHttpRequest) {
          DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;
          ctx.attr(AttributeKey.valueOf("test")).set(request.uri());
        }
        super.write(ctx, msg, promise);
      }
    });

    // Remove the following line if you don't want automatic content decompression.
    p.addLast(new HttpContentDecompressor());
    p.addLast(httpsHandler);
  }
}
