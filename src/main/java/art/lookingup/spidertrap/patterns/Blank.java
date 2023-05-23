package art.lookingup.spidertrap.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

/**
 * Utility pattern that just plays black with alpha.  This will be the default pattern for the DJ FX channel.
 * When a DJ hits a button for an effect, it will run the clip.  The clip should play some pattern and then always
 * end with this Blank pattern.  The FX channel will always be enabled.  Most of the time the channel will just
 * be adding a blank transparency to the bus.
 */
@LXCategory(LXCategory.OTHER)
public class Blank extends LXPattern {

  public Blank(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (LXPoint p : lx.getModel().points) {
      colors[p.index] = LXColor.rgba(0, 0, 0, 0);
    }
  }
}
