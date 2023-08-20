package art.lookingup.spidertrap.ui;

import art.lookingup.spidertrap.Output;
import art.lookingup.ui.UIConfig;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class UIPixliteConfig extends UIConfig {
  public static final String PIXLITE_IP = "pxlt_ip";
  public static final String PIXLITE_PORT = "pxlt_port";

  public static final String FLOOD_IP = "flood_ip";
  public static final String FLOOD_PORT = "flood_port";

  public static final String title = "pixlite IP";
  public static final String filename = "pixliteconfig.json";
  public LX lx;
  private boolean parameterChanged = false;

  public UIPixliteConfig(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    this.lx = lx;

    registerStringParameter(PIXLITE_IP, "127.0.0.1");
    registerStringParameter(PIXLITE_PORT, "6454");

    registerStringParameter(FLOOD_IP, "127.0.0.1");
    registerStringParameter(FLOOD_PORT, "6455");

    save();

    buildUI(ui);
  }

  public String pixliteIp() {
    return getStringParameter(PIXLITE_IP).getString();
  }

  public int pixlitePort() {
    return Integer.parseInt(getStringParameter(PIXLITE_PORT).getString());
  }

  public String floodIp() { return getStringParameter(FLOOD_IP).getString(); }
  public int floodPort() { return Integer.parseInt(getStringParameter(FLOOD_PORT).getString()); }

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
