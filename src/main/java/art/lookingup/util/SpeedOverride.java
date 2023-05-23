package art.lookingup.util;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.studio.LXStudio;

/**
 * Custom component for global speed override.  We need this to be a legitimate component so that we
 * can use the global speed value as a modulation source.
 */
public class SpeedOverride extends LXComponent {
  private final LXStudio.UI ui;

  public CompoundParameter speed = new CompoundParameter("speed", 0.5, 0, 1);

  public SpeedOverride(LX lx, LXStudio.UI ui) {
    super(lx);
    this.ui = ui;
    addParameter("speed", speed);
  }
}
