package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.util.FileItemBase;
import art.lookingup.util.GLUtil;
import art.lookingup.util.PathUtils;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UIItemList;
import heronarts.p4lx.ui.component.UIKnob;
import heronarts.p4lx.ui.component.UITextBox;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

public class Image extends LXPattern implements UIDeviceControls<Image> {
  private static final Logger logger = Logger.getLogger(Image.class.getName());

  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, 40.0)
          .setDescription("Controls the frames per second.");
  public final StringParameter imgKnob =
      new StringParameter("img", "")
          .setDescription("Texture image for rainbow.");
  public final BooleanParameter tileKnob = new BooleanParameter("tile", false);
  public CompoundParameter xOff = new CompoundParameter("x", 0.0, -500.0, 500.0)
      .setDescription("X offset");
  public CompoundParameter yOff = new CompoundParameter("y", 0.0, -100.0, 100.0)
      .setDescription("Y offset");
  public CompoundParameter scale = new CompoundParameter("scale", 1.0, 0.01, 10.0)
      .setDescription("Scale image");
  public BooleanParameter spriteMode = new BooleanParameter("sprite", false);
  public CompoundParameter speed = new CompoundParameter("speed", 0f, -20f, 20f)
      .setDescription("Speed for image pan to center");
  public BooleanParameter rbbg = new BooleanParameter("rbbg", false);
  public CompoundParameter rbBright = new CompoundParameter("rbbrt", 0.5, 0.0, 1.0);
  public BooleanParameter alpha = new BooleanParameter("alpha", true);

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 320;

  private static final List<String> IMG_EXTS = Arrays.asList(".gif", ".png", ".jpg");

  protected PImage image;
  protected PImage tileImage;
  protected PImage originalImage;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;  // Must end in a '/'
  protected int paddingX;
  protected int numTiles;
  protected PGraphics pg;
  GLUtil.SpiderGLContext spGLCtx;

  public Image(LX lx) {
    super(lx);
    this.imageWidth = 512;
    this.imageHeight = 512;
    this.filesDir = "images";
    if (!filesDir.endsWith("/")) {
      filesDir = filesDir + "/";
    }

    reloadFileList();
    pg = SpiderTrapApp.pApplet.createGraphics(imageWidth, imageHeight);

    addParameter("fps", fpsKnob);
    addParameter("img", imgKnob);
    imgKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        StringParameter iKnob = (StringParameter) parameter;
        loadImg(iKnob.getString());
      }
    });
    addParameter("xOff", xOff);
    addParameter("yOff", yOff);
    addParameter("scale", scale);

    imgKnob.setValue("sky.jpg");
    loadImg(imgKnob.getString());
    PGraphicsOpenGL pgOpenGL = (processing.opengl.PGraphicsOpenGL)(SpiderTrapApp.pApplet.getGraphics());
    PJOGL pJogl = (PJOGL)(pgOpenGL.pgl);
    GL jogl = pJogl.gl;
    com.jogamp.opengl.util.texture.Texture glTexture = AWTTextureIO.newTexture(jogl.getGLProfile(), (BufferedImage) image.getNative(), true);
    spGLCtx = GLUtil.spiderGLInit(jogl.getGL3(), glTexture, "texture");

    addParameter("tile", tileKnob);
    addParameter("sprite", spriteMode);
    addParameter("speed", speed);
    addParameter("rbbg", rbbg);
    addParameter("rbbrt", rbBright);
    addParameter("alpha", alpha);
  }

  public void loadImg(String imgname) {
    logger.info("Loading image: " + imgname);
    tileImage = SpiderTrapApp.pApplet.loadImage(filesDir + imgname);
    if (!tileKnob.getValueb()) {
      //tileImage.resize(imageWidth, imageHeight);
      image = tileImage;
    } else {
      // Tile the image to fill the space horizontally.  Scale the image vertically
      // to fit.
      float yScale = imageHeight / tileImage.height;
      //tileImage.resize((int)(tileImage.width * yScale), imageHeight);
      tileImage.loadPixels();
      logger.info("tileImage.width=" + tileImage.width + " tileImage.height=" + tileImage.height);
      numTiles = (int)Math.ceil((float)imageWidth  / (float)tileImage.width);
      int remainderPixelsX = imageWidth - (numTiles * tileImage.width);
      // No vertical padding right now int paddingY = imageHeight - image.height;
      paddingX = remainderPixelsX / (numTiles+1);
      logger.info("Tiling image: " + imgname + " numTiles=" + numTiles + " paddingX=" + paddingX);
      pg.beginDraw();

      // NOTE(tracy): This is commented out so that we don't fully black out our frame, which we don't
      // want to do if we want to layer multiple channels using partially transparent images.
      // pg.background(0);

      for (int i = 0; i < numTiles; i++) {
        pg.image(tileImage, i * tileImage.width + (i +1) * paddingX, 0);
      }

      pg.endDraw();
      pg.updatePixels();
      pg.loadPixels();
      image = pg;
    }
    // We need to save the original image for scaling.
    originalImage = image.copy();
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    /* Leaving FPS and frame logic for now incase we want to do some Ken Burns
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }
    */
    GLUtil.glRun(spGLCtx, deltaMs, speed.getValuef());
    GLUtil.copyTFBufferToPoints(colors, spGLCtx);
  }

  public void reloadFileList() {
    imgFiles = PathUtils.findDataFiles(filesDir, IMG_EXTS);
    fileItems.clear();
    for (String filename : imgFiles) {
      // Use a name that's suitable for the knob
      int index = filename.lastIndexOf('/');
      if (index >= 0) {
        filename = filename.substring(index + 1);
      }
      fileItems.add(new FileItem(filename));
    }
    if (fileItemList != null) {
      fileItemList.setItems(fileItems);
    }
  }

  //
  // Custom UI to allow for the selection of the image file.
  //
  @Override
  public void buildDeviceControls(LXStudio.UI ui, UIDevice device, Image pattern) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);

    int knobWidth = 35;

    UI2dContainer knobsContainer = new UI2dContainer(0, 0, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(fpsKnob).setWidth(knobWidth).addToContainer(knobsContainer);

    // We need to reload the image if the tile button is selected.  For tiled images, we build
    // an intermediate PGraphics object and tile the selected image into that and then use it
    // as our base PImage 'image'.
    new UIButton() {
      @Override
      public void onToggle(boolean on) {
        // Need to reload the image
        loadImg(imgKnob.getString());
      }
    }.setParameter(tileKnob).setLabel("tile").setTextOffset(0, 20)
        .setWidth(28).setHeight(25).addToContainer(knobsContainer);

    /*
    new UIButton() {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          reloadFileList();
        }
      }
    }.setLabel("rescan dir")
        .setMomentary(true)
        .setWidth(50)
        .setHeight(25)
        .addToContainer(knobsContainer);
        */
    new UIButton()
        .setParameter(spriteMode)
        .setLabel("sprite")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(rbbg)
        .setLabel("rbbg")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(alpha)
        .setLabel("alpha")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);

    new UIKnob(xOff).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(yOff).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(scale).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(speed).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(rbBright).setWidth(knobWidth).addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(imgKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(filenameEntry);


    // Button for reloading image file list.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          loadImg(imgKnob.getString());
        }
      }
    }.setLabel("\u21BA")
        .setMomentary(true)
        .addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(device);
  }

  public class FileItem extends FileItemBase {
    FileItem(String filename) {
      super(filename);
    }
    public void onActivate() {
      imgKnob.setValue(filename);
      loadImg(filename);
    }
  }
}
