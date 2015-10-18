package com.supersoftcafe.json_stream.impl;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class PathImplTest {

    PathImpl path;

    @Before
    public void before() {
        path = new PathImpl();
    }

    @After
    public void after() {
        path = null;
    }


    @Test
    public void testValueOf() throws Exception {
        PathImpl path = PathImpl.valueOf("$[3].fred[4].bill.bert");

        assertEquals(5, path.size());
        assertEquals("[3]", path.get(0).toString());
        assertEquals(".fred", path.get(1).toString());
        assertEquals("[4]", path.get(2).toString());
        assertEquals(".bill", path.get(3).toString());
        assertEquals(".bert", path.get(4).toString());
    }

    @Test
    public void testReadOnlyCopy() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        PathImpl readOnlyCopy1 = path.readOnlyCopy();
        PathImpl readOnlyCopy2 = path.readOnlyCopy();

        assertEquals(2, readOnlyCopy1.size());
        assertEquals("one", readOnlyCopy1.get(0).getName());
        assertEquals("two", readOnlyCopy1.get(1).getName());
        assertEquals(2, readOnlyCopy2.size());
        assertEquals("one", readOnlyCopy2.get(0).getName());
        assertEquals("two", readOnlyCopy2.get(1).getName());
    }

    @Test(expected = IllegalStateException.class)
    public void testReadOnlyCopy_whenModified() throws Exception {
        path.pushAttributeName("one");

        PathImpl readOnlyCopy = path.readOnlyCopy();

        readOnlyCopy.pushArrayIndex(1);
    }


    @Test
    public void testMutableCopy() throws Exception {
        path.pushAttributeName("one");

        PathImpl mutableCopy = path.readOnlyCopy().mutableCopy();
        mutableCopy.pushArrayIndex(2l);

        assertEquals(2, mutableCopy.size());
        assertEquals("one", mutableCopy.get(0).getName());
        assertEquals(2l, mutableCopy.get(1).getIndex());
    }

    @Test
    public void testClone() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        PathImpl clonedPath = path.clone();
        clonedPath.pop();
        clonedPath.pushAttributeName("three");

        assertEquals(2, path.size());
        assertEquals("one", path.get(0).getName());
        assertEquals("two", path.get(1).getName());
        assertEquals(2, clonedPath.size());
        assertEquals("one", clonedPath.get(0).getName());
        assertEquals("three", clonedPath.get(1).getName());
    }

    @Test
    public void testSet() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        PathImpl otherPath = new PathImpl();
        otherPath.pushAttributeName("three");
        path.set(1, otherPath.peek());

        assertEquals(2, path.size());
        assertEquals("one", path.get(0).getName());
        assertEquals("three", path.get(1).getName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSet_OutOfRange() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        PathImpl otherPath = new PathImpl();
        otherPath.pushAttributeName("three");
        path.set(2, otherPath.peek());
    }

    @Test
    public void testRemove() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");
        path.pushAttributeName("three");

        path.remove(1);

        assertEquals(2, path.size());
        assertEquals("one", path.get(0).getName());
        assertEquals("three", path.get(1).getName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_OutOfRange() throws Exception {
        path.pushAttributeName("one");

        path.remove(1);
    }

    @Test
    public void testPopArray_IfArray() throws Exception {
        path.pushArrayIndex(1l);

        PathImpl.ArrayIndex node = path.popArray();

        assertEquals(1l, node.getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void testPopArray_IfObject() throws Exception {
        path.pushAttributeName("fred");

        PathImpl.ArrayIndex node = path.popArray();
    }

    @Test
    public void testPopObject_IfObject() throws Exception {
        path.pushAttributeName("fred");

        PathImpl.AttributeName node = path.popObject();

        assertEquals("fred", node.getName());
    }

    @Test(expected = IllegalStateException.class)
    public void testPopObject_IfArray() throws Exception {
        path.pushArrayIndex(1l);

        PathImpl.AttributeName node = path.popObject();
    }

    @Test
    public void testUpdateArrayIndex() throws Exception {
        path.pushDummyArray();
        path.pushDummyArray();

        path.updateArrayIndex(5l);

        assertEquals(5l, path.peek().getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateArrayIndex_FailsIfNotArray() throws Exception {
        path.pushDummyArray();
        path.pushDummyObject();

        path.updateArrayIndex(5l);
    }

    @Test
    public void testUpdateAttributeName() throws Exception {
        path.pushDummyObject();
        path.pushDummyObject();

        path.updateAttributeName("fred");

        assertEquals("fred", path.peek().getName());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateAttributeName_FailsIfNotObject() throws Exception {
        path.pushDummyObject();
        path.pushDummyArray();

        path.updateAttributeName("fred");
    }

    @Test
    public void testAdvanceArrayIndex_WhenArray() throws Exception {
        path.pushDummyArray();
        path.pushDummyArray();

        path.advanceArrayIndex();

        assertEquals(0l, path.peek().getIndex());
    }

    @Test
    public void testAdvanceArrayIndex_WhenObject() throws Exception {
        path.pushDummyArray();
        path.pushDummyObject();

        path.advanceArrayIndex();

        assertEquals(PathImpl.DUMMY_ATTRIBUTE_NAME, path.peek().getName());
    }

    @Test
    public void testPeek() throws Exception {
        path.pushAttributeName("fred");
        path.pushArrayIndex(2);

        PathImpl.NodeImpl node = path.peek();

        assertEquals(2l, node.getIndex());
    }

    @Test
    public void testPop() throws Exception {
        path.pushArrayIndex(2);
        path.pushAttributeName("fred");
        path.pushAttributeName("bill");

        PathImpl.NodeImpl node = path.pop();

        assertEquals(2, path.size());
        assertEquals(2l, path.get(0).getIndex());
        assertEquals("fred", path.get(1).getName());
        assertEquals("bill", node.getName());
    }

    @Test
    public void testToString() throws Exception {
        path.pushArrayIndex(2);
        path.pushAttributeName("fred");
        path.pushAttributeName("bill");

        String value = path.toString();

        assertEquals("$[2].fred.bill", value);
    }
}