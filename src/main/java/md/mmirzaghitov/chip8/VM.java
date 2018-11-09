package md.mmirzaghitov.chip8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VM extends Application {

	private Display display = new Display(64, 32);
	private Keyboard keyboard = new Keyboard();
	private int[] memory = new int[4096];
	private Cpu cpu = new Cpu(memory, display, keyboard);

	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	public VM() {
		System.out.println("Started");
	}

	public void loadProgram(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[4096];
		int size;
		while ((size = inputStream.read(buffer)) != -1) {
			for (int i = 0; i < size; i += 1) {
				memory[0x200 + i] = buffer[i] & 0xFF;
			}
		}
	}

	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("CHIP-8-Emulator");

		Group root = new Group(display);
		Scene scene = new Scene(root);

		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

		loadProgram(new FileInputStream("src/test/resources/INVADERS"));
		ScheduledFuture<?> scheduledFuture = executorService
			 .scheduleAtFixedRate(cpu, 0, 1000 / 60, TimeUnit.MILLISECONDS);
		primaryStage.setOnCloseRequest(e -> executorService.shutdownNow());

		scene.setOnKeyPressed(keyboard::keyPressed);
	//	scene.setOnKeyReleased(keyboard::keyReleased);
	}

}
