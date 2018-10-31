package md.mmirzaghitov.chip8;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VM extends Application {

	private Display display = new Display(64, 32);
	private int[] memory = new int[4096];
	private int pc = 0x200;
	private int sc = 0;
	private int ireg = 0;

	private int[] registers = new int[16];
	private int[] stack = new int[16];

	private Random random = new Random();

	private Map<Integer, OPCodeProcessor> opcodeTable = new HashMap<>();

	{
		opcodeTable.put(0x0, this::_00NN);
		opcodeTable.put(0x1, this::_1NNN);
		opcodeTable.put(0x2, this::_2NNN);
		opcodeTable.put(0x3, this::_3NNN);
		opcodeTable.put(0x4, this::_4NNN);
		opcodeTable.put(0x5, this::_5NNN);
		opcodeTable.put(0x6, this::_6NNN);
		opcodeTable.put(0x7, this::_7NNN);
		opcodeTable.put(0x8, this::_8NNN);
		opcodeTable.put(0x9, this::_9NNN);
		opcodeTable.put(0xA, this::_ANNN);
		opcodeTable.put(0xB, this::_BNNN);
		opcodeTable.put(0xC, this::_CXNN);
		opcodeTable.put(0xD, this::_DXYN);

	}

	public VM() {
		System.out.println("Started");
	}

	public byte[] loadProgram(InputStream inputStream) throws IOException {

		return null;
	}

	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("CHIP-8-Emulator");

		Group root = new Group(display);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for (int x = 0; x < 100; x++) {
//					ireg = 0x200;
//					for (int i = 0x200; i < 0x200 + 5; i++) {
//						memory[i] = random.nextInt(0xFF);
//					}
//					processOpcode(0xD335);
//					try {
//						Thread.sleep(500);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
	}

	public void processOpcode(int opcode) {
		int prefix = (opcode & 0xF000) >> 12;
		OPCodeProcessor opCodeProcessor = opcodeTable.get(prefix);
		opCodeProcessor.process(opcode);
	}

	private void _00NN(int opcode) {
		int suffix = opcode & 0x000F;

		if (suffix == 0x0) {
			display.clearScreen();
		}
	}

	private void _1NNN(int opcode) {
		pc = opcode & 0x0FFF;
	}

	private void _2NNN(int opcode) {
		stack[++sc] = pc;
		pc = opcode & 0x0FFF;
	}

	private void _3NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		if (registers[x] == (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _4NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		if (registers[x] != (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _5NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		if (registers[x] == registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _6NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		registers[x] = opcode & 0x00FF;
		pc += 2;
	}

	private void _7NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		registers[x] += opcode & 0x00FF;
		pc += 2;
	}

	private void _8NNN(int opcode) {
		int suffix = opcode & 0x000F;

		if (suffix == 0x0) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[y];
			pc += 2;
		}

		if (suffix == 0x1) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] | registers[y];
			pc += 2;
		}

		if (suffix == 0x2) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] & registers[y];
			pc += 2;
		}

		if (suffix == 0x3) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			registers[x] = registers[x] ^ registers[y];
			pc += 2;
		}

		if (suffix == 0x4) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[x] + registers[y];
			if (res > 256) {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0x5) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[x] - registers[y];
			if (res < 0) {
				registers[0xF] = 0;
			} else {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0x6) {
			int x = (opcode & 0x0F00) >> 8;
			registers[0xF] = registers[x] & 1;
			registers[x] = registers[x] >> 1;
			pc += 2;
		}

		if (suffix == 0x7) {
			int x = (opcode & 0x0F00) >> 8;
			int y = (opcode & 0x00F0) >> 4;
			int res = registers[y] - registers[x];
			if (res < 0) {
				registers[0xF] = 0;
			} else {
				registers[0xF] = 1;
			}
			registers[x] = res & 0xFF;
			pc += 2;
		}

		if (suffix == 0xE) {
			int x = (opcode & 0x0F00) >> 8;
			registers[0xF] = registers[x] & 1;
			registers[x] = registers[x] >> 1;
			pc += 2;
		}
	}

	private void _9NNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		if (registers[x] != registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _ANNN(int opcode) {
		int addr = opcode & 0x0FFF;
		ireg = memory[addr];
		pc += 2;
	}

	private void _BNNN(int opcode) {
		int addr = opcode & 0x0FFF;
		pc = addr + registers[0];
	}

	private void _CXNN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int addr = opcode & 0x00FF;
		registers[x] = addr & random.nextInt(256);
	}

	private void _DXYN(int opcode) {
		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		int n = opcode & 0x000F;

		int lreg = ireg;
		int flip = 0;

		for (int i = 0; i < n; i++) {
			int val = memory[lreg + i];
			for (int j = 8; j > 0; j--) {
				flip |= display.setPixel(x + j, y + i, val >> j);
			}
		}

		display.render();
		registers[0xF] = flip;
		pc += 2;
	}

	private void _EXNN(int opcode) {

	}

	private void _FXNN(int opcode) {

	}

	private interface OPCodeProcessor {

		void process(int opCode);
	}

}
