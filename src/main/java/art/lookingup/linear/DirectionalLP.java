package art.lookingup.linear;

import art.lookingup.spidertrap.SpiderTrapModel;

import java.util.concurrent.ThreadLocalRandom;

public class DirectionalLP {
  public DirectionalLP(int lpNum, boolean forward) {
    int edgeNum = lpNum;
    Edge edge = SpiderTrapModel.allEdges.get(edgeNum);
    lp = edge.linearPoints;
    this.forward = forward;
    disableRender = false;
  }

  public LinearPoints lp;
  public boolean forward;
  public boolean disableRender;


  public DirectionalLP chooseNextBar(int jointSelector) {
    if (forward) {
      return chooseBarFromJoints(lp.edge, this.forward, lp.edge.myEndPointJoints, jointSelector);
    } else {
      return chooseBarFromJoints(lp.edge, this.forward, lp.edge.myStartPointJoints, jointSelector);
    }
  }

  public DirectionalLP choosePrevBar(int jointSelector) {
    if (forward) {
      return chooseBarFromJoints(lp.edge, this.forward, lp.edge.myStartPointJoints, jointSelector);
    } else {
      return chooseBarFromJoints(lp.edge, this.forward, lp.edge.myEndPointJoints, jointSelector);
    }
  }

  /**
   * Given an array of joints, select the next light bar.
   *
   * @param joints        Array of joints to select from.
   * @param jointSelector Which joint to select the next bar from.  If -1, then choose a random joint.
   */
  static public DirectionalLP chooseBarFromJoints(Edge thisEdge, boolean thisForward, Joint[] joints, int jointSelector) {
    int jointNum = jointSelector;
    if (jointNum == -1)
      jointNum = ThreadLocalRandom.current().nextInt(joints.length);
    Edge nextEdge;
    if (jointSelector < 3 && joints[jointNum] != null) nextEdge = joints[jointNum].edge;
    else nextEdge = thisEdge;
    DirectionalLP dlb;
    if (joints[jointNum] != null)
      dlb = new DirectionalLP(nextEdge.linearPoints.lpNum, joints[jointNum].isAdjacentEdgeAStartPoint);
    else
      dlb = new DirectionalLP(nextEdge.linearPoints.lpNum, true);
    return dlb;
  }

  public float computeNextBarPos(float pos, DirectionalLP nextBar) {
    float distanceToJoint = 1.0f - pos;
    if (!forward) {
      distanceToJoint = pos;
    }
    if (nextBar.forward) {
      return -distanceToJoint;
    } else {
      return 1.0f + distanceToJoint;
    }
  }

  public float computePrevBarPos(float pos, DirectionalLP prevBar) {
    // For the previous bar, in the straightforward case, the distance to this joint will be the current
    // position on the bar since the joint will be at 0.0.  If the current bar is not forward, the position
    // at the joint with the previous bar (bar is too the left) is actually 1.0 so the distance is 1.0 - pos.
    float distanceToJoint = pos;
    if (!forward) {
      distanceToJoint = 1.0f - pos;
    }
    // If the previous bar is oriented normally, then off to the right will be 1.0 + distance.
    // If the previous bar is backwards, then off to the right will be 0 - distance.
    if (prevBar.forward) {
      return 1.0f + distanceToJoint;
    } else {
      return -distanceToJoint;
    }
  }
}
