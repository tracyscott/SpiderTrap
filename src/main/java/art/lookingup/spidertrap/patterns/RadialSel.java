package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class RadialSel extends LXPattern {

  CompoundParameter radialNum = new CompoundParameter("radial", -1, -1, SpiderTrapModel.MAX_RADIALS - 0.75).setDescription("radial");
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");

  public RadialSel(LX lx) {
    super(lx);
    addParameter("radial", radialNum);
    addParameter("web", webNum);
  }

  public void run(double deltaMs) {

    float whichRadial = radialNum.getValuef();
    if (whichRadial > ModelParams.getRadials()) {
      whichRadial = ModelParams.getRadials() - 1;
    }
    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    int numRadials = ModelParams.getRadials();
    for (int mNum = 0; mNum < SpiderTrapModel.NUM_WEBS; mNum++) {
      if (mNum == webNum.getValuei() || webNum.getValuei() == -1) {
        for (int gNum = 0; gNum < numRadials; gNum++) {
          SpiderTrapModel.Radial radial = SpiderTrapModel.allWebs.get(mNum).radials.get(gNum);
          if (gNum == (int)Math.round(whichRadial) || (int)Math.round(whichRadial) == -1) {
            for (LXPoint p : radial.points) {
              colors[p.index] = LXColor.WHITE;
            }
          }
        }
      }
    }
  }
}
