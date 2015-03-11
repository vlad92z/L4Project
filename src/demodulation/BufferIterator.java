package demodulation;

public class BufferIterator {

	private LinkedBuffer currentBuffer;
	private int currentIndex;

	public BufferIterator(LinkedBuffer buffer, int index) {
		this.currentBuffer = buffer;
		this.currentIndex = index;
//		float[] temp = buffer.getBuffer();
//		next();
//		next();
//		next();
//		next();
//		next();
//		next();
//		next();
//		temp[currentIndex - 1] = 0;
//		temp[currentIndex - 2] = 0;
//		temp[currentIndex - 3] = 0;
//		temp[currentIndex - 4] = 0;
//		temp[currentIndex - 5] = 0;
	}
	
	public BufferIterator(int index, LinkedBuffer buffer) {
		this.currentBuffer = buffer;
		this.currentIndex = index;
		float[] temp = buffer.getBuffer();
	}

	public Float next() {
		if (currentIndex + 1 >= currentBuffer.size()) {
			if (currentBuffer.hasNext()) {
				currentIndex = 0;
				currentBuffer = currentBuffer.next();
				return currentBuffer.getBuffer()[currentIndex];
			} else {
				return null;
			}
		} else {
			currentIndex++;
			return currentBuffer.getBuffer()[currentIndex];
		}
	}

	public boolean hasNext() {
		if (currentIndex + 1 >= currentBuffer.size()
				&& !currentBuffer.hasNext())
			return false;
		else
			return true;
	}

	public Float getPrevious(int delay) {
		if (currentIndex - delay >= 0) {
			return currentBuffer.getBuffer()[currentIndex - delay];
		} else {
			LinkedBuffer previous = currentBuffer.getPrevious();
			if (previous == null)
				return new Float(0);
			else
				return previous.getBuffer()[previous.getBuffer().length
						- (delay - currentIndex)];
		}
	}

	public LinkedBuffer getCurrentBuffer() {
		return currentBuffer;
	}

	public void setCurrentBuffer(LinkedBuffer currentBuffer) {
		this.currentBuffer = currentBuffer;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}
	
	

}
