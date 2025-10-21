package rpgclasses.settings;

import customsettingslib.components.settings.BooleanSetting;
import necesse.gfx.forms.components.FormCheckBox;
import necesse.gfx.forms.components.localComponents.FormLocalCheckBox;

import java.util.concurrent.atomic.AtomicReference;

public class ClassEnabledSetting extends BooleanSetting {
    protected String classStringID;

    public ClassEnabledSetting(String id, Boolean defaultValue) {
        super(id + "_class", defaultValue);
        this.classStringID = id;
    }

    private final AtomicReference<Boolean> newValue = new AtomicReference<>();

    @Override
    public int addComponents(int y, int n) {
        this.newValue.set(this.value);
        int width = getWidth();
        FormCheckBox component = settingsForm.addComponent(new FormLocalCheckBox("classes", this.classStringID, 4, y, width), 8)
                .onClicked(
                        (e) -> this.newValue.set(!(Boolean) this.newValue.get())
                );
        component.checked = this.getTrueValue();
        component.setActive(this.isEnabled());
        return component.getHitboxes().get(0).height;
    }

    @Override
    public void onSave() {
        this.changeValue(this.newValue.get());
    }


}
