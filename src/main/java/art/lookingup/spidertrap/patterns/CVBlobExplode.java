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
public class CVBlobExplode extends LXPattern {
  private static final Logger logger = Logger.getLogger(CVBlobTest.class.getName());
  CompoundParameter fall = new CompoundParameter("fall", 1.2, 1, 5);
  CompoundParameter parts = new CompoundParameter("parts", 100, 1, 200);
  CompoundParameter exps = new CompoundParameter("exps", 1, 1, 20);
  CompoundParameter alive = new CompoundParameter("alive", 100, 10, 5000);
  GLUtil.SpiderGLContext spGLCtx;

  static public boolean alreadyLoggedException = false;

  public CVBlobExplode(LX lx) {

    super(lx);
    addParameter("fall", fall);
    addParameter("parts", parts);
    addParameter("exps", exps);
    addParameter("alive", alive);

    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    scriptParams.put("x1", 0f);
    scriptParams.put("y1", 0f);
    scriptParams.put("fall", fall.getValuef());
    scriptParams.put("parts", parts.getValuef());
    scriptParams.put("exps", exps.getValuef());
    spGLCtx = GLUtil.spiderGLInit(null, "FireworksPos", scriptParams);
  }

  public void run(double deltaMs) {
    CVBlob.cleanExpired(alive.getValuef());

    for (LXPoint p : SpiderTrapModel.allPoints)
      colors[p.index] = LXColor.BLACK;

    for (CVBlob cvBlob : CVBlob.blobs) {
      spGLCtx.scriptParams.put("x1", cvBlob.u - 0.5f);
      spGLCtx.scriptParams.put("y1", cvBlob.v - 0.5f);
      spGLCtx.scriptParams.put("fall", fall.getValuef());
      spGLCtx.scriptParams.put("parts", parts.getValuef());
      spGLCtx.scriptParams.put("exps", exps.getValuef());
      try {
        GLUtil.glRun(spGLCtx, deltaMs, 1f);

        GLUtil.copyTFBufferToPoints(colors, spGLCtx, LXColor.Blend.ADD);
      } catch (com.jogamp.opengl.GLException glex) {
        if (!alreadyLoggedException) {
          logger.info("Caught GL Exception: " + glex.getMessage());
          alreadyLoggedException = true;
        }
      }
    }
  }
}
