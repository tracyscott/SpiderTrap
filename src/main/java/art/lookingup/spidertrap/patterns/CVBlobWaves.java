package art.lookingup.spidertrap.patterns;

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
import heronarts.lx.pattern.LXPattern;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class CVBlobWaves extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());

  CompoundParameter speed = new CompoundParameter("speed", 3.24f, 0f, 10f);
  CompoundParameter freq = new CompoundParameter("freq", 34, 10, 30);
  CompoundParameter rscale = new CompoundParameter("rscale", 0.5, .01, 20);
  CompoundParameter palval = new CompoundParameter("palval", 0.1f, 0.1f, 9.5f);
  CompoundParameter pw = new CompoundParameter("pw", 1.68, 0f, 5f);
  CompoundParameter alive = new CompoundParameter("alive", 100, 10, 1000);
  GLUtil.SpiderGLContext spGLCtx;

  public CVBlobWaves(LX lx) {

    super(lx);
    addParameter("freq", freq);
    addParameter("rscale", rscale);
    addParameter("palval", palval);
    addParameter("alive", alive);
    addParameter("speed", speed);
    addParameter("pw", pw);

    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("freq", freq.getValuef());
    scriptParams.put("rscale", rscale.getValuef());
    scriptParams.put("palval", palval.getValuef());
    scriptParams.put("pw", pw.getValuef());
    spGLCtx = GLUtil.spiderGLInit(null, "Ripple2", scriptParams);
  }

  public void run(double deltaMs) {
    CVBlob.cleanExpired(alive.getValuef());

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    synchronized (CVBlob.blobs) {
      for (CVBlob cvBlob : CVBlob.blobs) {
        spGLCtx.scriptParams.put("x1", cvBlob.u - 0.5f);
        spGLCtx.scriptParams.put("y1", cvBlob.v - 0.5f);
        spGLCtx.scriptParams.put("freq", freq.getValuef());
        spGLCtx.scriptParams.put("rscale", rscale.getValuef());
        spGLCtx.scriptParams.put("palval", palval.getValuef());
        spGLCtx.scriptParams.put("pw", pw.getValuef());
        GLUtil.glRun(spGLCtx, deltaMs, speed.getValuef());
        GLUtil.copyTFBufferToPoints(colors, spGLCtx, LXColor.Blend.ADD);
      }
    }
  }
}
