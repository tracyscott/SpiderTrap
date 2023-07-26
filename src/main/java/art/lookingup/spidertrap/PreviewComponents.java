package art.lookingup.spidertrap;

import heronarts.p4lx.ui.UI;
import heronarts.p4lx.ui.UI3dComponent;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PConstants.TRIANGLE_STRIP;

/**
 * TODO(tracy): Add Spider Model components here.
 */
public class PreviewComponents {

  public static class BodyRender extends UI3dComponent {
    public boolean showBodies = false;

    public BodyRender() {
    }

    public void onDraw(UI ui, PGraphics pg) {
      if (showBodies) {

        for (int i = 0; i < Body.bodies.size(); i++) {
          Body body = Body.bodies.get(i);
          pg.pushMatrix();
          pg.translate(body.x, 8, body.z);
          BodyBox.onDraw(body, ui, pg);
          pg.popMatrix();
        }
      }
    }
  }

  static public class BodyBox {
    static public void onDraw(Body body, UI ui, PGraphics pg) {
      pg.noStroke();
      pg.fill(20, 255, 20);
      pg.beginShape();
      pg.vertex(body.width/2f, 0, body.height/2f);
      pg.vertex(-body.width/2f, 0, body.height/2f);
      pg.vertex(-body.width/2f,0, - body.height/2f);
      pg.vertex(body.width/2f, 0,- body.height/2f);
      pg.endShape(PConstants.CLOSE);
    }
  }
}
