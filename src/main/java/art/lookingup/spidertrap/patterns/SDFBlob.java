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
public class SDFBlob extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());

  public DiscreteParameter numBlobs = new DiscreteParameter("blobs", 2, 1, MAX_BLOBS + 1);
  public CompoundParameter bspeed = new CompoundParameter("bspeed", 1.0, 0.0, 60.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter jointKnob = new DiscreteParameter("joint", 0, -1, 3);

  CompoundParameter fall = new CompoundParameter("fall", 0.5, .05, 2);
  CompoundParameter parts = new CompoundParameter("parts", 100, 1, 200);
  CompoundParameter exps = new CompoundParameter("exps", 1, 1, 20);
  CompoundParameter alive = new CompoundParameter("alive", 100, 10, 1000);
  GLUtil.SpiderGLContext spGLCtx;

  public static final int MAX_BLOBS = 60;

  public SDFBlob(LX lx) {

    super(lx);
    addParameter("blobs", numBlobs);
    addParameter("bspeed", bspeed);
    addParameter("randspd", randSpeed);
    addParameter("joint", jointKnob);

    addParameter("fall", fall);
    addParameter("parts", parts);
    addParameter("exps", exps);
    addParameter("alive", alive);

    PGraphicsOpenGL pgOpenGL = (processing.opengl.PGraphicsOpenGL)(SpiderTrapApp.pApplet.getGraphics());
    PJOGL pJogl = (PJOGL)(pgOpenGL.pgl);
    GL jogl = pJogl.gl;
    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("fall", fall.getValuef());
    scriptParams.put("parts", parts.getValuef());
    scriptParams.put("exps", exps.getValuef());
    spGLCtx = GLUtil.spiderGLInit(jogl.getGL3(), null, "LightPos", scriptParams);
    resetBlobs();
  }

  public void run(double deltaMs) {
    CVBlob.cleanExpired(alive.getValuef());

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    for (int blobNum = 0; blobNum < numBlobs.getValuei(); blobNum++) {
      Blob blob = blobs[blobNum];
      //spGLCtx.scriptParams.put("x1", blob.u - 0.5f);
      //spGLCtx.scriptParams.put("y1", blob.v - 0.5f);
      spGLCtx.scriptParams.put("fall", fall.getValuef());
      spGLCtx.scriptParams.put("parts", parts.getValuef());
      spGLCtx.scriptParams.put("exps", exps.getValuef());
      blob.renderBlobShader(colors, bspeed.getValuef(), jointKnob.getValuei(), LXColor.Blend.ADD, deltaMs);
    }
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
