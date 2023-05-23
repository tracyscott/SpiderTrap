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

  String[] defaultOutputMapping = {
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "10",
      "11",
      "12",
      "13",
      "14",
      "15",
      "16",

      "17",
      "18",
      "19",
      "20",
      "21",
      "22",
      "23",
      "24",
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
