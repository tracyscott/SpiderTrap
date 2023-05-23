package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class HScanner extends LXPattern {

  CompoundParameter posX = new CompoundParameter("pos", 0, -40, 40);
  CompoundParameter thick = new CompoundParameter("thick", 6, 0, 120);

  public HScanner(LX lx) {
    super(lx);
    addParameter("pos", posX);
    addParameter("thick", thick);
  }

  public void run(double deltaMs) {
    for (LXPoint p : SpiderTrapModel.allPoints) {
      if (p.x > posX.getValuef() && p.x < posX.getValuef() + thick.getValuef())
        colors[p.index] = LXColor.WHITE;
      else
        colors[p.index] = LXColor.BLACK;
    }
  }
}
