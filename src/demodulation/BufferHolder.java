package demodulation;

public class BufferHolder {

    private LinkedBuffer head;
    private LinkedBuffer tail;
    private int size;


    public BufferHolder(){
        size = 0;
    }

    public void addBuffer(LinkedBuffer buffer){
        if (head == null){
            this.head = buffer;
            this.tail = buffer;
        }
        else {
            tail.setNext(buffer);
            buffer.setPrevious(tail);
            tail = buffer;
        }
        size++;
    }

    public LinkedBuffer getFirst(){
        LinkedBuffer outBuffer = head;
        head = head.next();
        size--;
        return outBuffer;
    }

    public LinkedBuffer getHead() {
        return head;
    }

    public void setHead(LinkedBuffer head) {
        this.head = head;
    }

    public LinkedBuffer getTail() {
        return tail;
    }

    public void setTail(LinkedBuffer tail) {
        this.tail = tail;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
