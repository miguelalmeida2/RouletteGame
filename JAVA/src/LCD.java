import isel.leic.utils.Time;

public class LCD { // Escreve no LCD usando a interface a 4 bits.

    public static final int LINES = 2, COLS = 16; // Dimensão do display.
    private static final int NIBBLE_SIZE = 4, BYTE_SIZE = 8;
    public static final int NIBBLE_MASK = 0x0f, NIBBLE_MASK_SIZE = 0x10;

    private static final int FUNCTION_SET_TO8BIT = 0x03, FUNCTION_SET_TO4BIT = 0x02;
    private static final int FUNCTION_SET_2LINES = 0x28;
    private static final int DISPLAY_OFF = 0x08;
    private static final int CLEAR_DISPLAY = 0x01;
    private static final int ENTRY_MODE_SET_DIR_RIGHT = 0x06;
    private static final int SET_CGRAM_ADRESS = 0x40;
    private static final int FIRST_INIT_TIME = 15, SECOND_INIT_TIME = 5, THIRD_INIT_TIME = 1, WRITEBYTE_SLEEP_TIME = 10;
    private static final int LINE0 = 0x00, LINE1 = 0x40;
    private static final int CURSOR_ON = 0x0f, DISPLAY_ON = 0x0f;
    private static final int CURSOR_OFF = 0x0c;
    private static final int TIME_TO_WRITE_EACH_CHAR_ANIMATION = 25;

    // Define se a interface com o LCD é série ou paralela
    private static final boolean SERIAL_INTERFACE = true;

    public static void main(String[] args) {
        HAL.init();
        init();
        write(" Roulette Game ");
    }

    // Envia a sequência de iniciação para comunicação a 4 bits.
    public static void init() {
        Time.sleep(FIRST_INIT_TIME);
        writeNibble(false,FUNCTION_SET_TO8BIT);
        Time.sleep(SECOND_INIT_TIME);
        writeNibble(false,FUNCTION_SET_TO8BIT);
        Time.sleep(THIRD_INIT_TIME);
        writeNibble(false,FUNCTION_SET_TO8BIT);
        writeNibble(false,FUNCTION_SET_TO4BIT);
        writeCMD(FUNCTION_SET_2LINES);
        writeCMD(DISPLAY_OFF);
        writeCMD(CLEAR_DISPLAY);
        writeCMD(ENTRY_MODE_SET_DIR_RIGHT);
        writeCMD(DISPLAY_ON);
    }

    // Escreve um nibble de comando/dados no LCD em paralelo
    private static void writeNibbleParallel(boolean rs, int data) {
        if(rs) HAL.writeBits(NIBBLE_MASK_SIZE,0x10);
        else HAL.writeBits(NIBBLE_MASK_SIZE,0);
        HAL.writeBits(0x20,0x20);
        HAL.writeBits(NIBBLE_MASK,data);
        HAL.writeBits(0x20,0);
    }

    // Escreve um nibble de comando/dados no LCD em série
    private static void writeNibbleSerial(boolean rs, int data) {
        data &= NIBBLE_MASK;
        data <<= 1;
        if (rs) data |= 0x1;
        SerialEmitter.send(SerialEmitter.Destination.LCD, data);
    }

    // Escreve um nibble de comando/dados no LCD
    private static void writeNibble(boolean rs, int data) {
        if(!SERIAL_INTERFACE) writeNibbleParallel(rs,data);
        else writeNibbleSerial(rs,data);
    }

    // Escreve um byte de comando/dados no LCD
    private static void writeByte(boolean rs, int data) {
         int highData = ((data >>> NIBBLE_SIZE) & NIBBLE_MASK); //Parte Alta do data
         int lowData = (data & NIBBLE_MASK);                    //Parte Baixa do data
         writeNibble(rs,highData);
         writeNibble(rs,lowData);
         Time.sleep(WRITEBYTE_SLEEP_TIME);
    }

    // Escreve um comando no LCD
    private static void writeCMD(int data) {
        writeByte(false,data);
    }

    // Escreve um dado no LCD
    private static void writeDATA(int data) {
        writeByte(true,data);
    }

    // Escreve um caráter na posição corrente.
    public static void write(char c) {
        writeDATA(c);
        Time.sleep(TIME_TO_WRITE_EACH_CHAR_ANIMATION);
    }

    // Escreve uma string na posição corrente.
    public static void write(String txt) {
        for (int i = 0; i < txt.length(); i++) {
            write(txt.charAt(i));
        }
    }

    // Envia comando para posicionar cursor (‘lin’:0..LINES-1 , ‘col’:0..COLS-1)
    public static void cursor(int lin, int col) {
        writeCMD(0x80 + (lin==1?LINE1:LINE0) + col);
    }

    // Envia comando para limpar o ecrã e posicionar o cursor em (0,0)
    public static void clear() {
        writeCMD(CLEAR_DISPLAY); // Clear Display e Coloca o cursor na posicao 0,0
    }

    public static void saveCustomChar(int charNum){
        writeCMD( SET_CGRAM_ADRESS+(charNum*BYTE_SIZE));
        for(int i = 0; i < BYTE_SIZE; i++) {
            writeByte(true,RouletteGameApp.specialChar[i+(charNum*BYTE_SIZE)]);
        }
    }

    public static void customChar(int charNum){ writeByte(true,charNum); }

    public static void customChar(int charNum,int line,int col){
        cursor(line,col);
        writeByte(true,charNum);
    }

    public static void displayCursor(boolean cursor){
        if (cursor) writeCMD(CURSOR_ON);
        else writeCMD(CURSOR_OFF);
    }
}