package art.lookingup.linear;

import java.util.ArrayList;
import java.util.List;

public class Joint {
  public Joint(Edge e, boolean isStartPoint) {
    edge = e;
    isAdjacentEdgeAStartPoint = isStartPoint;
  }

  public Point3D getJointPt() {
    if (isAdjacentEdgeAStartPoint)
      return edge.p1;
    else
      return edge.p2;
  }

  public Point3D getFarPt() {
    if (isAdjacentEdgeAStartPoint)
      return edge.p2;
    else
      return edge.p1;
  }

  /**
   * Given an array of joints and a Point3D, return a sorted list where the joints are sorted
   * by the distance to their farPt() (i.e. not joint point) to the given point).  The two edges on
   * the outside of a joint are the closest to the reference edge.  The edges connected to the interior
   * of the joint we have their far points a greater distance from the far point of the reference edge.
   *
   * @param joints
   * @param pt
   * @return
   */
  static public List<Joint> sortByDistanceFrom(Joint[] joints, Point3D pt) {
    List<Joint> sortedJoints = new ArrayList<Joint>();
    for (int i = 0; i < joints.length; i++) {
      boolean inserted = false;
      for (int j = 0; j < sortedJoints.size(); j++) {
        if (joints[i].getFarPt().distanceTo(pt) < sortedJoints.get(j).getFarPt().distanceTo(pt)) {
          sortedJoints.add(j, joints[i]);
          inserted = true;
          break;
        }
      }
      if (!inserted) sortedJoints.add(joints[i]);
    }
    return sortedJoints;
  }

  public Edge edge;
  public boolean isAdjacentEdgeAStartPoint;
  public float sortAngle;
}
