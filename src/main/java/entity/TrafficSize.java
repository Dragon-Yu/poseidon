package entity;

/**
 * Traffic size, including input and output
 * Created by Johnson on 16/6/19.
 */
public class TrafficSize {

  private final long input;
  private final long output;

  public TrafficSize(long input, long output) {
    this.input = input;
    this.output = output;
  }

  public long getInput() {
    return input;
  }

  public long getOutput() {
    return output;
  }

  @Override
  public String toString() {
    return "TrafficSize{" +
      "input=" + input +
      ", output=" + output +
      '}';
  }
}
