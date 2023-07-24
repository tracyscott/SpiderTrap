package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.Body;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class BodyBox extends LXPattern {


  public BodyBox(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (LXPoint p : SpiderTrapModel.allPoints) {
      if (Body.inAnyBody(p.x, p.z))
        colors[p.index] = LXColor.WHITE;
      else
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
    }
  }
}
