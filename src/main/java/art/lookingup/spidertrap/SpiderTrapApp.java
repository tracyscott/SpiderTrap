/**
 * Copyright 2020- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package art.lookingup.spidertrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.*;

import art.lookingup.KinectV2;
import art.lookingup.spidertrap.ui.ModelParams;
import art.lookingup.spidertrap.ui.UIPixliteConfig;
import art.lookingup.spidertrap.ui.UIPreviewComponents;
import art.lookingup.ui.*;
import art.lookingup.util.SpeedOverride;
import com.google.common.reflect.ClassPath;
import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.UI3dContext;
import heronarts.p4lx.ui.UIEventHandler;
import heronarts.p4lx.ui.component.UIGLPointCloud;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import KinectPV2.*;

/**
 * This is an example top-level class to build and run an LX Studio
 * application via an IDE. The main() method of this class can be
 * invoked with arguments to either run with a full Processing 4 UI
 * or as a headless command-line only engine.
 */
public class SpiderTrapApp extends PApplet implements LXPlugin {

  private static int pixelDensity = 1;

  private static final String WINDOW_TITLE = "SpiderTrap";

  private static int WIDTH = 1280;
  private static int HEIGHT = 800;
  private static boolean FULLSCREEN = false;
  private static final String LOG_FILENAME_PREFIX = "spidertrap";
  private static final int MAX_LOG_AGE_DAYS = 2;
  private static final int MAX_LOG_AGE_SECS = MAX_LOG_AGE_DAYS * 24 * 60 * 60;

  public static UIPixliteConfig pixliteConfig;
  public static ModelParams modelParams;

  public static OutputMapping outputMap;
  UIPreviewComponents previewComponents;
  public static PreviewComponents.BodyRender preview;
  public static PApplet pApplet;

  static public float[] panelPosParams;

  public static final int GLOBAL_FRAME_RATE = 30;

  // For receiving OSC messages for interactive Leaf mapping.
  public static SpiderTrapOSC spiderTrapOSC;

  // Speed override component.
  public static SpeedOverride speedOverride;

  public static LX lx;

  public static LXStudio lxstudio;

  public static boolean fullscreenMode = false;
  public static UI3dContext fullscreenContext;
  public static KinectV2 kinect;

  static {
    System.setProperty(
        "java.util.logging.SimpleFormatter.format",
        "%3$s: %1$tc [%4$s] %5$s%6$s%n");
  }

  /**
   * Set the main logging level here.
   *
   * @param level the new logging level
   */
  public static void setLogLevel(Level level) {
    // Change the logging level here
    Logger root = Logger.getLogger("");
    root.setLevel(level);
    for (Handler h : root.getHandlers()) {
      h.setLevel(level);
    }
  }


  /**
   * Adds logging to a file. The file name will be appended with a dash, date stamp, and
   * the extension ".log".
   *
   * @param prefix prefix of the log file name
   * @throws IOException if there was an error opening the file.
   */
  public static void addLogFileHandler(String prefix) throws IOException {
    deleteOldLogs(prefix, MAX_LOG_AGE_SECS);
    Logger root = Logger.getLogger("");
    String suffix = "" + System.currentTimeMillis() / 1000;
    // Human readable version, but harder to clean up old logs:
    // suffix = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    Handler h = new FileHandler(prefix + "-" + suffix + ".log");
    h.setFormatter(new SimpleFormatter());
    root.addHandler(h);
  }

  public static void deleteOldLogs(String prefix, int seconds) throws IOException {
    File logDir = new File(".");
    for (final File fileEntry: logDir.listFiles()) {
      if (fileEntry.getName().endsWith(".log")) {
        try {
          if (fileEntry.getName().startsWith("ntree")) {
            String[] parts1 = fileEntry.getName().split("-");
            String[] parts2 = parts1[1].split("\\.");
            int time = Integer.parseInt(parts2[0]);
            if (time < System.currentTimeMillis()/1000 - seconds) {
              logger.info("Cleaning old log file: " + fileEntry.getName());
              fileEntry.delete();
              // Clean up .lck file also
              File lckFile = new File(fileEntry.getAbsolutePath() + ".lck");
              lckFile.delete();
            } else {
              logger.info("Keeping log file: " + fileEntry.getName());
            }
          }
        } catch (Exception ex) {
          logger.info("Bad log file name, skipping cleanup: " + fileEntry.getName());
        }
      }
    }
  }

  private static final Logger logger = Logger.getLogger(SpiderTrapApp.class.getName());

  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
    pixelDensity(pixelDensity);
  }

  @Override
  public void setup() {
    frameRate(GLOBAL_FRAME_RATE);
    LXStudio.Flags flags = new LXStudio.Flags(this);
    flags.resizable = false;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = false;

    try {
      addLogFileHandler("logs/" + LOG_FILENAME_PREFIX);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error creating log file: " + LOG_FILENAME_PREFIX, ex);
    }

    logger.info("Current renderer:" + sketchRenderer());
    logger.info("Current graphics:" + getGraphics());
    logger.info("Current graphics is GL:" + getGraphics().isGL());
    PGraphicsOpenGL pgOpenGL = (processing.opengl.PGraphicsOpenGL)getGraphics();
    PJOGL pJogl = (PJOGL)(pgOpenGL.pgl);
    logger.info("JOGL Reference: " + pJogl.gl);

    logger.info("Initializing KinectV2");
    try {
      // kinect = new KinectV2(new KinectPV2(this));
      logger.info("Done initializing KinectV2");
    } catch (Exception ex) {
      logger.info("WARNING: Couldn't initialize KinectV2!");
    }


    pApplet = this;

    loadModelParams();
    LXModel model;
    logger.info("Creating model");
    model = SpiderTrapModel.createModel();

    logger.info("Starting LXStudio UI");
    lxstudio = new LXStudio(this, flags, model);
    this.surface.setTitle(WINDOW_TITLE);
  }

  public void loadModelParams() {
    ModelParams.loadModelConfig();
  }

  @Override
  public void initialize(LX lx) {
    // Here is where you should register any custom components or make modifications
    // to the LX engine or hierarchy. This is also used in headless mode, so note that
    // you cannot assume you are working with an LXStudio class or that any UI will be
    // available.
    registerAll(lx);
    this.lx = lx;
  }

  public void initializeUI(LXStudio lx, LXStudio.UI ui) {
    // Here is where you may modify the initial settings of the UI before it is fully
    // built. Note that this will not be called in headless mode. Anything required
    // for headless mode should go in the raw initialize method above.
    //ui.registry.addUIDeviceControls(art.lookingup.mushroom.patterns.UIVolumetric.class);
    speedOverride = new SpeedOverride(lx, ui);
    lx.engine.registerComponent("speedoverride", speedOverride);
    // While we need the component in order to be a modulation source, we want to reset the
    // value to the default value of 0.5 each time we reload a project.
    lx.addProjectListener(new LX.ProjectListener() {
      public void projectChanged(File file, LX.ProjectListener.Change change) {
        if (change == LX.ProjectListener.Change.OPEN) {
          speedOverride.speed.setValue(0.5f);
        }
      }
    });
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    // At this point, the LX Studio application UI has been built. You may now add
    // additional views and components to the Ui heirarchy.
    Body.initBodies();
    preview = new PreviewComponents.BodyRender();
    ui.preview.addComponent(preview);
    pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    modelParams = (ModelParams) new ModelParams(ui, lx, ModelParams.modelParamFile).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    outputMap = (OutputMapping) new OutputMapping(ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    previewComponents = (UIPreviewComponents) new UIPreviewComponents(ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    logger.info("Configuring pixlite output");
    Output.configurePixliteOutput(lx);

    logger.info("Model bounds: " + lx.getModel().xMin + "," + lx.getModel().yMin + " to " + lx.getModel().xMax + "," + lx.getModel().yMax);

    lx.ui.leftPane.audio.setExpanded(false);
    lx.ui.leftPane.snapshots.setExpanded(false);

    // Start up the NTreeOSC OSC listener for accepting mapping updates from a tablet.
    spiderTrapOSC = new SpiderTrapOSC(lx);

    // Set up some stuff for fullscreen mode.
    // Support Fullscreen Mode.  We create a second UIGLPointCloud and
    // add it to a LXStudio.UI layer.  When entering fullscreen mode,
    // toggleFullscreen() will set the
    // standard UI components visibility to false and the larger
    // fullscreenContext visibility to true.
    UIGLPointCloud fullScreenPointCloud = new UIGLPointCloud(lx);
    fullscreenContext = new UI3dContext(lx.ui, 0, 0, WIDTH, HEIGHT);
    fullscreenContext.addComponent(fullScreenPointCloud);
    lx.ui.addLayer(fullscreenContext);
    fullscreenContext.setVisible(false);
    fullscreenContext.setBackgroundColor(0);

    lx.ui.setTopLevelKeyEventHandler(new TopLevelKeyEventHandler());
  }


  public class TopLevelKeyEventHandler extends UIEventHandler {
    int originalStripWidth = 72;
    int collapsedStripWidth = 20;

    TopLevelKeyEventHandler() {
      super();
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      super.onKeyPressed(keyEvent, keyChar, keyCode);
      if (keyChar == 'f') {
        toggleFullscreen();
      }
    }
  }

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that Processing
    // will run a draw-loop.
  }

  /**
   * Main interface into the program. Two modes are supported, if the --headless
   * flag is supplied then a raw CLI version of LX is used. If not, then we embed
   * in a Processing 3 applet and run as such.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LX.log("Initializing LX version " + LXStudio.VERSION);
    boolean headless = false;
    File projectFile = null;
    //FULLSCREEN = true;
    for (int i = 0; i < args.length; ++i) {
      if ("--help".equals(args[i]) || "-h".equals(args[i])) {
      } else if ("--headless".equals(args[i])) {
        headless = true;
      } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
        FULLSCREEN = true;
      } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
        try {
          WIDTH = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Width command-line argument must be followed by integer");
        }
      } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
        try {
          HEIGHT = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Height command-line argument must be followed by integer");
        }
      } else if (args[i].endsWith(".lxp")) {
        try {
          projectFile = new File(args[i]);
        } catch (Exception x) {
          LX.error(x, "Command-line project file path invalid: " + args[i]);
        }
      }
    }
    if (headless) {
      // We're not actually going to run this as a PApplet, but we need to explicitly
      // construct and set the initialize callback so that any custom components
      // will be run
      LX.Flags flags = new LX.Flags();
      flags.initialize = new SpiderTrapApp();
      if (projectFile == null) {
        LX.log("WARNING: No project filename was specified for headless mode!");
      }
      LX.headless(flags, projectFile);
    } else {
      String[] newArgs = new String[2];
      String[] sketchArgs = {"--density=" + 2, "art.lookingup.spidertrap.SpiderTrapApp"};
      File hdpiFlag = new File("hdpi");
      if (hdpiFlag.exists())
        pixelDensity = 2;
      PApplet.main(concat(sketchArgs, args));
      //PApplet.runSketch(sketchArgs, null);
    }
  }


  /**
   * Registers all patterns and effects that LX doesn't already have registered.
   * This check is important because LX just adds to a list.
   *
   * @param lx the LX environment
   */
  private void registerAll(LX lx) {
    List<Class<? extends LXPattern>> patterns = lx.registry.patterns;
    List<Class<? extends LXEffect>> effects = lx.registry.effects;
    final String parentPackage = getClass().getPackage().getName();

    try {
      ClassPath classPath = ClassPath.from(getClass().getClassLoader());
      for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
        // Limit to this package and sub-packages
        if (!classInfo.getPackageName().startsWith(parentPackage)) {
          continue;
        }
        Class<?> c = classInfo.load();
        if (Modifier.isAbstract(c.getModifiers())) {
          continue;
        }
        if (LXPattern.class.isAssignableFrom(c)) {
          Class<? extends LXPattern> p = c.asSubclass(LXPattern.class);
          if (!patterns.contains(p)) {
            lx.registry.addPattern(p);
            logger.info("Added pattern: " + p);
          }
        } else if (LXEffect.class.isAssignableFrom(c)) {
          Class<? extends LXEffect> e = c.asSubclass(LXEffect.class);
          if (!effects.contains(e)) {
            lx.registry.addEffect(e);
            logger.info("Added effect: " + e);
          }
        }
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Error finding pattern and effect classes", ex);
    }

  }

 static private void toggleFullscreen() {
    if (!fullscreenMode) {
      lxstudio.ui.preview.setBackgroundColor(0);
      /*
      lxstudio.ui.leftPane.setVisible(false);
      lxstudio.ui.leftPane.setSize(1,1);
      lxstudio.ui.rightPane.setVisible(false);
      lxstudio.ui.rightPane.setSize(1,1);
      lxstudio.ui.helpBar.setVisible(false);
      lxstudio.ui.helpBar.setSize(1, 1);
      lxstudio.ui.bottomTray.setVisible(false);
      lxstudio.ui.bottomTray.setSize(1, 1);
      lxstudio.ui.preview.setVisible(false);
      lxstudio.ui.preview.setSize(1, 1);
      lxstudio.ui.preview.setBackgroundColor(0);

      fullscreenContext.setVisible(true);
      fullscreenMode = true;

       */
    } else {
      /*
      fullscreenContext.setVisible(false);

      lxstudio.ui.leftPane.setVisible(true);
      lxstudio.ui.rightPane.setVisible(true);
      lxstudio.ui.helpBar.setVisible(true);
      lxstudio.ui.bottomTray.setVisible(true);
      lxstudio.ui.preview.setVisible(true);
      fullscreenMode = false;

       */
    }
  }
}
