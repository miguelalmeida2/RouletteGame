import isel.leic.UsbPort;

// Virtualiza o acesso ao sistema UsbPort
public class HAL {

    private static int lastValue;

    public static void main(String[] args) {
        init();
    }

    // Inicia a classe
    public static void init() { out(lastValue = 0); }

    // Retorna true se o bit tiver o valor lógico ‘1’
    public static boolean isBit(int mask){
        return readBits(mask) != 0;
    }

    // Retorna os valores dos bits representados por mask presentes no UsbPort
    public static int readBits(int mask) {
      return (~UsbPort.in() & mask);
    }

    // Escreve nos bits representados por mask o valor de value
    public static void writeBits(int mask, int value) {
        clrBits(mask);
        setBits(value & mask);
    }

    // Coloca os bits representados por mask no valor lógico ‘1’
    public static void setBits(int mask) {
        out(lastValue |= mask);
    }

    // Coloca os bits representados por mask no valor lógico ‘0’
    public static void clrBits(int mask){
        out(lastValue &= ~mask);
    }

    private static void out(int val){
        UsbPort.out(~val);
    }

}

