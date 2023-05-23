package art.lookingup.spidertrap.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.OTHER)
public class StrobeFX extends DJFXPattern {
  public DiscreteParameter cycles = new DiscreteParameter("cycles", 2, 1, 41);
  public CompoundParameter onMs = new CompoundParameter("onms", 100f, 1f, 1000f).setDescription("On time(ms)");
  public CompoundParameter offMs = new CompoundParameter("offms", 100f, 1f, 1000f).setDescription("Off time(ms)");

  int currentCycles = 0;
  public boolean isOn = true;
  float currentOnTime = 0f;
  float currentOffTime = 0f;

  public StrobeFX(LX lx) {
    super(lx);
    addParameter("cycles", cycles);
    addParameter("onms", onMs);
    addParameter("offms", offMs);
  }

  public boolean isFinished() {
    if (currentCycles >= cycles.getValuei()) {
      currentCycles = 0;
      return true;
    }
    return false;
  }

  public void runFx(double deltaMs) {
    if (isOn) {
      currentOnTime += deltaMs;
    } else {
      currentOffTime += deltaMs;
    }
    if (isOn) {
      for (LXPoint p : lx.getModel().points) {
        colors[p.index] = LXColor.WHITE;
      }
    } else {
      for (LXPoint p : lx.getModel().points) {
        colors[p.index] = LXColor.BLACK;
      }
    }

    if (isOn && currentOnTime > onMs.getValuef()) {
      currentOnTime = 0f;
      isOn = false;
      currentCycles++;
    } else if (!isOn && currentOffTime > offMs.getValuef()) {
      currentOffTime = 0f;
      isOn = true;
    }
  }
}