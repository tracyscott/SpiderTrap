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
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class FireBlob extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());

  public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 2, 1, MAX_BLOBS + 1);
  public CompoundParameter bspeed = new CompoundParameter("bspeed", 1.0, 0.0, 60.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter jointKnob = new DiscreteParameter("joint", 0, -1, 3);

  CompoundParameter sspeed = new CompoundParameter("sspeed", 1.0, 0.0, 20.0).setDescription("shader speed");
  CompoundParameter paletteKnob = new CompoundParameter("pal", 0.0, 0.0, 9.5).setDescription("palette select");
  CompoundParameter brt = new CompoundParameter("brt", 1.13, .05, 5);
  CompoundParameter pw = new CompoundParameter("pw", 1.3, 0.0, 5.0);
  CompoundParameter thick = new CompoundParameter("thick", 0.0, 0.0, 0.3);
  CompoundParameter s1 = new CompoundParameter("s1", 0.0, 0.0, 0.5);
  CompoundParameter s2 = new CompoundParameter("s2", 0.08, 0.0, 1.5);


  GLUtil.SpiderGLContext spGLCtx;

  public static final int MAX_BLOBS = 60;

  public FireBlob(LX lx) {

    super(lx);
    addParameter("blobs", numBlobs);
    addParameter("bspeed", bspeed);
    addParameter("randspd", randSpeed);
    addParameter("joint", jointKnob);

    addParameter("sspeed", sspeed);
    addParameter("pal", paletteKnob);
    addParameter("brt", brt);
    addParameter("pw", pw);
    addParameter("thick", thick);
    addParameter("s1", s1);
    addParameter("s2", s2);

    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("palval", paletteKnob.getValuef());
    scriptParams.put("brt", brt.getValuef());
    scriptParams.put("pw", pw.getValuef());
    scriptParams.put("thick", thick.getValuef());
    scriptParams.put("s1", s1.getValuef());
    scriptParams.put("s2", s2.getValuef());
    spGLCtx = GLUtil.spiderGLInit(null, "Squigglies", scriptParams);
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
      spGLCtx.scriptParams.put("palval", paletteKnob.getValuef());
      spGLCtx.scriptParams.put("brt", brt.getValuef());
      spGLCtx.scriptParams.put("pw", pw.getValuef());
      spGLCtx.scriptParams.put("thick", thick.getValuef());
      spGLCtx.scriptParams.put("s1", s1.getValuef());
      spGLCtx.scriptParams.put("s2", s2.getValuef());
      blob.shaderSpeed = sspeed.getValuef();
      blob.renderBlobShader(colors, bspeed.getValuef(), jointKnob.getValuei(), LXColor.Blend.ADD, deltaMs);
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
      int lbNum = (i % SpiderTrapModel.allEdges.size());
      initialPos = 0;
      blobs[i].reset(lbNum, initialPos, randSpeed.getValuef(), true);
      //logger.info("Adding to lightBar: " + lbNum + " initialPos: " + initialPos);
      //blobs[i].color = color.getColor();

      blobs[i].pal = -1;
      blobs[i].spGLCtx = spGLCtx;
    }
  }

}
