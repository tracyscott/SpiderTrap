package art.lookingup.ui;

import art.lookingup.util.ParameterFile;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class ModelParams extends UIConfig {
  public static final String RADIALS = "radials";
  public static final String RADIAL_INCR = "rad_incr";
  public static final String INNER_RADIUS = "inner_radius";
  public static final String OUTER_RADIUS = "outer_radius";
  public static final String LED_SPACING = "led_spacing";  // LEDs per foot


  public static final String title = "Model Params";
  public static final String filename = "modelparams.json";
  public LX lx;
  private boolean parameterChanged = false;

  public static ParameterFile modelParamFile;

  public ModelParams(final LXStudio.UI ui, LX lx, ParameterFile paramFile) {
    super(ui, title, filename, paramFile);
    this.lx = lx;

    registerStringParameter(RADIALS, "8");
    registerStringParameter(RADIAL_INCR, "0.05");
    registerStringParameter(INNER_RADIUS, "1");
    registerStringParameter(OUTER_RADIUS, "4");
    registerStringParameter(LED_SPACING, "9.6");

    save();

    buildUI(ui);
  }

  public static void loadModelConfig() {
    if (modelParamFile == null) {
      modelParamFile = ParameterFile.instantiateAndLoad(filename);
    }
    modelParamFile.getStringParameter(RADIALS, "8");
    modelParamFile.getStringParameter(RADIAL_INCR, "0.05");
    modelParamFile.getStringParameter(INNER_RADIUS, "1");
    modelParamFile.getStringParameter(OUTER_RADIUS, "4");
    modelParamFile.getStringParameter(LED_SPACING, "9.6");
  }

  static public int getRadials() {
    return Integer.parseInt(modelParamFile.getStringParameter(RADIALS, "8").getString());
  }

  static public float getRadialIncr() {
    return modelParamFile.getStringParameterF(RADIAL_INCR, "0.05");
  }

  static public float getInnerRadius() {
    return modelParamFile.getStringParameterF(INNER_RADIUS, "1");
  }

  static public float getOuterRadius() {
    return modelParamFile.getStringParameterF(OUTER_RADIUS, "4");
  }

  static public float getLedSpacing() {
    return modelParamFile.getStringParameterF(LED_SPACING, "9.6");
  }

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
