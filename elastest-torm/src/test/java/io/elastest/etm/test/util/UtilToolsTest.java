package io.elastest.etm.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
public class UtilToolsTest {

    @Test
    public void testGetElasTestHostOnWin() {
        assertEquals("localhost", UtilTools.getElasTestHostOnWin());
    }

    @Test
    public void testGetDockerHostIpOnWin() {
        assertEquals("", UtilTools.getDockerHostIpOnWin());
    }

    @Test
    public void testGetHostIp() {
        assertNotNull(UtilTools.getHostIp());
    }

    @Test
    public void testGetDockerHostIp() {
        assertNotNull(UtilTools.getDockerHostIp());
    }

    @Test
    public void testGetMyIp() {
        assertNotEquals("", UtilTools.getMyIp());
    }

    @Test
    public void testConvertJsonString() {
        assertNotNull(UtilTools.convertJsonString(5, int.class));
    }

    @Test
    public void testFindRandomOpenPort() {
        boolean works = true;
        try {
            UtilTools.findRandomOpenPort();
        } catch (IOException e) {
            works = false;
        }

        assertTrue(works);
    }

    @Test
    public void testDoPing() throws IOException {
        assertNotNull(UtilTools.doPing("localhost"));
    }

}
