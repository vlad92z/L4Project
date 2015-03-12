package demodulation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import modulation.Modulator;

public class Demodulator {

	public static final String TAG = "DEMODULATOR";

	private BufferHolder bufferHolder;
	private int samplesPerBit;
	private double frequency;
	private LinkedBuffer currentBuffer;
	private Thread demodThread;

	private boolean messageReceived;

	/**
	 * Constructor creates new instance of buffer holder
	 */
	public Demodulator() {
		bufferHolder = new BufferHolder();
		messageReceived = true;
	}

	public BufferHolder getHolder() {
		return bufferHolder;
	}

	/**
	 * Adds a new linked buffer to the BufferHolder
	 * 
	 * @param buffer
	 *            of values to be added
	 */
	public void addBuffer(float[] buffer) {
		LinkedBuffer newTail = new LinkedBuffer(buffer);
		bufferHolder.addBuffer(newTail);
	}

	public void setMessageReceived(boolean value) {
		messageReceived = value;
	}

	/**
	 * Starts demodulation in a new thread
	 */
	public void startDemodulation() {
		if (messageReceived) { // no modulation in progress
			messageReceived = false;// modulation in progress
			demodThread = new Thread(new Runnable() {
				public void run() {
					demodulateStartingSequence();
				}
			}, "Demodulation Thread");
			demodThread.start();
		}
	}

	public void demodulateStartingSequence() {
		double divider = (1 + StaticValues.INITIAL_MOD_INDEX) * (2 / Math.PI)
				/ 2; // average of expected sum value between 0 and 1
		double sum = 0;// some of values for a bit

		LinkedBuffer current = null;
		while (current == null && !messageReceived) {
			current = bufferHolder.getHead();// loops until a buffer is added
			// will stop is messageReceived is changed
		}

		BufferIterator iterator = new BufferIterator(current, 0);

		while (!messageReceived) {// loops until message is received on
									// demodulation cancelled
			if (!iterator.hasNext()) {
				System.out.println("Out of buffers");
				return; // stop if we run out of buffers
			}
			Float value = iterator.next();
			sum = sum // update sum
					- Math.abs(iterator.getPrevious(StaticValues.INITIAL_SPB))
					+ Math.abs(value);

			// this means we hit a '1' so it might be the starting sequence
			if (sum / StaticValues.INITIAL_SPB > divider) {
				int currentMatch = 1;
				// creates a temp iterator, in case this is not the sequence
				BufferIterator tempIterator = new BufferIterator(
						iterator.getCurrentIndex(), iterator.getCurrentBuffer());
				int bitcounter = 0; // counts bits
				float tempsum = 0; // sum for this specific check
				for (;;) {// will break out of this loop when needed
					if (!tempIterator.hasNext()) {
						System.out.println("NO STARTING SEQUENCE");
						return;
					}
					tempsum += Math.abs(tempIterator.next());
					bitcounter++;
					if (bitcounter >= StaticValues.INITIAL_SPB) {
						if (tempsum / StaticValues.INITIAL_SPB > divider) {
							// then we have a 1
							if (1 == StaticValues.STARTING_SEQ_INT[currentMatch]) {
								// good move on
								currentMatch++;
								tempsum = 0;
								bitcounter = 0;
							} else {
								currentMatch = 0;
								break;
							}
						} else {// we have a 0
							if (0 == StaticValues.STARTING_SEQ_INT[currentMatch]) {
								// good move on
								currentMatch++;
								tempsum = 0;
								bitcounter = 0;
							} else {
								currentMatch = 0;
								break;
							}
						}
						// if sequence complete
						if (currentMatch == StaticValues.STARTING_SEQ.length) {
							// System.out.println("STARTING SEQUENCE");
							try {
								// demodulate parameters
								// continue with the same iterator
								String result = demodulateParams(tempIterator);
								System.out.println("Message: " + result);
								return;
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * Demodulates parameters for further demodulation Here it uses the default
	 * AM demodulation Uses iterator from demodulating the starting sequence
	 */
	public String demodulateParams(BufferIterator iterator)
			throws UnsupportedEncodingException {
		double divider = (1 + StaticValues.INITIAL_MOD_INDEX) * (2 / Math.PI)
				/ 2;
		int bitIndex = 0;// tracks number of bits in sum
		double sum = 0;
		String byteString = "";
		ArrayList<Byte> parameters = new ArrayList<Byte>();
		messageReceived = false;
		Float currentVal;
		while (!messageReceived) {
			currentVal = null;
			while (currentVal == null) {
				currentVal = iterator.next();
				if (currentVal == null) {
					messageReceived = true;
					break;
				}
			}
			if (messageReceived)
				break;
			sum += Math.abs(currentVal);
			bitIndex += 1;
			if (bitIndex >= StaticValues.INITIAL_SPB) {
				bitIndex = 0;
				if (sum / StaticValues.INITIAL_SPB > divider) {
					byteString += 1;
				} else
					byteString += 0;
				sum = 0;
				if (byteString.length() >= 7) {// reached a byte
					byte paramByte = Byte.parseByte(byteString, 2);
					parameters.add(paramByte);
					byteString = "";
					// if all parameters found
					if (parameters.size() >= StaticValues.PARAMETERS_LENGTH) {
						byte[] result = new byte[parameters.size()];
						// convert from Byte to primitive byte
						for (int i = 0; i < parameters.size(); i++) {
							result[i] = parameters.get(i).byteValue();
						}
						String s = new String(result,
								StaticValues.ENCODING_UTF8_VAL);

						//System.out.println("Params: " + s);
						samplesPerBit = Integer.parseInt(s.substring(1, 4));
						frequency = 100 * Integer.parseInt(s.substring(4, 7));
						int modIndex = Integer.parseInt(s.substring(7, 9));
						// for now ignore encoding s.substring(9, 11);
						String substring = s.substring(0, 1);
						if (substring.equals("a")) {// determine modulation
							return demodulateAmplitude(iterator, modIndex);
						} else if (substring.equals("f")) {
							return demodulateFrequency(iterator, modIndex);
						} else if (substring.equals("p")) {
							// add psk demod
						}
					}
				}
			}

		}
		// returns null if message was too short or unreadable
		return null;
	}

	public String demodulateAmplitude(BufferIterator iterator, double modIndex)
			throws UnsupportedEncodingException {
		modIndex = modIndex / 100;// adjust mod index for AM
		double divider = (1 + modIndex) * (2 / Math.PI) / 2;
		int finishMatch = 0;// tracks if finishing sequence is matched
		int bitIndex = 0;// tracks how many bits added to current byte
		double sum = 0;
		String byteString = "";// current byte as string
		ArrayList<Byte> byteMessage = new ArrayList<Byte>();// message in bytes
		Float currentVal;
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		while (!messageReceived) {
			// try to get the next value until we get one
			while (!iterator.hasNext()) {
				// break for now
				messageReceived = true;
				return null;

			}
			currentVal = iterator.next();
			sum += Math.abs(currentVal);
			bitIndex += 1;
			if (bitIndex >= samplesPerBit) {
				bitIndex = 0;
				if (sum / samplesPerBit > divider) {
					byteString += 1;
				} else {
					byteString += 0;
				}
				sum = 0;
				if (byteString.length() >= 7) {// full byte read
					byte currentByte = Byte.parseByte(byteString, 2);
					byteMessage.add(currentByte);
					byteString = "";// reset byte string
					// if possible final sequence
					if (StaticValues.FINAL_SEQUENCE[finishMatch] == currentByte) {
						finishMatch++;
						// if final sequence complete
						if (finishMatch == StaticValues.FINAL_SEQUENCE.length) {
							// stop and convert from ArrayList of Byte
							// to primitive byte array
							byte[] result = new byte[byteMessage.size()];
							for (int i = 0; i < byteMessage.size(); i++) {
								result[i] = byteMessage.get(i).byteValue();
							}
							// System.out.println("FINISHING SEQUENCE READ");
							// return string decoded using UTF-8
							String rString = new String(result, "UTF-8");
							// return removing final sequence
							return rString.substring(0, rString.length()
									- StaticValues.FINAL_SEQUENCE.length);

						}
					} else {// otherwise reset final sequence matching
						finishMatch = 0;
					}
				}
			}
			// System.out.println(currentVal);
		}
		// if reading was interrupted or no final sequence was found
		return null;
	}

	public String demodulateFrequency(BufferIterator iterator, double modIndex)
			throws UnsupportedEncodingException {
		modIndex = modIndex / 10;
		double divider = (1 + modIndex) * (2 / Math.PI) / 2;
		int finishMatch = 0;
		int bitIndex = 0;
		double sum = 0;
		String byteString = "";
		ArrayList<Byte> byteMessage = new ArrayList<Byte>();
		Float currentVal;
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();

		int orderIndex, filterOrder = 4;// filter order
		filterOrder = filterOrder / 2;
		double a = Math.tan(Math.PI * modIndex / 2);
		double a2 = a * a;
		double r;
		double[] A = new double[filterOrder];
		double[] d1 = new double[filterOrder];
		double[] d2 = new double[filterOrder];
		double[] w0 = new double[filterOrder];
		double[] w1 = new double[filterOrder];
		double[] w2 = new double[filterOrder];

		for (orderIndex = 0; orderIndex < filterOrder; ++orderIndex) {
			r = Math.sin(Math.PI * (2.0 * orderIndex + 1.0)
					/ (4.0 * filterOrder));
			frequency = a2 + 2.0 * a * r + 1.0;
			A[orderIndex] = 1.0 / frequency;
			d1[orderIndex] = 2.0 * (1 - a2) / frequency;
			d2[orderIndex] = -(a2 - 2.0 * a * r + 1.0) / frequency;
		}

		while (!messageReceived) {
			// try to get the next value until we get one
			while (!iterator.hasNext()) {
				// break for now
				messageReceived = true;
				return null;

			}
			
			currentVal = iterator.next();
			
			for (orderIndex = 0; orderIndex < filterOrder; ++orderIndex) {
				w0[orderIndex] = d1[orderIndex] * w1[orderIndex]
						+ d2[orderIndex] * w2[orderIndex] + currentVal;
				currentVal = (float) (A[orderIndex]
						* (w0[orderIndex] - 2.0 * w1[orderIndex] + w2[orderIndex]));
				w2[orderIndex] = w1[orderIndex];
				w1[orderIndex] = w0[orderIndex];
			}
			System.out.println(currentVal);
			
			
			sum += Math.abs(currentVal);
			bitIndex += 1;
			if (bitIndex >= samplesPerBit) {
				bitIndex = 0;
				//System.out.println(sum / samplesPerBit);
				if (sum / samplesPerBit > 0.15) {
					byteString += 0;
				} else
					byteString += 1;
				sum = 0;
				if (byteString.length() >= 7) {
					byte currentByte = Byte.parseByte(byteString, 2);
					byteMessage.add(currentByte);
					byteString = "";
					if (StaticValues.FINAL_SEQUENCE[finishMatch] == currentByte) {
						finishMatch++;
						if (finishMatch == StaticValues.FINAL_SEQUENCE.length) {
							byte[] result = new byte[byteMessage.size()];
							for (int index = 0; index < byteMessage.size(); index++) {
								result[index] = byteMessage.get(index).byteValue();
							}
							String rString = new String(result, "UTF-8");
							return rString.substring(0, rString.length()
									- StaticValues.FINAL_SEQUENCE.length);

						}
					} else {
						finishMatch = 0;
					}
				}
			}

		}
		return null;
	}

	/**
	 * returns the value at that index returns 0 if out of bounds
	 */
	public static double getValue(int index, float[] list) {
		if (index < 0 || index >= list.length)
			return 0;
		else
			return list[index];
	}
	
	/**
	 * returns the value at that index returns 0 if out of bounds
	 */
	public static double getValueFirst(int index, float[] list) {
		if (index < 0 || index >= list.length)
			return list[0];
		else
			return list[index];
	}

	/**
	 * returns the value at that index returns 0 if out of bounds
	 */
	public static double getValueInRange(int index, float[] list, int min) {
		if (index < min)
			return 0;
		else
			return list[index];
	}

	/**
	 * Returns start + next value, taking the next LinkedBuffer if necessary.
	 * Returns null if next value is not available
	 */
	public static Float getBufferValue(LinkedBuffer buffer, int start, int next) {
		if (start + next >= buffer.size()) {
			if (buffer.hasNext()) {
				return buffer.next().getBuffer()[start + next - buffer.size()];// todo
				// throws
				// an
				// error
			} else {
				return null;
			}
		} else
			return buffer.getBuffer()[start + next];
	}
}