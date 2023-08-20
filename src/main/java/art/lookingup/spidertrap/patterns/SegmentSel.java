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

public class SegmentSel extends LXPattern {

  DiscreteParameter segNum = new DiscreteParameter("seg", -1, -1, 200).setDescription("segment");
  DiscreteParameter jointSel = new DiscreteParameter("joint", -1, 0, 3);
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");
  DiscreteParameter pos = new DiscreteParameter("pos", -1, -1, 190);

  public SegmentSel(LX lx) {
    super(lx);
    addParameter("seg", segNum);
    addParameter("joint", jointSel);
    addParameter("web", webNum);
    addParameter("pos", pos);
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
            int ptCounter = 0;
            for (LXPoint p : segment.points) {
              if (pos.getValuei() == -1 || ptCounter <= pos.getValuei())
                colors[p.index] = LXColor.WHITE;
              ++ptCounter;
            }
            int joint = jointSel.getValuei();
            if (joint != -1 && whichSeg != -1) {
              Edge edge = segment.edge;
              Joint startJoint = null;
              if (joint < edge.myStartPointJoints.size())
                startJoint = edge.myStartPointJoints.get(joint);
              Joint endJoint = null;
              if (joint < edge.myEndPointJoints.size())
                endJoint = edge.myEndPointJoints.get(joint);
              if (startJoint != null) {
                for (LXPoint p : startJoint.edge.points) {
                  colors[p.index] = LXColor.RED;
                }
              }
              if (endJoint != null) {
                for (LXPoint p : endJoint.edge.points) {
                  colors[p.index] = LXColor.GREEN;
                }
              }
            }
          }
        }
      }
    }
  }
}
