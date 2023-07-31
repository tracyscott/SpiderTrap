package art.lookingup.spidertrap.ui;

import art.lookingup.ui.UIConfig;
import art.lookingup.util.ParameterFile;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class ModelParams extends UIConfig {
  public static final String RADIALS = "radials";
  public static final String RADIAL_INCR = "rad_incr";
  public static final String INNER_RADIUS = "inner_radius";
  public static final String OUTER_RADIUS = "outer_radius";
  public static final String HEX_INNER = "hex_inner";
  public static final String LEDS_PER_FOOT = "leds_per_foot";  // LEDs per foot

  public static final String NUM_RINGS = "num_rings";


  public static final String title = "Model Params";
  public static final String filename = "modelparams.json";
  public LX lx;
  private boolean parameterChanged = false;

  public static ParameterFile modelParamFile;

  public ModelParams(final LXStudio.UI ui, LX lx, ParameterFile paramFile) {
    super(ui, title, filename, paramFile);
    this.lx = lx;

    registerStringParameter(RADIALS, "");
    registerStringParameter(RADIAL_INCR, "");
    registerStringParameter(INNER_RADIUS, "");
    registerStringParameter(OUTER_RADIUS, "");
    registerStringParameter(HEX_INNER, "");
    registerStringParameter(LEDS_PER_FOOT, "");
    registerStringParameter(NUM_RINGS, "");

    save();

    buildUI(ui);
  }

  public static void loadModelConfig() {
    if (modelParamFile == null) {
      modelParamFile = ParameterFile.instantiateAndLoad(filename);
    }
    modelParamFile.getStringParameter(RADIALS, "6");
    modelParamFile.getStringParameter(RADIAL_INCR, "0.03");
    modelParamFile.getStringParameter(INNER_RADIUS, "1.2");
    modelParamFile.getStringParameter(OUTER_RADIUS, "9.0");
    modelParamFile.getStringParameter(HEX_INNER, ".25");
    modelParamFile.getStringParameter(LEDS_PER_FOOT, "21.946");
    modelParamFile.getStringParameter(NUM_RINGS, "9");
  }

  static public int getRadials() {
    return Integer.parseInt(modelParamFile.getStringParameter(RADIALS, "6").getString());
  }

  static public float getRadialIncr() {
    return modelParamFile.getStringParameterF(RADIAL_INCR, "0.03");
  }

  static public float getInnerRadius() {
    return modelParamFile.getStringParameterF(INNER_RADIUS, "1.2");
  }

  static public float getOuterRadius() {
    return modelParamFile.getStringParameterF(OUTER_RADIUS, "9.0");
  }

  static public float getHexInner() { return modelParamFile.getStringParameterF(HEX_INNER, ".25"); }

  static public float getLedsPerFoot() {
    return modelParamFile.getStringParameterF(LEDS_PER_FOOT, "21.946");
  }

  static public int getNumRings() { return Integer.parseInt(modelParamFile.getStringParameter(NUM_RINGS, "9").getString()); }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    if (parameterChanged) {
    }
  }
}
