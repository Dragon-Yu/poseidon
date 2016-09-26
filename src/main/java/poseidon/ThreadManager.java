package poseidon;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Created by Johnson on 16/9/10.
 */
public class ThreadManager {
  private static ThreadManager ourInstance;
  private final EventLoopGroup workingGroup = new NioEventLoopGroup();
  private final EventExecutorGroup eventExecutorGroup = new NioEventLoopGroup();
  private Context context = null;

  private ThreadManager(Context context) {
    this.context = context;
  }

  public static synchronized ThreadManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new ThreadManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new ThreadManager(context);
    }
    return ourInstance;
  }

  public EventLoopGroup getWorkingGroup() {
    return workingGroup;
  }

  public EventExecutor getEventExecutor() {
    return eventExecutorGroup.next();
  }
}
