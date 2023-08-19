package art.lookingup.spidertrap.patterns;


import art.lookingup.spidertrap.Output;
import art.lookingup.util.Gamma;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.util.List;

public class PixelKiller extends LXEffect {

  DiscreteParameter outa = new DiscreteParameter("out_a", -1, -1, 32);
  DiscreteParameter posas = new DiscreteParameter("a_strt", -1, -1, 400);
  DiscreteParameter posae = new DiscreteParameter("a_end", -1, -1, 400);

  DiscreteParameter outb = new DiscreteParameter("out_b", -1, -1, 32);
  DiscreteParameter posbs = new DiscreteParameter("b_strt", -1, -1, 400);
  DiscreteParameter posbe = new DiscreteParameter("b_end", -1, -1, 400);

  DiscreteParameter outc = new DiscreteParameter("out_c", -1, -1, 32);
  DiscreteParameter poscs = new DiscreteParameter("c_strt", -1, -1, 400);
  DiscreteParameter posce = new DiscreteParameter("c_end", -1, -1, 400);

  DiscreteParameter outd = new DiscreteParameter("out_d", -1, -1, 32);
  DiscreteParameter posds = new DiscreteParameter("d_strt", -1, -1, 400);
  DiscreteParameter posde = new DiscreteParameter("d_end", -1, -1, 400);

  DiscreteParameter oute = new DiscreteParameter("out_e", -1, -1, 32);
  DiscreteParameter poses = new DiscreteParameter("e_strt", -1, -1, 400);
  DiscreteParameter posee = new DiscreteParameter("e_end", -1, -1, 400);


  public PixelKiller(LX lx) {
    super(lx);
    addParameter("out_a", outa);
    addParameter("a_strt", posas);
    addParameter("a_end", posae);

    addParameter("out_b", outb);
    addParameter("b_strt", posbs);
    addParameter("b_end", posbe);

    addParameter("out_c", outc);
    addParameter("c_strt", poscs);
    addParameter("c_end", posce);

    addParameter("out_d", outd);
    addParameter("d_strt", posds);
    addParameter("d_end", posde);

    addParameter("out_e", oute);
    addParameter("e_strt", poses);
    addParameter("e_end", posee);
  }

  public void run(double deltaMs, double damping) {
    for (int output = 0; output < 32; output++) {
      List<LXPoint> outputPoints = Output.allOutputsPoints.get(output);
      for (int pixel = 0; pixel < outputPoints.size(); pixel++) {
        if (output == outa.getValuei()) {
          if (pixel >= posas.getValuei() && pixel <= posae.getValuei()) {
            colors[outputPoints.get(pixel).index] = LXColor.BLACK;
          }
        }
        if (output == outb.getValuei()) {
          if (pixel >= posbs.getValuei() && pixel <= posbe.getValuei()) {
            colors[outputPoints.get(pixel).index] = LXColor.BLACK;
          }
        }
        if (output == outc.getValuei()) {
          if (pixel >= poscs.getValuei() && pixel <= posce.getValuei()) {
            colors[outputPoints.get(pixel).index] = LXColor.BLACK;
          }
        }
        if (output == outd.getValuei()) {
          if (pixel >= posds.getValuei() && pixel <= posde.getValuei()) {
            colors[outputPoints.get(pixel).index] = LXColor.BLACK;
          }
        }
        if (output == oute.getValuei()) {
          if (pixel >= poses.getValuei() && pixel <= posee.getValuei()) {
            colors[outputPoints.get(pixel).index] = LXColor.BLACK;
          }
        }
      }
    }
  }
}
