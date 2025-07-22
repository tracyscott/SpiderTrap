package art.lookingup.linear;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LinearPoints {
  private static final Logger logger = Logger.getLogger(LinearPoints.class.getName());

  public float length;
  public float ledLength; // not including margins.
  public int numPoints;
  public int lpNum;
  public float spacing;
  public List<LPPoint> points;
  public Edge edge;

  public LinearPoints side1;
  public boolean doubleSided = false;
  List<List<LPPoint>> pointSets = new ArrayList<List<LPPoint>>();

  /**
   * Obsolete constructor that was used for double-sided LinearPoints.  Probably not going to be used on this
   * project.  Won't take out until sure about it though.  In that case, we have multiple linear points per Edge
   * and we just use an offset hack to deal with the issue.
   * @param side1
   */
  public LinearPoints(LinearPoints side1) {
    this.side1 = side1;
    this.lpNum = (side1.lpNum + 1) * 100;
    this.length = side1.length;
    this.edge = side1.edge;
    this.numPoints = side1.numPoints;
    this.doubleSided = false;
    pointSets.add(side1.points);
  }

  public List<List<LPPoint>> getPointSets() {
    return pointSets;
  }

  /**
   * Construct a LinearPoints from a list of points.  This will compute the end points based on first and last
   * LXPoint and also construct the necessary LPPoints.  This does not support specifying a margin.
   *
   * @param lpNum Inherited from associated Edge when created by the Edge.
   * @param length
   * @param lxpoints
   * @param edge
   */
  public LinearPoints(int lpNum, float length, List<Point3D> lxpoints, Edge edge) {
    // The LinearPoints object's ID is inherited from the associated Edge.
    this.lpNum = lpNum;
    this.length = length;
    this.edge = edge;
    this.numPoints = lxpoints.size();

    this.points = new ArrayList<LPPoint>();
    for (int i = 0; i < lxpoints.size(); i++) {
      float lpt = (float)i/(float)(numPoints - 1);
      LPPoint p = new LPPoint(this,lxpoints.get(i).x,
          lxpoints.get(i).y,
          lxpoints.get(i).z, lpt * length, lpt * length, lpt, lpt);
      points.add(p);
    }
  }


  public LinearPoints(int lpNum, float length, float pointSpacing, Edge edge, Point3D a, Point3D b, float marginDist) {
     this(lpNum, length, (int)((length - marginDist * 2f) * pointSpacing), edge, a, b, marginDist);
  }

  public LinearPoints(int lpNum, float length, float pointSpacing, Edge edge, Point3D a, Point3D b, float startMargin, float endMargin) {
    this.lpNum = lpNum;
    this.length = length;
    this.ledLength = length - (startMargin + endMargin);
    this.edge = edge;
    int numPoints = (int)Math.floor(ledLength * pointSpacing) + 1;
    this.numPoints = numPoints;
    // With fixed point spacing, we sometimes encounter a situation where we have some extra space at the end of
    // the length from point a to point b.  We should split that extra space between the beginning margin and end
    // margin.
    float lengthActual = (numPoints-1)/pointSpacing;
    float trailingSpace = ledLength - lengthActual;
    startMargin += trailingSpace/2f;
    endMargin += trailingSpace/2f;

    float feetPerLed = 1f/pointSpacing;

    int stretches = numPoints - 1;
    float ux = b.x - a.x;
    float uy = b.y - a.y;
    float uz = b.z - a.z;
    Point3D unitVector = Point3D.unitVectorTo(b, a);

    float dx = feetPerLed * unitVector.x;
    float dy = feetPerLed * unitVector.y;
    float dz = feetPerLed * unitVector.z;

    float startMx = startMargin * unitVector.x;
    float startMy = startMargin * unitVector.y;
    float startMz = startMargin * unitVector.z;

    float pSpacing = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    /*
    logger.info("startMargin=" + startMargin + " endMargin=" + endMargin);
    if (startMargin == 0f && endMargin != 0f) {
      logger.info("COMPUTED POINT SPACING: " + 1f/pSpacing);
    } else {
      logger.info("0 MARGIN COMPUTED SPACING: " + 1f/pSpacing);
    }
     */

    points = new ArrayList<LPPoint>();
    for (int i = 0; i < numPoints; i++) {
      float lpt = (float)i/(float)(numPoints - 1);
      float lptM = (lpt * ledLength + startMargin + endMargin) / length;
      LPPoint p = new LPPoint(this,a.x + dx * i + startMx,
          a.y + dy * i + startMy,
          a.z + dz * i + startMz, lpt*length, lptM * length, lpt, lptM);
      points.add(p);
    }

    pointSets.add(points);
  }


  public LinearPoints(int lpNum, float length, int numPoints, Edge edge, Point3D a, Point3D b,
                      float marginDist) {
    this(lpNum, length, numPoints, edge, a, b, marginDist/2f, marginDist/2f);
  }

  /**
   * Create based on a given number of points.
   * @param lpNum  Inherited from associated Edge when created by the Edge.
   * @param length
   * @param numPoints
   * @param edge
   * @param a
   * @param b
   * @param startMargin Margin space at beginning of the linear points.
   * @param endMargin Margin space at the end of the linear points.
   */
  public LinearPoints(int lpNum, float length, int numPoints, Edge edge, Point3D a, Point3D b,
                      float startMargin, float endMargin) {
    this.lpNum = lpNum;
    this.length = length;
    this.ledLength = length - (startMargin + endMargin);
    this.edge = edge;
    this.numPoints = numPoints;

    int stretches = numPoints - 1;
    float ux = b.x - a.x;
    float uy = b.y - a.y;
    float uz = b.z - a.z;
    Point3D unitVector = Point3D.unitVectorTo(b, a);

    // Sum the margins and multiply times the unit vector to get margins
    // vector.  We use these for computing our delta-x, delta-y, and delta-z
    // increments as we move along the direction of points.
    float mx = (startMargin + endMargin) * unitVector.x;
    float my = (startMargin + endMargin) * unitVector.y;
    float mz = (startMargin + endMargin) * unitVector.z;


    // Subtract off the margin vector components in order to compute the total
    // distance traveled in each dimension so we can get per-dimensions step size
    float dx = ((b.x - a.x) - mx) / stretches;
    float dy = ((b.y - a.y) - my) / stretches;
    float dz = ((b.z - a.z) - mz) / stretches;

    float startMx = startMargin * unitVector.x;
    float startMy = startMargin * unitVector.y;
    float startMz = startMargin * unitVector.z;

    float pSpacing = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);

    /*
    logger.info("startMargin=" + startMargin + " endMargin=" + endMargin);
    if (startMargin == 0f && endMargin != 0f) {
      logger.info("COMPUTED POINT SPACING: " + 1f/pSpacing);
    } else {
      logger.info("0 MARGIN COMPUTED SPACING: " + 1f/pSpacing);
    }
     */

    points = new ArrayList<LPPoint>();
    for (int i = 0; i < numPoints; i++) {
      float lpt = (float)i/(float)(numPoints - 1);
      float lptM = (lpt * ledLength + startMargin + endMargin) / length;
      LPPoint p = new LPPoint(this,a.x + dx * i + startMx,
          a.y + dy * i + startMy,
          a.z + dz * i + startMz, lpt*length, lptM * length, lpt, lptM);
      points.add(p);
    }

    pointSets.add(points);
  }

  public void translate(float x, float y, float z) {
    for (LXPoint pt : points) {
      pt.x += x;
      pt.y += y;
      pt.z += z;
    }
  }
}
