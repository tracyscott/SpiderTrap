package art.lookingup.spidertrap.patterns;

import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.logging.Logger;

/**
 *
 */
@LXCategory(LXCategory.TEST)
public class CoordTest extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(CoordTest.class.getName());

  private int curBlockPanel;
  private int curBlockPosX;
  private int curBlockPosY;
  private long currentPanelTestFrame;

  public CoordTest(LX lx) {
    super(lx, null);
    curBlockPosX = 0;
    curBlockPosY = 0;
    curBlockPanel = 0;
    currentPanelTestFrame = 0;
  }

  public void draw(double deltaDrawMs) {
    pg.noSmooth();
    pg.background(255);

    pg.fill(Colors.RED);
    pg.rect(0, 0, 256, 256);
    pg.fill(Colors.GREEN);
    pg.rect(256, 0, 256, 256);
    pg.fill(Colors.BLUE);
    pg.rect(0, 256, 256, 256);
    pg.fill(Colors.YELLOW);
    pg.rect(256, 256, 256, 256);
  }
}
