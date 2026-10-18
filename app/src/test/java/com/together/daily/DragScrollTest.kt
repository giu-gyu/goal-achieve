package com.together.daily

import org.junit.Assert.*
import org.junit.Test

class DragScrollTest {
    @Test fun middleDoesNotScroll() {
        assertEquals(0f,dragScrollFraction(180f,60f,0f,500f,64f),0f)
    }
    @Test fun edgesScrollInBothDirectionsAndClampSpeed() {
        assertEquals(-1f,dragScrollFraction(-100f,60f,0f,500f,64f),0f)
        assertEquals(1f,dragScrollFraction(500f,60f,0f,500f,64f),0f)
        assertEquals(-.5f,dragScrollFraction(32f,60f,0f,500f,64f),.001f)
        assertEquals(.5f,dragScrollFraction(408f,60f,0f,500f,64f),.001f)
    }
    @Test fun smallOrEmptyViewportsHaveSafeZones() {
        assertEquals(0f,dragScrollFraction(0f,60f,0f,0f,64f),0f)
        assertEquals(-1f,dragScrollFraction(0f,20f,0f,90f,64f),0f)
        assertEquals(1f,dragScrollFraction(70f,20f,0f,90f,64f),0f)
    }
}
