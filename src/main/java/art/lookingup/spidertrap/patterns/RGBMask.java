package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;

public class RGBMask extends LXEffect {

  BooleanParameter red = new BooleanParameter("red", true);
  BooleanParameter green = new BooleanParameter("green", true);
  BooleanParameter blue = new BooleanParameter("blue", true);

  public RGBMask(LX lx) {
    super(lx);
    addParameter("red", red);
    addParameter("green", green);
    addParameter("blue", blue);
  }

  public void run(double deltaMs, double damping) {
    for (LXPoint p : SpiderTrapModel.allPoints) {
      int redClr = LXColor.red(colors[p.index]);
      int greenClr = LXColor.green(colors[p.index]);
      int blueClr = LXColor.blue(colors[p.index]);
      if (!red.isOn())
        redClr = 0;
      if (!green.isOn())
        greenClr = 0;
      if (!blue.isOn())
        blueClr = 0;
      colors[p.index] = LXColor.rgb(redClr, greenClr, blueClr);
    }
  }
}
