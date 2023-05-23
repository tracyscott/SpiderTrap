package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class MRScanner extends LXPattern {

  CompoundParameter radius = new CompoundParameter("radius", 0, 0, 4);
  CompoundParameter thick = new CompoundParameter("thick", 1, 0, 10);

  BooleanParameter inverse = new BooleanParameter("inverse", false);

  public MRScanner(LX lx) {
    super(lx);
    addParameter("radius", radius);
    addParameter("thick", thick);
    addParameter("inverse", inverse);
  }

  float distancePXZ(float xr, float zr, float xcenter, float zcenter) {
    if (inverse.isOn())
      return (float)Math.sqrt((xcenter - xr) * (xcenter - xr) * (zcenter - zr)* (zcenter - zr));
    else
      return (float)Math.sqrt((xcenter - xr) * (xcenter - xr) + (zcenter - zr)*(zcenter -zr));
  }

  public void run(double deltaMs) {

    for (int mId = 0; mId < SpiderTrapModel.allWebs.size(); mId++) {
      float centerX = SpiderTrapModel.allWebs.get(mId).webx;
      float centerZ = SpiderTrapModel.allWebs.get(mId).webz;

      for (LXPoint p : SpiderTrapModel.allWebs.get(mId).points) {
        float pRadius = distancePXZ(p.x, p.z, centerX, centerZ);
        if (pRadius > radius.getValuef() && pRadius < radius.getValuef() + thick.getValuef())
          colors[p.index] = LXColor.WHITE;
        else
          colors[p.index] = LXColor.BLACK;
      }
    }
  }
}
