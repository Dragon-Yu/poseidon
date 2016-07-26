package fullweb;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpMessage;

/**
 * Created by Johnson on 16/7/24.
 */
public class FullWebContext {

  private HttpMessage httpMessage;
  private ByteToMessageDecoder.Cumulator cumulator = ByteToMessageDecoder.MERGE_CUMULATOR;
  ByteBuf content;

  public void clear() {
    httpMessage = null;
    content = null;
  }

  public HttpMessage getHttpMessage() {
    return httpMessage;
  }

  public ByteBuf getContent() {
    return content;
  }

  public void addContent(ByteBufAllocator allocator, ByteBuf byteBuf) {
    if (content == null) {
      content = byteBuf.retain();
    } else {
      content = cumulator.cumulate(allocator, content, byteBuf.retain());
    }

  }

  public void setHttpMessage(HttpMessage httpMessage) {
    this.httpMessage = httpMessage;
  }
}
