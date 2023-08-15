package art.lookingup.spidertrap.patterns;

import art.lookingup.linear.LPPoint;
import art.lookingup.spidertrap.SpiderTrapApp;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.util.EaseUtil;
import art.lookingup.colors.Colors;
import art.lookingup.util.GLUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.lx.utils.LXUtils;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UILabel;
import heronarts.p4lx.ui.component.UISlider;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.*;

/**
 * First attempt at using vertex shaders for volumetric rendering.
 */
@LXCategory(LXCategory.FORM)
public class Sdf2D extends LXPattern implements UIDeviceControls<Sdf2D> {
  private static final Logger logger = Logger.getLogger(Sdf2D.class.getName());
  public GL3 gl;

  StringParameter scriptName = new StringParameter("scriptName", "SquareDot");
  CompoundParameter speed = new CompoundParameter("speed", 1f, 0f, 20f);

  // These parameters are loaded from the ISF Json declaration at the top of the shader
  LinkedHashMap<String, CompoundParameter> scriptParams = new LinkedHashMap<String, CompoundParameter>();
  // For each script based parameter, store the uniform location in the compiled shader.  We use this
  // to pass in the values for each frame.
  Map<String, Integer> paramLocations = new HashMap<String, Integer>();
  public final MutableParameter onReload = new MutableParameter("Reload");
  public final StringParameter error = new StringParameter("Error", null);
  private UIButton openButton;

  public static GLOffscreenAutoDrawable glDrawable;


  public Sdf2D(LX lx) {
    super(lx);

    initializeGLContext();
    //glDrawable.getContext().makeCurrent();
    //logger.info("glDrawable.getGL()=" + glDrawable.getGL());
    //gl = glDrawable.getGL().getGL3();
    //glDrawable.getContext().release();
    addParameter("scriptName", scriptName);
    addParameter("speed", speed);
    glInit();
  }

  /**
   * NOTE(tracy): These need to be called only after the the UI is up and running otherwise we are causing GL Context
   * issues with threads.  We need top defer all initialization until the first run time.  This is going to be
   * annoying.
   */
  static public void initializeGLContext() {
    logger.info("Calling initializeGLContext");
    if (glDrawable == null) {
      GLProfile glp = GLProfile.get(GLProfile.GL3);
      GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL3));
      caps.setOnscreen(false);
      GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
      glDrawable = factory.createOffscreenAutoDrawable(null, caps,null,512,512);

      glDrawable.display();
      //glDrawable.getContext().makeCurrent();
      //glDrawable.getContext().release();
    }
  }

  private interface Buffer {
    int VERTEX = 0;
    int TBO = 1;
    int MAX = 2;
  }

  // Destination for transform feedback buffer when copied back from the GPU
  protected FloatBuffer tfbBuffer;
  // Staging buffer for vertex data to be copied to the VBO on the GPU
  protected FloatBuffer vertexBuffer;
  // Stores the buffer IDs for the buffer IDs allocated on the GPU.
  protected IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
  // The shader's ID on the GPU.
  protected int shaderProgramId = -1;

  protected int fTimeLoc = -2;

  protected double totalTime = 0.0;

  protected JsonObject isfObj;

  protected EaseUtil easeUtil = new EaseUtil(0);

  /**
   * Allocate the CPU buffers for the input and the output.  Vertex setting should be moved to later if the
   * LXPoints are going to move around.
   * Also, reserve the OpenGL buffer IDs.
   * Load the shader, bind the output that feeds into the transform feedback buffer, and link the shader.
   */
  public void glInit() {
    float[] ledPositions = new float[SpiderTrapModel.allPoints.size() * 3];
    for (int i = 0; i < SpiderTrapModel.allPoints.size(); i++) {
      // Use the normalized u, v, w coordinates.
      ledPositions[i * 3] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).u;
      ledPositions[i * 3 + 1] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).v;
      ledPositions[i * 3 + 2] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).w;
    }

    glDrawable.getContext().makeCurrent();
    gl = glDrawable.getGL().getGL3();
    vertexBuffer = GLBuffers.newDirectFloatBuffer(ledPositions);
    // This is just a destination, make it large enough to accept all the vertex data.  The vertex
    // shader always outputs the all the elements.  To return just some of the points, attach a
    // geometry shader and filter there.  You will also need to carry along the lxpoint index with
    // the vertex data in that scenario to match it up after the transform feedback.
    tfbBuffer = GLBuffers.newDirectFloatBuffer(vertexBuffer.capacity());

    gl.glGenBuffers(Buffer.MAX, bufferNames);
    glDrawable.getContext().release();

    reloadShader(scriptName.getString());
  }

  private List<String> newSliderKeys = new ArrayList<String>();
  private List<String> removeSliderKeys = new ArrayList<String>();

  public void reloadShader(String shaderName) {
    reloadShader(shaderName, true);
  }


  public void reloadShader(String shaderName, boolean clearSliders) {

    glDrawable.getContext().makeCurrent();

    if (shaderProgramId != -1)
      gl.glDeleteProgram(shaderProgramId);

    if (clearSliders) clearSliders();

    String paletteDefs = GLUtil.loadPalettes();

    ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), "shaders",
        null, shaderName, "vert", null, true);
    vertShader.insertShaderSource(0, "INSERT-PALETTES", 0, paletteDefs);
    CharSequence[][] source = vertShader.shaderSource();
    newSliderKeys.clear();
    removeSliderKeys.clear();
    for (int i = 0; i < source.length; i++) {
      for (int j = 0; j < source[i].length; j++) {
        int endOfComment = source[i][j].toString().indexOf("*/");
        int startOfComment = source[i][j].toString().indexOf("/*");
        String jsonDef = source[i][j].toString().substring(startOfComment + 2, endOfComment);
        //logger.info("JsonDef: " + jsonDef);
        isfObj = (JsonObject)new JsonParser().parse(jsonDef);
        JsonArray inputs = isfObj.getAsJsonArray("INPUTS");

        for (int k = 0; k < inputs.size(); k++) {
          JsonObject input = (JsonObject)inputs.get(k);
          String pName = input.get("NAME").getAsString();
          String pType = input.get("TYPE").getAsString(); // must be float for now
          float pDefault = input.get("DEFAULT").getAsFloat();
          float pMin = input.get("MIN").getAsFloat();
          float pMax =  input.get("MAX").getAsFloat();
          // Add the parameter
          if (clearSliders || (!clearSliders && !scriptParams.containsKey(pName))) {
            CompoundParameter cp = new CompoundParameter(pName, pDefault, pMin, pMax);
            scriptParams.put(pName, cp);
            addParameter(pName, cp);
          }
          newSliderKeys.add(pName);
          // How to remove ones we haven't seen?
        }
        if (!clearSliders) {
          for (String key : scriptParams.keySet()) {
            if (!newSliderKeys.contains(key)) {
              removeSliderKeys.add(key);
            }
          }
          for (String key : removeSliderKeys) {
            removeParameter(key);
            scriptParams.remove(key);
          }
        }
      }
    }
    ShaderProgram shaderProgram = new ShaderProgram();
    shaderProgram.add(vertShader);
    shaderProgram.init(gl);
    shaderProgramId = shaderProgram.program();

    gl.glTransformFeedbackVaryings(shaderProgramId, 1, new String[]{"tPosition"}, GL_INTERLEAVED_ATTRIBS);
    shaderProgram.link(gl, System.err);

    // Now, find uniform locations.
    fTimeLoc = gl.glGetUniformLocation(shaderProgramId, "fTime");
    logger.info("Found fTimeLoc at: " + fTimeLoc);
    for (String scriptParam : scriptParams.keySet()) {
      int paramLoc = gl.glGetUniformLocation(shaderProgramId, scriptParam);
      paramLocations.put(scriptParam, paramLoc);
      //logger.info("Found " + scriptParam + " at: " + paramLoc);
    }
    glDrawable.getContext().release();
    // Notify the UI
    onReload.bang();
  }

  @Override
  public void load(LX lx, JsonObject obj) {
    // Force-load the script name first so that slider parameter values can come after
    if (obj.has(LXComponent.KEY_PARAMETERS)) {
      JsonObject params = obj.getAsJsonObject(LXComponent.KEY_PARAMETERS);
      if (params.has("scriptName")) {
        this.scriptName.setValue(params.get("scriptName").getAsString());
      }
    }
    super.load(lx, obj);
  }

  /**
   * Run once per frame.  Copy the vertex data to the OpenGL buffer.
   * Tell OpenGL which buffer to use as the transform feedback buffer.
   * GL_RASTERIZER_DISCARD tells OpenGL to stop the pipeline after the vertex shader.
   *
   * @param deltaMs
   */
  public void glRun(double deltaMs) {
    totalTime += deltaMs/1000.0;
    glDrawable.getContext().makeCurrent();
    gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX));
    gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
    int inputAttrib = gl.glGetAttribLocation(shaderProgramId, "position");
    gl.glEnableVertexAttribArray(inputAttrib);
    gl.glVertexAttribPointer(inputAttrib, 3, GL_FLOAT, false, 0, 0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.TBO));
    gl.glBufferData(GL_ARRAY_BUFFER, tfbBuffer.capacity() * Float.BYTES, tfbBuffer, GL_STATIC_READ);
    gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferNames.get(Buffer.TBO));

    gl.glEnable(GL_RASTERIZER_DISCARD);
    gl.glUseProgram(shaderProgramId);

    gl.glUniform1f(fTimeLoc, speed.getValuef() * (float)totalTime);
    for (String paramName : scriptParams.keySet()) {
      gl.glUniform1f(paramLocations.get(paramName), scriptParams.get(paramName).getValuef());
    }
    gl.glBeginTransformFeedback(GL_POINTS);
    {
      gl.glDrawArrays(GL_POINTS, 0, SpiderTrapModel.allPoints.size());
    }
    gl.glEndTransformFeedback();
    gl.glFlush();

    gl.glGetBufferSubData(GL_TRANSFORM_FEEDBACK_BUFFER, 0, tfbBuffer.capacity() * Float.BYTES, tfbBuffer);

    gl.glUseProgram(0);
    gl.glDisable(GL_RASTERIZER_DISCARD);

    // For manually checking the first few output values in the transform feedback buffer.
    for (int i = 0; i < 21; i++) {
      //System.out.print(tfbBuffer.get(i) + ",");
    }
    //System.out.println();

    glDrawable.getContext().release();
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if (p == this.scriptName) {
      logger.info("scriptName parameter changed!");
      reloadShader(((StringParameter)p).getString());
    }
  }

  @Override
  public void onActive() {
    super.onActive();
    totalTime = 0f;
  }

  private void clearSliders() {
    for (String key : scriptParams.keySet()) {
      removeParameter(key);
    }
    scriptParams.clear();
  }

  public void run(double deltaMs) {
    glRun(deltaMs);
    float maxDistance = 0.1f;
    for (int i = 0; i < tfbBuffer.capacity(); i++) {
      if (tfbBuffer.get(i) > maxDistance) {
        maxDistance = tfbBuffer.get(i);
      }
    }
    for (int i = 0; i < SpiderTrapModel.allPoints.size(); i++) {
      if (tfbBuffer.get(i*3) < 0f) {
        colors[SpiderTrapModel.allPoints.get(i).index] = LXColor.rgbf(tfbBuffer.get(i*3), tfbBuffer.get(i*3 + 1), tfbBuffer.get(i*3+2));
      } else {
        int color = Colors.getParameterizedPaletteColor(lx, 0, tfbBuffer.get(i*3)/maxDistance, easeUtil);
        //colors[SpiderTrapModel.allPoints.get(i).index] = LXColor.BLACK;
        colors[SpiderTrapModel.allPoints.get(i).index] = LXColor.rgbf(tfbBuffer.get(i*3), tfbBuffer.get(i*3 + 1), tfbBuffer.get(i*3+2));
      }
    }
  }

  @Override
  public void buildDeviceControls(LXStudio.UI ui, UIDevice uiDevice, Sdf2D pattern) {
    final UILabel fileLabel = (UILabel)
        new UILabel(0, 0, 120, 18)
            .setLabel(pattern.scriptName.getString())
            .setBackgroundColor(LXStudio.UI.BLACK)
            .setBorderRounding(4)
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .setTextOffset(0, -1)
            .addToContainer(uiDevice);

    pattern.scriptName.addListener(p -> {
      fileLabel.setLabel(pattern.scriptName.getString());
    });

    this.openButton = (UIButton) new UIButton(122, 0, 18, 18) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          ui.applet.selectInput(
              "Select a file to open:",
              "onOpen",
              new File("target/classes/shaders", "shaders"),
              Sdf2D.this
          );
        }
      }
    }
        .setIcon(ui.theme.iconOpen)
        .setMomentary(true)
        .setDescription("Open Shader")
        .addToContainer(uiDevice);


    final UIButton resetButton = (UIButton) new UIButton(140, 0, 18, 18) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          lx.engine.addTask(() -> {
            logger.info("Reloading script");
            reloadShader(scriptName.getString(), false);
          });
        }
      }
    }.setIcon(ui.theme.iconLoad)
        .setMomentary(true)
        .setDescription("Reload shader")
        .addToContainer(uiDevice);


    final UI2dContainer sliders = (UI2dContainer)
        UI2dContainer.newHorizontalContainer(uiDevice.getContentHeight() - 20, 2)
            .setPosition(0, 20)
            .addToContainer(uiDevice);

    final UILabel error = (UILabel)
        new UILabel(0, 20, uiDevice.getContentWidth(), uiDevice.getContentHeight() - 20)
            .setBreakLines(true)
            .setTextAlignment(PConstants.LEFT, PConstants.TOP)
            .addToContainer(uiDevice)
            .setVisible(false);

    // Add sliders to container on every reload
    pattern.onReload.addListener(p -> {
      sliders.removeAllChildren();
      new UISlider(UISlider.Direction.VERTICAL, 40, sliders.getContentHeight() - 14, speed)
          .addToContainer(sliders);
      for (CompoundParameter slider : pattern.scriptParams.values()) {
        new UISlider(UISlider.Direction.VERTICAL, 40, sliders.getContentHeight() - 14, slider)
            .addToContainer(sliders);
      }
      float contentWidth = LXUtils.maxf(140, sliders.getContentWidth());
      uiDevice.setContentWidth(contentWidth);
      //resetButton.setX(contentWidth - resetButton.getWidth());
      //this.openButton.setX(resetButton.getX() - 2 - this.openButton.getWidth());
      error.setWidth(contentWidth);
      fileLabel.setWidth(this.openButton.getX() - 2);
    }, true);

    pattern.error.addListener(p -> {
      String str = pattern.error.getString();
      boolean hasError = (str != null && !str.isEmpty());
      error.setLabel(hasError ? str : "");
      error.setVisible(hasError);
      sliders.setVisible(!hasError);
    }, true);

  }

  public void onOpen(final File openFile) {
    this.openButton.setActive(false);
    if (openFile != null) {
      LX lx = getLX();
      String baseFilename = openFile.getName().substring(0, openFile.getName().indexOf('.'));
      logger.info("Loading: " + baseFilename);

      lx.engine.addTask(() -> {
        logger.info("Running script name setting task");
        lx.command.perform(new LXCommand.Parameter.SetString(
            scriptName,
            baseFilename
        ));
      });
    }
  }
}
