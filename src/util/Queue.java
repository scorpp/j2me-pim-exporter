package util;

import java.util.Vector;

/**
 * Queue data type implementation.
 */
public class Queue extends Vector {

    public Queue(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    public Queue(int initialCapacity) {
        super(initialCapacity);
    }

    public Queue() {
        super();
    }

    public void push(Object o) {
        addElement(o);
    }

    public Object pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue empty");
        }

        Object o = elementAt(0);
        removeElementAt(0);
        return o;
    }

    public Object peek() {
        return elementAt(0);
    }
}
