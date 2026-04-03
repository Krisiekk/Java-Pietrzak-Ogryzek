package pl.pk.pietrzak.ogryzek.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testy enum TaskType")
public class TaskTypeTest {

    @Test
    @DisplayName("TaskType powinien mieć wartość BUG")
    void shouldHaveBugValue() {
        assertNotNull(TaskType.BUG);
        assertEquals("BUG", TaskType.BUG.name());
    }

    @Test
    @DisplayName("TaskType powinien mieć wartość FEATURE")
    void shouldHaveFeatureValue() {
        assertNotNull(TaskType.FEATURE);
        assertEquals("FEATURE", TaskType.FEATURE.name());
    }

    @Test
    @DisplayName("TaskType powinien mieć wartość IMPROVEMENT")
    void shouldHaveImprovementValue() {
        assertNotNull(TaskType.IMPROVEMENT);
        assertEquals("IMPROVEMENT", TaskType.IMPROVEMENT.name());
    }

    @Test
    @DisplayName("TaskType valueOf powinien działać")
    void shouldParseTaskTypeFromString() {
        assertEquals(TaskType.BUG, TaskType.valueOf("BUG"));
        assertEquals(TaskType.FEATURE, TaskType.valueOf("FEATURE"));
        assertEquals(TaskType.IMPROVEMENT, TaskType.valueOf("IMPROVEMENT"));
    }

    @Test
    @DisplayName("TaskType values powinien zwrócić wszystkie wartości")
    void shouldReturnAllValues() {
        TaskType[] values = TaskType.values();
        assertEquals(3, values.length);
    }
}

