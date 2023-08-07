package art.lookingup.linear;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Edge {
  private static final Logger logger = Logger.getLogger(Edge.class.getName());

  static public int edgeCounter = 0;

  static public int virtualEdgeCounter = 10000;


  // Edge id's are globally unique identifiers.  We use them in various places when dealing with the
  // topology.
  public int id;

  public Point3D p1;
  public Point3D p2;

  public Point3D pb1;
  public Point3D pb2;

  public Point3D midp;

  public Point3D deltaVector;
  public Point3D unitVector;
  public float deltaLength;

  public List<LPPoint> strip1;
  public List<LPPoint> points;

  public LinearPoints linearPoints;

  // Each edge can be connected up to 3 adjacent edges.  Edges on the outer or inner boundaries can have fewer
  // connections.
  public Joint[] myStartPointJoints = new Joint[3];
  public Joint[] myEndPointJoints = new Joint[3];

  static public float margins = 0f;

  public Edge(Point3D p1, Point3D p2, float pointSpacing, float margins) {
    this(p1, p2, pointSpacing, margins, false);
  }

  public Edge(Point3D p1, Point3D p2, float pointSpacing, float margins,  boolean virtual) {
    this.p1 = new Point3D(p1);
    this.p2 = new Point3D(p2);
    deltaVector = Point3D.delta(p2, p1);
    deltaLength = deltaVector.length();
    unitVector = Point3D.unitVectorTo(p2, p1);
    this.margins = margins;
    points = new ArrayList<LPPoint>();

    // Virtual edges are edges without actual points.
    if (!virtual) {
      this.id = edgeCounter;
      edgeCounter++;
      // The LinearPoints object's ID is inherited from the associated Edge.
      linearPoints = new LinearPoints(this.id, deltaLength, pointSpacing, this, this.p1, this.p2, margins);
      strip1 = linearPoints.points;
      points.addAll(strip1);
    } else {
      // Virtual edges have a separate global ID space.
      this.id = virtualEdgeCounter;
      virtualEdgeCounter++;
    }

    midp = new Point3D((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f, (p1.z + p2.z) / 2f);
    pb1 = new Point3D(p1.x , p1.y , p1.z);
    pb2 = new Point3D(p2.x , p2.y , p2.z);
  }

  public List<LXPoint> getPointsWireOrder() {
    List<LXPoint> pts = new ArrayList<LXPoint>();
    pts.addAll(points);
    return pts;
  }

  public Point3D midpoint() {
    return midp;
  }

  public void interpolate(Point3D result, float t) {
    result.x = p1.x + t * (p2.x - p1.x);
    result.y = p1.y + t * (p2.y - p1.y);
    result.z = p1.z + t * (p2.z - p1.z);
  }

  public void translate(float x, float y, float z) {
    p1.x += x;
    p1.y += y;
    p1.z += z;
    p2.x += x;
    p2.y += y;
    p2.z += z;
    midp.x += x;
    midp.y += y;
    midp.z += z;
    for (LXPoint p : points) {
      p.x += x;
      p.y += y;
      p.z += z;
    }
  }

  public void scale(float scale) {
    p1.x *= scale;
    p1.y *= scale;
    p1.z *= scale;
    p2.x *= scale;
    p2.y *= scale;
    p2.z *= scale;
    midp.x *= scale;
    midp.y *= scale;
    midp.z *= scale;
    for (LXPoint p : points) {
      p.x *= scale;
      p.y *= scale;
      p.z *= scale;
    }
  }


  public void rotateZAxis(float angle) {
    p1.rotateZAxis(angle);
    p2.rotateZAxis(angle);
    pb1.rotateZAxis(angle);
    pb2.rotateZAxis(angle);
    midp.rotateZAxis(angle);
    for (LXPoint p : points) {
      Point3D p3d = new Point3D(p.x, p.y, p.z);
      p3d.rotateZAxis(angle);
      p.x = p3d.x;
      p.y = p3d.y;
      p.z = p3d.z;
    }
  }

  public void rotateYAxis(float angle) {
    p1.rotateYAxis(angle);
    p2.rotateYAxis(angle);
    pb1.rotateYAxis(angle);
    pb2.rotateYAxis(angle);
    midp.rotateYAxis(angle);
    for (LXPoint p : points) {
      Point3D p3d = new Point3D(p.x, p.y, p.z);
      p3d.rotateYAxis(angle);
      p.x = p3d.x;
      p.y = p3d.y;
      p.z = p3d.z;
    }
  }

  public void rotateXAxis(float angle) {
    p1.rotateXAxis(angle);
    p2.rotateXAxis(angle);
    pb1.rotateXAxis(angle);
    pb2.rotateXAxis(angle);
    midp.rotateXAxis(angle);
    for (LXPoint p : points) {
      Point3D p3d = new Point3D(p.x, p.y, p.z);
      p3d.rotateXAxis(angle);
      p.x = p3d.x;
      p.y = p3d.y;
      p.z = p3d.z;
    }
  }

  static public final float adjacencyDistance = 2f/12f;

  public int isEdgeAdjacentStart(Edge otherEdge) {
    if (p1.distanceTo(otherEdge.p1) < adjacencyDistance)
      return 1;
    if (p1.distanceTo(otherEdge.p2) < adjacencyDistance)
      return 2;
    return 0;
  }

  public int isEdgeAdjacentEnd(Edge otherEdge) {
    if (p2.distanceTo(otherEdge.p1) < adjacencyDistance)
      return 1;
    if (p2.distanceTo(otherEdge.p2) < adjacencyDistance)
      return 2;
    return 0;
  }



  public static void computeAdjacentEdges(List<Edge> edges) {
    for (Edge thisEdge : edges) {
      int currentStartJointNum = 0;
      int currentEndJointNum = 0;
      for (Edge otherEdge : edges) {
        if (thisEdge.id == otherEdge.id)
          continue;
        int adjacentValue = thisEdge.isEdgeAdjacentStart(otherEdge);
        if (adjacentValue == 1) {
          thisEdge.myStartPointJoints[currentStartJointNum++] = new Joint(otherEdge, true);
          //logger.info("start of edge " + thisEdge.id + " adjacent to start of " + otherEdge.id);
        } else if (adjacentValue == 2) {
          thisEdge.myStartPointJoints[currentStartJointNum++] = new Joint(otherEdge, false);
          //logger.info("start of edge " + thisEdge.id + " adjacent to end of " + otherEdge.id);
        }
        adjacentValue = thisEdge.isEdgeAdjacentEnd(otherEdge);
        if (adjacentValue == 1) {
          thisEdge.myEndPointJoints[currentEndJointNum++] = new Joint(otherEdge, true);
          //logger.info("end of edge " + thisEdge.id + " adjacent to start of " + otherEdge.id);
        } else if (adjacentValue == 2) {
          thisEdge.myEndPointJoints[currentEndJointNum++] = new Joint(otherEdge, false);
          //logger.info("end of edge " + thisEdge.id + " adjacent to end of " + otherEdge.id);
        }
      }
    }
  }
}
