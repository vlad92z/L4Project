package demodulation;

public class MovingAvgBufferIterator {
	
	private LinkedBuffer currentBuffer;
	private int currentIndex;
	
	public MovingAvgBufferIterator(LinkedBuffer buffer, int index){
		this.currentBuffer = buffer;
		this.currentIndex = index;
	}
	
	public Float next(){
		if (currentIndex+1 >= currentBuffer.size()){
			if (currentBuffer.hasNext()){
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

}
