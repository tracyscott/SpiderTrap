package art.lookingup.util;

import heronarts.lx.LX;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.pattern.LXPattern;

public class UtilForLX {
  /**
   * Utility method for finding the LeafMapper pattern, regardless of which channel it is in.
   *
   * @param lx
   * @return
   */

  static public LXChannel findChannel(LX lx, String channelName) {
    for (LXAbstractChannel absChannel : lx.engine.mixer.channels) {
      if (absChannel.label.getString().equals(channelName)) {
        return (LXChannel) absChannel;
      }
    }
    return null;
  }
}

