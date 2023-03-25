package moe.kirao.mgx.ui;

import android.content.Context;
import android.view.View;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.ui.SettingsAdapter;
import org.thunderdog.challegram.v.CustomRecyclerView;

import java.util.ArrayList;

import moe.kirao.mgx.MoexConfig;

public class GeneralSettingsMoexController extends RecyclerViewController<Void> implements View.OnClickListener {
  private SettingsAdapter adapter;

  public GeneralSettingsMoexController (Context context, Tdlib tdlib) {
    super(context, tdlib);
  }

  @Override public CharSequence getName () {
    return Lang.getString(R.string.GeneralMoexSettings);
  }

  @Override public void onClick (View v) {
    int id = v.getId();
    switch (id) {
      case R.id.btn_hidePhone:
        MoexConfig.instance().toggleHidePhoneNumber();
        adapter.updateValuedSettingById(R.id.btn_hidePhone);
        break;
      case R.id.btn_enableFeaturesButton:
        MoexConfig.instance().toggleEnableFeaturesButton();
        adapter.updateValuedSettingById(R.id.btn_enableFeaturesButton);
        break;
      case R.id.btn_showIdProfile:
        MoexConfig.instance().toggleShowIdProfile();
        adapter.updateValuedSettingById(R.id.btn_showIdProfile);
        break;
    }
  }

  @Override public int getId () {
    return R.id.controller_moexSettings;
  }

  @Override protected void onCreateView (Context context, CustomRecyclerView recyclerView) {
    adapter = new SettingsAdapter(this) {
      @Override protected void setValuedSetting (ListItem item, SettingView view, boolean isUpdate) {
        view.setDrawModifier(item.getDrawModifier());
        switch (item.getId()) {
          case R.id.btn_hidePhone:
            view.getToggler().setRadioEnabled(MoexConfig.hidePhoneNumber, isUpdate);
            break;
          case R.id.btn_enableFeaturesButton:
            view.getToggler().setRadioEnabled(MoexConfig.enableTestFeatures, isUpdate);
            break;
          case R.id.btn_showIdProfile:
            view.getToggler().setRadioEnabled(MoexConfig.showId, isUpdate);
            break;
        }
      }
    };

    ArrayList<ListItem> items = new ArrayList<>();
    items.add(new ListItem(ListItem.TYPE_EMPTY_OFFSET_SMALL));
    items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.DrawerOptions));
    items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_hidePhone, 0, R.string.hidePhoneNumber));
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_showIdProfile, 0, R.string.showIdProfile));
    items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

    items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.ExperimentalOptions));
    items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_enableFeaturesButton, 0, R.string.EnableFeatures));
    items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
    items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getMarkdownString(this, R.string.FeaturesButtonInfo), false));
    items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));

    adapter.setItems(items, true);
    recyclerView.setAdapter(adapter);
  }
}