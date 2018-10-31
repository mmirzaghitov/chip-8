package md.mmirzaghitov.chip8;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Display extends Application {

    private static final int DEFAULT_SCALE = 12;

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("CHIP-8-Emulator");

        Group root = new Group();
        Canvas canvas = new Canvas(64 * DEFAULT_SCALE, 32 * DEFAULT_SCALE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);


        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 400);

        gc.setFill(Color.WHITE);
        gc.fillRect(10 * DEFAULT_SCALE, 20 * DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
