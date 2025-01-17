package com.ss.editor.ui.control.property.impl;

import com.jme3.scene.Geometry;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.ui.control.property.PropertyControl;
import com.ss.editor.ui.css.CssClasses;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.fx.util.FxControlUtils;
import com.ss.rlib.fx.util.FxUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of the {@link PropertyControl} to edit the LOD levels.
 *
 * @author JavaSaBr
 */
public class LodLevelPropertyControl<C extends ChangeConsumer> extends PropertyControl<C, Geometry, Integer> {

    private class LodLevelCell extends ListCell<Integer> {

        @Override
        protected void updateItem(@Nullable Integer level, boolean empty) {
            super.updateItem(level, empty);

            var geometry = getEditObject();
            var mesh = geometry.getMesh();

            if (level == null || mesh == null) {
                setText("None");
                return;
            }

            int elements;

            if (level < mesh.getNumLodLevels()) {
                var lodLevel = mesh.getLodLevel(level);
                elements = lodLevel.getNumElements();
            } else {
                elements = mesh.getTriangleCount();
            }

            setText(Messages.MODEL_PROPERTY_LEVEL + ": " + level + " (" + elements + " " +
                    Messages.MODEL_PROPERTY_TRIANGLE_COUNT + ")");
        }
    }

    /**
     * The lod level combobox.
     */
    @Nullable
    private ComboBox<Integer> levelComboBox;

    public LodLevelPropertyControl(
            @Nullable Integer element,
            @NotNull String paramName,
            @NotNull C changeConsumer
    ) {
        super(element, paramName, changeConsumer);
    }

    @Override
    @FromAnyThread
    protected boolean isSingleRow() {
        return true;
    }

    @Override
    @FxThread
    protected void createComponents(@NotNull HBox container) {
        super.createComponents(container);

        levelComboBox = new ComboBox<>();
        levelComboBox.setCellFactory(param -> new LodLevelCell());
        levelComboBox.setButtonCell(new LodLevelCell());
        levelComboBox.setEditable(false);
        levelComboBox.prefWidthProperty()
                .bind(widthProperty().multiply(CONTROL_WIDTH_PERCENT));

        FxControlUtils.onSelectedItemChange(levelComboBox, this::updateLevel);

        FxUtils.addClass(levelComboBox,
                CssClasses.PROPERTY_CONTROL_COMBO_BOX);

        FxUtils.addChild(container, levelComboBox);
    }

    /**
     * Update lod level.
     *
     * @param newValue the new level.
     */
    @FxThread
    private void updateLevel(@Nullable Integer newValue) {
        if (isIgnoreListener()) return;
        changed(ObjectUtils.ifNull(newValue, 0), getPropertyValue());
    }

    @Override
    @FxThread
    public @NotNull Integer getPropertyValue() {
        return ObjectUtils.ifNull(super.getPropertyValue(), 0);
    }

    /**
     * Get the level combo box.
     *
     * @return The lod level combo box.
     */
    @FxThread
    protected @NotNull ComboBox<Integer> getLevelComboBox() {
        return ObjectUtils.notNull(levelComboBox);
    }

    @Override
    @FxThread
    protected void reload() {

        if (!hasEditObject()) {
            return;
        }

        var geometry = getEditObject();
        var mesh = geometry.getMesh();
        if (mesh == null) {
            return;
        }

        var element = getPropertyValue();
        var levelComboBox = getLevelComboBox();
        var items = levelComboBox.getItems();
        items.clear();

        var numLodLevels = mesh.getNumLodLevels();

        for (var i = 0; i < numLodLevels; i++) {
            items.add(i);
        }

        if (items.isEmpty()) {
            items.add(0);
        }

        levelComboBox.getSelectionModel()
                .select(element);
    }
}
