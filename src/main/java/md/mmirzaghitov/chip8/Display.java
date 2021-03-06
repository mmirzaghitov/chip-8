package md.mmirzaghitov.chip8;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Display extends Canvas {

	private static final int DEFAULT_SCALE = 12;

	private final int x;
	private final int y;
	private final int[][] grid;

	private GraphicsContext graphicsContext;

	public Display(int width, int height) {
		super(width * DEFAULT_SCALE, height * DEFAULT_SCALE);
		setFocusTraversable(true);
		this.x = width;
		this.y = height;
		this.grid = new int[y][x];
		this.graphicsContext = getGraphicsContext2D();
		clearScreen();
	}

	public void clearScreen() {
		graphicsContext.setFill(Color.BLACK);
		graphicsContext.fillRect(0, 0, getWidth(), getHeight());
	}

	public int setPixel(int x, int y, int val) {
		if (val == 1 && val == grid[y][x]) {
			return val;
		}
		grid[y][x] = val;
		return 0;
	}

	public void render() {
		clearScreen();
		graphicsContext.setFill(Color.WHITE);
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				if (grid[i][j] > 0) {
					graphicsContext.fillRect(j * DEFAULT_SCALE, i * DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE);
				}
			}
		}
	}

}
