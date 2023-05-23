package art.lookingup.ui;

import art.lookingup.spidertrap.SpiderTrapApp;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UICollapsibleSection;

public class UIPreviewComponents extends UICollapsibleSection {
  public UIPreviewComponents(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("Overlay");
    UI2dContainer knobsContainer = new UI2dContainer(0, 0, getContentWidth(), 20);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(10, 10, 10, 10);
    knobsContainer.addToContainer(this);

    UIButton showTrees = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        SpiderTrapApp.preview.showTrees = on;
      }
    }.setLabel("trees").setActive(SpiderTrapApp.preview.showTrees);
    showTrees.setWidth(35).setHeight(16);
    showTrees.addToContainer(knobsContainer);
  }
}



