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
    modelParamFile.getStringParameter(INNER_RADIUS, "0.8");
    modelParamFile.getStringParameter(OUTER_RADIUS, "4.8");
    modelParamFile.getStringParameter(LEDS_PER_FOOT, "9.144");
    modelParamFile.getStringParameter(NUM_RINGS, "10");
  }

  static public int getRadials() {
    return Integer.parseInt(modelParamFile.getStringParameter(RADIALS, "6").getString());
  }

  static public float getRadialIncr() {
    return modelParamFile.getStringParameterF(RADIAL_INCR, "0.03");
  }

  static public float getInnerRadius() {
    return modelParamFile.getStringParameterF(INNER_RADIUS, "0.8");
  }

  static public float getOuterRadius() {
    return modelParamFile.getStringParameterF(OUTER_RADIUS, "4.8");
  }

  static public float getLedsPerFoot() {
    return modelParamFile.getStringParameterF(LEDS_PER_FOOT, "9.144");
  }

  static public int getNumRings() { return Integer.parseInt(modelParamFile.getStringParameter(NUM_RINGS, "10").getString()); }

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
