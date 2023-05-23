package art.lookingup.spidertrap;

import heronarts.lx.color.LXColor;
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

  public static class Trees extends UI3dComponent {
    public boolean showTrees = true;
    List<UICylinder> trees;

    public Trees() {
      trees = new ArrayList<UICylinder>();

    }

    public void onDraw(UI ui, PGraphics pg) {
      if (showTrees) {
        /*
        for (int i = 0; i < trees.size(); i++) {
          UICylinder tree = trees.get(i);
          AnchorTree aTree = NTreeModel.anchorTrees.get(i);
          pg.pushMatrix();
          pg.translate(aTree.x, 0, aTree.z);
          tree.onDraw(ui, pg);
          pg.popMatrix();
        }
         */
      }
    }
  }

  /**
   * Utility class for drawing cylinders. Assumes the cylinder is oriented with the
   * y-axis vertical. Use transforms to position accordingly.
   */
  public static class UICylinder extends UI3dComponent {

    private final PVector[] base;
    private final PVector[] top;
    private final int detail;
    public final float len;
    private int fill;
    private int stroke;

    public UICylinder(float radius, float len, int detail, int fill) {
      this(radius, radius, 0, len, detail, fill, 0);
    }

    public UICylinder(float radius, float len, int detail, int fill, int stroke) {
      this(radius, radius, 0, len, detail, fill, stroke);
    }

    public UICylinder(float baseRadius, float topRadius, float len, int detail, int fill) {
      this(baseRadius, topRadius, 0, len, detail, fill, 0);
    }

    public UICylinder(float baseRadius, float topRadius, float yMin, float yMax, int detail, int fill, int stroke) {
      this.base = new PVector[detail];
      this.top = new PVector[detail];
      this.detail = detail;
      this.len = yMax - yMin;
      this.fill = fill;
      this.stroke = stroke;
      for (int i = 0; i < detail; ++i) {
        float angle = i * PConstants.TWO_PI / detail;
        this.base[i] = new PVector(baseRadius * (float)Math.cos(angle), yMin, baseRadius * (float)Math.sin(angle));
        this.top[i] = new PVector(topRadius * (float)Math.cos(angle), yMax, topRadius * (float)Math.sin(angle));
      }
    }

    public void onDraw(UI ui, PGraphics pg) {
      if (stroke == 0) {
        pg.fill(fill);
        pg.noStroke();
      } else {
        pg.noFill();
        pg.stroke(stroke);
      }
      pg.beginShape(TRIANGLE_STRIP);
      for (int i = 0; i <= this.detail; ++i) {
        int ii = i % this.detail;
        pg.vertex(this.base[ii].x, this.base[ii].y, this.base[ii].z);
        pg.vertex(this.top[ii].x, this.top[ii].y, this.top[ii].z);
      }
      pg.endShape(PConstants.CLOSE);
    }
  }

  static public class Floor extends UI3dComponent {
    public Floor() {

    }

    public void onDraw(UI ui, PGraphics pg) {
      float x1 = 120.0f;
      float y1 = 0f;
      float z1 = -1170.0f;
      pg.noStroke();
      pg.fill(20, 20, 20);
      pg.beginShape();
      pg.vertex(x1, y1, 0f);
      pg.vertex(-x1, y1, 0f);
      pg.vertex(-x1, y1, -z1);
      pg.vertex(x1, y1, -z1);
      pg.endShape(PConstants.CLOSE);
    }
  }
}
