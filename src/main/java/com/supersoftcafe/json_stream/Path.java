package com.supersoftcafe.json_stream;

import java.util.*;

public final class Path extends AbstractList<Path.Node> implements Cloneable {
    public static final String DUMMY_ATTRIBUTE_NAME = "-dummy-attribute-";
    public static final long   DUMMY_ARRAY_INDEX    = -1234567890l;

    private static final Node[] EMPTY_NODES_ARRAY = new Path.Node[0];
    private static final int INITIAL_ARRAY_SIZE = 8;

    private static final ArrayIndex[] COMMON_ARRAY_INDEXES;
    static {
        COMMON_ARRAY_INDEXES = new ArrayIndex[1000];
        for (int index = 1000; --index >= 0; )
            COMMON_ARRAY_INDEXES[index] = new ArrayIndex(index);
    }



    private Path.Node[] nodes ;
    private int          size ;
    private boolean  readOnly ;
    private Path readOnlyCopy ;



    public Path() {
        nodes = EMPTY_NODES_ARRAY;
    }

    public Path(Node... nodes) {
        this.size = (this.nodes = nodes.clone()).length;
        for (Node node : nodes) Objects.requireNonNull(node);
    }

    public Path(Collection<Node> collection) {
        this(collection.toArray(new Node[collection.size()]));
    }

    private Path(Node[] nodes, int size, boolean readOnly) {
        this.nodes = nodes;
        this.size = size;
        this.readOnly = readOnly;
    }


    public static Path valueOf(String pathString) {
        MatchRule matchRule = new MatchRule(pathString);

        Path path = new Path();
        for (Rule rule : matchRule) {
            if (rule instanceof RuleArrayIndex && ((RuleArrayIndex) rule).size() == 1) {
                path.pushArrayIndex(((RuleArrayIndex) rule).indexAt(0));
            } else if (rule instanceof RuleAttributeName && ((RuleAttributeName) rule).size() == 1) {
                path.pushAttributeName(((RuleAttributeName) rule).nameAt(0));
            } else {
                throw new IllegalArgumentException();
            }
        }

        return path;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("$");
        for (Node node : this) sb.append(node);
        return sb.toString();
    }


    public boolean isReadOnly() {
        return readOnly;
    }

    public Path readOnlyCopy() {
        Path copy = readOnlyCopy;
        if (copy != null) return copy;
        copy = new Path(nodes, size, true);
        return copy.readOnlyCopy = readOnlyCopy = copy;
    }

    public Path mutableCopy() {
        Path path = new Path(Arrays.copyOf(nodes, size), size, false);
        path.readOnlyCopy = readOnlyCopy;
        return path;
    }

    public @Override Path clone() {
        return readOnly ? readOnlyCopy() : mutableCopy();
    }



    @Override
    public int size() {
        return size;
    }

    @Override
    public Node get(int index) {
        return nodes[index];
    }

    public @Override void add(int index, Path.Node node) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        if (size >= nodes.length) nodes = Arrays.copyOf(nodes, size==0 ? INITIAL_ARRAY_SIZE : size*2);
        if (index < size) System.arraycopy(nodes, index, nodes, index + 1, size - index);
        nodes[index] = node;
        size++;
    }

    public @Override Node set(int index, Node element) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        Node oldElement = nodes[index];
        nodes[index] = element;
        return oldElement;
    }

    public @Override Node remove(int index) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        Node oldElement = nodes[index];
        if (index < size-1) System.arraycopy(nodes, index+1, nodes, index, size - index - 1);
        nodes[--size] = null; // To help GC
        return oldElement;
    }



    public void pushDummyArray() {
        pushArrayIndex(DUMMY_ARRAY_INDEX);
    }

    public void pushDummyObject() {
        pushAttributeName(DUMMY_ATTRIBUTE_NAME);
    }


    public Path.ArrayIndex popArray() {
        if (!peek().isArray()) {
            throw new IllegalStateException();
        }
        return (Path.ArrayIndex) pop();
    }

    public Path.AttributeName popObject() {
        if (!peek().isObject()) {
            throw new IllegalStateException();
        }
        return (Path.AttributeName) pop();
    }


    public void pushAttributeName(String name) {
        add(new AttributeName(name));
    }

    public void pushArrayIndex(long index) {
        add(index >= 0 && index < 1000 ? COMMON_ARRAY_INDEXES[(int) index] : new ArrayIndex(index));
    }


    public void updateArrayIndex(long index) {
        Path.Node node = peek();
        if (!node.isArray()) {
            throw new IllegalStateException();
        }
        pop();
        pushArrayIndex(index);
    }

    public void updateAttributeName(String name) {
        Path.Node node = peek();
        if (!node.isObject()) {
            throw new IllegalStateException();
        }
        pop();
        pushAttributeName(name);
    }

    public void advanceArrayIndex() {
        Path.Node node = peek();
        if (node != null && node.isArray()) {
            long index = pop().getIndex();
            pushArrayIndex(index != DUMMY_ARRAY_INDEX ? index + 1 : 0);
        }
    }



    public Node peek() {
        int size = size();
        return size == 0 ? null : get(size - 1);
    }

    public Node pop() {
        if (readOnly) throw new IllegalStateException();
        if (size == 0) throw new NoSuchElementException();
        readOnlyCopy = null;
        return nodes[--size];
    }




    public static abstract class Node {
        private Node() {
        }

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


    static final class ArrayIndex extends Node {
        private final long index;

        ArrayIndex(long index) {
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
    }

    static final class AttributeName extends Node {
        private final String name;

        AttributeName(String name) {
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
    }
}
