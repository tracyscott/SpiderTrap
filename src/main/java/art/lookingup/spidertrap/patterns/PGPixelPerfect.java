package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.util.GLUtil;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import heronarts.lx.LX;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import static processing.core.PConstants.*;

/**
 * Abstract base class for pixel perfect Processing drawings.  Use this
 * class for 1-1 pixel mapping with the rainbow.  The drawing will be
 * a rectangle but in physical space it will be distorted by the bend of
 * the rainbow. Gets FPS knob from PGBase.
 */
abstract class PGPixelPerfect extends PGBase {
  GLUtil.SpiderGLContext spGLCtx;
  PImage newImage;

  public PGPixelPerfect(LX lx, String drawMode) {
    super(lx, 512,
        512,
        "");

    Sdf2D.initializeGLContext();
    Sdf2D.glDrawable.getContext().makeCurrent();
    newImage = SpiderTrapApp.pApplet.createImage(pg.width, pg.height, pg.format);
    com.jogamp.opengl.util.texture.Texture glTexture = AWTTextureIO.newTexture(Sdf2D.glDrawable.getGLProfile(), (BufferedImage) newImage.getNative(), true);
    Sdf2D.glDrawable.getContext().release();
    LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
    //addParameter("zoom", zoomKnob);
    //addParameter("rotate", rotateKnob);
    scriptParams.put("zoom", zoomKnob.getValuef());
    scriptParams.put("rotate", rotateKnob.getValuef());

    spGLCtx = GLUtil.spiderGLInit(glTexture, "render2d", scriptParams);
  }

  protected void imageToPoints(double deltaMs) {
    pg.updatePixels();
    newImage.pixels = pg.pixels;
    newImage.updatePixels();
    spGLCtx.gl.getContext().makeCurrent();
    TextureData textureData = AWTTextureIO.newTextureData(spGLCtx.gl.getGLProfile(), (BufferedImage) newImage.getNative(), false);

    spGLCtx.glTexture.updateImage(spGLCtx.gl, textureData, 0);
    spGLCtx.gl.getContext().release();
    spGLCtx.scriptParams.put("zoom", zoomKnob.getValuef());
    spGLCtx.scriptParams.put("rotate", rotateKnob.getValuef());
    GLUtil.glRun(spGLCtx, deltaMs, 1f);
    GLUtil.copyTFBufferToPoints(colors, spGLCtx);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}
