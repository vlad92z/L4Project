package demodulation;

public class StaticValues {

	public static final String MODULATION_FM = "f";
	public static final String MODULATION_AM = "a";
	public static final String MODULATION_PSK = "p";
	
	public static final String ENCODING_UTF8 = "08";
	public static final String ENCODING_UTF16 = "16";
	public static final String ENCODING_UTF8_VAL = "UTF-8";
	public static final String ENCODING_UTF16_VAL = "UTF-16";
	
	public static final char[] STARTING_SEQ = { '1', '1', '1', '0', '1', '0', '1'};
	public static final int[] STARTING_SEQ_INT = { 1, 1, 1, 0, 1, 0, 1};
	
	public static final byte[] FINAL_SEQUENCE = { 115, 116, 111, 112 };
	public static final String FINAL_STRING = "stop";
	public static final int PARAMETERS_LENGTH = 11;
	
	public static final int INITIAL_SPB = 20;
	public static final float INITIAL_MOD_INDEX = (float) 0.1;
	public static final float INITIAL_FREQUENCY = 7080;
	
	public static final int SAMPLING_RATE = 22050;
	
}
