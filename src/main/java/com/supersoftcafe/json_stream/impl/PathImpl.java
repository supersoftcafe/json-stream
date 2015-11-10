package com.supersoftcafe.json_stream.impl;

import com.supersoftcafe.json_stream.Path;

import java.util.*;

public final class PathImpl extends AbstractList<Path.Node>
        implements Path, RandomAccess, Cloneable {

    public static final String DUMMY_ATTRIBUTE_NAME = "-dummy-attribute-";
    public static final long   DUMMY_ARRAY_INDEX    = -1234567890l;

    private static final NodeImpl[] EMPTY_NODES_ARRAY = new NodeImpl[0];
    private static final int INITIAL_ARRAY_SIZE = 8;

    private static final ArrayIndex[] COMMON_ARRAY_INDEXES;
    static {
        COMMON_ARRAY_INDEXES = new ArrayIndex[1000];
        for (int index = 1000; --index >= 0; )
            COMMON_ARRAY_INDEXES[index] = new ArrayIndex(index);
    }



    private NodeImpl[]  nodes ;
    private int          size ;
    private boolean  readOnly ;
    private PathImpl readOnlyCopy ;



    public PathImpl() {
        nodes = EMPTY_NODES_ARRAY;
    }

    public PathImpl(NodeImpl... nodes) {
        this.size = (this.nodes = nodes.clone()).length;
        for (NodeImpl node : nodes) Objects.requireNonNull(node);
    }

    public PathImpl(Collection<NodeImpl> collection) {
        this(collection.toArray(new NodeImpl[collection.size()]));
    }

    private PathImpl(NodeImpl[] nodes, int size, boolean readOnly) {
        this.nodes = nodes;
        this.size = size;
        this.readOnly = readOnly;
    }


    public static PathImpl valueOf(String pathString) {
        MatchRule matchRule = new MatchRule(pathString);

        PathImpl path = new PathImpl();
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
        for (Path.Node node : this) sb.append(node);
        return sb.toString();
    }


    public boolean isReadOnly() {
        return readOnly;
    }

    public PathImpl readOnlyCopy() {
        PathImpl copy = readOnlyCopy;
        if (copy != null) return copy;
        copy = new PathImpl(nodes, size, true);
        return copy.readOnlyCopy = readOnlyCopy = copy;
    }

    public PathImpl mutableCopy() {
        PathImpl path = new PathImpl(Arrays.copyOf(nodes, size), size, false);
        path.readOnlyCopy = readOnlyCopy;
        return path;
    }

    public @Override
    PathImpl clone() {
        return readOnly ? readOnlyCopy() : mutableCopy();
    }



    public @Override int size() {
        return size;
    }


    public @Override NodeImpl get(int index) {
        return nodes[index];
    }

    public @Override void add(int index, Node node) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        if (size >= nodes.length) nodes = Arrays.copyOf(nodes, size==0 ? INITIAL_ARRAY_SIZE : size*2);
        if (index < size) System.arraycopy(nodes, index, nodes, index + 1, size - index);
        nodes[index] = (NodeImpl)node;
        size++;
    }

    public @Override NodeImpl set(int index, Node element) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        NodeImpl oldElement = nodes[index];
        nodes[index] = (NodeImpl)element;
        return oldElement;
    }

    public @Override NodeImpl remove(int index) {
        if (readOnly) throw new IllegalStateException();
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        readOnlyCopy = null;
        NodeImpl oldElement = nodes[index];
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


    public PathImpl.ArrayIndex popArray() {
        if (!peek().isArray()) {
            throw new IllegalStateException();
        }
        return (PathImpl.ArrayIndex) pop();
    }

    public PathImpl.AttributeName popObject() {
        if (!peek().isAttribute()) {
            throw new IllegalStateException();
        }
        return (PathImpl.AttributeName) pop();
    }


    public void pushAttributeName(String name) {
        add(new AttributeName(name));
    }

    public void pushArrayIndex(long index) {
        add(index >= 0 && index < 1000 ? COMMON_ARRAY_INDEXES[(int) index] : new ArrayIndex(index));
    }


    public void updateArrayIndex(long index) {
        NodeImpl node = peek();
        if (!node.isArray()) {
            throw new IllegalStateException();
        }
        pop();
        pushArrayIndex(index);
    }

    public void updateAttributeName(String name) {
        NodeImpl node = peek();
        if (!node.isAttribute()) {
            throw new IllegalStateException();
        }
        pop();
        pushAttributeName(name);
    }

    public void advanceArrayIndex() {
        NodeImpl node = peek();
        if (node != null && node.isArray()) {
            long index = pop().getArrayIndex();
            pushArrayIndex(index != DUMMY_ARRAY_INDEX ? index + 1 : 0);
        }
    }



    public NodeImpl peek() {
        int size = size();
        return size == 0 ? null : get(size - 1);
    }

    public NodeImpl pop() {
        if (readOnly) throw new IllegalStateException();
        if (size == 0) throw new NoSuchElementException();
        readOnlyCopy = null;
        return nodes[--size];
    }




    public static abstract class NodeImpl implements Path.Node {
        private NodeImpl() {
        }

        public boolean isArray() {
            return false;
        }

        public boolean isAttribute() {
            return false;
        }

        public String getAttributeName() {
            throw new UnsupportedOperationException();
        }

        public long getArrayIndex() {
            throw new UnsupportedOperationException();
        }
    }


    static final class ArrayIndex extends NodeImpl {
        private final long index;

        ArrayIndex(long index) {
            this.index = index;
        }

        public boolean isArray() {
            return true;
        }

        public @Override long getArrayIndex() {
            return index;
        }

        public String toString() {
            return "[" + index + "]";
        }
    }

    static final class AttributeName extends NodeImpl {
        private final String name;

        AttributeName(String name) {
            this.name = name;
        }

        public boolean isAttribute() {
            return true;
        }

        public @Override String getAttributeName() {
            return name;
        }

        public String toString() {
            return "." + name;
        }
    }
}
