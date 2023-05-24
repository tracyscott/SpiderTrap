package art.lookingup.spidertrap;

import art.lookingup.linear.Edge;
import art.lookingup.linear.Point3D;
import art.lookingup.spidertrap.ui.ModelParams;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.*;
import java.util.logging.Logger;

/**
 * SpiderTrap Model
 *
 *
 *
 *
 */
public class SpiderTrapModel extends LXModel {
  public static final int MAX_RADIALS = 16;
  public static final int NUM_WEBS = 1;

  private static final Logger logger = Logger.getLogger(SpiderTrapModel.class.getName());

  static public float polarAngle(int i, int total) {
    return 360f * (float)i/(float)total;
  }

  static public float polarX(float radius, float angleDegrees) {
    return radius * (float) Math.cos(Math.toRadians(angleDegrees));
  }

  static public float polarZ(float radius, float angleDegrees) {
    return radius * (float) Math.sin(Math.toRadians(angleDegrees));
  }

  static public final float DISTANCE_FROM_CENTER = 4f;
  static public final float METERS_TO_FEET = 3.28084f;

  static public List<Edge> edges = new ArrayList<Edge>();

  static public class Web {
    public List<Radial> radials;
    public List<Segment> segments;

    public float webx;
    public float weby;
    public float webz;
    public List<LXPoint> points;
    public int radialOffset;  // Used for correcting orientation.

    public Web(float webx, float weby, float webz, int numRadials, int radialOffset) {
      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      this.radialOffset = radialOffset;

      // Create the radial strands
      radials = createRadials(webx, weby, webz, numRadials);
      // Create the spiral strand segments
      segments = createSpiralSegments(webx, weby, webz);
    }

    public List<Radial> createRadials(float mx, float my, float mz, int numRadials) {
      List<Radial> radials = new ArrayList<Radial>();
      float angleIncr = 360f / (numRadials);
      float curAngle = radialOffset * angleIncr;
      points = new ArrayList<LXPoint>();
      float innerRadius = ModelParams.getInnerRadius();
      float outerRadius = ModelParams.getOuterRadius();

      for (int i = 0; i < numRadials; i++) {
        logger.info("Creating Radial");
        Radial radial = new Radial(i, curAngle, innerRadius, outerRadius, mx, my, mz);
        radials.add(radial);
        allRadials.add(radial);
        allPoints.addAll(radial.points);
        points.addAll(radial.points);
        curAngle += angleIncr;
      }
      return radials;
    }

    public List<Segment> createSpiralSegments(float webx, float weby, float webz) {
      // Iterate while current radial distance is less than outer radius.
      float curRadialDist = ModelParams.getInnerRadius();
      int segmentId = 0;
      int startRadialId = 0;
      int endRadialId;
      segments = new ArrayList<Segment>();
      // Segment (int id, int startRadialId, int endRadialId, float radialDist, float webx, float weby, float webz) {
      float outerRadius = ModelParams.getOuterRadius();
      int numRadials = ModelParams.getRadials();
      while (curRadialDist < outerRadius) {
        endRadialId = startRadialId + 1;
        if (endRadialId >= numRadials)
          endRadialId = 0;
        Segment segment = new Segment(segmentId, startRadialId, endRadialId, curRadialDist, webx, weby, webz);
        segments.add(segment);
        allSegments.add(segment);
        allPoints.addAll(segment.points);
        points.addAll(segment.points);
        startRadialId++;

        if (startRadialId >= numRadials)
          startRadialId = 0;

        segmentId++;
        curRadialDist += ModelParams.getRadialIncr();
      }
      logger.info("Created " + segments.size() + " segments");
      return segments;
    }
  }

  static public class Radial {

    public Radial (int id, float angle, float innerRadius, float outerRadius, float webx, float weby, float webz) {
      this.id = id;
      this.angle = angle;
      this.innerRadius = innerRadius;
      this.outerRadius = outerRadius;
      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      //points = createPoints(mx, my, mz);
      edge = createEdge(webx, weby, webz);
      edges.add(edge);
      points = new ArrayList<LXPoint>();
      points.addAll(edge.points);
      logger.info("Added radial with " + points.size() + " points id: " + id);
      logger.info("Radial start x: " + edge.p1.x + " end x: " + edge.p2.x);
    }
    int id;
    public float angle;
    public float innerRadius;
    public float webx;
    public float weby;
    public float webz;

    public Edge edge;

    public float outerRadius;
    public List<LXPoint> points;
    public List<LXPoint> pointsWireOrder;


    public Edge createEdge(float x, float y, float z) {
      Point3D edgeA = new Point3D(x + polarX(innerRadius, angle),
                                  y,
                                  z + polarZ(innerRadius, angle));
      Point3D edgeB = new Point3D(x + polarX(outerRadius, angle),
                                  y,
                                     z + polarZ(outerRadius, angle));
      return new Edge(edgeA, edgeB, ModelParams.getLedsPerFoot());
    }

    List<LXPoint> getPointsWireOrder() {
      return edge.getPointsWireOrder();
    }
  }


  static public class Segment {

    public Segment (int id, int startRadialId, int endRadialId, float radialDist, float webx, float weby, float webz) {
      this.id = id;
      this.startRadialId = startRadialId;
      this.endRadialId = endRadialId;
      this.radialDist = radialDist;
      this.endRadialDist = radialDist + ModelParams.getRadialIncr();

      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      edge = createEdge(webx, weby, webz);
      edges.add(edge);
      points = new ArrayList<LXPoint>();
      points.addAll(edge.points);
      logger.info("Added segment with " + points.size() + " points, startRad: " + startRadialId + " endRad: " + endRadialId);
    }
    int id;
    public int startRadialId;
    public int endRadialId;
    public float radialDist; // distance along start radial.
    public float endRadialDist;  // distance along the end radial

    public float webx;
    public float weby;
    public float webz;


    public Edge edge;

    public List<LXPoint> points;

    public Edge createEdge(float x, float y, float z) {
      // The start point is radialDist along the start radial unit vector
      Radial startRadial = allRadials.get(startRadialId);
      Radial endRadial = allRadials.get(endRadialId);
      Point3D startPoint = new Point3D(x + startRadial.edge.unitVector.x * radialDist,
                                   y + startRadial.edge.unitVector.y * radialDist,
                                   z + startRadial.edge.unitVector.z * radialDist);
      Point3D endPoint = new Point3D(x + endRadial.edge.unitVector.x * endRadialDist,
                                     y + endRadial.edge.unitVector.y * endRadialDist,
                                     z + endRadial.edge.unitVector.z * endRadialDist);

      return new Edge(startPoint, endPoint, ModelParams.getLedsPerFoot());
    }

    List<LXPoint> getPointsWireOrder() {
      return edge.getPointsWireOrder();
    }
  }

  static public List<Web> allWebs = new ArrayList<Web>();
  static public List<Radial> allRadials = new ArrayList<Radial>();
  static public List<Segment> allSegments = new ArrayList<Segment>();
  static public List<LXPoint> allPoints = new ArrayList<LXPoint>();


  /**
   * Generate a model with random points.  Leaves are assigned to run based on leaf number.
   * @return
   */
  static public LXModel createModel() {

    for (int webNum = 0; webNum < 1; webNum++) {
      float mx = (webNum == 0)?-DISTANCE_FROM_CENTER:DISTANCE_FROM_CENTER;
      Web web = new Web(0, 10, 0, ModelParams.getRadials(), 0);
      allWebs.add(web);
    }

    Edge.computeAdjacentEdges(edges);

    logger.info("Number of edges: " + edges.size());

    return new SpiderTrapModel(allPoints);
  }

  public SpiderTrapModel(List<LXPoint> points) {
    super(points);
  }
}
