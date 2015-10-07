package com.supersoftcafe.json_stream;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Path extends ArrayList<Path.Node> {
    public Path() {

    }

    public Path(List<Node> pathNodes) {
        super(pathNodes);
    }


    public Path copy() {
        return new Path(this);
    }


    public void pushAttributeName(String name) {
        add(new AttributeName(name));
    }

    public void pushArrayIndex(long index) {
        add(index>=0 && index<1000 ? ARRAY_INDEXES[(int)index] : new ArrayIndex(index));
    }



    public Node peek() {
        int size = size();
        return size == 0 ? null : get(size - 1);
    }

    public Node pop() {
        if (isEmpty()) throw new NoSuchElementException();
        return remove(size() - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("$");
        for (Node node : this) sb.append(node);
        return sb.toString();
    }




    public static abstract class Node {
        public abstract <T> T visit(Visitor<T> visitor);

        public boolean isArray() {
            return false;
        }

        public boolean isObject() {
            return false;
        }

        public String getName() {
            throw new UnsupportedOperationException();
        }

        public long getIndex() {
            throw new UnsupportedOperationException();
        }
    }


    public static final class ArrayIndex extends Node {
        private final long index;

        private ArrayIndex(long index) {
            this.index = index;
        }

        public boolean isArray() {
            return true;
        }

        public @Override long getIndex() {
            return index;
        }

        public String toString() {
            return "[" + index + "]";
        }

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static final class AttributeName extends Node {
        private final String name;

        private AttributeName(String name) {
            this.name = name;
        }

        public boolean isObject() {
            return true;
        }

        public @Override String getName() {
            return name;
        }

        public String toString() {
            return "." + name;
        }

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public interface Visitor<T> {
        T visit(AttributeName attributeName);
        T visit(ArrayIndex arrayIndex);
    }

    private static final ArrayIndex[] ARRAY_INDEXES;
    static {
        ARRAY_INDEXES = new ArrayIndex[1000];
        for (int index = 1000; --index >= 0; )
            ARRAY_INDEXES[index] = new ArrayIndex(index);
    }
}
