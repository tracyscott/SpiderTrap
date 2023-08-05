package art.lookingup.spidertrap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computer vision blobs.  These will be reported via OSC approximately at 10 FPS.  We will
 * timestamp their arrival and expire any old blobs.
 */
public class CVBlob {

  public static final int MAX_AGE = 100;
  public static final float FEET_PER_METER = 3.28084f;

  public static List<CVBlob> blobs = new ArrayList<CVBlob>();

  public float x;
  public float y;
  public float z;
  public long created;

  public CVBlob(float x, float y, float z) {
    this.x = FEET_PER_METER * (x/1000f);
    this.y = FEET_PER_METER * (y/1000f);
    this.z = FEET_PER_METER * (z/1000f);
    created = System.currentTimeMillis();
  }

  public boolean isInBlob(float x, float z, float radius) {
    x = -x; // Adjust for camera coordinate system.  Positive x for the camera is -x for LED world coordinates.
    // Positive y for the camera is positive z for LED world coordinates.
    if (Math.sqrt((this.x-x)*(this.x-x) + (this.y - z)*(this.y - z)) < radius)
      return true;
    return false;
  }

  static public void addCVBlob(float x, float y, float z) {
    synchronized(blobs) {
      blobs.add(new CVBlob(x, y, z));
    }
  }

  static public boolean isInAnyBlob(float x, float z, float radius) {
    synchronized(blobs) {
      for (CVBlob blob : blobs) {
        if (blob.isInBlob(x, z, radius))
          return true;
      }
    }
    return false;
  }

  static public void cleanExpired() {
    cleanExpired(1000);
  }

  static public void cleanExpired(float age) {
    synchronized (blobs) {
      Iterator<CVBlob> blobIter = blobs.iterator();
      long now = System.currentTimeMillis();
      while (blobIter.hasNext()) {
        CVBlob blob = blobIter.next();
        if (now - blob.created > age)
          blobIter.remove();
      }
    }
  }
}
