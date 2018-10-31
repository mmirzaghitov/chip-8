package md.mmirzaghitov.chip8;

import java.io.IOException;
import java.io.InputStream;

public class VM {

    private int[] memory = new int[4096];
    private int pc = 0x200;
    private int sc = 0;

    private int[] registers = new int[16];

    private int[] stack = new int[16];


    public static void main(String[] args) {
        Display.launch(Display.class, args);
    }

    public byte[] loadProgram(InputStream inputStream) throws IOException {

        return null;
    }

    public void processOpcode(int opcode) {

        int prefix = (opcode & 0xF000) >> 12;

        if (prefix == 0x0) {

        }

        if (prefix == 0x1) {
            pc = opcode & 0x0FFF;
        }

        if (prefix == 0x2) {
            stack[++sc] = pc;
            pc = opcode & 0x0FFF;
        }

        if (prefix == 0x3) {
            int x = (opcode & 0x0F00) >> 8;
            if (registers[x] == (opcode & 0x00FF)) {
                pc += 4;
            } else {
                pc += 2;
            }
        }

        if (prefix == 0x4) {
            int x = (opcode & 0x0F00) >> 8;
            if (registers[x] != (opcode & 0x00FF)) {
                pc += 4;
            } else {
                pc += 2;
            }
        }

        if (prefix == 0x5) {
            int x = (opcode & 0x0F00) >> 8;
            int y = (opcode & 0x00F0) >> 4;
            if (registers[x] == registers[y]) {
                pc += 4;
            } else {
                pc += 2;
            }
        }

        if (prefix == 0x6) {
            int x = (opcode & 0x0F00) >> 8;
            registers[x] = opcode & 0x00FF;
            pc += 2;
        }

        if (prefix == 0x7) {
            int x = (opcode & 0x0F00) >> 8;
            registers[x] += opcode & 0x00FF;
            pc += 2;
        }

        // ================================================= 0x8000====================================================

        if (prefix == 0x8) {
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

    }

}
