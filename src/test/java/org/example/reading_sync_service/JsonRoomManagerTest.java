package org.example.reading_sync_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonRoomManagerTest {

    private JsonRoomManager roomManager;

    @BeforeEach
    void setUp() {
        roomManager = JsonRoomManager.getInstance();
        // Reset room values before each test
        roomManager.setRoomValue("room1", "none");
        roomManager.setRoomValue("room2", "none");
        roomManager.setRoomValue("room3", "none");
    }

    @Test
    void testSingletonInstance() {
        JsonRoomManager anotherInstance = JsonRoomManager.getInstance();
        assertSame(roomManager, anotherInstance, "Instances should be the same singleton instance.");
    }

    @Test
    void testGetRooms_initialState() {
        Map<String, String> rooms = roomManager.getRooms();
        assertEquals(3, rooms.size(), "There should be exactly 3 rooms.");
        assertEquals("none", rooms.get("room1"), "room1 should initially have value 'none'.");
        assertEquals("none", rooms.get("room2"), "room2 should initially have value 'none'.");
        assertEquals("none", rooms.get("room3"), "room3 should initially have value 'none'.");
    }

    @Test
    void testSetAndGetRoomValue() {
        roomManager.setRoomValue("room1", "BookA");
        roomManager.setRoomValue("room2", "BookB");

        assertEquals("BookA", roomManager.getRoomValue("room1"), "room1 should have 'BookA'.");
        assertEquals("BookB", roomManager.getRoomValue("room2"), "room2 should have 'BookB'.");
        assertEquals("none", roomManager.getRoomValue("room3"), "room3 should remain 'none'.");
    }

    @Test
    void testUpdateRoomValue() {
        roomManager.setRoomValue("room1", "NewBook");
        roomManager.setRoomValue("room1", "NewBookTest");
        assertEquals("NewBookTest", roomManager.getRoomValue("room1"), "room1 should be updated to 'NewBook'.");
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {

        Thread thread1 = new Thread(() -> roomManager.setRoomValue("room1", "Thread1Book"));
        Thread thread2 = new Thread(() -> roomManager.setRoomValue("room1", "Thread2Book"));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        String finalValue = roomManager.getRoomValue("room1");
        assertTrue(finalValue.equals("Thread1Book") || finalValue.equals("Thread2Book"),
                "room1 should be updated by one of the threads.");
    }


    @Test
    void testConcurrentAccessMultipleRooms() throws InterruptedException {

        Thread thread1 = new Thread(() -> roomManager.setRoomValue("room1", "Thread1Book"));
        Thread thread2 = new Thread(() -> roomManager.setRoomValue("room2", "Thread2Book"));
        Thread thread3 = new Thread(() -> roomManager.setRoomValue("room3", "Thread3Book"));

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        assertTrue("Thread1Book".equals(roomManager.getRoomValue("room1")) ||
                "Thread2Book".equals(roomManager.getRoomValue("room1")));
        assertTrue("Thread2Book".equals(roomManager.getRoomValue("room2")) ||
                "Thread3Book".equals(roomManager.getRoomValue("room2")));
        assertTrue("Thread3Book".equals(roomManager.getRoomValue("room3")) ||
                "Thread1Book".equals(roomManager.getRoomValue("room3")));
    }

    @Test
    void testRoomDoesNotExist() {
        assertNull(roomManager.getRoomValue("nonExistingRoom"), "Should return null for a non-existing room.");
    }

    @Test
    void testSetInvalidRoomName() {

        Exception exception = assertThrows(NullPointerException.class, () -> {
            roomManager.setRoomValue(null, "InvalidBook");
        });
        assertNull(exception.getMessage(), "Should throw an exception for null room name.");
    }

}
