package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.CVBlob;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class CVBlobTest extends LXPattern {

  CompoundParameter radius = new CompoundParameter("radius", 0.2, 0.01, 2.0);
  CompoundParameter alive = new CompoundParameter("alive", 100, 10, 1000);

  public CVBlobTest(LX lx) {

    super(lx);
    addParameter("radius", radius);
    addParameter("alive", alive);
  }

  public void run(double deltaMs) {
    CVBlob.cleanExpired(alive.getValuef());
    for (LXPoint p : SpiderTrapModel.allPoints) {
      if (CVBlob.isInAnyBlob(p.x, p.z, radius.getValuef()))
        colors[p.index] = LXColor.WHITE;
      else
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
    }
  }
}
