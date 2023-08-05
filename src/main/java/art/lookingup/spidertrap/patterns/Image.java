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

import static processing.core.PConstants.P2D;

public class Image extends PGPixelPerfect implements UIDeviceControls<Image> {
  private static final Logger logger = Logger.getLogger(Image.class.getName());

  public final StringParameter imgKnob =
      new StringParameter("img", "")
          .setDescription("Texture image.");
  public CompoundParameter xOff = new CompoundParameter("x", 0.0, -500.0, 500.0)
      .setDescription("X offset");
  public CompoundParameter yOff = new CompoundParameter("y", 0.0, -100.0, 100.0)
      .setDescription("Y offset");
  public BooleanParameter alpha = new BooleanParameter("alpha", true);

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 220;

  private static final List<String> IMG_EXTS = Arrays.asList(".gif", ".png", ".jpg");

  protected PImage image;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;  // Must end in a '/'
  protected int paddingX;
  GLUtil.SpiderGLContext spGLCtx;

  public Image(LX lx) {
    super(lx, P2D);
    this.imageWidth = 512;
    this.imageHeight = 512;
    this.filesDir = "images";
    if (!filesDir.endsWith("/")) {
      filesDir = filesDir + "/";
    }

    reloadFileList();

    addParameter("img", imgKnob);
    imgKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        StringParameter iKnob = (StringParameter) parameter;
        loadImg(iKnob.getString());
      }
    });
    //addParameter("fps", fpsKnob);
    //addParameter("fps", fpsKnob);
    addParameter("xOff", xOff);
    addParameter("yOff", yOff);
    addParameter("zoom", zoomKnob);
    addParameter("rotate", rotateKnob);

    imgKnob.setValue("sky.jpg");
    loadImg(imgKnob.getString());
    PGraphicsOpenGL pgOpenGL = (processing.opengl.PGraphicsOpenGL)(SpiderTrapApp.pApplet.getGraphics());
    PJOGL pJogl = (PJOGL)(pgOpenGL.pgl);
    GL jogl = pJogl.gl;
    com.jogamp.opengl.util.texture.Texture glTexture = AWTTextureIO.newTexture(jogl.getGLProfile(), (BufferedImage) image.getNative(), true);
    spGLCtx = GLUtil.spiderGLInit(jogl.getGL3(), glTexture, "texture");

    addParameter("alpha", alpha);
  }

  public void loadImg(String imgname) {
    logger.info("Loading image: " + imgname);
    image = SpiderTrapApp.pApplet.loadImage(filesDir + imgname);
    image.resize(512, 512);
  }

  public void draw(double deltaMs) {
    if (image != null)
      pg.background(image);
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
    new UIButton()
        .setParameter(alpha)
        .setLabel("alpha")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);

    new UIKnob(xOff).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(yOff).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(zoomKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(rotateKnob).setWidth(knobWidth).addToContainer(knobsContainer);

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
