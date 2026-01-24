package com.tara.ui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SphereUI extends Application {

    public enum AssistantState {
        IDLE, LISTENING, THINKING, SPEAKING
    }

    private static Stage stage;
    private static Pane root;
    private static boolean initialized = false;
    private static AssistantState currentState = AssistantState.IDLE;

    private static Sphere orb; // now 3D Sphere
    private static final List<Circle> rings = new ArrayList<>();

    private static final Color[][] COLORS = {
            { Color.web("#191970"), Color.web("#1F51FF"), Color.web("#00FFFF") }, // IDLE
            { Color.web("#9649CB"), Color.web("#FF00FF"), Color.web("#FF6EC7") }, // LISTENING
            { Color.web("#E3856B"), Color.web("#FFD93D"), Color.web("#F0EEE9") }, // THINKING
            { Color.web("#3CD070"), Color.web("#50C878"), Color.web("#C3CF5A") }  // SPEAKING
    };

    public static void launchUI() {
        new Thread(() -> Application.launch(SphereUI.class)).start();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        root = new Pane();
        root.setPrefSize(320, 320);
        root.setPickOnBounds(false);

        Scene scene = new Scene(root, 320, 320, true); // enable depth buffer for 3D
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);

        createOrb();      // 3D orb
        createRings();    // 2D rings
        enableDrag();
        positionTopRight();

        stage.hide();
        initialized = true;
    }

    // ===================== 3D CENTRAL ORB =====================
    private void createOrb() {
        orb = new Sphere(26); // radius
        orb.setTranslateX(160); // center
        orb.setTranslateY(160);
        orb.setTranslateZ(0);

        PhongMaterial material = new PhongMaterial(COLORS[0][0]);
        orb.setMaterial(material);

        // 3D rotation animation for sphere
        RotateTransition rotate3D = new RotateTransition(Duration.seconds(20), orb);
        rotate3D.setByAngle(360);
        rotate3D.setAxis(Rotate.Y_AXIS); // rotate around vertical
        rotate3D.setCycleCount(Animation.INDEFINITE);
        rotate3D.setInterpolator(Interpolator.LINEAR);
        rotate3D.play();

        root.getChildren().add(orb);
    }

    // ===================== 2D PLANETARY RINGS =====================
    private void createRings() {
        Random rnd = new Random();
        double centerX = 160;
        double centerY = 160;

        for (int i = 0; i < 6; i++) {
            Circle ring = new Circle(centerX, centerY, 40 + i * 12);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(COLORS[0][i < 3 ? 0 : 1]);
            ring.setStrokeWidth(2);
            ring.setOpacity(0.7);
            ring.setEffect(new Glow(0.8));

            // random tilt & scale for elliptical look
            ring.setScaleY(0.3 + rnd.nextDouble() * 0.3);
            ring.setRotate(rnd.nextDouble() * 360);

            // random spin left/right/up/down
            RotateTransition spin = new RotateTransition(Duration.seconds(5 + rnd.nextDouble() * 5), ring);
            spin.setByAngle(rnd.nextBoolean() ? 360 : -360);
            spin.setCycleCount(Animation.INDEFINITE);
            spin.setInterpolator(Interpolator.LINEAR);
            spin.play();

            // pulse size slightly
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(2 + rnd.nextDouble() * 2), ring);
            pulse.setFromX(1);
            pulse.setFromY(1);
            pulse.setToX(1 + rnd.nextDouble() * 0.05);
            pulse.setToY(1 + rnd.nextDouble() * 0.05);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            // color animation
            Timeline colorAnim = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(ring.strokeProperty(), ring.getStroke())),
                    new KeyFrame(Duration.seconds(2 + rnd.nextDouble() * 2),
                            new KeyValue(ring.strokeProperty(), Color.hsb(rnd.nextDouble() * 360, 0.7, 1.0)))
            );
            colorAnim.setAutoReverse(true);
            colorAnim.setCycleCount(Animation.INDEFINITE);
            colorAnim.play();

            rings.add(ring);
            root.getChildren().add(ring);
        }
    }

    // ===================== STATE CONTROL =====================
    public static void setState(AssistantState state) {
        if (!initialized || state == currentState) return;
        currentState = state;

        Platform.runLater(() -> {
            Color[] palette = COLORS[state.ordinal()];

            ((PhongMaterial) orb.getMaterial()).setDiffuseColor(palette[0]);

            for (int i = 0; i < rings.size(); i++) {
                rings.get(i).setStroke(palette[i < 3 ? 0 : 1]);
            }

            pulseOrb();
        });
    }

    private static void pulseOrb() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(400), orb);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    // ===================== VISIBILITY =====================
    public static void show() {
        if (!initialized) return;
        Platform.runLater(stage::show);
    }

    public static void hide() {
        if (!initialized) return;
        Platform.runLater(stage::hide);
    }

    public static void wakeUp() {
        show();
        setState(AssistantState.LISTENING);
    }

    // ===================== DRAG =====================
    private void enableDrag() {
        final double[] offset = new double[2];

        root.setOnMousePressed(e -> {
            offset[0] = e.getScreenX() - stage.getX();
            offset[1] = e.getScreenY() - stage.getY();
        });

        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });
    }

    // ===================== POSITION =====================
    private void positionTopRight() {
        Platform.runLater(() -> {
            var bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMaxX() - 360);
            stage.setY(40);
        });
    }
}
