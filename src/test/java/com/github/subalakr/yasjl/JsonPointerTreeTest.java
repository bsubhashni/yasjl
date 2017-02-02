package com.github.subalakr.yasjl;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @Subhashni Balakrishnan
 */
public class JsonPointerTreeTest {

    @Test
    public void testInvalidPathsIntermediaryAndTerminal() throws Exception {
        JsonPointer jp1 = new JsonPointer("/a/b/c");
        JsonPointer jp2 = new JsonPointer("/a/b");
        JsonPointerTree tree = new JsonPointerTree();

        assertTrue(tree.addJsonPointer(jp1));
        assertFalse(tree.addJsonPointer(jp2));
    }

    @Test
    public void testPaths() throws Exception {
        JsonPointer jp1 = new JsonPointer("/a/b/c");
        JsonPointer jp2 = new JsonPointer("/a/b");

        JsonPointerTree tree = new JsonPointerTree();
        assertTrue(tree.addJsonPointer(jp1));

        assertTrue(tree.isTerminalPath(jp1));
        assertFalse(tree.isIntermediaryPath(jp1));

        assertTrue(tree.isIntermediaryPath(jp2));
        assertFalse(tree.isTerminalPath(jp2));
    }
}
