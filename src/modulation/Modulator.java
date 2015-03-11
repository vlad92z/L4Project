package modulation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import demodulation.Demodulator;
import demodulation.StaticValues;
import main.Main;

public class Modulator {

	private float increment;
	private float angle;
	private int samplesPerBit;
	float frequency;

	public Modulator(int samplesPerBit, float frequency) {
		this.samplesPerBit = samplesPerBit;
		this.frequency = frequency;
		angle = 0;
		increment = (float) (2 * Math.PI) * frequency / StaticValues.SAMPLING_RATE;
	}

	/**
	 *
	 * @return an array with the bits 1110101 modulated using amplitude
	 *         modulation samples per bit = 20 frequency = 7080 modulation index
	 *         = 0.1
	 */
	public float[] modulateStartingSequence() {
		float[] output = new float[StaticValues.STARTING_SEQ.length
				* StaticValues.INITIAL_SPB];// initialise starting sequence
		int samplesIndex = 0;
		float alpha;
		for (char c : StaticValues.STARTING_SEQ) {// for every bit in the byte

			if (c == '1')// set modulation index to 1 or the initial index
				alpha = 1;
			else
				alpha = StaticValues.INITIAL_MOD_INDEX;

			increment = (float) (2 * Math.PI) * StaticValues.INITIAL_FREQUENCY
					/ StaticValues.SAMPLING_RATE;
			for (int i = 0; i < StaticValues.INITIAL_SPB; i++) {// repeat for
																// samplesPerBit
				angle += increment; // increment angle
				float waveValue = alpha * (float) Math.sin(angle);
				output[samplesIndex] = waveValue;
				samplesIndex++;
			}
		}
		return output;
	}

	public float[] modulateParameters(String modulation, int bps,
			float frequency, double modIndex, String encoding)
			throws UnsupportedEncodingException {
		String params = modulation + String.format("%03d", bps)
				+ String.format("%02d", (int) (frequency / 100))
				+ String.format("%03d", (int) (modIndex * 10)) + encoding;
		switch (encoding) {
		case StaticValues.ENCODING_UTF8:
			encoding = StaticValues.ENCODING_UTF8_VAL;
			break;
		case StaticValues.ENCODING_UTF16:
			encoding = StaticValues.ENCODING_UTF16_VAL;
			break;
		default:
			encoding = StaticValues.ENCODING_UTF8_VAL;
			break;
		}
		byte message[] = params.getBytes(encoding);
		float[] output = new float[message.length * 7 * StaticValues.INITIAL_SPB];
		int samplesIndex = 0;
		for (byte value : message) {
			samplesIndex = paramDigit(value, output, samplesIndex);
		}
		return output;
	}

	public int paramDigit(Byte byteToEncode, float[] output,
			int samplesIndex) {
		float alpha;
		for (char c : String
				.format("%7s", Integer.toBinaryString(byteToEncode))
				.replace(' ', '0').toCharArray()) {// for every bit
			// in the byte
			if (c == '1')
				alpha = 1;
			else
				alpha = (float) StaticValues.INITIAL_MOD_INDEX;
			increment = (float) (2 * Math.PI) * StaticValues.INITIAL_FREQUENCY / StaticValues.SAMPLING_RATE;
			for (int i = 0; i < StaticValues.INITIAL_SPB; i++) {// repeat for samplesPerBit
				angle += increment; // increment angle
				float waveValue = alpha * (float) Math.sin(angle);
				output[samplesIndex] = (float) (waveValue);
				samplesIndex++;
			}
		}
		return samplesIndex;
	}

	/**
	 *
	 * @param message
	 *            to be encoded
	 * @param modIndex
	 *            modulation index
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public float[] modulateAmplitude(String myString, double modIndex,
			String encoding) throws UnsupportedEncodingException {

		byte message[] = (myString + StaticValues.FINAL_STRING)
				.getBytes(encoding);

		float[] output = new float[message.length * 7 * samplesPerBit];

		int samplesIndex = 0;
		for (byte value : message) {
			samplesIndex = amplitudeDigit(value, output, samplesIndex, modIndex);
		}
		float[] params = modulateParameters(StaticValues.MODULATION_AM,
				samplesPerBit, frequency, 10*modIndex,
				StaticValues.ENCODING_UTF8);
		float[] startingSequence = modulateStartingSequence();
		return concat(concat(startingSequence, params), output);
	}

	public int amplitudeDigit(Byte byteToEncode, float[] output,
			int samplesIndex, double modIndex) {
		float alpha;
		for (char c : String
				.format("%7s", Integer.toBinaryString(byteToEncode))
				.replace(' ', '0').toCharArray()) {// for every bit
			// in the byte
			if (c == '1')// set frequency
				alpha = 1;
			else
				alpha = (float) modIndex;
			increment = (float) (2 * Math.PI) * frequency / StaticValues.SAMPLING_RATE;
			for (int i = 0; i < samplesPerBit; i++) {// repeat for samplesPerBit
				angle += increment; // increment angle
				float waveValue = alpha * (float) Math.sin(angle);
				output[samplesIndex] = (float) (waveValue);// +
															// randomDouble(0.2));
				samplesIndex++;
			}
		}
		return samplesIndex;
	}

	/**
	 *
	 * @param message
	 *            message to be encoded
	 * @param modCoefficient
	 *            multiplier for frequency modulation
	 * @return an array of doubles representing the modulated sound wave
	 * @throws UnsupportedEncodingException
	 */
	public float[] modulateFrequency(String myString, double modIndex,
			String encoding)
			throws UnsupportedEncodingException {
		
		byte message[] = (myString + StaticValues.FINAL_STRING)
				.getBytes(encoding);

		float[] output = new float[message.length * 7 * samplesPerBit];

		int samplesIndex = 0;
		for (byte value : message) {
			samplesIndex = frequencyDigit(value, output, samplesIndex, modIndex);
		}
		float[] params = modulateParameters(StaticValues.MODULATION_FM,
				samplesPerBit, frequency, modIndex,
				StaticValues.ENCODING_UTF8);
		float[] startingSequence = modulateStartingSequence();
		return output;
//		return concat(concat(startingSequence, params), output);
	}

	/**
	 *
	 * Modulates a byte on to the sine wave, represented as an array of doubles
	 * 
	 * @param byteToEncode
	 *            this is the byte to be encoded
	 * @param output
	 *            array representing the final signal
	 * @param samplesIndex
	 *            current index in the output array
	 * @param modCoefficient
	 *            this is the coefficient by which frequency is multiplied for
	 *            modulation
	 * @return next index in the output array
	 */
	public int frequencyDigit(byte byteToEncode, float[] output,
			int samplesIndex, double modCoefficient) {
		double alpha; // is either 1 or modCoefficient depending on the bit
		// for every bit in the byte
		for (char c : String
				.format("%7s", Integer.toBinaryString(byteToEncode))
				.replace(' ', '0').toCharArray()) {
			if (c == '1')// set frequency
				alpha = 1;
			else
				alpha = modCoefficient;
			increment = (float) (2 * Math.PI * alpha) * frequency
					/ StaticValues.SAMPLING_RATE;
			for (int i = 0; i < samplesPerBit; i++) {// repeat for samplesPerBit
				angle += increment; // increment angle
				float waveValue = (float) Math.sin(angle);
				output[samplesIndex] = (float) (waveValue);// write the value to
															// the output
				samplesIndex++;
			}
		}
		return samplesIndex;
	}

	public float[] modulatePhase(String myString)
			throws UnsupportedEncodingException {
		byte message[] = (myString + "stop").getBytes("UTF-8");
		float[] output = new float[message.length * 7 * samplesPerBit];
		int samplesIndex = 0;
		for (byte value : message) {
			samplesIndex = phaseDigit(value, output, samplesIndex);
		}
		float[] startingSequence = modulateStartingSequence();
		return concat(startingSequence, output);
	}

	public int phaseDigit(byte byteToEncode, float[] output, int samplesIndex) {
		double alpha;
		for (char c : Integer.toBinaryString(byteToEncode).toCharArray()) {// for
																			// every
																			// bit
			// in the byte
			if (c == '1')// set frequency
				alpha = 1;
			else
				alpha = -1;
			increment = (float) (2 * Math.PI * alpha) * frequency
					/ StaticValues.SAMPLING_RATE;
			for (int i = 0; i < samplesPerBit; i++) {// repeat for samplesPerBit
				angle += increment; // increment angle
				float waveValue = (float) Math.sin(angle);
				output[samplesIndex] = waveValue;
				samplesIndex++;
			}
		}
		return samplesIndex;
	}

	public float[] concat(float[] a, float[] b) {
		int aLen = a.length;
		int bLen = b.length;
		float[] c = new float[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	public static double randomDouble(double max) {
		return (Math.random() - 0.5) / (0.5 / max); // +-max
	}

	public void movingAvg(float[] samples) {
		float sum = 0;
		for (int i = 0; i < samples.length; i++) {
			sum += 3 * samples[i];
			sum -= Demodulator.getValue(i - 1, samples);
			sum -= Demodulator.getValue(i - 2, samples);
			sum -= Demodulator.getValue(i - 5, samples);
		}
	}
}