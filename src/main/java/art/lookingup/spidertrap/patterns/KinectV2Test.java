package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapApp;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;

public class KinectV2Test extends LXPattern {

  public KinectV2Test(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    if (SpiderTrapApp.kinect != null) {
      SpiderTrapApp.kinect.update();
    }
  }
}
