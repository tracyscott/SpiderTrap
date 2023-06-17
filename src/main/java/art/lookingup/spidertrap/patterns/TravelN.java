package art.lookingup.spidertrap.patterns;

import art.lookingup.linear.Blob;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.logging.Logger;

public class TravelN extends LXPattern {

  private static final Logger logger = Logger.getLogger(TravelN.class.getName());

  public static final int MAX_BLOBS = 400;
  //private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

  public BooleanParameter rndOff = new BooleanParameter("rndOff", false);
  public BooleanParameter usePal = new BooleanParameter("usePal", true);
  public BooleanParameter perBlobColor = new BooleanParameter("perBlC", false).setDescription("Use 1 pal color per blob");

  public CompoundParameter slope = new CompoundParameter("slope", 1.0, 0.001, 200.0);
  public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
  public CompoundParameter speed = new CompoundParameter("speed", 1.0, 0.0, 60.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 10, 1, MAX_BLOBS + 1);
  public DiscreteParameter nextBarKnob = new DiscreteParameter("nxtBar", 0, -1, 2);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 10.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);

  /*
  DiscreteParameter gradpal = new DiscreteParameter("gradpal", paletteLibrary.getNames());
  // selected colour palette
  CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
  CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
  CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
  CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
  CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
  */
  ColorParameter color = new ColorParameter("clr");

  //ZigzagPalette pal = new ZigzagPalette();

  public Blob[] blobs = new Blob[MAX_BLOBS];

  public TravelN(LX lx) {
    super(lx);
    addParameter("rndOff", rndOff);
    addParameter("usePal", usePal);
    addParameter("perBlC", perBlobColor);
    addParameter(slope);
    addParameter(maxValue);
    addParameter(speed);
    addParameter(numBlobs);
    addParameter(randSpeed);
    addParameter(nextBarKnob);
    addParameter(fxKnob);
    addParameter(fxDepth);
    addParameter(waveKnob);
    addParameter(widthKnob);
    addParameter(cosineFreq);

    /*
    addParameter(gradpal);
    addParameter(palStart);
    addParameter(palStop);
    addParameter(palBias);
    addParameter(palShift);
    addParameter(palCutoff);
     */
    addParameter("clr", color);

    resetBlobs();
  }

  public void resetBlobs() {

    for (int i = 0; i < MAX_BLOBS; i++) {
      blobs[i] = new Blob();
      float initialPos = 0f;
      float blobsPerGill = ((float)numBlobs.getValuei()) / 56f;
      float offsetEachBlob = (1f / blobsPerGill);
      int rowNum = i / 56;
      initialPos = initialPos + (rowNum * offsetEachBlob);
      if (rndOff.isOn())
        initialPos = initialPos - (float)Math.random();
      int lbNum = (i % SpiderTrapModel.allEdges.size());
      initialPos = 0;
      blobs[i].reset(lbNum, initialPos, randSpeed.getValuef(), true);
      logger.info("Adding to lightBar: " + lbNum + " initialPos: " + initialPos);
      blobs[i].color = color.getColor();
      /*
      if (usePal.isOn()) {
        if (perBlobColor.isOn())
          blobs[i].color = pal.getColor(Math.random());
        else
          blobs[i].pal = pal;
      }
       */
    }
  }

  /**
   * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
   * our speeds to generate some randomness in the speeds.
   */
  @Override
  public void onActive() {
    resetBlobs();
  }

  @Override
  public void run(double deltaMs) {
    /*
    pal.setPalette(paletteLibrary.get(gradpal.getOption()));
    pal.setBottom(palStart.getValue());
    pal.setTop(palStop.getValue());
    pal.setBias(palBias.getValue());
    pal.setShift(palShift.getValue());
    pal.setCutoff(palCutoff.getValue());
     */

    for (LXPoint pt : lx.getModel().points) {
      colors[pt.index] = LXColor.rgba(0,0,0, 255);
    }

    float fadeLevel = maxValue.getValuef();

    for (int i = 0; i < numBlobs.getValuei(); i++) {
      blobs[i].renderBlob(colors, speed.getValuef(), widthKnob.getValuef(), slope.getValuef(), fadeLevel,
          waveKnob.getValuei(), nextBarKnob.getValuei(), false, fxKnob.getValuei(), fxDepth.getValuef(),
          cosineFreq.getValuef());
    }
  }
}
