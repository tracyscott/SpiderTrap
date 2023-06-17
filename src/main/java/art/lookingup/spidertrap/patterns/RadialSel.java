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

public class RadialSel extends LXPattern {

  CompoundParameter radialNum = new CompoundParameter("radial", -1, -1, SpiderTrapModel.MAX_RADIALS - 0.75).setDescription("radial");
  DiscreteParameter edgeSel = new DiscreteParameter("edge", -1, -1, 20);
  DiscreteParameter jointSel = new DiscreteParameter("joint", -1, 0, 3);
  DiscreteParameter webNum = new DiscreteParameter("web", -1, -1, SpiderTrapModel.NUM_WEBS).setDescription("web");

  public RadialSel(LX lx) {
    super(lx);
    addParameter("radial", radialNum);
    addParameter("edge", edgeSel);
    addParameter("joint", jointSel);
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
    for (int webN = 0; webN < SpiderTrapModel.NUM_WEBS; webN++) {
      if (webN == webNum.getValuei() || webNum.getValuei() == -1) {
        for (int radN = 0; radN < numRadials; radN++) {
          SpiderTrapModel.Radial radial = SpiderTrapModel.allWebs.get(webN).radials.get(radN);
          int numRadEdges = radial.edges.size();
          for (int edgeN = 0; edgeN < numRadEdges; edgeN++) {
            if (radN == (int) Math.round(whichRadial) || (int) Math.round(whichRadial) == -1) {
              if (edgeN == edgeSel.getValuei() || edgeSel.getValuei() == -1) {
                for (LXPoint p : radial.edges.get(edgeN).points) {
                  colors[p.index] = LXColor.WHITE;
                }
                int joint = jointSel.getValuei();
                if (joint != -1 && edgeSel.getValuei() != -1) {
                  Edge edge = SpiderTrapModel.allEdges.get(radial.edges.get(edgeN).id);
                  Joint startJoint = edge.myStartPointJoints[joint];
                  Joint endJoint = edge.myEndPointJoints[joint];
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
  }
}
