package art.lookingup.spidertrap.patterns;

import heronarts.lx.LX;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.pattern.LXPattern;

/**
 * Base-class that handles transitioning to our 'Blank' pattern for the EFX overlay channel.
 * For DJ triggered effects
 */
abstract public class DJFXPattern extends LXPattern {

  public DJFXPattern(LX lx) {
    super(lx);
  }

  public void run(double deltaDrawMs) {
    runFx(deltaDrawMs);
    if (isFinished()) {
      LXChannel channel = getChannel();
      for (LXPattern p : channel.getPatterns()) {
        if (p.getLabel().equals("Blank")) {
          channel.goPattern(p);
          return;
        }
      }
    }
  }

  abstract public void runFx(double drawDeltaMs);
  abstract public boolean isFinished();

}
