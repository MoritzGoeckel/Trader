package com.moritzgoeckel.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test collection")
class MainTest {

    @Test
    @DisplayName("Testtest")
    void myFirstTest() {
        assertEquals(2, 1 + 1);
    }

}
