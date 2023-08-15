package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.util.GLUtil;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.pattern.LXPattern;
import processing.core.PGraphics;
import processing.opengl.PGraphics2D;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import static processing.core.PApplet.abs;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

@LXCategory(LXCategory.FORM)
public class Fluid extends LXPattern {
  private static final Logger logger = java.util.logging.Logger.getLogger(Fluid.class.getName());

  private class MyFluidData implements DwFluid2D.FluidData {

    // update() is called during the fluid-simulation update step.
    @Override
    public void update(DwFluid2D fluid) {

      float px, py, radius, r, g, b, intensity, temperature;

      // LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
      py = 5;
      radius = 30;
      intensity = 1.0f;
      // add impulse: density + temperature
      float animator = abs(sin(fluid.simulation_step*0.01f));
      temperature = animator * 100f;

      float animatorG = abs(sin(fluid.simulation_step*0.02f));
      float animatorB = abs(sin(fluid.simulation_step*0.04f));
      // Rainbow Colors
      // add impulse: density + temperature
      px = 5;
      r = 228.0f / 255.0f;
      g = 3.0f / 255.0f;
      b = 3.0f / 255.0f;
      //r = animator/2f;
      //g = animatorG/2f;
      //b = animatorB/2f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 1.0f * pg.width / 5.0f;
      r = 255.0f / 255.0f;
      g = 140.0f / 255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 2.0f * pg.width / 5.0f;
      r = 255.0f / 255.0f;
      g = 237.0f / 255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 3.0f * pg.width / 5.0f;
      r = 0.0f;
      g = 128.0f / 255.0f;
      b = 38.0f / 255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 4 * pg.width / 5.0f;
      r = 0.0f;
      g = 77.0f / 255.0f;
      b = 1.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = pg.width - 5;
      r = 117.0f / 255.0f;
      g = 7.0f / 255.0f;
      b = 135.0f / 255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);
    }
  }

  private int fluidgrid_scale = 1;
  private DwFluid2D fluid;
  private PGraphics2D pg_fluid;
  private PGraphics2D pg_obstacles;
  private int     BACKGROUND_COLOR           = 0;
  private boolean UPDATE_FLUID               = true;
  private boolean DISPLAY_FLUID_TEXTURES     = true;
  private boolean DISPLAY_FLUID_VECTORS      = false;
  private int     DISPLAY_fluid_texture_mode = 0;

  protected PGraphics pg;
  GLUtil.SpiderGLContext spGLCtx;

  public Fluid(LX lx) {
    super(lx);
    pg = SpiderTrapApp.pApplet.createGraphics(512, 512, P3D);

    Sdf2D.initializeGLContext();

    com.jogamp.opengl.util.texture.Texture glTexture =
        AWTTextureIO.newTexture(Sdf2D.glDrawable.getGLProfile(), (BufferedImage) pg.getNative(), false);

    spGLCtx = GLUtil.spiderGLInit(glTexture, "render2d");


    DwPixelFlow context = new DwPixelFlow(SpiderTrapApp.pApplet);
    context.print();
    context.printGL();


    // fluid simulation
    logger.info(pg.width + "," + pg.height);
    fluid = new DwFluid2D(context, pg.width, pg.height, fluidgrid_scale);
    // set some simulation parameters
    fluid.param.dissipation_density     = 0.999f;
    fluid.param.dissipation_velocity    = 0.99f;
    fluid.param.dissipation_temperature = 0.80f;
    fluid.param.vorticity               = 0.10f;
    // interface for adding data to the fluid simulation
    MyFluidData cb_fluid_data = new MyFluidData();
    fluid.addCallback_FluiData(cb_fluid_data);
    // pgraphics for fluid
    pg_fluid = (PGraphics2D) SpiderTrapApp.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_fluid.smooth(0);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) SpiderTrapApp.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    // border-obstacle
    pg_obstacles.strokeWeight(1);
    pg_obstacles.stroke(100);
    pg_obstacles.noFill();
    pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);
    pg_obstacles.endDraw();
  }

  public void run(double deltaMs) {
    draw(deltaMs);
    GLUtil.glRun(spGLCtx, deltaMs, 1f);
    GLUtil.copyTFBufferToPoints(colors, spGLCtx);
  }

  public static boolean needsSave = true;
  public static int frameCounter = 0;

  public void draw(double deltaDrawMs) {
    ++frameCounter;
    pg.background(0);
    fluid.addObstacles(pg_obstacles);
    fluid.update();
    // clear render target
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    fluid.renderFluidTextures(pg_fluid, DISPLAY_fluid_texture_mode);
    pg_fluid.loadPixels();
    pg_fluid.updatePixels();
    pg.image(pg_fluid, 0, 0);
    pg.loadPixels();
    pg.updatePixels();
    if (needsSave && frameCounter > 300) {
      needsSave = false;
      logger.info("saving debug frame");
      //pg_fluid.save("fluid.png");
      logger.info("fluid wxh: " + pg_fluid.width + "," + pg_fluid.height);
    }
    // Update each frame.
    TextureData textureData = AWTTextureIO.newTextureData(spGLCtx.gl.getGLProfile(), (BufferedImage) pg_fluid.getNative(), false);
    spGLCtx.glTexture.updateImage(spGLCtx.gl, textureData, 0);
  }
}
