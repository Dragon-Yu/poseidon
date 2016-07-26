package https.client;

/**
 * Copyright 2012 The Netty Project
 * <p/>
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import config.BaseTestConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsHandler extends SimpleChannelInboundHandler<HttpObject> {

  Logger logger = LoggerFactory.getLogger(HttpsHandler.class);

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    if (msg instanceof HttpContent) {
      HttpContent content = (HttpContent) msg;
      logger.info(String.valueOf(content.content().toString(CharsetUtil.UTF_8).length()));
      if (content instanceof LastHttpContent) {
        HttpsClient.counter.getAndAdd(1);
      }
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    if (HttpsClient.counter.get() > BaseTestConfig.REQUEST_TIMES) {
      ctx.close();
      return;
    }
    super.channelReadComplete(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
