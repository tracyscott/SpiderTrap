package art.lookingup.spidertrap.patterns;


import art.lookingup.util.Gamma;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class GammaEffect extends LXEffect {

  CompoundParameter red = new CompoundParameter("red", 1.8, 1, 3);
  CompoundParameter green = new CompoundParameter("green", 1.8, 1, 3);
  CompoundParameter blue = new CompoundParameter("blue", 1.8, 1, 3);

  public GammaEffect(LX lx) {
    super(lx);
    addParameter("red", red);
    addParameter("green", green);
    addParameter("blue", blue);
    red.addListener(new LXParameterListener() {

      @Override
      public void onParameterChanged(LXParameter lxParameter) {
        Gamma.buildRedGammaLUT(red.getValuef());
      }
    });
    green.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter lxParameter) {
        Gamma.buildGreenGammaLUT(green.getValuef());
      }
    });
    blue.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter lxParameter) {
        Gamma.buildBlueGammaLUT(blue.getValuef());
      }
    });
  }

  public void run(double deltaMs, double damping) {
    final byte[] gammaRed = Gamma.GAMMA_LUT_RED[Math.round(255 * lx.engine.output.brightness.getValuef())];
    final byte[] gammaGreen = Gamma.GAMMA_LUT_GREEN[Math.round(255 * lx.engine.output.brightness.getValuef())];
    final byte[] gammaBlue = Gamma.GAMMA_LUT_BLUE[Math.round(255 * lx.engine.output.brightness.getValuef())];

    for (LXPoint p : SpiderTrapModel.allPoints) {
      int c = colors[p.index];
      int red = gammaRed[0xff & (c >> 16)];
      int green = gammaGreen[0xff & (c >> 8)];
      int blue = gammaBlue[0xff & (c)];
      colors[p.index] = LXColor.rgb(red, green, blue);
    }
  }
}
