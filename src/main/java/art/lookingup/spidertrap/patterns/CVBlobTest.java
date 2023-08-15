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
public class CVBlobTest extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());
  CompoundParameter freq = new CompoundParameter("freq", 0, 50, 200);
  CompoundParameter rscale = new CompoundParameter("rscale", 1, .1, 20);
  CompoundParameter alive = new CompoundParameter("alive", 100, 10, 1000);
  GLUtil.SpiderGLContext spGLCtx;

  public CVBlobTest(LX lx) {

    super(lx);
    addParameter("freq", freq);
    addParameter("rscale", rscale);
    addParameter("alive", alive);

    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("freq", freq.getValuef());
    scriptParams.put("rscale", rscale.getValuef());
    spGLCtx = GLUtil.spiderGLInit(null, "Ripple", scriptParams);
  }

  public void run(double deltaMs) {
    CVBlob.cleanExpired(alive.getValuef());

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    for (CVBlob cvBlob : CVBlob.blobs) {
      logger.info("rendering blob");
      spGLCtx.scriptParams.put("x1", cvBlob.u);
      spGLCtx.scriptParams.put("y1", cvBlob.v);
      spGLCtx.scriptParams.put("freq", freq.getValuef());
      spGLCtx.scriptParams.put("rscale", rscale.getValuef());
      GLUtil.glRun(spGLCtx, deltaMs, 1f);
      GLUtil.copyTFBufferToPoints(colors, spGLCtx, LXColor.Blend.ADD);
    }
  }
}
