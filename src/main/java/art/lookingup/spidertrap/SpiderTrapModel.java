package art.lookingup.spidertrap;

import art.lookingup.linear.Edge;
import art.lookingup.linear.LPPoint;
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

  public static final float SEGMENT_MARGINS = 2.75f/12f;
  public static final float RING_SEG_MARGIN[] =  {
      0f,  // 16
      0f,  // 37
      0.02f,  // 58
      0f,  // 80
      0f, // 101
      0.02f, // 122
      0f, // 144
      0.1f/12f,  // 165
      -0.65f/12f  // 189
  };


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
  static public final float MIN_CUT_DISTANCE = 0.005f/12f; //9.84f/12f;

  static public int allSegmentsCount = 0;

  static public class Web {
    public List<Radial> radials;
    public List<Segment> segments;
    public List<List<Segment>> rings;

    public float webx;
    public float weby;
    public float webz;
    public List<LXPoint> points;
    public int radialOffset;  // Used for correcting orientation.
    List<Float> radialDistances = new ArrayList<Float>();

    public Web(float webx, float weby, float webz, int numRadials, int radialOffset) {
      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      this.radialOffset = radialOffset;

      rings = new ArrayList<List<Segment>>();
      // Create the radial strands.  In the first pass, we just create a virtual edge to use as scaffolding for
      // building the segments.
      radials = createRadials(webx, weby, webz, numRadials);

      // Create the spiral strand segments
      if (ModelParams.getNumRings() == 0)
        segments = createSpiralSegments(webx, weby, webz);
      else segments = createRingSegments(webx, weby, webz, SEGMENT_MARGINS);

      // When radials are first created, we just create a virtual edge as a reference for building the
      // spiral segments.  Once the spiral segments are constructed, we will know the intersection points
      // so we can then construct the segmented radial edges.  Each radial is comprised of multiple segments
      // so that we can create our topological joints for patterns that want to use topology.
      for (Radial radial : radials) {
        radial.radialDistances = radialDistances;
        radial.initializeEdges();
      }
    }

    public List<Radial> createRadials(float mx, float my, float mz, int numRadials) {
      List<Radial> radials = new ArrayList<Radial>();
      float angleIncr = 360f / (numRadials);
      float curAngle = radialOffset * angleIncr;
      points = new ArrayList<LXPoint>();
      float innerRadius = ModelParams.getInnerRadius();
      float outerRadius = ModelParams.getOuterRadius();
      float hexInnerRadius = ModelParams.getHexInner();

      for (int i = 0; i < numRadials; i++) {
        logger.info("Creating Radial");
        Radial radial = new Radial(i, curAngle, innerRadius, outerRadius, hexInnerRadius, mx, my, mz);
        radials.add(radial);
        allRadials.add(radial);
        // TODO(tracy): We will initially create the Radials without points so that we have a scaffolding for
        // creating the spiral segments.
        //allPoints.addAll(radial.points);
        //points.addAll(radial.points);
        curAngle += angleIncr;
      }
      return radials;
    }

    public List<Segment> createRingSegments(float webx, float weby, float webz, float margins) {
      float curRadialDist = ModelParams.getInnerRadius();
      int startRadialId = 0;
      int endRadialId;
      segments = new ArrayList<Segment>();
      int numRadials = ModelParams.getRadials();
      int curRing = 0;
      float radialIncrement = (ModelParams.getOuterRadius() - ModelParams.getInnerRadius()) / (ModelParams.getNumRings() - 1);
      List<Segment> ringSegments = new ArrayList<Segment>();
      while (curRing < ModelParams.getNumRings()) {
        endRadialId = startRadialId + 1;
        if (endRadialId >= numRadials)
          endRadialId = 0;

        Segment segment = new Segment(allSegmentsCount, startRadialId, endRadialId, curRadialDist, true,
            margins + RING_SEG_MARGIN[curRing], webx, weby, webz);
        segments.add(segment);
        ringSegments.add(segment);
        allSegments.add(segment);
        allPoints.addAll(segment.points);
        points.addAll(segment.points);
        startRadialId++;

        if (startRadialId >= numRadials) {
          startRadialId = 0;
          curRing++;
          radialDistances.add(curRadialDist);
          curRadialDist += radialIncrement;
          rings.add(ringSegments);
          allRings.add(ringSegments);
          ringSegments = new ArrayList<Segment>();
        }
        allSegmentsCount++;
      }
      logger.info("Created " + segments.size() + " segments");
      return segments;
    }

    public List<Segment> createSpiralSegments(float webx, float weby, float webz) {
      // Iterate while current radial distance is less than outer radius.
      float curRadialDist = ModelParams.getInnerRadius();
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
        Segment segment = new Segment(allSegmentsCount, startRadialId, endRadialId, curRadialDist, false, 0f, webx, weby, webz);
        segments.add(segment);
        allSegments.add(segment);
        allPoints.addAll(segment.points);
        points.addAll(segment.points);
        startRadialId++;
        radialDistances.add(curRadialDist);

        if (startRadialId >= numRadials)
          startRadialId = 0;

        allSegmentsCount++;
        curRadialDist += ModelParams.getRadialIncr();
      }
      logger.info("Created " + segments.size() + " segments");
      return segments;
    }
  }

  static public class Radial {

    public Radial (int id, float angle, float innerRadius, float outerRadius, float hexInnerRadius, float webx, float weby, float webz) {
      this.id = id;
      this.angle = angle;
      this.innerRadius = innerRadius;
      this.outerRadius = outerRadius;
      this.hexInnerRadius = hexInnerRadius;
      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      //points = createPoints(mx, my, mz);
      // TODO(tracy): We need to compute a series of edges for each radial based on where the previously generated
      // rings intersect.  Those positions are passed in as radialDistances.
      virtualEdge = createVirtualEdge(webx, weby, webz);

      //edges.add(edge);

      //logger.info("Radial start x: " + edge.p1.x + " end x: " + edge.p2.x);
    }

    public void initializeEdges() {
      for (Float dist : radialDistances) {
        logger.info("Radial distance: " + radialDistances);
      }
      edges = createEdges(webx, weby, webz, radialDistances);
      points = new ArrayList<LXPoint>();
      for (Edge edge: edges) {
        points.addAll(edge.points);
        allPoints.addAll(edge.points);
      }
      logger.info("Added radial with " + points.size() + " points id: " + id);
    }


    int id;
    public float angle;
    public float innerRadius;
    public float hexInnerRadius;
    public float webx;
    public float weby;
    public float webz;

    public Edge virtualEdge;

    public List<Edge> edges;

    public float outerRadius;
    public List<LXPoint> points;
    public List<Float> radialDistances;

    // These can be computed from segment ID numbers.  For example, for radial 0, the segments are 0, 6, 12, 18, 24, 30, 36, 42, 48
    // for radial 1 it is 1, 7, 13, 19, etc...
    // for i in 0..8, add segId = i * 6 + radialId
    public List<Segment> ccwSegments;
    public List<Segment> cwSegments;

    public List<Edge> createEdges(float x, float y, float z, List<Float> radialDistances) {
      edges = new ArrayList<Edge>();
      Point3D radialStart = new Point3D(x + polarX(hexInnerRadius, angle),
                                        y,
                                        z + polarZ(hexInnerRadius, angle));
      Point3D unitVector = Point3D.unitVectorTo(new Point3D(polarX(innerRadius, angle), 0, polarZ(innerRadius, angle)),
          new Point3D(0, 0, 0));
      Point3D prevEdgeEnd = null;
      for (float curRadialDist : radialDistances) {
        if (prevEdgeEnd == null) {
          prevEdgeEnd = radialStart;
          continue;
        }
        Point3D edgeEnd = new Point3D(unitVector.x * curRadialDist,
            prevEdgeEnd.y,
            unitVector.z * curRadialDist);
        Edge edge = new Edge(prevEdgeEnd, edgeEnd, ModelParams.getLedsPerFoot(),0f);
        edges.add(edge);
        allEdges.add(edge);
        prevEdgeEnd = edgeEnd;
      }

      addCCWSegments();
      return edges;
    }

    /**
     * Creates a virtual edge along the radial.  We will create the virtual edges for the Radials first so that
     * we have a reference when constructing the spiral segments, but eventually we want each radial to be broken
     * up into multiple segments so that we can construct all the edge joints.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Edge createVirtualEdge(float x, float y, float z) {
      Point3D edgeA = new Point3D(x + polarX(hexInnerRadius, angle),
                                  y,
                                  z + polarZ(hexInnerRadius, angle));
      Point3D edgeB = new Point3D(x + polarX(outerRadius, angle),
                                  y,
                                     z + polarZ(outerRadius, angle));
      return new Edge(edgeA, edgeB, ModelParams.getLedsPerFoot(), 0f, true);
    }

    List<LXPoint> getPointsWireOrder() {
      return points;
    }

    public void addCCWSegments() {
      ccwSegments = new ArrayList<Segment>();
      for (int i = 0; i < 9; i++) {
        Segment segment = allSegments.get(i * 6 + id);
        ccwSegments.add(segment);
      }
    }
  }


  static public class Segment {

    public Segment (int id, int startRadialId, int endRadialId, float radialDist, boolean ring, float margins, float webx, float weby, float webz) {
      this.id = id;
      this.startRadialId = startRadialId;
      this.endRadialId = endRadialId;
      this.radialDist = radialDist;
      this.endRadialDist = radialDist + (ring?0f : ModelParams.getRadialIncr());
      this.margins = margins;

      this.webx = webx;
      this.weby = weby;
      this.webz = webz;
      edge = createEdge(webx, weby, webz);
      allEdges.add(edge);
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

    public float margins;

    public Edge edge;

    public List<LXPoint> points;

    public Edge createEdge(float x, float y, float z) {
      // The start point is radialDist along the start radial unit vector
      Radial startRadial = allRadials.get(startRadialId);
      Radial endRadial = allRadials.get(endRadialId);
      Point3D startPoint = new Point3D(x + startRadial.virtualEdge.unitVector.x * radialDist,
                                   y + startRadial.virtualEdge.unitVector.y * radialDist,
                                   z + startRadial.virtualEdge.unitVector.z * radialDist);
      Point3D endPoint = new Point3D(x + endRadial.virtualEdge.unitVector.x * endRadialDist,
                                     y + endRadial.virtualEdge.unitVector.y * endRadialDist,
                                     z + endRadial.virtualEdge.unitVector.z * endRadialDist);

      // TODO(tracy): Strips might have a minimum cut distance of every 9.84 inches.  We need to compute
      // the distances between the points of the segments, subtract our requested margins, and then compute
      // the greatest integer multiple of 9.84 that fits the remaining distance.
      float distance = startPoint.distanceTo(endPoint);
      float remDistance = distance - (2f* margins);  // remaining distance after margin removal
      int multiple = (int)(Math.floor(remDistance/MIN_CUT_DISTANCE));
      float spanDistance = multiple * MIN_CUT_DISTANCE;  // actual distance points can span accounting for cut distance
      float additionalMargins = remDistance - spanDistance;  // compute the additional margin padding to account for cut distance

      /*
      TODO(tracy): This looks pretty correct but it would be better to implement some more model validation code so
      that we don't get off-by-one or two issues with the physical build which will look terrible if it happens.  We should
      also have some functionality to adjust at run time so we can fix any build time issues.
      logger.info("min_cut_distance: " + MIN_CUT_DISTANCE);
      logger.info("distance: " + distance);
      logger.info("margins: " + margins);
      logger.info("rem_distance: " + remDistance);
      logger.info("multiple: " + multiple);
      logger.info("span distance: "+ spanDistance);
      logger.info("additional margins: " + additionalMargins);
       */

      return new Edge(startPoint, endPoint, ModelParams.getLedsPerFoot(), margins + additionalMargins/2f);
    }

    List<LXPoint> getPointsWireOrder() {
      return edge.getPointsWireOrder();
    }
  }

  static public List<Web> allWebs = new ArrayList<Web>();
  static public List<Radial> allRadials = new ArrayList<Radial>();
  static public List<Segment> allSegments = new ArrayList<Segment>();

  static public List<Edge> allEdges = new ArrayList<Edge>();

  static public List<List<Segment>> allRings = new ArrayList<List<Segment>>();

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

    Edge.computeAdjacentEdges(allEdges);

    logger.info("Number of logical edges: " + allEdges.size());
    logger.info("Number of ring edges: " + allSegments.size());
    logger.info("Number of points: " + allPoints.size());

    SpiderTrapModel m = new SpiderTrapModel(allPoints);

    float xRange = m.xMax - m.xMin;
    float zRange = m.zMax - m.zMin;
    float yRange = m.yMax - m.yMin;
    float largestRange = Math.max(xRange, zRange);

    // Shaders expect a 0 to 1 range in both X and Z.  Since X is our largest range, we use that as
    // the scaling factor for both X and Z so that we don't get aspect distortion.  We also need to offset
    // the z, aka w, coordinates to re-center the z coordinates in the 0..1 by 0..1 rendering range.
    float zOffset = 0.07f;
    zOffset = (1.0f - (zRange / xRange))/2f;
    // Compute normalized coordinates for all points for use in shaders.
    for (LXPoint lxp : allPoints) {
      ((LPPoint)lxp).u = (lxp.x - m.xMin)/largestRange;
      ((LPPoint)lxp).v = (lxp.y - m.yMin)/yRange;
      ((LPPoint)lxp).w = (lxp.z - m.zMin)/largestRange + zOffset;
    }
    return m;
  }

  public SpiderTrapModel(List<LXPoint> points) {
    super(points);
  }
}
