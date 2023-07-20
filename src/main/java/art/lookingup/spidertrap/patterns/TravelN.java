package art.lookingup.spidertrap.patterns;

import art.lookingup.colors.Colors;
import art.lookingup.linear.Blob;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.util.EaseUtil;
import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.logging.Logger;

public class TravelN extends FPSPattern {

  private static final Logger logger = Logger.getLogger(TravelN.class.getName());

  public static final int MAX_BLOBS = 400;
  //private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

  public BooleanParameter rndOff = new BooleanParameter("rndOff", false);
  public BooleanParameter usePal = new BooleanParameter("usePal", true);
  public BooleanParameter perBlobColor = new BooleanParameter("perBlC", false).setDescription("Use 1 pal color per blob");

  public CompoundParameter slope = new CompoundParameter("slope", 1.0, 0.001, 3.0);
  public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
  public CompoundParameter speed = new CompoundParameter("speed", 1.0, 0.0, 60.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 10, 1, MAX_BLOBS + 1);
  public DiscreteParameter nextBarKnob = new DiscreteParameter("nxtBar", 0, -1, 3);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 10.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);

  DiscreteParameter ease = new DiscreteParameter("ease", 0, EaseUtil.MAX_EASE+1);
  DiscreteParameter pal = new DiscreteParameter("pal", 0, 21);

  ColorParameter color = new ColorParameter("clr");

  public Blob[] blobs = new Blob[MAX_BLOBS];
  EaseUtil easeUtil = new EaseUtil(0);

  public TravelN(LX lx) {
    super(lx);
    addParameter("fps", fpsKnob);
    addParameter("rndOff", rndOff);
    addParameter("usePal", usePal);
    addParameter("perBlC", perBlobColor);
    addParameter("slope", slope);
    addParameter("maxv", maxValue);
    addParameter("speed", speed);
    addParameter("blobs", numBlobs);
    addParameter("randspd", randSpeed);
    addParameter("nxtBar", nextBarKnob);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);

    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("pal", pal);
    addParameter("ease", ease);

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
      //logger.info("Adding to lightBar: " + lbNum + " initialPos: " + initialPos);
      blobs[i].color = color.getColor();

      if (usePal.isOn()) {
        if (perBlobColor.isOn()) {
          blobs[i].color = Colors.getParameterizedPaletteColor(lx, pal.getValuei(), (float) Math.random(), easeUtil);
          blobs[i].pal = -1;
        }
        else {
          blobs[i].pal = pal.getValuei();
          blobs[i].easeUtil = easeUtil;
        }
      } else {
        blobs[i].pal = -1;
      }
    }
  }

  /**
   * Used to bind a palette index to a blob.  When we do that, we will effectively paint the blob gradient with the
   * palette.  Only applies when blob.pal != -1.
   * @return
   */
  int getPaletteIndex() {
    if (usePal.isOn() && !perBlobColor.isOn())
      return pal.getValuei();
    else
      return -1;
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
  public void renderFrame(double deltaMs) {


    for (LXPoint pt : lx.getModel().points) {
      colors[pt.index] = LXColor.rgba(0,0,0, 255);
    }

    float fadeLevel = maxValue.getValuef();

    for (int i = 0; i < numBlobs.getValuei(); i++) {
      blobs[i].easeUtil.easeNum = ease.getValuei();
      blobs[i].pal = getPaletteIndex();
      blobs[i].renderBlob(colors, speed.getValuef(), widthKnob.getValuef(), slope.getValuef(), fadeLevel,
          waveKnob.getValuei(), nextBarKnob.getValuei(), false, fxKnob.getValuei(), fxDepth.getValuef(),
          cosineFreq.getValuef());
    }
  }
}
