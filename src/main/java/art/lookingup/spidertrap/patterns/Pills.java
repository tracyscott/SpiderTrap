package art.lookingup.spidertrap.patterns;

import art.lookingup.colors.Colors;
import art.lookingup.linear.Edge;
import art.lookingup.linear.Joint;
import art.lookingup.linear.LPRender;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class Pills extends LXPattern {

  CompoundParameter width = new CompoundParameter("width", 0.05, 0, .2);

  public Pills(LX lx) {
    super(lx);
    addParameter("width", width);
  }

  public void run(double deltaMs) {

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    int numEdges = SpiderTrapModel.allEdges.size();
    int color = LXColor.rgb(255, 255, 255);
    for (int edgeNum = 0; edgeNum < numEdges; edgeNum++) {
      Edge edge = SpiderTrapModel.allEdges.get(edgeNum);
      LPRender.renderSquare(colors, edge.linearPoints, 0.25f, width.getValuef(), 1f, LXColor.Blend.ADD,
          color, -1, null, 0);
      LPRender.renderSquare(colors, edge.linearPoints, 0.5f, width.getValuef(), 1f, LXColor.Blend.ADD,
          color, -1, null, 0);
      LPRender.renderSquare(colors, edge.linearPoints, 0.75f, width.getValuef(), 1f, LXColor.Blend.ADD,
         color, -1, null, 0);
    }
  }
}
