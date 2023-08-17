package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class FloodColor extends LXPattern {

  ColorParameter color = new ColorParameter("color");

  public FloodColor(LX lx) {
    super(lx);
    addParameter("color", color);
  }

  public void run(double deltaMs) {
    for (LXPoint p : SpiderTrapModel.floods) {
      colors[p.index] = color.getColor();
    }
  }
}
