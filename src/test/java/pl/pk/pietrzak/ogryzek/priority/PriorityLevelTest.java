package pl.pk.pietrzak.ogryzek.priority;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PriorityLevelTest {

    @Test
    @DisplayName("HighPriority powinien zwrócić wartość 3")
    void shouldReturnHighPriorityValue() {
        PriorityLevel highPriority = new HighPriority();
        assertEquals(3, highPriority.getPriority());
    }

    @Test
    @DisplayName("MediumPriority powinien zwrócić wartość 2")
    void shouldReturnMediumPriorityValue() {
        PriorityLevel mediumPriority = new MediumPriority();
        assertEquals(2, mediumPriority.getPriority());
    }

    @Test
    @DisplayName("LowPriority powinien zwrócić wartość 1")
    void shouldReturnLowPriorityValue() {
        PriorityLevel lowPriority = new LowPriority();
        assertEquals(1, lowPriority.getPriority());
    }
}

