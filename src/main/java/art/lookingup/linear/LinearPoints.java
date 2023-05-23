package art.lookingup.linear;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LinearPoints {
  private static final Logger logger = Logger.getLogger(LinearPoints.class.getName());

  public float length;
  public int numPoints;
  public int lpNum;
  public float spacing;
  public List<LPPoint> points;
  public Edge edge;

  public LinearPoints side1;
  public boolean doubleSided = false;
  List<List<LPPoint>> pointSets = new ArrayList<List<LPPoint>>();

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
   * Construct a lightbqr from a list of points.  This will compute the end points based on first and last
   * LXPoint and also construct the necessary LBPoints.
   *
   * @param lpNum
   * @param length
   * @param lxpoints
   * @param edge
   */
  public LinearPoints(int lpNum, float length, List<Point3D> lxpoints, Edge edge) {
    this.lpNum = lpNum;
    this.length = length;
    this.edge = edge;
    this.numPoints = lxpoints.size();

    this.points = new ArrayList<LPPoint>();
    for (int i = 0; i < lxpoints.size(); i++) {

      LPPoint p = new LPPoint(this,lxpoints.get(i).x,
          lxpoints.get(i).y,
          lxpoints.get(i).z, (float)i/(float)(numPoints-1));
      points.add(p);
    }
  }


  public LinearPoints(int lpNum, float length, float pointSpacing, Edge edge, Point3D a, Point3D b, float marginDist) {
     this(lpNum, length, (int)((length - marginDist * 2f) * pointSpacing), edge, a, b, marginDist);
  }

  /**
   * Create based on a given number of points.
   * @param lpNum
   * @param length
   * @param numPoints
   * @param edge
   * @param a
   * @param b
   */
  public LinearPoints(int lpNum, float length, int numPoints, Edge edge, Point3D a, Point3D b,
                      float marginDist) {
    this.lpNum = lpNum;
    this.length = length;
    this.edge = edge;
    this.numPoints = numPoints;

    int stretches = numPoints - 1;
    float ux = b.x - a.x;
    float uy = b.y - a.y;
    float uz = b.z - a.z;
    float mx = marginDist * ux;
    float my = marginDist * uy;
    float mz = marginDist * uz;

    float dx = ((b.x - a.x) - 2f * mx) / stretches;
    float dy = ((b.y - a.y) - 2f * my) / stretches;
    float dz = ((b.z - a.z) - 2f * mz) / stretches;
    points = new ArrayList<LPPoint>();
    for (int i = 0; i < numPoints; i++) {

      LPPoint p = new LPPoint(this,a.x + dx * i + mx,
          a.y + dy * i + my,
          a.z + dz * i + mz, (float)i/(float)(numPoints-1));
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
