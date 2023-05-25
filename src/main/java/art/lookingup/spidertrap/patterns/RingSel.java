package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.List;

public class RingSel extends LXPattern {

  DiscreteParameter ringNum = new DiscreteParameter("ring", -1, -1, 20).setDescription("ring");
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");

  public RingSel(LX lx) {
    super(lx);
    addParameter("ring", ringNum);
    addParameter("web", webNum);
  }

  public void run(double deltaMs) {

    float whichRing = ringNum.getValuef();
    // NOTE(tracy): This will need to be fixed for multiple webs with different numbers of rings.
    if (whichRing >= SpiderTrapModel.allWebs.get(0).rings.size()) {
      whichRing = SpiderTrapModel.allWebs.get(0).rings.size() - 1;
    }
    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    int numRings = SpiderTrapModel.allRings.size();
    for (int mNum = 0; mNum < SpiderTrapModel.NUM_WEBS; mNum++) {
      if (mNum == webNum.getValuei() || webNum.getValuei() == -1) {
        for (int rNum = 0; rNum < numRings; rNum++) {
          List<SpiderTrapModel.Segment> ring = SpiderTrapModel.allWebs.get(mNum).rings.get(rNum);
          if (rNum == whichRing || whichRing == -1) {
            for (SpiderTrapModel.Segment segment : ring) {
              for (LXPoint p : segment.points) {
                colors[p.index] = LXColor.WHITE;
              }
            }
          }
        }
      }
    }
  }
}
