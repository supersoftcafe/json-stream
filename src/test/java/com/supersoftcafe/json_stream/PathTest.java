package com.supersoftcafe.json_stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class PathTest {

    Path path;

    @Before
    public void before() {
        path = new Path();
    }

    @After
    public void after() {
        path = null;
    }


    @Test
    public void testReadOnlyCopy() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        Path readOnlyCopy1 = path.readOnlyCopy();
        Path readOnlyCopy2 = path.readOnlyCopy();

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

        Path readOnlyCopy = path.readOnlyCopy();

        readOnlyCopy.pushArrayIndex(1);
    }


    @Test
    public void testMutableCopy() throws Exception {
        path.pushAttributeName("one");

        Path mutableCopy = path.readOnlyCopy().mutableCopy();
        mutableCopy.pushArrayIndex(2l);

        assertEquals(2, mutableCopy.size());
        assertEquals("one", mutableCopy.get(0).getName());
        assertEquals(2l, mutableCopy.get(1).getIndex());
    }

    @Test
    public void testClone() throws Exception {
        path.pushAttributeName("one");
        path.pushAttributeName("two");

        Path clonedPath = path.clone();
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

        Path otherPath = new Path();
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

        Path otherPath = new Path();
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

        Path.ArrayIndex node = path.popArray();

        assertEquals(1l, node.getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void testPopArray_IfObject() throws Exception {
        path.pushAttributeName("fred");

        Path.ArrayIndex node = path.popArray();
    }

    @Test
    public void testPopObject_IfObject() throws Exception {
        path.pushAttributeName("fred");

        Path.AttributeName node = path.popObject();

        assertEquals("fred", node.getName());
    }

    @Test(expected = IllegalStateException.class)
    public void testPopObject_IfArray() throws Exception {
        path.pushArrayIndex(1l);

        Path.AttributeName node = path.popObject();
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

        assertEquals(Path.DUMMY_ATTRIBUTE_NAME, path.peek().getName());
    }

    @Test
    public void testPeek() throws Exception {
        path.pushAttributeName("fred");
        path.pushArrayIndex(2);

        Path.Node node = path.peek();

        assertEquals(2l, node.getIndex());
    }

    @Test
    public void testPop() throws Exception {
        path.pushArrayIndex(2);
        path.pushAttributeName("fred");
        path.pushAttributeName("bill");

        Path.Node node = path.pop();

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