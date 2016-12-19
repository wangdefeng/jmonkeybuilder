package com.ss.editor.ui.dialog;

import com.jme3.math.Vector3f;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.ToneMapFilter;
import com.ss.editor.Editor;
import com.ss.editor.JFXApplication;
import com.ss.editor.Messages;
import com.ss.editor.config.EditorConfig;
import com.ss.editor.manager.ClasspathManager;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import com.ss.editor.ui.scene.EditorFXScene;

import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.io.File;
import java.nio.file.Path;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import rlib.ui.util.FXUtils;
import rlib.util.StringUtils;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

/**
 * The dialog with settings.
 *
 * @author JavaSaBr
 */
public class SettingsDialog extends EditorDialog {

    private static final Insets OK_BUTTON_OFFSET = new Insets(0, 4, 0, 0);
    private static final Insets CANCEL_BUTTON_OFFSET = new Insets(0, 15, 0, 0);
    private static final Insets MESSAGE_OFFSET = new Insets(5, 0, 5, 0);
    private static final Insets LAST_FIELD_OFFSET = new Insets(5, 20, 10, 0);
    private static final Insets FIELD_OFFSET = new Insets(5, 20, 0, 0);
    private static final Insets ADD_REMOVE_BUTTON_OFFSET = new Insets(0, 0, 0, 2);

    private static final Point DIALOG_SIZE = new Point(600, 340);

    private static final Array<Integer> ANISOTROPYCS = ArrayFactory.newArray(Integer.class);

    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();
    private static final JFXApplication JFX_APPLICATION = JFXApplication.getInstance();
    private static final Editor EDITOR = Editor.getInstance();

    static {
        ANISOTROPYCS.add(0);
        ANISOTROPYCS.add(2);
        ANISOTROPYCS.add(4);
        ANISOTROPYCS.add(8);
        ANISOTROPYCS.add(16);
    }

    /**
     * The message label.
     */
    private Label messageLabel;

    /**
     * The combobox with anisotropy levels.
     */
    private ComboBox<Integer> anisotropyComboBox;

    /**
     * Координата белой точки экспозиции.
     */
    private Spinner<Double> toneMapFilterWhitePointX;

    /**
     * Координата белой точки экспозиции.
     */
    private Spinner<Double> toneMapFilterWhitePointY;

    /**
     * Координата белой точки экспозиции.
     */
    private Spinner<Double> toneMapFilterWhitePointZ;

    /**
     * Включение/выключение режима гамма коррекции.
     */
    private CheckBox gammaCorrectionCheckBox;

    /**
     * Включение/выключение фиьтра для коррекции экспозиции.
     */
    private CheckBox toneMapFilterCheckBox;

    /**
     * Включение/выключение FXAA.
     */
    private CheckBox fxaaFilterCheckBox;

    /**
     * The checkbox for enabling decorating.
     */
    private CheckBox decoratedCheckBox;

    /**
     * Поле для отображения выбранной папки дополнительного classpath.
     */
    private TextField additionalClasspathField;

    /**
     * Выбранная папка для расширения classpath.
     */
    private Path additionalClasspathFolder;

    /**
     * Игнорировать ли слушателей.
     */
    private boolean ignoreListeners;

    @Override
    public void show(@NotNull final Window owner) {
        super.show(owner);
        setIgnoreListeners(true);
        try {
            load();
        } finally {
            setIgnoreListeners(false);
        }
    }

    /**
     * @param ignoreListeners Игнорировать ли слушателей.
     */
    private void setIgnoreListeners(boolean ignoreListeners) {
        this.ignoreListeners = ignoreListeners;
    }

    /**
     * @return Игнорировать ли слушателей.
     */
    private boolean isIgnoreListeners() {
        return ignoreListeners;
    }

    @Override
    protected void createContent(@NotNull final VBox root) {
        super.createContent(root);

        messageLabel = new Label();
        messageLabel.setId(CSSIds.SETTINGS_DIALOG_MESSAGE_LABEL);

        FXUtils.bindFixedWidth(messageLabel, root.widthProperty().multiply(0.8));
        FXUtils.addClassTo(messageLabel, CSSClasses.SPECIAL_FONT_15);
        FXUtils.addToPane(messageLabel, root);

        VBox.setMargin(messageLabel, MESSAGE_OFFSET);

        createAnisotropyControl(root);
        createGammaCorrectionControl(root);
        createFXAAControl(root);
        createDecoratedControl(root);
        createToneMapFilterControl(root);
        createToneMapFilterWhitePointControl(root);
        createAdditionalClasspathControl(root);
    }

    /**
     * Создание настройки для выбора папки для расширения classpath.
     */
    private void createAdditionalClasspathControl(final VBox root) {

        final HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);

        final Label label = new Label(Messages.OTHER_SETTINGS_DIALOG_CLASSPATH_FOLDER_LABEL + ":");
        label.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        additionalClasspathField = new TextField();
        additionalClasspathField.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        additionalClasspathField.setEditable(false);
        additionalClasspathField.prefWidthProperty().bind(root.widthProperty());

        final Button addButton = new Button();
        addButton.setId(CSSIds.CREATE_SKY_DIALOG_BUTTON);
        addButton.setGraphic(new ImageView(Icons.ADD_18));
        addButton.setOnAction(event -> processAddCF());

        final Button removeButton = new Button();
        removeButton.setId(CSSIds.CREATE_SKY_DIALOG_BUTTON);
        removeButton.setGraphic(new ImageView(Icons.REMOVE_18));
        removeButton.setOnAction(event -> processRemoveCF());

        FXUtils.addToPane(label, container);
        FXUtils.addToPane(additionalClasspathField, container);
        FXUtils.addToPane(addButton, container);
        FXUtils.addToPane(removeButton, container);
        FXUtils.addToPane(container, root);

        FXUtils.addClassTo(label, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(additionalClasspathField, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(addButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(removeButton, CSSClasses.TOOLBAR_BUTTON);

        HBox.setMargin(addButton, ADD_REMOVE_BUTTON_OFFSET);
        HBox.setMargin(removeButton, ADD_REMOVE_BUTTON_OFFSET);
        VBox.setMargin(container, LAST_FIELD_OFFSET);
    }

    /**
     * Процесс удаления дополнительного classpath.
     */
    private void processRemoveCF() {
        setAdditionalClasspathFolder(null);

        final TextField textField = getAdditionalClasspathField();
        textField.setText(StringUtils.EMPTY);
    }

    /**
     * @return поле для отображения выбранной папки дополнительного classpath.
     */
    private TextField getAdditionalClasspathField() {
        return additionalClasspathField;
    }

    /**
     * Процесс указания папки для расширения classpath.
     */
    private void processAddCF() {

        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Messages.OTHER_SETTINGS_DIALOG_CLASSPATH_FOLDER_CHOOSER_TITLE);

        final EditorConfig config = EditorConfig.getInstance();
        final Path currentAdditionalCP = config.getAdditionalClasspath();
        final File currentFolder = currentAdditionalCP == null ? null : currentAdditionalCP.toFile();

        if (currentFolder != null) chooser.setInitialDirectory(currentFolder);

        final EditorFXScene scene = JFX_APPLICATION.getScene();
        final File folder = chooser.showDialog(scene.getWindow());

        if (folder == null) return;

        setAdditionalClasspathFolder(folder.toPath());

        final TextField textField = getAdditionalClasspathField();
        textField.setText(folder.toString());
    }

    /**
     * Создание контрола для активации гамма коррекции.
     */
    private void createGammaCorrectionControl(final VBox root) {

        final HBox gammaCorrectionContainer = new HBox();
        gammaCorrectionContainer.setAlignment(Pos.CENTER_LEFT);

        final Label gammaCorrectionLabel = new Label(Messages.SETTINGS_DIALOG_GAMMA_CORRECTION + ":");
        gammaCorrectionLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        gammaCorrectionCheckBox = new CheckBox();
        gammaCorrectionCheckBox.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        gammaCorrectionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(gammaCorrectionLabel, gammaCorrectionContainer);
        FXUtils.addToPane(gammaCorrectionCheckBox, gammaCorrectionContainer);
        FXUtils.addToPane(gammaCorrectionContainer, root);

        FXUtils.addClassTo(gammaCorrectionLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(gammaCorrectionCheckBox, CSSClasses.SPECIAL_FONT_14);

        VBox.setMargin(gammaCorrectionContainer, FIELD_OFFSET);
    }

    /**
     * Создание настройки для активации фильтра экспозиции.
     */
    private void createToneMapFilterControl(final VBox root) {

        final HBox toneMapFilterContainer = new HBox();
        toneMapFilterContainer.setAlignment(Pos.CENTER_LEFT);

        final Label toneMapFilterLabel = new Label(Messages.SETTINGS_DIALOG_TONEMAP_FILTER + ":");
        toneMapFilterLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        toneMapFilterCheckBox = new CheckBox();
        toneMapFilterCheckBox.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        toneMapFilterCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(toneMapFilterLabel, toneMapFilterContainer);
        FXUtils.addToPane(toneMapFilterCheckBox, toneMapFilterContainer);
        FXUtils.addToPane(toneMapFilterContainer, root);

        FXUtils.addClassTo(toneMapFilterLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(toneMapFilterCheckBox, CSSClasses.SPECIAL_FONT_14);

        VBox.setMargin(toneMapFilterContainer, FIELD_OFFSET);
    }

    /**
     * Создание настройки белой точки фильтра экспозиции.
     */
    private void createToneMapFilterWhitePointControl(final VBox root) {

        final HBox toneMapFilterWhitePointContainer = new HBox();
        toneMapFilterWhitePointContainer.setAlignment(Pos.CENTER_LEFT);
        toneMapFilterWhitePointContainer.disableProperty().bind(toneMapFilterCheckBox.selectedProperty().not());

        final Label toneMapFilterWhitePointLabel = new Label(Messages.SETTINGS_DIALOG_TONEMAP_FILTER_WHITE_POINT + ":");
        toneMapFilterWhitePointLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        final Label xLabel = new Label("x:");
        xLabel.setId(CSSIds.SETTINGS_DIALOG_FIELD);

        final Label yLabel = new Label("y:");
        yLabel.setId(CSSIds.SETTINGS_DIALOG_FIELD);

        final Label zLabel = new Label("z:");
        zLabel.setId(CSSIds.SETTINGS_DIALOG_FIELD);

        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-30, 30, 0, 0.1);

        toneMapFilterWhitePointX = new Spinner<>();
        toneMapFilterWhitePointX.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        toneMapFilterWhitePointX.setValueFactory(valueFactory);
        toneMapFilterWhitePointX.setEditable(true);
        toneMapFilterWhitePointX.setOnScroll(event -> processScroll(toneMapFilterWhitePointX, event));
        toneMapFilterWhitePointX.valueProperty().addListener((observable, oldValue, newValue) -> validate());

        valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-30, 30, 0, 0.1);

        toneMapFilterWhitePointY = new Spinner<>();
        toneMapFilterWhitePointY.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        toneMapFilterWhitePointY.setValueFactory(valueFactory);
        toneMapFilterWhitePointY.setEditable(true);
        toneMapFilterWhitePointY.setOnScroll(event -> processScroll(toneMapFilterWhitePointY, event));
        toneMapFilterWhitePointY.valueProperty().addListener((observable, oldValue, newValue) -> validate());

        valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-30, 30, 0, 0.1);

        toneMapFilterWhitePointZ = new Spinner<>();
        toneMapFilterWhitePointZ.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        toneMapFilterWhitePointZ.setValueFactory(valueFactory);
        toneMapFilterWhitePointZ.setEditable(true);
        toneMapFilterWhitePointZ.setOnScroll(event -> processScroll(toneMapFilterWhitePointZ, event));
        toneMapFilterWhitePointZ.valueProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(toneMapFilterWhitePointLabel, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(xLabel, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(toneMapFilterWhitePointX, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(yLabel, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(toneMapFilterWhitePointY, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(zLabel, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(toneMapFilterWhitePointZ, toneMapFilterWhitePointContainer);
        FXUtils.addToPane(toneMapFilterWhitePointContainer, root);

        FXUtils.addClassTo(toneMapFilterWhitePointLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(xLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(toneMapFilterWhitePointX, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(yLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(toneMapFilterWhitePointY, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(zLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(toneMapFilterWhitePointZ, CSSClasses.SPECIAL_FONT_14);

        HBox.setMargin(xLabel, new Insets(0, 0, 0, 4));
        VBox.setMargin(toneMapFilterWhitePointContainer, FIELD_OFFSET);
    }

    /**
     * Процесс скролирования значения.
     */
    private void processScroll(final Spinner<Double> spinner, final ScrollEvent event) {
        if (!event.isControlDown()) return;

        final double deltaY = event.getDeltaY();

        if (deltaY > 0) {
            spinner.increment(1);
        } else {
            spinner.decrement(1);
        }
    }

    /**
     * Создание настройки для активации FXAA.
     */
    private void createFXAAControl(final VBox root) {

        final HBox fxaaContainer = new HBox();
        fxaaContainer.setAlignment(Pos.CENTER_LEFT);

        final Label fxaaLabel = new Label(Messages.SETTINGS_DIALOG_FXAA + ":");
        fxaaLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        fxaaFilterCheckBox = new CheckBox();
        fxaaFilterCheckBox.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        fxaaFilterCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(fxaaLabel, fxaaContainer);
        FXUtils.addToPane(fxaaFilterCheckBox, fxaaContainer);
        FXUtils.addToPane(fxaaContainer, root);

        FXUtils.addClassTo(fxaaLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(fxaaFilterCheckBox, CSSClasses.SPECIAL_FONT_14);

        VBox.setMargin(fxaaContainer, FIELD_OFFSET);
    }

    /**
     * Create the checkbox for configuring decorated windows.
     */
    private void createDecoratedControl(final VBox root) {

        final HBox decoratedContainer = new HBox();
        decoratedContainer.setAlignment(Pos.CENTER_LEFT);

        final Label decoratedLabel = new Label(Messages.SETTINGS_DIALOG_DECORATED + ":");
        decoratedLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        decoratedCheckBox = new CheckBox();
        decoratedCheckBox.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        decoratedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(decoratedLabel, decoratedContainer);
        FXUtils.addToPane(decoratedCheckBox, decoratedContainer);
        FXUtils.addToPane(decoratedContainer, root);

        FXUtils.addClassTo(decoratedLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(decoratedCheckBox, CSSClasses.SPECIAL_FONT_14);

        VBox.setMargin(decoratedContainer, FIELD_OFFSET);
    }

    /**
     * Создание настройкид ля выбора анизатроной фильтрации.
     */
    private void createAnisotropyControl(final VBox root) {

        final HBox anisotropyContainer = new HBox();
        anisotropyContainer.setAlignment(Pos.CENTER_LEFT);

        final Label anisotropyLabel = new Label(Messages.SETTINGS_DIALOG_ANISOTROPY + ":");
        anisotropyLabel.setId(CSSIds.SETTINGS_DIALOG_LABEL);

        anisotropyComboBox = new ComboBox<>();
        anisotropyComboBox.setId(CSSIds.SETTINGS_DIALOG_FIELD);
        anisotropyComboBox.prefWidthProperty().bind(root.widthProperty());
        anisotropyComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> validate());

        FXUtils.addToPane(anisotropyLabel, anisotropyContainer);
        FXUtils.addToPane(anisotropyComboBox, anisotropyContainer);
        FXUtils.addToPane(anisotropyContainer, root);

        FXUtils.addClassTo(anisotropyLabel, CSSClasses.SPECIAL_FONT_14);
        FXUtils.addClassTo(anisotropyComboBox, CSSClasses.SPECIAL_FONT_14);

        VBox.setMargin(anisotropyContainer, FIELD_OFFSET);

        final ObservableList<Integer> items = anisotropyComboBox.getItems();

        ANISOTROPYCS.forEach(items::add);
    }

    /**
     * @return включение/выключение режима гамма коррекции.
     */
    private CheckBox getGammaCorrectionCheckBox() {
        return gammaCorrectionCheckBox;
    }

    /**
     * @return включение/выключение фиьтра для коррекции экспозиции.
     */
    private CheckBox getToneMapFilterCheckBox() {
        return toneMapFilterCheckBox;
    }

    /**
     * @return координата белой точки экспозиции.
     */
    private Spinner<Double> getToneMapFilterWhitePointX() {
        return toneMapFilterWhitePointX;
    }

    /**
     * @return координата белой точки экспозиции.
     */
    private Spinner<Double> getToneMapFilterWhitePointY() {
        return toneMapFilterWhitePointY;
    }

    /**
     * @return координата белой точки экспозиции.
     */
    private Spinner<Double> getToneMapFilterWhitePointZ() {
        return toneMapFilterWhitePointZ;
    }

    /**
     * @return включение/выключение FXAA.
     */
    private CheckBox getFXAAFilterCheckBox() {
        return fxaaFilterCheckBox;
    }

    /**
     * @return комбобокс с выбором анизатропного фильтра.
     */
    private ComboBox<Integer> getAnisotropyComboBox() {
        return anisotropyComboBox;
    }

    /**
     * @return the checkbox for enabling decorating.
     */
    private CheckBox getDecoratedCheckBox() {
        return decoratedCheckBox;
    }

    /**
     * @return надпись с сообщением.
     */
    public Label getMessageLabel() {
        return messageLabel;
    }

    /**
     * Валидация изменений.
     */
    private void validate() {
        if (isIgnoreListeners()) return;

        final Label messageLabel = getMessageLabel();

        int needRestart = 0;

        final EditorConfig editorConfig = EditorConfig.getInstance();
        final int currentAnisotropy = editorConfig.getAnisotropy();
        final boolean currentGammaCorrection = editorConfig.isGammaCorrection();
        final boolean currentDecorated = editorConfig.isDecorated();

        final ComboBox<Integer> anisotropyComboBox = getAnisotropyComboBox();
        final Integer anisotropy = anisotropyComboBox.getSelectionModel().getSelectedItem();

        final CheckBox gammaCorrectionCheckBox = getGammaCorrectionCheckBox();
        final boolean gammaCorrection = gammaCorrectionCheckBox.isSelected();

        final CheckBox decoratedCheckBox = getDecoratedCheckBox();
        final boolean decorated = decoratedCheckBox.isSelected();

        if (currentAnisotropy != anisotropy) {
            needRestart++;
        } else if (currentGammaCorrection != gammaCorrection) {
            needRestart++;
        } else if (decorated != currentDecorated) {
            needRestart++;
        }

        if (needRestart > 0) {
            messageLabel.setText(Messages.SETTINGS_DIALOG_MESSAGE);
        } else {
            messageLabel.setText(StringUtils.EMPTY);
        }
    }

    /**
     * Загрузка текущих параметров.
     */
    private void load() {

        final EditorConfig editorConfig = EditorConfig.getInstance();

        final ComboBox<Integer> anisotropyComboBox = getAnisotropyComboBox();
        final SingleSelectionModel<Integer> selectedAnisotropy = anisotropyComboBox.getSelectionModel();
        selectedAnisotropy.select(Integer.valueOf(editorConfig.getAnisotropy()));

        final CheckBox fxaaFilterCheckBox = getFXAAFilterCheckBox();
        fxaaFilterCheckBox.setSelected(editorConfig.isFXAA());

        final CheckBox gammaCorrectionCheckBox = getGammaCorrectionCheckBox();
        gammaCorrectionCheckBox.setSelected(editorConfig.isGammaCorrection());

        final CheckBox toneMapFilterCheckBox = getToneMapFilterCheckBox();
        toneMapFilterCheckBox.setSelected(editorConfig.isToneMapFilter());

        final CheckBox decoratedCheckBox = getDecoratedCheckBox();
        decoratedCheckBox.setSelected(editorConfig.isDecorated());

        final Vector3f toneMapFilterWhitePoint = editorConfig.getToneMapFilterWhitePoint();

        final Spinner<Double> toneMapFilterWhitePointX = getToneMapFilterWhitePointX();
        toneMapFilterWhitePointX.getValueFactory().setValue((double) toneMapFilterWhitePoint.getX());

        final Spinner<Double> toneMapFilterWhitePointY = getToneMapFilterWhitePointY();
        toneMapFilterWhitePointY.getValueFactory().setValue((double) toneMapFilterWhitePoint.getY());

        final Spinner<Double> toneMapFilterWhitePointZ = getToneMapFilterWhitePointZ();
        toneMapFilterWhitePointZ.getValueFactory().setValue((double) toneMapFilterWhitePoint.getZ());

        final Path additionalClasspath = editorConfig.getAdditionalClasspath();

        final TextField additionalClasspathField = getAdditionalClasspathField();

        if (additionalClasspath != null) {
            additionalClasspathField.setText(additionalClasspath.toString());
        }

        setAdditionalClasspathFolder(additionalClasspath);
    }

    /**
     * @return выбранная папка для расширения classpath.
     */
    private Path getAdditionalClasspathFolder() {
        return additionalClasspathFolder;
    }

    /**
     * @param additionalClasspathFolder выбранная папка для расширения classpath.
     */
    private void setAdditionalClasspathFolder(final Path additionalClasspathFolder) {
        this.additionalClasspathFolder = additionalClasspathFolder;
    }

    @Override
    protected void createActions(@NotNull final VBox root) {
        super.createActions(root);

        final HBox container = new HBox();
        container.setId(CSSIds.ASSET_EDITOR_DIALOG_BUTTON_CONTAINER);

        final Button okButton = new Button(Messages.SETTINGS_DIALOG_BUTTON_OK);
        okButton.setId(CSSIds.EDITOR_DIALOG_BUTTON_OK);
        okButton.setOnAction(event -> processOk());

        final Button cancelButton = new Button(Messages.SETTINGS_DIALOG_BUTTON_CANCEL);
        cancelButton.setId(CSSIds.EDITOR_DIALOG_BUTTON_CANCEL);
        cancelButton.setOnAction(event -> hide());

        FXUtils.addClassTo(okButton, CSSClasses.SPECIAL_FONT_16);
        FXUtils.addClassTo(cancelButton, CSSClasses.SPECIAL_FONT_16);

        FXUtils.addToPane(okButton, container);
        FXUtils.addToPane(cancelButton, container);
        FXUtils.addToPane(container, root);

        HBox.setMargin(okButton, OK_BUTTON_OFFSET);
        HBox.setMargin(cancelButton, CANCEL_BUTTON_OFFSET);
    }

    /**
     * Процесс сохранения и приминения изменений.
     */
    private void processOk() {

        int needRestart = 0;

        final EditorConfig editorConfig = EditorConfig.getInstance();
        final int currentAnisotropy = editorConfig.getAnisotropy();
        final boolean currentGammaCorrection = editorConfig.isGammaCorrection();
        final boolean currentDecorated = editorConfig.isDecorated();

        final ComboBox<Integer> anisotropyComboBox = getAnisotropyComboBox();
        final Integer anisotropy = anisotropyComboBox.getSelectionModel().getSelectedItem();

        final CheckBox fxaaFilterCheckBox = getFXAAFilterCheckBox();
        final boolean fxaa = fxaaFilterCheckBox.isSelected();

        final CheckBox gammaCorrectionCheckBox = getGammaCorrectionCheckBox();
        final boolean gammaCorrection = gammaCorrectionCheckBox.isSelected();

        final CheckBox toneMapFilterCheckBox = getToneMapFilterCheckBox();
        final boolean toneMapFilter = toneMapFilterCheckBox.isSelected();

        final CheckBox decoratedCheckBox = getDecoratedCheckBox();
        final boolean decorated = decoratedCheckBox.isSelected();

        final float toneMapFilterWhitePointX = getToneMapFilterWhitePointX().getValue().floatValue();
        final float toneMapFilterWhitePointY = getToneMapFilterWhitePointY().getValue().floatValue();
        final float toneMapFilterWhitePointZ = getToneMapFilterWhitePointZ().getValue().floatValue();

        final Vector3f toneMapFilterWhitePoint = new Vector3f(toneMapFilterWhitePointX, toneMapFilterWhitePointY, toneMapFilterWhitePointZ);

        if (currentAnisotropy != anisotropy) {
            needRestart++;
        } else if (currentGammaCorrection != gammaCorrection) {
            needRestart++;
        } else if (currentDecorated != decorated) {
            needRestart++;
        }

        final ClasspathManager classpathManager = ClasspathManager.getInstance();
        classpathManager.updateAdditionalCL();

        editorConfig.setAnisotropy(anisotropy);
        editorConfig.setFXAA(fxaa);
        editorConfig.setDecorated(decorated);
        editorConfig.setGammaCorrection(gammaCorrection);
        editorConfig.setToneMapFilter(toneMapFilter);
        editorConfig.setToneMapFilterWhitePoint(toneMapFilterWhitePoint);
        editorConfig.setAdditionalClasspath(getAdditionalClasspathFolder());
        editorConfig.save();

        EXECUTOR_MANAGER.addEditorThreadTask(() -> {

            final FXAAFilter fxaaFilter = EDITOR.getFXAAFilter();
            fxaaFilter.setEnabled(editorConfig.isFXAA());

            final ToneMapFilter filter = EDITOR.getToneMapFilter();
            filter.setEnabled(editorConfig.isToneMapFilter());
            filter.setWhitePoint(editorConfig.getToneMapFilterWhitePoint());
        });

        if (needRestart > 0) {
            System.exit(2);
        } else {
            hide();
        }
    }

    @NotNull
    @Override
    protected String getTitleText() {
        return Messages.SETTINGS_DIALOG_TITLE;
    }

    @Override
    protected Point getSize() {
        return DIALOG_SIZE;
    }
}