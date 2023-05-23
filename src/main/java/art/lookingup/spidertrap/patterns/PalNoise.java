package art.lookingup.spidertrap.patterns;

import art.lookingup.util.EaseUtil;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.texture.NoisePattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIDoubleBox;
import heronarts.p4lx.ui.component.UIIntegerBox;
import heronarts.p4lx.ui.component.UISlider;

public class PalNoise extends NoisePattern implements UIDeviceControls<PalNoise>  {
  DiscreteParameter ease = new DiscreteParameter("ease", 0, EaseUtil.MAX_EASE+1);
  DiscreteParameter pal = new DiscreteParameter("pal", 0, 21);
  BooleanParameter bright = new BooleanParameter("bright", true);

  EaseUtil easeUtil = new EaseUtil(0);


  public UIIntegerBox seedBox;
  public UIIntegerBox octavesBox;
  public UIDoubleBox lacunarityBox;
  public UIDoubleBox gainBox;
  public UIDoubleBox ridgeOffsetBox;


  public PalNoise(LX lx) {
    super(lx);
    addParameter("ease", ease);
    addParameter("pal", pal);
    addParameter("bright", bright);
  }

  @Override
  public void run(double deltaMs) {
    super.run(deltaMs);
    easeUtil.easeNum = ease.getValuei();
    // For all points, extract the greyscale value (brightness) and map it to a palette color and then
    // multiply it back down
    float hsb[] = new float[3];
    for (int index = 0; index < colors.length; index++) {
      float b = (float)LXColor.red(colors[index])/255f;
      hsb = Colors.RGBtoHSB(colors[index], hsb);
      colors[index] = Colors.getParameterizedPaletteColor(lx, pal.getValuei(), hsb[2], easeUtil);
    }
  }

  @Override
  public void buildDeviceControls(LXStudio.UI ui, UIDevice uiDevice, PalNoise noise) {
    uiDevice.setLayout(UI2dContainer.Layout.HORIZONTAL);
    uiDevice.setChildSpacing(4);

    addColumn(uiDevice,
        "Zoom",
        newKnob(noise.scale),
        newDoubleBox(noise.minScale),
        controlLabel(ui, "Min"),
        newDoubleBox(noise.maxScale),
        controlLabel(ui, "Max")
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        "Dynamics",
        newKnob(noise.level),
        newKnob(noise.contrast)
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        sectionLabel("X-Pos").setBottomMargin(-6),
        newButton(noise.xMode).setBottomMargin(-6),
        newHorizontalSlider(noise.xOffset).setShowLabel(false),

        sectionLabel("Y-Pos").setMargin(-6, 0),
        newButton(noise.yMode).setBottomMargin(-6),
        newHorizontalSlider(noise.yOffset).setShowLabel(false),

        sectionLabel("Z-Pos").setMargin(-6, 0),
        newButton(noise.zMode).setBottomMargin(-6),
        newHorizontalSlider(noise.zOffset).setShowLabel(false)
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        "Animate",
        new UISlider(UISlider.Direction.VERTICAL, COL_WIDTH, 116, noise.motionSpeed)
    );

    addColumn(uiDevice,
        newButton(noise.motion),
        newDoubleBox(noise.motionSpeedRange),
        newHorizontalSlider(noise.xMotion),
        newHorizontalSlider(noise.yMotion),
        newHorizontalSlider(noise.zMotion)
    ).setLeftMargin(4);

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        "Algorithm",
        newDropMenu(noise.algorithm),
        seedBox = newIntegerBox(seed),
        octavesBox = newIntegerBox(octaves),
        gainBox = newDoubleBox(gain),
        lacunarityBox = newDoubleBox(lacunarity),
        ridgeOffsetBox = newDoubleBox(ridgeOffset),
        newKnob(pal),
        newKnob(ease),
        newButton(bright)
    );

    noise.algorithm.addListener((p) -> {
      updateAlgorithmControls(noise);
    });
    updateAlgorithmControls(noise);
  }

  private void updateAlgorithmControls(NoisePattern noise) {
    NoisePattern.Algorithm algorithm = noise.algorithm.getEnum();
    this.seedBox.setVisible(algorithm == NoisePattern.Algorithm.PERLIN);
    this.octavesBox.setVisible(algorithm.isPerlinFeedback());
    this.gainBox.setVisible(algorithm.isPerlinFeedback());
    this.lacunarityBox.setVisible(algorithm.isPerlinFeedback());
    this.ridgeOffsetBox.setVisible(algorithm == NoisePattern.Algorithm.RIDGE);
  }

}
