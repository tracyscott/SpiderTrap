package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.FORM)
public class EQPattern extends LXPattern {
  CompoundParameter scale = new CompoundParameter("scale", 1, .1, 10);
  BooleanParameter inverse = new BooleanParameter("inverse", false);

  public int[] audioVals = new int [SpiderTrapModel.MAX_RADIALS];

  public EQPattern(LX lx) {
    super(lx);
    addParameter("scale", scale);
    addParameter("inverse", inverse);
  }

  float distancePXZ(float xr, float zr, float xcenter, float zcenter) {
    if (inverse.isOn())
      return (float)Math.sqrt((xcenter - xr) * (xcenter - xr) * (zcenter - zr)* (zcenter - zr));
    else
      return (float)Math.sqrt((xcenter - xr) * (xcenter - xr) + (zcenter - zr)*(zcenter -zr));
  }

  public void mapAudio() {
    GraphicMeter eq = lx.engine.audio.meter;
    byte[] fftAudioTex = new byte[1024];
    float[] audioSamples = eq.getSamples();
    int numRadials = ModelParams.getRadials();
    for (int i = 0; i < numRadials; i++) {
      int audioValue = (int) (8192 * audioSamples[(int)((float)i * (float)(512f/ numRadials))]);
      audioVals[i] = audioValue;
    }
  }

  public void run(double deltaMs) {
    float centerX = (lx.getModel().xMax + lx.getModel().xMin) /2f;
    float centerZ = (lx.getModel().zMax + lx.getModel().zMin) /2f;

    mapAudio();

    for (int radialNum = 0; radialNum < ModelParams.getRadials(); ++radialNum) {
      // Retrieve the 0 ... 256 audio value and scale to the number of leds in this radial.
      float numLedsInRadial = SpiderTrapModel.allRadials.get(radialNum).points.size();
      int audioValIndex = (ModelParams.getRadials()-1) - radialNum;
      int maxLed = (int) ((((float)audioVals[audioValIndex] * scale.getValuef()) / 256f) * numLedsInRadial);
      for (int ledNum = 0; ledNum < numLedsInRadial; ledNum++) {
        LXPoint p = SpiderTrapModel.allRadials.get(radialNum).points.get(ledNum);
        if (ledNum < maxLed) {
          colors[p.index] = LXColor.WHITE;
        } else {
          colors[p.index] = LXColor.BLACK;
        }
        p = SpiderTrapModel.allRadials.get(radialNum).points.get(ledNum);
        if (ledNum < maxLed) {
          colors[p.index] = LXColor.WHITE;
        } else {
          colors[p.index] = LXColor.BLACK;
        }
      }
    }
  }
}
