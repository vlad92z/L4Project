package demodulation;

public class LinkedBuffer {

    private float[] buffer;
    private LinkedBuffer previous;
    private LinkedBuffer next;

    public LinkedBuffer(float[] buffer, LinkedBuffer previous){
        this.buffer = buffer;
        this.previous = previous;
    }
    
    public int size(){
    	return buffer.length;
    }
    
    public boolean hasNext(){
    	return (next != null);
    }

    public LinkedBuffer(float[] buffer){
        this.buffer = buffer;
    }

    public float[] getBuffer() {
        return buffer;
    }

    public void setBuffer(float[] buffer) {
        this.buffer = buffer;
    }

    public LinkedBuffer getPrevious() {
        return previous;
    }

    public void setPrevious(LinkedBuffer previous) {
        this.previous = previous;
    }

    public LinkedBuffer next() {
        return next;
    }

    public void setNext(LinkedBuffer next) {
        this.next = next;
    }
}
