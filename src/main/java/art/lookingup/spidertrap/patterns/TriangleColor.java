package art.lookingup.spidertrap.patterns;


import art.lookingup.spidertrap.Output;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.List;

@LXCategory(LXCategory.TEST)
public class TriangleColor extends LXPattern {

  DiscreteParameter outputNum = new DiscreteParameter("output", -1, -1, 32).setDescription("output");

  DiscreteParameter pos = new DiscreteParameter("pos", -1, -1, 354);

  public TriangleColor(LX lx) {
    super(lx);
    addParameter("output", outputNum);
    addParameter("pos", pos);
  }

  public void run(double deltaMs) {

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;


    for (int output = 0; output < 32; output++) {
      List<LXPoint> outputPoints = Output.allOutputsPoints.get(output);
      for (int pixel = 0; pixel < outputPoints.size(); pixel++) {
        if (output == outputNum.getValuei() || outputNum.getValuei() == -1) {
          if (pixel < pos.getValuei() || pos.getValuei() == -1) {
            int color = LXColor.RED;
            int triangleNum = output / 4;
            if (triangleNum == 1)
              color = LXColor.GREEN;
            if (triangleNum == 2)
              color = LXColor.BLUE;
            if (triangleNum == 3)
              color = LXColor.WHITE;
            if (triangleNum == 4)
              color = LXColor.rgb(255, 255, 0);
            if (triangleNum == 5)
              color = LXColor.rgb(0, 255, 255);
            colors[outputPoints.get(pixel).index] = color;
          }
        }
      }
    }
  }
}
