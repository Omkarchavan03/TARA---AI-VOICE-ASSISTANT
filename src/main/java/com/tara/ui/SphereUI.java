package com.tara.ui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SphereUI extends Application {

    public enum AssistantState {
        IDLE, LISTENING, THINKING, SPEAKING, SLEEP
    }

    private static Stage stage;
    private static Pane root;
    private static boolean initialized = false;
    private static AssistantState currentState = AssistantState.IDLE;

    private static Circle orb; // central circle (2D now)
    private static final List<Circle> rings = new ArrayList<>();

    private static final Color[][] COLORS = {
            { Color.web("#191970"), Color.web("#1F51FF") }, // IDLE
            { Color.web("#9649CB"), Color.web("#FF00FF") }, // LISTENING
            { Color.web("#E3856B"), Color.web("#FFD93D") }, // THINKING
            { Color.web("#3CD070"), Color.web("#50C878") }  // SPEAKING
    };

    public static void launchUI() {
        new Thread(() -> Application.launch(SphereUI.class)).start();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        root = new Pane();
        root.setPrefSize(320, 320);

        Scene scene = new Scene(root, 320, 320);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);

        createOrb();
        createRings();
        enableDrag();
        positionTopRight();

        stage.hide();
        initialized = true;
    }

    // ===================== CENTRAL ORB =====================
    private void createOrb() {
        orb = new Circle(26, COLORS[0][0]);
        orb.setCenterX(160);
        orb.setCenterY(160);
        root.getChildren().add(orb);
    }

    // ===================== 3 RINGS =====================
    private void createRings() {
        Random rnd = new Random();
        double centerX = 160;
        double centerY = 160;

        for (int i = 0; i < 3; i++) {
            Circle ring = new Circle(centerX, centerY, 40 + i * 12);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(COLORS[0][i % 2]);
            ring.setStrokeWidth(2);
            ring.setOpacity(0.7);
            ring.setEffect(new Glow(0.7));

            // tilt for elliptical look
            ring.setScaleY(0.5 + rnd.nextDouble() * 0.3);
            ring.setRotate(rnd.nextDouble() * 360);

            // spin animation
            RotateTransition spin = new RotateTransition(Duration.seconds(5 + rnd.nextDouble() * 5), ring);
            spin.setByAngle(rnd.nextBoolean() ? 360 : -360);
            spin.setCycleCount(Animation.INDEFINITE);
            spin.setInterpolator(Interpolator.LINEAR);
            spin.play();

            rings.add(ring);
            root.getChildren().add(ring);
        }
    }

    // ===================== STATE CONTROL =====================
    public static void setState(AssistantState state) {
        if (!initialized || state == currentState) return;
        currentState = state;

        Platform.runLater(() -> {
            if (state == AssistantState.SLEEP) {
                stage.hide();
                return;
            }

            Color[] palette = COLORS[state.ordinal()];

            orb.setFill(palette[0]);

            for (int i = 0; i < rings.size(); i++) {
                rings.get(i).setStroke(palette[i % 2]);
            }

            pulseOrb();
            show();
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
        setState(AssistantState.LISTENING);
    }

    public static void sleep() {
        setState(AssistantState.SLEEP);
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
