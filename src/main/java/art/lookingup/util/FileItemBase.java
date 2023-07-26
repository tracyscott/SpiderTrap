package art.lookingup.util;

import heronarts.p4lx.ui.UI;
import heronarts.p4lx.ui.component.UIItemList;

public class FileItemBase extends UIItemList.Item {
  protected final String filename;

  public FileItemBase(String str) {
    this.filename = str;
  }
  public boolean isActive() {
    return false;
  }
  public int getActiveColor(UI ui) {
    return ui.theme.getAttentionColor();
  }
  public String getLabel() {
    return filename;
  }
}
