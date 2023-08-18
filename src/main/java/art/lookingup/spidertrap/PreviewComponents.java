package art.lookingup.spidertrap;

import art.lookingup.linear.Edge;
import art.lookingup.linear.Point3D;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.UI;
import heronarts.p4lx.ui.UI3dComponent;
import heronarts.p4lx.ui.UI3dContext;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static processing.core.PConstants.TRIANGLE_STRIP;
import static processing.opengl.PShapeOpenGL.NORMAL;

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

  public static class EdgeLabels extends UI3dComponent {

    static public Map<Integer, PGraphics> labelCache = new HashMap<Integer, PGraphics>();
    public boolean showEdges = false;

    public void onDraw(UI ui, PGraphics pg) {

      if (!showEdges) return;

      for (Edge e : SpiderTrapModel.allEdges) {
        String id = e.id + "";
        float textWidth = pg.textWidth(id);
        int tWidth = (int) textWidth + 1;
        int tHeight = 14;
        PGraphics label = labelCache.get(e.id);
        if (label == null) {
          label = SpiderTrapApp.pApplet.createGraphics(tWidth, tHeight);
          label.beginDraw();
          //label.background(0, 255);
          label.background(0);
          label.stroke(255);
          label.textSize(tHeight);
          label.text(id, 0, tHeight - 1);
          label.endDraw();
          labelCache.put(e.id, label);
        }

        float aspectRatio = (float) label.width / (float) label.height;
        float size = 0.2f;
        float z = 0.1f;
        pg.noStroke();
        pg.pushMatrix();
        LXStudio.UI lxui = (LXStudio.UI) ui;
        UI3dContext.Camera camera = lxui.preview.focusCamera.getObject();
        Point3D pos = e.midpoint();
        pg.translate(pos.x, pos.y + 0.2f, pos.z);
        pg.rotateY(-camera.theta.getValuef());
        pg.rotateX(camera.phi.getValuef());

        pg.textureMode(NORMAL);
        pg.beginShape();
        pg.texture(label);
        pg.vertex(-size * aspectRatio, -size, z, 0, 1);  // bottom left
        pg.vertex(size * aspectRatio, -size, z, 1, 1);  // bottom right
        pg.vertex(size * aspectRatio, size, z, 1, 0); // top right
        pg.vertex(-size * aspectRatio, size, z, 0, 0); // top left
        pg.endShape();
        pg.popMatrix();
        label.dispose();
      }
    }
  }

}
