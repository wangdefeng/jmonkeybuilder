package com.ss.builder.fx.control.tree.action.impl;

import static com.ss.builder.jme.editor.part3d.impl.scene.AbstractSceneEditor3dPart.KEY_LOADED_MODEL;
import static com.ss.builder.util.EditorUtils.*;
import static com.ss.rlib.common.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ss.builder.FileExtensions;
import com.ss.builder.Messages;
import com.ss.builder.annotation.FxThread;
import com.ss.builder.jme.editor.part3d.impl.scene.AbstractSceneEditor3dPart;
import com.ss.builder.model.undo.editor.ChangeConsumer;
import com.ss.builder.model.undo.editor.ModelChangeConsumer;
import com.ss.builder.model.undo.impl.AddChildOperation;
import com.ss.builder.fx.Icons;
import com.ss.builder.fx.component.asset.tree.context.menu.action.DeleteFileAction;
import com.ss.builder.fx.component.asset.tree.context.menu.action.NewFileAction;
import com.ss.builder.fx.component.asset.tree.context.menu.action.RenameFileAction;
import com.ss.builder.fx.util.UiUtils;
import com.ss.builder.util.EditorUtils;
import com.ss.builder.FileExtensions;
import com.ss.builder.Messages;
import com.ss.builder.annotation.FxThread;
import com.ss.editor.extension.scene.SceneLayer;
import com.ss.builder.model.undo.editor.ChangeConsumer;
import com.ss.builder.model.undo.editor.ModelChangeConsumer;
import com.ss.builder.fx.Icons;
import com.ss.builder.fx.component.asset.tree.context.menu.action.DeleteFileAction;
import com.ss.builder.fx.component.asset.tree.context.menu.action.NewFileAction;
import com.ss.builder.fx.component.asset.tree.context.menu.action.RenameFileAction;
import com.ss.builder.model.undo.impl.AddChildOperation;
import com.ss.builder.fx.control.tree.NodeTree;
import com.ss.builder.fx.control.tree.action.AbstractNodeAction;
import com.ss.builder.fx.control.tree.node.TreeNode;
import com.ss.builder.fx.util.UiUtils;
import com.ss.builder.util.EditorUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * The implementation of the {@link AbstractNodeAction} for loading the {@link Spatial} to the editor.
 *
 * @author JavaSaBr
 */
public class LoadModelAction extends AbstractNodeAction<ModelChangeConsumer> {

    @NotNull
    private static final Predicate<Class<?>> ACTION_TESTER = type -> type == NewFileAction.class ||
            type == DeleteFileAction.class ||
            type == RenameFileAction.class;

    @NotNull
    private static final Array<String> MODEL_EXTENSIONS = ArrayFactory.newArray(String.class);

    static {
        MODEL_EXTENSIONS.add(FileExtensions.JME_OBJECT);
    }

    public LoadModelAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.OPEN_FILE_16;
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_LOAD_MODEL;
    }

    @Override
    @FxThread
    protected void process() {
        super.process();
        UiUtils.openFileAssetDialog(this::processOpen, MODEL_EXTENSIONS, ACTION_TESTER);
    }

    /**
     * The process of opening file.
     *
     * @param file the file
     */
    @FxThread
    protected void processOpen(@NotNull final Path file) {

        final NodeTree<?> nodeTree = getNodeTree();
        final ChangeConsumer consumer = notNull(nodeTree.getChangeConsumer());
        final SceneLayer defaultLayer = EditorUtils.getDefaultLayer(consumer);

        final Path assetFile = notNull(EditorUtils.getAssetFile(file), "Not found asset file for " + file);
        final String assetPath = EditorUtils.toAssetPath(assetFile);

        final ModelKey modelKey = new ModelKey(assetPath);

        final AssetManager assetManager = EditorUtils.getAssetManager();
        final Spatial loadedModel = assetManager.loadModel(modelKey);
        loadedModel.setUserData(AbstractSceneEditor3dPart.KEY_LOADED_MODEL, true);

        if (defaultLayer != null) {
            SceneLayer.setLayer(defaultLayer, loadedModel);
        }

        final TreeNode<?> treeNode = getNode();
        final Node parent = (Node) treeNode.getElement();
        consumer.execute(new AddChildOperation(loadedModel, parent));
    }
}