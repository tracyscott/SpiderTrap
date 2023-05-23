package art.lookingup.spidertrap.patterns;

import art.lookingup.util.EaseUtil;
import art.lookingup.colors.Colors;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class CosX extends LXPattern {
  CompoundParameter freq = new CompoundParameter("freq", 1f, 0f, 20f);
  CompoundParameter thick = new CompoundParameter("thick", 1f, 0f, 20f);
  CompoundParameter yOffset = new CompoundParameter("yoff", 0f, -10f, 10f);
  CompoundParameter amp = new CompoundParameter("amp", 1f, 0f, 5f);
  CompoundParameter phase = new CompoundParameter("phase", 0f, -Math.PI * 4f, Math.PI * 4f);
  CompoundParameter palT = new CompoundParameter("palT", 0f, 0f, 1f);
  DiscreteParameter ease = new DiscreteParameter("ease", 0, 0, EaseUtil.MAX_EASE + 1);
  CompoundParameter perlinFreq = new CompoundParameter("perFreq", 1f, 0f, 20f);

  double timeSecs = 0.0;

  public CosX(LX lx) {
    super(lx);
    addParameter(freq);
    addParameter(thick);
    addParameter(yOffset);
    addParameter(amp);
    addParameter(phase);
    addParameter(palT);
    addParameter(ease);
    addParameter(perlinFreq);
  }

  public float distance(LXPoint p, float x, float y, float z) {
    return (float)Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z));
  }

  public void run(double deltaMs) {


    EaseUtil easeUtil = new EaseUtil(ease.getValuei());
    if (ease.getValuei() == 8) {
      easeUtil.perlin2D = true;
      easeUtil.perlinFreq = perlinFreq.getValuef();
    } else {
      easeUtil.perlin2D = false;
    }
    for (LXPoint p : SpiderTrapModel.allPoints) {
      float xT = (p.x - lx.getModel().xMin) / lx.getModel().xRange;
      float zT = (p.z - lx.getModel().zMin) / lx.getModel().zRange;
      if (easeUtil.perlin2D)
        easeUtil.t2 = zT;
      int color = Colors.getParameterizedPaletteColor(lx, 0, xT, easeUtil); //0.5f + 0.5f * (float)Math.sin(timeSecs));
      float y = amp.getValuef() * (float)Math.cos(p.x * freq.getValuef() + phase.getValuef()) + 8f + yOffset.getValuef();
      float y2 = y + thick.getValuef();
      if (p.y >= y && p.y  <= y2)
        colors[p.index] = color;
      else
        colors[p.index] = LXColor.BLACK;
    }
    timeSecs += (deltaMs/1000f);
  }
}
