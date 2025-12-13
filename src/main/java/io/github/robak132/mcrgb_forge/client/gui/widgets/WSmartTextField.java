package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.libgui_forge.widget.WTextField;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class WSmartTextField extends WTextField {

    private boolean wasFocused = false;
    private String lastCommittedText = "";

    private Consumer<String> onCommit;

    public WSmartTextField() {
        super();
    }

    public WSmartTextField(Component suggestion) {
        super(suggestion);
    }

    public void setCommitListener(Consumer<String> listener) {
        this.onCommit = listener;
    }

    @Override
    public void tick() {
        super.tick();

        boolean focused = this.isFocused();

        // Focus gained
        if (focused && !wasFocused) {
            wasFocused = true;
            lastCommittedText = getText();
            return;
        }

        // Focus lost → commit
        if (!focused && wasFocused) {
            wasFocused = false;
            commitIfChanged();
        }
    }

    private void commitIfChanged() {
        String current = getText();

        if (!current.equals(lastCommittedText)) {
            lastCommittedText = current;
            if (onCommit != null) {
                onCommit.accept(current);
            }
        }
    }

    /**
     * Programmatic commit (optional utility)
     */
    public void commit() {
        commitIfChanged();
    }
}