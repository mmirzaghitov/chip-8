package md.mmirzaghitov.chip8;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Cpu implements Runnable {

	private final int[] memory;
	private final Display display;
	private final Keyboard keyboard;

	private int pc = 0x200;
	private int sc = 0;
	private int ireg = 0;

	private int dt;
	private int st;

	private int[] registers = new int[16];
	private int[] stack = new int[16];

	private Random random = new Random();

	private long cycleCount;

	private boolean draw;

	private OPCodeProcessor[] opcodeTable = new OPCodeProcessor[]{
		 this::_00NN, this::_1NNN, this::_2NNN, this::_3NNN,
		 this::_4NNN, this::_5NNN, this::_6NNN, this::_7NNN,
		 this::_8NNN, this::_9NNN, this::_ANNN, this::_BNNN,
		 this::_CXNN, this::_DXYN, this::_EXNN, this::_FXNN
	};

	public Cpu(int[] memory, Display display, Keyboard keyboard) {
		this.memory = memory;
		this.display = display;
		this.keyboard = keyboard;
	}

	@Override
	public void run() {
		int opcode = fetchOpcode();
		//	System.out.println(Integer.toString(opcode, 16));
		processOpcode(opcode);

		if (cycleCount > 8) {
			if (draw) {
				display.render();
				draw = false;
			}

			if (dt > 0) {
				dt--;
			}

			if (st > 0) {
				st--;
			}
			cycleCount = 0;
		}
		cycleCount++;
	}

	private int fetchOpcode() {
		return (memory[pc] << 8) | memory[pc + 1];
	}

	private void processOpcode(int opcode) {
		try {
			int prefix = (opcode & 0xF000) >>> 12;
			OPCodeProcessor opCodeProcessor = opcodeTable[prefix];
			opCodeProcessor.process(opcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _00NN(int opcode) {
		int suffix = opcode & 0x000F;

		switch (suffix) {
			case 0x0:
				display.clearScreen();
				break;

			case 0xE:
				pc = stack[sc--];
				break;
			default:
				throw new IllegalArgumentException("Not supported opcode");
		}
		pc += 2;
	}

	private void _1NNN(int opcode) {
		pc = opcode & 0x0FFF;
	}

	private void _2NNN(int opcode) {
		stack[++sc] = pc;
		pc = opcode & 0x0FFF;
	}

	private void _3NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		if (registers[x] == (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _4NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		if (registers[x] != (opcode & 0x00FF)) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _5NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int y = (opcode & 0x00F0) >>> 4;
		if (registers[x] == registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _6NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		registers[x] = opcode & 0x00FF;
		pc += 2;
	}

	private void _7NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		registers[x] = (registers[x] + (opcode & 0xFF)) & 0xFF;
		pc += 2;
	}

	private void _8NNN(int opcode) {
		int suffix = opcode & 0x000F;
		int x = (opcode & 0x0F00) >>> 8;
		int y = (opcode & 0x00F0) >>> 4;

		switch (suffix) {
			case 0x0:
				registers[x] = registers[y];
				pc += 2;
				break;

			case 0x1:
				registers[x] = registers[x] | registers[y];
				pc += 2;
				break;

			case 0x2:
				registers[x] = registers[x] & registers[y];
				pc += 2;
				break;

			case 0x3:
				registers[x] = registers[x] ^ registers[y];
				pc += 2;
				break;

			case 0x4: {
				int res = registers[x] + registers[y];
				if (res > 0xFF) {
					registers[0xF] = 1;
				} else {
					registers[0xF] = 0;
				}
				registers[x] = res & 0xFF;
				pc += 2;
				break;
			}

			case 0x5:
				int res = registers[x] - registers[y];
				if (res < 0) {
					registers[0xF] = 0;
				} else {
					registers[0xF] = 1;
				}
				registers[x] = res & 0xFF;
				pc += 2;
				break;

			case 0x6:
				registers[0xF] = registers[x] & 1;
				registers[x] = registers[x] >>> 1;
				pc += 2;
				break;

			case 0x7: {
				int res1 = registers[y] - registers[x];
				if (res1 < 0) {
					registers[0xF] = 0;
				} else {
					registers[0xF] = 1;
				}
				registers[x] = res1 & 0xFF;
				pc += 2;
				break;
			}

			case 0xE: {
				registers[0xF] = registers[x] & 1;
				registers[x] = (registers[x] << 1) & 0xFF;
				pc += 2;
				break;
			}

			default: {
				throw new IllegalArgumentException("Not supported opcode");
			}
		}
	}

	private void _9NNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int y = (opcode & 0x00F0) >>> 4;
		if (registers[x] != registers[y]) {
			pc += 4;
		} else {
			pc += 2;
		}
	}

	private void _ANNN(int opcode) {
		ireg = opcode & 0x0FFF;
		pc += 2;
	}

	private void _BNNN(int opcode) {
		int addr = opcode & 0x0FFF;
		pc = addr + registers[0];
	}

	private void _CXNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int addr = opcode & 0x00FF;
		registers[x] = (addr & random.nextInt(256)) & 0xFF;
		pc += 2;
	}

	private void _DXYN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int y = (opcode & 0x00F0) >>> 4;
		int n = opcode & 0x000F;

		int flip = 0;

		for (int i = 0; i < n; i++) {
			int val = memory[ireg + i];
			for (int j = 7; j >= 0; j--) {
				int the_x = (registers[x] + 7 - j) & 63;
				int the_y = (registers[y] + i) & 31;
				int pixelVal = (val >> j) & 1;
//				if (the_x < 64 && the_y < 32) {
				flip |= display.setPixel(the_x, the_y, pixelVal);
//				}
			}
		}
		registers[0xF] = flip;
		pc += 2;
		draw = true;
	}

	private void _EXNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int suffix = opcode & 0xF;
		System.out.println("===================" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		switch (suffix) {
			case 0xE:
				if (keyboard.isKeyPressed(registers[x])) {
					System.out.println("pressed");
					pc += 2;
				}
				break;

			case 0x1:
				if (!keyboard.isKeyPressed(registers[x])) {
					pc += 2;
				}
				break;

			default:
				throw new IllegalArgumentException("Unsupported opcode");
		}
		pc += 2;
	}

	private void _FXNN(int opcode) {
		int x = (opcode & 0x0F00) >>> 8;
		int suffix = opcode & 0xFF;

		switch (suffix) {

			case 0x07:
				registers[x] = dt & 0xFF;
				break;
			case 0x0A:
				registers[x] = keyboard.waitUntilPressed();
				break;
			case 0x15:
				dt = registers[x];
				break;
			case 0x18:
				st = registers[x];
				break;
			case 0x1E:
				ireg += registers[x];
				break;
			case 0x29:
				System.out.println("Not implemented");
				break;
			case 0x33:
				memory[ireg] = registers[x] / 100;
				memory[ireg + 1] = (registers[x] % 100) / 10;
				memory[ireg + 2] = (registers[x] % 100) % 10;
				break;
			case 0x55:
				for (int i = 0; i <= x; i++) {
					memory[ireg + i] = registers[i];
				}
				break;
			case 0x65:
				for (int i = 0; i <= x; i++) {
					registers[i] = memory[ireg + i] & 0xFF;
				}
				break;
			default:
				throw new IllegalArgumentException("Not implemented");

		}
		pc += 2;
	}

	private interface OPCodeProcessor {

		void process(int opCode);
	}

}