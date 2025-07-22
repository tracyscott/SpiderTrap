package art.lookingup.spidertrap;

import art.lookingup.spidertrap.ui.UIPixliteConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.ArtSyncDatagram;
import heronarts.lx.output.LXBufferOutput;
import heronarts.lx.output.LXDatagram;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static int artnetPort = 6454;

  static public List<List<LXPoint>> allOutputsPoints = new ArrayList<List<LXPoint>>();

  // We keep track of these so we can restart the network.  There is no way to get a list of children from
  // LXOutputGroup which is the class for lx.engine.output.
  static public List<ArtNetDatagram> outputDatagrams = new ArrayList<ArtNetDatagram>();
  static public LXDatagram artSyncDatagram;


  public static void configureUnityArtNet(LX lx) {
    String unityIpAddress = "127.0.0.1";
    logger.log(Level.INFO, "Using ArtNet: " + unityIpAddress + ":" + artnetPort);

    List<LXPoint> points = lx.getModel().getPoints();
    int numUniverses = (int)Math.ceil(((double)points.size())/170.0);
    logger.info("Num universes: " + numUniverses);
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int totalPointsOutput = 0;

    for (int univNum = 0; univNum < numUniverses; univNum++) {
      int[] dmxChannelsForUniverse = new int[170];
      for (int i = 0; i < 170 && totalPointsOutput < points.size(); i++) {
        LXPoint p = points.get(univNum*170 + i);
        dmxChannelsForUniverse[i] = p.index;
        totalPointsOutput++;
      }

      ArtNetDatagram artnetDatagram = new ArtNetDatagram(lx, dmxChannelsForUniverse, univNum);
      try {
        artnetDatagram.setAddress(InetAddress.getByName(unityIpAddress)).setPort(artnetPort);
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "Configuring ArtNet: " + unityIpAddress, uhex);
      }
      datagrams.add(artnetDatagram);
    }

    for (ArtNetDatagram dgram : datagrams) {
      lx.engine.addOutput(dgram);
    }
  }

  List<LXPoint> output1Points = new ArrayList<LXPoint>();
  List<LXPoint> output2Points = new ArrayList<LXPoint>();
  List<LXPoint> output3Points = new ArrayList<LXPoint>();
  List<LXPoint> output4Points = new ArrayList<LXPoint>();
  List<List<LXPoint>> allOutputPoints = new ArrayList<List<LXPoint>>();


  /**
   *
   *
   * @param lx
   */
  public static void configurePixliteOutputGeneric(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    String artNetIpAddress = SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_IP).getString();
    int artNetIpPort = Integer.parseInt(SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_PORT).getString());
    logger.log(Level.INFO, "Using Pixlite ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    int universesPerOutput = 2;
    int numUniverses = 4;
    /**
     * Each mushroom will have 280 leds.  So we should take first 17 gills and
     * put them in one universe and then add 11 additional gills of 10 leds into an additional universe.
     */
    List<LXPoint> workingPoints = new ArrayList<LXPoint>();

    //
    // ----- Web 1 ------
    //
    for (int radialNum = 0; radialNum < 17; radialNum++) {
      workingPoints.addAll(SpiderTrapModel.allRadials.get(radialNum).getPointsWireOrder());
    }
    int[] dmxChannelsForUniverse = new int[170];
    for (int i = 0; i < 170; i++) {
      dmxChannelsForUniverse[i] = workingPoints.get(i).index;
    }
    ArtNetDatagram artnetDatagram = new ArtNetDatagram(lx, dmxChannelsForUniverse, 0);
    try {
      artnetDatagram.setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artnetPort);
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress, uhex);
    }
    datagrams.add(artnetDatagram);

    workingPoints = new ArrayList<LXPoint>();
    // The next 17 gills on the second half of the mushroom go into the third universe (artnet univ 2)
    for (int radialNum = 17; radialNum < 28; radialNum++) {
      workingPoints.addAll(SpiderTrapModel.allRadials.get(radialNum).getPointsWireOrder());
    }
    dmxChannelsForUniverse = new int[110];
    for (int i = 0; i < 110; i++) {
      dmxChannelsForUniverse[i] = workingPoints.get(i).index;
    }
    artnetDatagram = new ArtNetDatagram(lx, dmxChannelsForUniverse, 1);
    try {
      artnetDatagram.setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artnetPort);
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress, uhex);
    }
    datagrams.add(artnetDatagram);


    // Add all datagrams to the output.
    for (ArtNetDatagram dgram : datagrams) {
      lx.engine.addOutput(dgram);
    }
    try {
      lx.engine.addOutput(new ArtSyncDatagram(lx).setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort));
    } catch (UnknownHostException unhex) {
      logger.info("Uknown host exception for Pixlite IP: " + artNetIpAddress + " msg: " + unhex.getMessage());
    }

    allOutputsPoints.clear();
    outputDatagrams.clear();
  }

  /**
   * Output for baby web.
   * 1- Rings 1, 2, 3
   * 2- Rings 4, 5
   * 3- Ring 6
   */

  static public void configurePixliteOutputBabyWeb(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    String artNetIpAddress = SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_IP).getString();
    int artNetIpPort = Integer.parseInt(SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_PORT).getString());
    logger.log(Level.INFO, "Using Pixlite ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    int universesPerOutput = 3;

    allOutputsPoints.clear();
    outputDatagrams.clear();

    for (int outputNum = 0; outputNum < 32; outputNum++) {
      List<LXPoint> outputPoints = new ArrayList<LXPoint>();
      allOutputsPoints.add(outputPoints);

      List<LXPoint> pointsWireOrder = new ArrayList<LXPoint>();
      // Output Number is 1 based in the UI.
      logger.info("========== PIXLITE OUTPUT #" + (outputNum + 1) + "     ==============");

      if (outputNum == 0) {
        // Rings 1, 2, 3
        List<List<SpiderTrapModel.Segment>> outRings = new ArrayList<List<SpiderTrapModel.Segment>>();
        outRings.add(SpiderTrapModel.allRings.get(0));
        outRings.add(SpiderTrapModel.allRings.get(1));
        outRings.add(SpiderTrapModel.allRings.get(2));

        for (List<SpiderTrapModel.Segment> ring : outRings) {
          for (SpiderTrapModel.Segment ringSegment : ring) {
            pointsWireOrder.addAll(ringSegment.getPointsWireOrder());
          }
        }
      } else if (outputNum == 1) {
        // Rings 4, 5
        List<List<SpiderTrapModel.Segment>> outRings = new ArrayList<List<SpiderTrapModel.Segment>>();
        outRings.add(SpiderTrapModel.allRings.get(3));
        outRings.add(SpiderTrapModel.allRings.get(4));
        for (List<SpiderTrapModel.Segment> ring : outRings) {
          for (SpiderTrapModel.Segment ringSegment : ring) {
            pointsWireOrder.addAll(ringSegment.getPointsWireOrder());
          }
        }
      } else if (outputNum == 2) {
        // Ring 6
        List<List<SpiderTrapModel.Segment>> outRings = new ArrayList<List<SpiderTrapModel.Segment>>();
        outRings.add(SpiderTrapModel.allRings.get(5));
        for (List<SpiderTrapModel.Segment> ring : outRings) {
          for (SpiderTrapModel.Segment ringSegment : ring) {
            pointsWireOrder.addAll(ringSegment.getPointsWireOrder());
          }
        }
      } else if (outputNum == 3) {
        // Radials
        int radialNum = 0;
        for (SpiderTrapModel.Radial radial : SpiderTrapModel.allRadials) {
          List<LXPoint> tempPoints = new ArrayList<LXPoint>();
          tempPoints.addAll(radial.getPointsWireOrder());
          if (radialNum % 2 == 1)
            Collections.reverse(tempPoints);
          pointsWireOrder.addAll(tempPoints);
        }
      }

      outputPoints.addAll(pointsWireOrder);

      int rgbwLedsPerUniverse = (int) (170f*(3f/4f)) + 1;
      int numUniversesThisWire = (int) Math.ceil((float) (pointsWireOrder.size()) / rgbwLedsPerUniverse);
      int univStartNum = outputNum * universesPerOutput;
      int lastUniverseCount = pointsWireOrder.size() - rgbwLedsPerUniverse * (numUniversesThisWire - 1);
      int maxLedsPerUniverse = ((pointsWireOrder.size())>rgbwLedsPerUniverse)?rgbwLedsPerUniverse:pointsWireOrder.size();
      int[] thisUniverseIndices = new int[maxLedsPerUniverse];
      int curIndex = 0;
      int curUnivOffset = 0;
      for (LXPoint pt : pointsWireOrder) {
        thisUniverseIndices[curIndex] = pt.index;
        curIndex++;
        if (curIndex == maxLedsPerUniverse || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
          logger.log(Level.INFO, "Adding datagram: for: PixLite universe=" + (univStartNum + curUnivOffset + 1) + " ArtNet universe=" + (univStartNum + curUnivOffset) + " points=" + curIndex);
          ArtNetDatagram datagram = new ArtNetDatagram(lx, thisUniverseIndices, LXBufferOutput.ByteOrder.RGBW, univStartNum + curUnivOffset);
          try {
            datagram.setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
          } catch (UnknownHostException uhex) {
            logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
          }
          datagrams.add(datagram);
          curUnivOffset++;
          curIndex = 0;
          if (curUnivOffset == numUniversesThisWire - 1) {
            thisUniverseIndices = new int[lastUniverseCount];
          } else {
            thisUniverseIndices = new int[maxLedsPerUniverse];
          }
        }
      }
    }
    for (ArtNetDatagram dgram : datagrams) {
      lx.engine.addOutput(dgram);
      outputDatagrams.add(dgram);
    }

    try {
      artSyncDatagram = new ArtSyncDatagram(lx).setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
      lx.engine.addOutput(artSyncDatagram);
    } catch (UnknownHostException unhex) {
      logger.info("Uknown host exception for Pixlite IP: " + artNetIpAddress + " msg: " + unhex.getMessage());
    }

  }

  /**
   * General description.
   * Per triangle.  This is repeated 6 times.
   *
   * First output:  Radial inwards and then T1 counter-clockwise, aka left and then T2 clockwise aka right.
   * Second output: T5, T4, T3.  T5 is CCW aka left, T4 is CW aka right, T3 CCW aka left
   * Third output: T7, T6.  T7 is CCW aka left and T6 is CW aka right.
   * Fourth output: T9, T8.  T9 is CCW aka left and T8 is CW aka right.
   *
   * Note: Segment edges (not radials) are always counter clockwise.  Radial segments are in to out.
   * @param lx
   */
  static public void configurePixliteOutputBurningMan(LX lx) {
    // For each radial, work on the radial and then work on all the CCW segments attached to the radial.
    // TODO(tracy):  For each radial, create a mapping to CCW segment and CW segment, regardless of topology Edge/Joints.
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    String artNetIpAddress = SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_IP).getString();
    int artNetIpPort = Integer.parseInt(SpiderTrapApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_PORT).getString());
    logger.log(Level.INFO, "Using Pixlite ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    int universesPerOutput = 3;

    allOutputsPoints.clear();
    outputDatagrams.clear();

    for (int outputNum = 0; outputNum < 32; outputNum++) {
      List<LXPoint> outputPoints = new ArrayList<LXPoint>();
      allOutputsPoints.add(outputPoints);

      List<LXPoint> pointsWireOrder = new ArrayList<LXPoint>();
      // Output Number is 1 based in the UI.
      String mapping = SpiderTrapApp.outputMap.getOutputMapping(outputNum + 1);
      logger.info("========== PIXLITE OUTPUT #" + (outputNum + 1) + "     ==============");

      logger.info("mapping=" + mapping);
      // Allow multiple components per output.  With a 1:1 mapping we are fully utilizing each long range receiver
      // so there is no room for future expansion.
      String[] components = mapping.split(",");

      for (int ci = 0; ci < components.length; ci++) {
        String ledSource = components[ci].trim();
        // Each component should be of the form t1.*something* where *something* can be r, t1 ... t9
        if ("".equals(ledSource)) continue;
        logger.info("Parsing: " + ledSource);
        String[] pieces = ledSource.split("\\.");
        if (pieces.length < 2) continue;
        int triangleNum = Integer.parseInt(pieces[0].substring(1));
        // By default, use edgeNum -1 to represent the radial leds.
        int edgeNum = -1;
        // For related edges, they are always ccw
        SpiderTrapModel.Radial radial = SpiderTrapModel.allRadials.get(triangleNum - 1);
        if (pieces[1].contains("t")) edgeNum = Integer.parseInt(pieces[1].substring(1));
        logger.info("  Triangle #" + triangleNum + " part: " + edgeNum);
        if (edgeNum == -1) {
          List<LXPoint> points = radial.points;
          List<LXPoint> reversedPoints = new ArrayList<LXPoint>();
          reversedPoints.addAll(points);
          Collections.reverse(reversedPoints);
          pointsWireOrder.addAll(reversedPoints);
        } else {
          List<LXPoint> points = new ArrayList<LXPoint>();
          points.addAll(radial.ccwSegments.get(edgeNum - 1).points);
          if (edgeNum == 2 || edgeNum == 4 || edgeNum == 6 || edgeNum ==8) {
            Collections.reverse(points);
          }
          pointsWireOrder.addAll(points);
        }
      }

      outputPoints.addAll(pointsWireOrder);

      int numUniversesThisWire = (int) Math.ceil((float) pointsWireOrder.size() / 170f);
      int univStartNum = outputNum * universesPerOutput;
      int lastUniverseCount = pointsWireOrder.size() - 170 * (numUniversesThisWire - 1);
      int maxLedsPerUniverse = (pointsWireOrder.size()>170)?170:pointsWireOrder.size();
      int[] thisUniverseIndices = new int[maxLedsPerUniverse];
      int curIndex = 0;
      int curUnivOffset = 0;
      for (LXPoint pt : pointsWireOrder) {
        thisUniverseIndices[curIndex] = pt.index;
        curIndex++;
        if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
          logger.log(Level.INFO, "Adding datagram: for: " + mapping + " PixLite universe=" + (univStartNum + curUnivOffset + 1) + " ArtNet universe=" + (univStartNum + curUnivOffset) + " points=" + curIndex);
          ArtNetDatagram datagram = new ArtNetDatagram(lx, thisUniverseIndices, univStartNum + curUnivOffset);
          try {
            datagram.setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
          } catch (UnknownHostException uhex) {
            logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
          }
          datagrams.add(datagram);
          curUnivOffset++;
          curIndex = 0;
          if (curUnivOffset == numUniversesThisWire - 1) {
            thisUniverseIndices = new int[lastUniverseCount];
          } else {
            thisUniverseIndices = new int[maxLedsPerUniverse];
          }
        }
      }
    }
    for (ArtNetDatagram dgram : datagrams) {
      lx.engine.addOutput(dgram);
      outputDatagrams.add(dgram);
    }

    try {
      artSyncDatagram = new ArtSyncDatagram(lx).setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
      lx.engine.addOutput(artSyncDatagram);
    } catch (UnknownHostException unhex) {
      logger.info("Uknown host exception for Pixlite IP: " + artNetIpAddress + " msg: " + unhex.getMessage());
    }

    //
    // FLOODS
    //
    String floodIpAddress = SpiderTrapApp.pixliteConfig.floodIp();
    int floodPort = SpiderTrapApp.pixliteConfig.floodPort();
    logger.log(Level.INFO, "Using Flood ArtNet: " + floodIpAddress + ":" + floodPort);

    // Output floods
    int[] thisUniverseIndices = new int[SpiderTrapModel.floods.size()];
    int curIndex = 0;
    for (LXPoint p : SpiderTrapModel.floods) {
      thisUniverseIndices[curIndex] = p.index;
      curIndex++;
    }

    ArtNetDatagram floodDatagram = new ArtNetDatagram(lx, thisUniverseIndices, 0);
    try {
      floodDatagram.setAddress(InetAddress.getByName(floodIpAddress)).setPort(floodPort);
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Configuring Flood ArtNet: " + floodIpAddress + ":" + floodPort, uhex);
    }
    lx.engine.addOutput(floodDatagram);
  }


  static public void restartOutput(LX lx) {
    boolean originalEnabled = lx.engine.output.enabled.getValueb();
    lx.engine.output.enabled.setValue(false);
    for (ArtNetDatagram dgram : outputDatagrams) {
      lx.engine.output.removeChild(dgram);
    }
    lx.engine.output.removeChild(artSyncDatagram);
    //configurePixliteOutputBurningMan(lx);
    configurePixliteOutputBabyWeb(lx);
    lx.engine.output.enabled.setValue(originalEnabled);
  }
}
