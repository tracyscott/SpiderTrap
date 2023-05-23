package art.lookingup.linear;

import heronarts.lx.model.LXPoint;

/**
 * LPPoint is a wrapper class for an LXPoint that adds some additional functionality
 * for tracking the LinearPoint-local x position of a point for simplifying 1D linearpoint-local
 * animations.
 * @see LPRender
 */
public class LPPoint extends LXPoint {

  public LinearPoints linearPoints;
  public float lpx;

  public LPPoint(LinearPoints linearPoints, double x, double y, double z, double lpx) {
    super(x, y, z);
    this.linearPoints = linearPoints;
    this.lpx = (float) lpx;
  }

}
