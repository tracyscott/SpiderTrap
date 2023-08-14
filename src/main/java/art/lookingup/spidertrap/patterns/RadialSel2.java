package art.lookingup.spidertrap.patterns;

import art.lookingup.linear.Edge;
import art.lookingup.linear.Joint;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class RadialSel2 extends LXPattern {

  CompoundParameter radialNum = new CompoundParameter("radial", -1, -1, SpiderTrapModel.MAX_RADIALS - 0.75).setDescription("radial");
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");

  DiscreteParameter pos = new DiscreteParameter("pos", -1, -1, 190);

  public RadialSel2(LX lx) {
    super(lx);
    addParameter("radial", radialNum);
    addParameter("web", webNum);
    addParameter("pos", pos);
  }

  public void run(double deltaMs) {

    float whichRadial = radialNum.getValuef();
    if (whichRadial > ModelParams.getRadials()) {
      whichRadial = ModelParams.getRadials() - 1;
    }
    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    int numRadials = ModelParams.getRadials();
    for (int webN = 0; webN < SpiderTrapModel.NUM_WEBS; webN++) {
      if (webN == webNum.getValuei() || webNum.getValuei() == -1) {
        for (int radN = 0; radN < numRadials; radN++) {
          SpiderTrapModel.Radial radial = SpiderTrapModel.allWebs.get(webN).radials.get(radN);
          if ((int)whichRadial == radN || whichRadial == -1) {
            int ptrNum = 0;
            for (LXPoint ptr : radial.points) {
              if (pos.getValuei() == -1 || ptrNum <= pos.getValuei())
                colors[ptr.index] = LXColor.WHITE;
              ++ptrNum;
            }
          }
        }
      }
    }
  }
}
