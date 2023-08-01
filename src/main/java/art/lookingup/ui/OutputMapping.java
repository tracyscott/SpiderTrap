package art.lookingup.ui;

import art.lookingup.spidertrap.Output;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class OutputMapping extends UIConfig {
  public static final String OUTPUTBASE = "out";

  public static final String title = "output map";
  public static final String filename = "outputmap.json";
  public LX lx;
  private boolean parameterChanged = false;
/*
 * Per triangle.  This is repeated 6 times.
 *
 * First output:  Radial inwards and then T1 counter-clockwise, aka left and then T2 clockwise aka right.
 * Second output: T5, T4, T3.  T5 is CCW aka left, T4 is CW aka right, T3 CCW aka left
 * Third output: T7, T6.  T7 is CCW aka left and T6 is CW aka right.
 * Fourth output: T9, T8.  T9 is CCW aka left and T8 is CW aka right.

 */
  String[] defaultOutputMapping = {
      "t1.r,t1.t1,t1.t2",
      "t1.t5,t1.t4,t1.t3",
      "t1.t7,t1.t6",
      "t1.t9,t1.t8",

      "t2.r,t2.t1,t2.t2",
      "t2.t5,t2.t4,t2.t3",
      "t2.t7,t2.t6",
      "t2.t9,t2.t8",

    "t3.r,t3.t1,t3.t2",
    "t3.t5,t3.t4,t3.t3",
    "t3.t7,t3.t6",
    "t3.t9,t3.t8",

    "t4.r,t4.t1,t4.t2",
    "t4.t5,t4.t4,t4.t3",
    "t4.t7,t4.t6",
    "t4.t9,t4.t8",

    "t5.r,t5.t1,t5.t2",
    "t5.t5,t5.t4,t5.t3",
    "t5.t7,t5.t6",
    "t5.t9,t5.t8",

    "t6.r,t6.t1,t6.t2",
    "t6.t5,t6.t4,t6.t3",
    "t6.t7,t6.t6",
    "t6.t9,t6.t8",

      "25",
      "26",
      "27",
      "28",
      "29",
      "30",
      "31",
      "32",
  };

  public OutputMapping(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    this.lx = lx;

    for (int i = 1; i <= 32; i++) {
      registerStringParameter(OUTPUTBASE + i, defaultOutputMapping[i-1]);
    }

    save();

    buildUI(ui);
  }

  public String getOutputMapping(int outputNum) {
    return getStringParameter(OUTPUTBASE + outputNum).getString();
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    if (parameterChanged) {
      Output.restartOutput(lx);
      parameterChanged = false;
    }
  }
}
