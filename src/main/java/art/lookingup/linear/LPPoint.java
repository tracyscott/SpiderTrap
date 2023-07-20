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
  public float lpx;  // non-normalized 0 to 1 x coordinate of point across the span of the leds.
  public float lpxM; // Includes margins

  public float lpt;  // normalized 0 to 1 x coordinate of point across the span of the leds.
  public float lptM; // normalized 0 to 1 x coordinate of point including margins.

  public LPPoint(LinearPoints linearPoints, double x, double y, double z, double lpx, double lpxM, double lpt, double lptM) {
    super(x, y, z);
    this.linearPoints = linearPoints;
    this.lpx = (float) lpx;
    this.lpxM = (float) lpxM;
    this.lpt = (float) lpt;
    this.lptM = (float) lptM;
  }

}
