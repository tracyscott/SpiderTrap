package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class SegmentSel extends LXPattern {

  DiscreteParameter segNum = new DiscreteParameter("seg", -1, -1, 200).setDescription("segment");
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");

  public SegmentSel(LX lx) {
    super(lx);
    addParameter("seg", segNum);
    addParameter("web", webNum);
  }

  public void run(double deltaMs) {

    float whichSeg = segNum.getValuef();
    if (whichSeg >= SpiderTrapModel.allSegments.size()) {
      whichSeg = SpiderTrapModel.allSegments.size() - 1;
    }
    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    int numSegs = SpiderTrapModel.allSegments.size();
    for (int mNum = 0; mNum < SpiderTrapModel.NUM_WEBS; mNum++) {
      if (mNum == webNum.getValuei() || webNum.getValuei() == -1) {
        for (int gNum = 0; gNum < numSegs; gNum++) {
          SpiderTrapModel.Segment segment = SpiderTrapModel.allWebs.get(mNum).segments.get(gNum);
          if (gNum == whichSeg || whichSeg == -1) {
            for (LXPoint p : segment.points) {
              colors[p.index] = LXColor.WHITE;
            }
          }
        }
      }
    }
  }
}
