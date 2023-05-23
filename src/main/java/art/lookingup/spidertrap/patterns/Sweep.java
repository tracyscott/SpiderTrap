package art.lookingup.spidertrap.patterns;

import art.lookingup.util.EaseUtil;
import art.lookingup.colors.Colors;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

@LXCategory(LXCategory.FORM)
public class Sweep extends FPSPattern {
  CompoundParameter speed = new CompoundParameter("speed", 1f, 0f, 30f).setDescription("Sweep speed");
  CompoundParameter angleWidth = new CompoundParameter("angleW", 45, 0, 360).setDescription("Angle width");
  CompoundParameter bgintensity = new CompoundParameter("bgi", 0, 0, 1 ).setDescription("Background Intensity");
  ColorParameter color = new ColorParameter("color");
  CompoundParameter maxIntensity = new CompoundParameter("maxi", 1f, 0f, 1f).setDescription("Max intensity");

  CompoundParameter sparkle = new CompoundParameter("sparkle", 0,0,1).setDescription("Sparkle Effect");
  BooleanParameter usePal = new BooleanParameter("usePal", false);
  DiscreteParameter easeParam = new DiscreteParameter("ease", 0, 0, EaseUtil.MAX_EASE + 1);
  DiscreteParameter swatch = new DiscreteParameter("swatch", 0, 0, 20);
  CompoundParameter perlinFreq = new CompoundParameter("perlFreq", 1f, 0f, 20f);
  CompoundParameter sinFreq = new CompoundParameter("sinFreq", 1f, 0f, 10f).setDescription("Freq for sine easing");
  CompoundParameter palStrt = new CompoundParameter("palStrt", 0f, 0f, 1f).setDescription("Palette start point");
  BooleanParameter rev1 = new BooleanParameter("rev1", false);
  BooleanParameter rev2 = new BooleanParameter("rev2", false);

  EaseUtil ease = new EaseUtil(0);
  float currentAngle = 0f;
  float currentAngle2 = 0f;

  public Sweep(LX lx) {
    super(lx);
    addParameter("fps", fpsKnob);
    addParameter("speed", speed);
    addParameter("angleW", angleWidth);
    addParameter("bgi", bgintensity);
    addParameter("maxi", maxIntensity);
    addParameter("sparkle",sparkle);
    addParameter("color", color);
    addParameter("usePal", usePal);
    addParameter("palStrt", palStrt);
    addParameter("ease", easeParam);
    addParameter("swatch", swatch);
    addParameter("perlFreq", perlinFreq);
    addParameter("sinFreq", sinFreq);
    addParameter("rev1", rev1);
    addParameter("rev2", rev2);

    color.brightness.setValue(100.0);
  }

  public void onActive() {
    super.onActive();
    currentAngle = 0f;
    currentAngle2 = 0f;
    if (rev2.isOn())
      currentAngle2 = 180f;
  }

  public float angle(LXPoint p, int broken) {
    return 360f * (float)(p.azimuth/(Math.PI * 2f));
  }

  public float webAngle(LXPoint p, SpiderTrapModel.Radial radial) {
    return radial.angle;
  }

  /**
   * Return a color based on t value. This function will apply easing the value of t.
   * If usePal is on, it will lookup the color based on eased T, otherwise it uses the
   * configured color.  Brightness reduction is also applied based on eased T.
   * @param t
   * @return
   */
  public int getColor(float t) {
    int clr = color.getColor();
    float easedT = ease.ease(t);
    if (usePal.getValueb()) {
      if (t < palStrt.getValuef())
        t = palStrt.getValuef();
      clr = Colors.getParameterizedPaletteColor(lx, swatch.getValuei(), t, ease);
    }
    if (easedT < bgintensity.getValuef())
      easedT = bgintensity.getValuef();
    clr = Colors.getWeightedColor(clr, easedT);

    return clr;
  }

  public boolean isinRange(float pointAngle) {
    if ( pointAngle > currentAngle - angleWidth.getValuef() /2f && pointAngle < currentAngle + angleWidth.getValue()/ 2f)
      return true;
    float overlap = currentAngle + angleWidth.getValuef() / 2f- 360f;
    if ( overlap > 0f)
      if (pointAngle < overlap)
        return true;
    float underlap = currentAngle - angleWidth.getValuef()/ 2f;
    if (underlap < 0 )
      if (pointAngle > 360f + underlap)
        return true;
    return false;
  }
  public float computeTValue(float pointangle, float thisCurAngle, boolean reverse) {
    float distancefromhead = distancefromhead(pointangle, thisCurAngle, reverse);
    if (distancefromhead >= 1f) {
      if (!usePal.isOn()) {
        return bgintensity.getValuef();
      } else {
        return palStrt.getValuef(); // If we are using the palette we will use this to grab lerp'd swatch color.
      }
    }
    if (!usePal.isOn()) {
      return bgintensity.getValuef() + (1 - distancefromhead) * (1 - bgintensity.getValuef());
    } else {
      return palStrt.getValuef() + (1f - distancefromhead) * (1 - palStrt.getValuef());
    }
  }

  /**
   * @param pointAngle
   * @return 1 to 0 based on distance from the head where 1 is equivalent to angleWidth.
   */
  public float distancefromhead(float pointAngle, float thisCurAngle, boolean reverse) {
    float headPos = thisCurAngle;
    float distance = Math.abs(headPos - pointAngle);

    float wrappedDistance = Math.abs(headPos + 360f - pointAngle);
    float wrappedDistance2 = Math.abs(pointAngle + 360f - headPos);

    distance = Math.min(distance, wrappedDistance);
    distance = Math.min(distance, wrappedDistance2);

    //if (reverse) distance = 1.0f - distance;
    return distance / angleWidth.getValuef();
  }

  public void renderFrame(double deltaMs) {
    ease.easeNum = easeParam.getValuei();
    if (ease.easeNum == 8) {
      ease.perlinFreq = perlinFreq.getValuef();
    } else if (ease.easeNum == 6) {
      ease.freq = sinFreq.getValuef();
    }
    for (LXPoint p : lx.getModel().points) {
      colors[p.index] = LXColor.BLACK;
    }
    int mushroomNum = 0;
    for (SpiderTrapModel.Web web : SpiderTrapModel.allWebs) {
      for (SpiderTrapModel.Radial radial : web.radials) {
        for (LXPoint p : radial.points) {
          float angleDegrees = webAngle(p, radial);
          boolean reverse = (mushroomNum == 0)?rev1.getValueb():rev2.getValueb();
          float thisCurAngle = (mushroomNum == 0)?currentAngle:currentAngle2;
          float tValue = computeTValue(angleDegrees, thisCurAngle, reverse);
          int clr = getColor(tValue);
          // Apply sparkle/random amount and maximum value.
          float intensityMod = (((1f - sparkle.getValuef()) + sparkle.getValuef() * (float) Math.random()) * 1f);
          intensityMod = intensityMod * maxIntensity.getValuef();
          clr = Colors.getWeightedColor(clr, intensityMod);
          colors[p.index] = clr;
        }
      }
      mushroomNum++;
    }
    currentAngle += (rev1.isOn())?-speed.getValuef():speed.getValuef();
    currentAngle2 += (rev2.isOn())?-speed.getValuef():speed.getValuef();
    if (currentAngle > 360f)
      currentAngle  -= 360f;
    if (currentAngle < 0f)
      currentAngle += 360f;
    if (currentAngle2 > 360f)
      currentAngle2  -= 360f;
    if (currentAngle2 < 0f)
      currentAngle2 += 360f;
  }
}
