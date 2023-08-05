package art.lookingup.util;

import art.lookingup.colors.Colors;
import art.lookingup.linear.LPPoint;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.spidertrap.patterns.Sdf2D;
import art.lookingup.spidertrap.patterns.Texture;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.TextureIO;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_RASTERIZER_DISCARD;

public class GLUtil {
  private static final Logger logger = Logger.getLogger(GLUtil.class.getName());

  static public class SpiderGLContext {

    public SpiderGLContext(GL3 gl) {
      this.gl = gl;
    }

    public GL3 gl;
    private interface Buffer {
      int VERTEX = 0;
      int TBO = 1;
      int MAX = 2;
    }

    // Destination for transform feedback buffer when copied back from the GPU
    public FloatBuffer tfbBuffer;
    // Staging buffer for vertex data to be copied to the VBO on the GPU
    public FloatBuffer vertexBuffer;
    // Stores the buffer IDs for the buffer IDs allocated on the GPU.
    static public IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    // The shader's ID on the GPU.

    public int shaderProgramId = -1;
    public int fTimeLoc = -2;
    public int textureLoc = -3;
    public com.jogamp.opengl.util.texture.Texture glTexture;
    public double totalTime;
    public Map<String, Integer> paramLocations = new HashMap<String, Integer>();
    public LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();
  }

  static public SpiderGLContext spiderGLInit(GL3 gl, com.jogamp.opengl.util.texture.Texture glTexture, String scriptName) {
    return spiderGLInit(gl, glTexture, scriptName, null);
  }

  // TODO(tracy): Load the TextureIO previous to this function.
  static public SpiderGLContext spiderGLInit(GL3 gl, com.jogamp.opengl.util.texture.Texture glTexture, String scriptName,
                                             LinkedHashMap<String, Float> scriptParams) {
    SpiderGLContext spGLCtx = new SpiderGLContext(gl);

    if (scriptParams != null)
      spGLCtx.scriptParams = scriptParams;

    float[] ledPositions = new float[SpiderTrapModel.allPoints.size() * 3];
    for (int i = 0; i < SpiderTrapModel.allPoints.size(); i++) {
      // Use the normalized u, v, w coordinates.
      ledPositions[i * 3] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).u;
      ledPositions[i * 3 + 1] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).v;
      ledPositions[i * 3 + 2] = ((LPPoint)SpiderTrapModel.allPoints.get(i)).w;
    }

    spGLCtx.vertexBuffer = GLBuffers.newDirectFloatBuffer(ledPositions);
    // This is just a destination, make it large enough to accept all the vertex data.  The vertex
    // shader always outputs the all the elements.  To return just some of the points, attach a
    // geometry shader and filter there.  You will also need to carry along the lxpoint index with
    // the vertex data in that scenario to match it up after the transform feedback.
    spGLCtx.tfbBuffer = GLBuffers.newDirectFloatBuffer(spGLCtx.vertexBuffer.capacity());

    gl.glGenBuffers(SpiderGLContext.Buffer.MAX, SpiderGLContext.bufferNames);



    //try {
    // Load the image from a file
    //File imageFile = new File("/Users/tracyscott/blackberries.jpg");
    //glTexture = TextureIO.newTexture(imageFile, true);

    spGLCtx.glTexture = glTexture;
    // Set texture parameters for sampling
    glTexture.bind(gl);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);


    //} catch (
    //IOException e) {
    //e.printStackTrace();
    //}

    reloadShader(spGLCtx, scriptName);
    return spGLCtx;
  }


  static public void reloadShader(SpiderGLContext spGLCtx, String shaderName) {
    if (spGLCtx.shaderProgramId != -1)
      spGLCtx.gl.glDeleteProgram(spGLCtx.shaderProgramId);


    ShaderCode vertShader = ShaderCode.create(spGLCtx.gl, GL_VERTEX_SHADER, spGLCtx.getClass(), "shaders",
        null, shaderName, "vert", null, true);

    ShaderProgram shaderProgram = new ShaderProgram();
    shaderProgram.add(vertShader);
    shaderProgram.init(spGLCtx.gl);
    spGLCtx.shaderProgramId = shaderProgram.program();

    spGLCtx.gl.glTransformFeedbackVaryings(spGLCtx.shaderProgramId, 1, new String[]{"tPosition"}, GL_INTERLEAVED_ATTRIBS);
    shaderProgram.link(spGLCtx.gl, System.err);

    // Now, find uniform locations.
    spGLCtx.fTimeLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, "fTime");
    logger.info("Found fTimeLoc at: " + spGLCtx.fTimeLoc);

    for (String scriptParam : spGLCtx.scriptParams.keySet()) {
      int paramLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, scriptParam);
      spGLCtx.paramLocations.put(scriptParam, paramLoc);
      logger.info("Found " + scriptParam + " at: " + paramLoc);
    }
    spGLCtx.textureLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, "textureSampler");
    logger.info("Found textureSampler at location: " + spGLCtx.textureLoc);
  }

  static public void glRun(SpiderGLContext spGLCtx, double deltaMs, float speed) {
    spGLCtx.totalTime += deltaMs/1000.0;
    spGLCtx.gl.glBindBuffer(GL_ARRAY_BUFFER, SpiderGLContext.bufferNames.get(SpiderGLContext.Buffer.VERTEX));
    spGLCtx.gl.glBufferData(GL_ARRAY_BUFFER, spGLCtx.vertexBuffer.capacity() * Float.BYTES, spGLCtx.vertexBuffer, GL_STATIC_DRAW);
    int inputAttrib = spGLCtx.gl.glGetAttribLocation(spGLCtx.shaderProgramId, "position");
    spGLCtx.gl.glEnableVertexAttribArray(inputAttrib);
    spGLCtx.gl.glVertexAttribPointer(inputAttrib, 3, GL_FLOAT, false, 0, 0);

    spGLCtx.gl.glBindBuffer(GL_ARRAY_BUFFER, SpiderGLContext.bufferNames.get(SpiderGLContext.Buffer.TBO));
    spGLCtx.gl.glBufferData(GL_ARRAY_BUFFER, spGLCtx.tfbBuffer.capacity() * Float.BYTES, spGLCtx.tfbBuffer, GL_STATIC_READ);
    spGLCtx.gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, SpiderGLContext.bufferNames.get(SpiderGLContext.Buffer.TBO));

    spGLCtx.gl.glEnable(GL_RASTERIZER_DISCARD);
    spGLCtx.gl.glUseProgram(spGLCtx.shaderProgramId);

    spGLCtx.gl.glUniform1f(spGLCtx.fTimeLoc, speed * (float)spGLCtx.totalTime);

    for (String paramName : spGLCtx.scriptParams.keySet()) {
      spGLCtx.gl.glUniform1f(spGLCtx.paramLocations.get(paramName), spGLCtx.scriptParams.get(paramName));
    }

    spGLCtx.glTexture.enable(spGLCtx.gl);
    spGLCtx.glTexture.bind(spGLCtx.gl);
    spGLCtx.gl.glUniform1i(spGLCtx.textureLoc, 0); // 0 is the texture unit
    spGLCtx.gl.glBeginTransformFeedback(GL_POINTS);
    {
      spGLCtx.gl.glDrawArrays(GL_POINTS, 0, SpiderTrapModel.allPoints.size());
    }
    spGLCtx.gl.glEndTransformFeedback();
    spGLCtx.gl.glFlush();

    spGLCtx.gl.glGetBufferSubData(GL_TRANSFORM_FEEDBACK_BUFFER, 0, spGLCtx.tfbBuffer.capacity() * Float.BYTES, spGLCtx.tfbBuffer);

    spGLCtx.gl.glUseProgram(0);
    spGLCtx.gl.glDisable(GL_RASTERIZER_DISCARD);

    // For manually checking the first few output values in the transform feedback buffer.
    for (int i = 0; i < 21; i++) {
      //System.out.print(tfbBuffer.get(i) + ",");
    }
    //System.out.println();
  }

  static public void copyTFBufferToPoints(int[] colors, SpiderGLContext spGLCtx) {
    for (int i = 0; i < SpiderTrapModel.allPoints.size(); i++) {
      colors[SpiderTrapModel.allPoints.get(i).index] = LXColor.rgbf(spGLCtx.tfbBuffer.get(i * 3),
          spGLCtx.tfbBuffer.get(i * 3 + 1),
          spGLCtx.tfbBuffer.get(i * 3 + 2));
    }
  }
}
