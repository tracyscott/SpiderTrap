package art.lookingup.spidertrap;

import java.util.ArrayList;
import java.util.List;

public class Body {

  public Body(float x, float z, float width, float height) {
    this.x = x;
    this.z = z;
    this.width = width;
    this.height = height;
  }

  public float x;
  public float z;

  public float width;
  public float height;

  public boolean inBody(float x, float z) {
    if (x > this.x - width/2f && x < this.x + width/2f) {
      if (z > this.z - height/2f && z < this.z + height/2f) {
        return true;
      }
    }
    return false;
  }

  public static List<Body> bodies = new ArrayList<Body>();

  public static void initBodies() {
    bodies.add(new Body(4, 4, 1, 1));
  }

  public static boolean inAnyBody(float x, float z) {
    for (Body body : bodies) {
      if (body.inBody(x, z))
        return true;
    }
    return false;
  }
}
