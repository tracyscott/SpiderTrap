package art.lookingup.spidertrap;

import art.lookingup.linear.Point3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static art.lookingup.spidertrap.SpiderTrapModel.largestRange;

/**
 * Computer vision blobs.  These will be reported via OSC approximately at 10 FPS.  We will
 * timestamp their arrival and expire any old blobs.
 */
public class CVBlob {
  private static final Logger logger = Logger.getLogger(CVBlob.class.getName());
  public static final int MAX_AGE = 100;
  public static final float FEET_PER_METER = 3.28084f;

  public static float cvBlobD = 0.5f;
  public static float cvBlobRotate = 0f;

  public static List<CVBlob> blobs = new ArrayList<CVBlob>();

  public float x;
  public float y;
  public float z;
  public long created;
  public float u;
  public float v;

  public CVBlob(float x, float y, float z) {
    this.x = FEET_PER_METER * (x/1000f);
    this.y = FEET_PER_METER * (y/1000f);
    this.z = FEET_PER_METER * (z/1000f);
    if (cvBlobRotate > 0) {
      // x2 = x * cos(theta) - y * sin(theta)
      // y2 = y * cos(theta) + x * sin(theta)
      float radians = (float)Math.toRadians(cvBlobRotate);
      float x2 = this.x * (float)Math.cos(radians) - this.y * (float)Math.sin(radians);
      float y2 = this.y * (float)Math.cos(radians) + this.x * (float)Math.sin(radians);
      this.x = x2;
      this.y = y2;
    }
    created = System.currentTimeMillis();
    // LXModel Z coordinates (or opencv vision y coordinates) have a vertical offset.  Since
    // the model is rectangular, to create non-distorted uv coordinates ranging from 0 to 1 we
    // need to "clip" the bottom and top of the Z or Y coordinates.
    // TODO(tracy): This will need to be changed depending on how things are oriented at the build
    // because the computer vision Y relative to the LX Model Z-coord might be different than our testing setup.
    // ALso, the x-coordinate is mirrored from what we would expect.
    float zOffset = 0.07f;
    u = (-this.x - SpiderTrapModel.modelXMin)/largestRange;
    v = (this.y - SpiderTrapModel.modelZMin)/largestRange + zOffset;
  }

  public float distanceTo(float x2, float y2, float z2) {
    return (float) Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2));
  }

  public boolean isInBlob(float x, float z, float radius) {
    x = -x; // Adjust for camera coordinate system.  Positive x for the camera is -x for LED world coordinates.
    // Positive y for the camera is positive z for LED world coordinates.
    if (Math.sqrt((this.x-x)*(this.x-x) + (this.y - z)*(this.y - z)) < radius)
      return true;
    return false;
  }

  /**
   * Uses raw world coordinates coordinates.
   * @param x
   * @param y
   * @param z
   * @param radius
   * @return
   */
  public boolean isInBlobNative(float x, float y, float radius) {
    if (Math.sqrt((this.x-x)*(this.x-x) + (this.y - y)*(this.y - y)) < radius)
      return true;
    return false;
  }

  static public void addCVBlob(float x, float y, float z) {
    //logger.info("Attempting to add cvblob");
    float xFt = FEET_PER_METER * (x/1000f);
    float yFt = FEET_PER_METER * (y/1000f);
    float zFt = FEET_PER_METER * (z/1000f);
    synchronized(blobs) {
      if (blobs.size() > 0) {
        if (blobs.size() > 19)
          return;
        for (CVBlob blob : blobs) {
          if (!blob.isInBlobNative(xFt, yFt, cvBlobD)) {
            CVBlob newBlob = new CVBlob(x, y, z);
            blobs.add(newBlob);
            //logger.info("Added new blob!");
          } else {
            //logger.info("Blob too close");
          }
        }
      } else {
        blobs.add(new CVBlob(x, y, z));
      }
    }
  }

  static public void addKinectV2CVBlob(float x, float y, float z) {
    addCVBlob(-x, y, z);
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
