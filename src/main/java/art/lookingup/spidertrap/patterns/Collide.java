package art.lookingup.spidertrap.patterns;

import art.lookingup.linear.Blob;
import art.lookingup.spidertrap.CVBlob;
import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.util.GLUtil;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class Collide extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());

  public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 2, 1, MAX_BLOBS + 1);
  public CompoundParameter bspeed = new CompoundParameter("bspeed", 1.0, 0.0, 60.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter jointKnob = new DiscreteParameter("joint", 0, -1, 3);
  public CompoundParameter collDist = new CompoundParameter("colld", 6f, 1f, 12f).setDescription("Collision distance inches");

  CompoundParameter sspeed = new CompoundParameter("sspeed", 1.0, 0.0, 20.0).setDescription("shader speed");
  CompoundParameter brt = new CompoundParameter("brt", 0.5, .05, 2);
  CompoundParameter pw = new CompoundParameter("pw", 1.0, 0.1, 20.0);
  CompoundParameter winT = new CompoundParameter("winT", 3.0, 0.0, 10.0);

  ColorParameter color1 = new ColorParameter("clr1");
  ColorParameter color2 = new ColorParameter("clr2");

  public boolean winnerExists = false;
  int whichWinner = 0;
  public double winTimeRemaining = 0;

  GLUtil.SpiderGLContext spGLCtx;

  public static final int MAX_BLOBS = 10;

  public Collide(LX lx) {

    super(lx);
    addParameter("blobs", numBlobs);
    addParameter("bspeed", bspeed);
    addParameter("randspd", randSpeed);
    addParameter("joint", jointKnob);
    addParameter("colld", collDist);

    addParameter("sspeed", sspeed);
    addParameter("brt", brt);
    addParameter("pw", pw);
    addParameter("clr1", color1);
    addParameter("clr2", color2);

    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("sspeed", sspeed.getValuef());
    scriptParams.put("brt", brt.getValuef());
    scriptParams.put("pw", pw.getValuef());
    scriptParams.put("r", 1f);
    scriptParams.put("g", 1f);
    scriptParams.put("b", 1f);
    spGLCtx = GLUtil.spiderGLInit(null, "LightPosRGB", scriptParams);
    resetBlobs();
  }

  @Override
  public void onActive() {
    super.onActive();
    resetBlobs();
    if (spGLCtx != null)
      spGLCtx.totalTime = 0f;
  }

  public void run(double deltaMs) {
    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    for (int blobNum = 0; blobNum < numBlobs.getValuei(); blobNum++) {
      Blob blob = blobs[blobNum];
      //spGLCtx.scriptParams.put("x1", blob.u - 0.5f);
      //spGLCtx.scriptParams.put("y1", blob.v - 0.5f);
      spGLCtx.scriptParams.put("sspeed", sspeed.getValuef());
      spGLCtx.scriptParams.put("brt", brt.getValuef());
      spGLCtx.scriptParams.put("pw", pw.getValuef());
      if ((blobNum % 2 == 0 && !winnerExists) || whichWinner == 1) {
        spGLCtx.scriptParams.put("r", (float)(0xff & (int)LXColor.red(color1.getColor()))/255f);
        spGLCtx.scriptParams.put("g", (float)(0xff & (int)LXColor.green(color1.getColor()))/255f);
        spGLCtx.scriptParams.put("b", (float)(0xff & (int)LXColor.blue(color1.getColor()))/255f);
      } else if ((blobNum % 2 == 1 && !winnerExists) || whichWinner == 2){
        spGLCtx.scriptParams.put("r", (float)(0xff & (int)LXColor.red(color2.getColor()))/255f);
        spGLCtx.scriptParams.put("g", (float)(0xff & (int)LXColor.green(color2.getColor()))/255f);
        spGLCtx.scriptParams.put("b", (float)(0xff & (int)LXColor.blue(color2.getColor()))/255f);
      }

      blob.shaderSpeed = sspeed.getValuef();
      blob.renderBlobShader(colors, bspeed.getValuef(), jointKnob.getValuei(), LXColor.Blend.ADD, deltaMs);
    }

    // Run collision detection.
    for (int blobNum = 0; blobNum < numBlobs.getValuei() && !winnerExists; blobNum++) {
      Blob blob = blobs[blobNum];
      for (int otherBlobNum = 0; otherBlobNum < numBlobs.getValuei() && !winnerExists; otherBlobNum++) {
        Blob otherBlob = blobs[otherBlobNum];
        if (blob == otherBlob)
          continue;
        if (blobNum % 2 == otherBlobNum % 2)
          continue;
        if (blob.worldSpace.distanceTo(otherBlob.worldSpace) < collDist.getValuef()/12f) {
          winnerExists = true;
          winTimeRemaining = winT.getValuef() * 1000;
          if (Math.random() < 0.50) {
            whichWinner = 1;
          } else {
            whichWinner = 2;
          }
        }
      }
    }

    if (winnerExists) {
      if (winTimeRemaining <= 0) {
        winnerExists = false;
        whichWinner = 0;
        resetBlobs();
      } else {
        winTimeRemaining -= deltaMs;
      }
    }

    GLUtil.glUpdateTotalTime(spGLCtx, deltaMs);
  }

  public Blob[] blobs = new Blob[MAX_BLOBS];

  public void resetBlobs() {

    for (int i = 0; i < MAX_BLOBS; i++) {
      blobs[i] = new Blob();
      float initialPos = 0f;
      float blobsPerGill = ((float)numBlobs.getValuei()) / 56f;
      float offsetEachBlob = (1f / blobsPerGill);
      int rowNum = i / 56;
      initialPos = initialPos + (rowNum * offsetEachBlob);
      //if (rndOff.isOn())
      //    initialPos = initialPos - (float)Math.random();

      int lbNum = ThreadLocalRandom.current().nextInt(SpiderTrapModel.allEdges.size());
      //int lbNum = (int)(Math.random()*(SpiderTrapModel.allEdges.size()-1));
      initialPos = 0;
      blobs[i].reset(lbNum, initialPos, randSpeed.getValuef(), true);
      //logger.info("Adding to lightBar: " + lbNum + " initialPos: " + initialPos);
      //blobs[i].color = color.getColor();

      blobs[i].pal = -1;
      blobs[i].spGLCtx = spGLCtx;
    }
  }

}
