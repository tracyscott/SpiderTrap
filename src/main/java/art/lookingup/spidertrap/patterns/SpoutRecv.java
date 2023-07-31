package art.lookingup.spidertrap.patterns;

import art.lookingup.spidertrap.SpiderTrapApp;
import heronarts.lx.LX;
import processing.core.PImage;
import spout.Spout;

import java.util.logging.Logger;

import static processing.core.PConstants.P2D;

public class SpoutRecv extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(Tracers.class.getName());

  Spout spout;
  PImage pImg;

  public SpoutRecv(LX lx) {
    super(lx, "");
    spout = new Spout(SpiderTrapApp.pApplet);
  }


  static boolean hasSaved = false;

  @Override
  protected void draw(double deltaDrawMs) {
    pImg = spout.receiveImage(pImg);
    if (pImg == null) return;
    pg.background(pImg);
  }
}
