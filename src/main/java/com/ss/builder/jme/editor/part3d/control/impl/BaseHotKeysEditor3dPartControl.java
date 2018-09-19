package com.ss.builder.jme.editor.part3d.control.impl;

import static com.ss.builder.jme.editor.part3d.control.impl.InputStateEditor3dPartControl.PROP_IS_CONTROL_DOWN;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.ss.builder.annotation.JmeThread;
import com.ss.builder.jme.editor.part3d.ExtendableEditor3dPart;
import com.ss.builder.util.JmeUtils;
import com.ss.builder.annotation.JmeThread;
import com.ss.builder.jme.editor.part3d.ExtendableEditor3dPart;
import com.ss.builder.jme.editor.part3d.SavableEditor3dPart;
import com.ss.builder.jme.editor.part3d.UndoableEditor3dPart;
import com.ss.builder.util.JmeUtils;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

/**
 * The control to base hotkeys of the editor 3d part.
 *
 * @author JavaSaBr
 */
public class BaseHotKeysEditor3dPartControl<T extends SavableEditor3dPart & UndoableEditor3dPart & ExtendableEditor3dPart>
        extends BaseInputEditor3dPartControl<T> {

    private static final ObjectDictionary<String, Trigger> TRIGGERS =
            ObjectDictionary.ofType(String.class, Trigger.class);

    private static final String KEY_CTRL_S = "jMB.baseHotKeysEditor.Ctrl.S";
    private static final String KEY_CTRL_Z = "jMB.baseHotKeysEditor.Ctrl.Z";
    private static final String KEY_CTRL_Y = "jMB.baseHotKeysEditor.Ctrl.Y";

    private static final String[] MAPPINGS;

    static {

        TRIGGERS.put(KEY_CTRL_S, new KeyTrigger(KeyInput.KEY_S));
        TRIGGERS.put(KEY_CTRL_Z, new KeyTrigger(KeyInput.KEY_Z));
        TRIGGERS.put(KEY_CTRL_Y, new KeyTrigger(KeyInput.KEY_Y));

        MAPPINGS = TRIGGERS.keyArray(String.class)
                .toArray(String.class);
    }

    public BaseHotKeysEditor3dPartControl(@NotNull T editor3dPart) {
        super(editor3dPart);

        actionHandlers.put(KEY_CTRL_Z, (isPressed, tpf) -> {
            if (!isPressed && editor3dPart.getBooleanProperty(InputStateEditor3dPartControl.PROP_IS_CONTROL_DOWN)) {
                editor3dPart.undo();
            }
        });

        actionHandlers.put(KEY_CTRL_Y, (isPressed, tpf) -> {
            if (!isPressed && editor3dPart.getBooleanProperty(InputStateEditor3dPartControl.PROP_IS_CONTROL_DOWN)) {
                editor3dPart.redo();
            }
        });

        actionHandlers.put(KEY_CTRL_S, (isPressed, tpf) -> {
            if (isPressed && editor3dPart.getBooleanProperty(InputStateEditor3dPartControl.PROP_IS_CONTROL_DOWN) && editor3dPart.isDirty()) {
                editor3dPart.save();
            }
        });
    }

    @Override
    @JmeThread
    public void register(@NotNull InputManager inputManager) {
        TRIGGERS.forEach(inputManager, JmeUtils::addMapping);
        inputManager.addListener(getActionListener(), MAPPINGS);
    }
}