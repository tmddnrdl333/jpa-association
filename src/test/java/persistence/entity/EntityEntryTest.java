package persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityEntryTest {
    @Test
    @DisplayName("정해진 라이프사이클대로 상태를 변경할 수 있다.")
    void setStatusSuccess() {
        EntityEntry entityEntry = new EntityEntry(EntityEntryStatus.LOADING);
        entityEntry.setStatus(EntityEntryStatus.MANAGED);
        entityEntry.setStatus(EntityEntryStatus.DELETED);
        entityEntry.setStatus(EntityEntryStatus.GONE);

        assertEquals(EntityEntryStatus.GONE, entityEntry.getStatus());
    }

    @Test
    @DisplayName("정해진 라이프사이클을 어긋나 상태를 바꾸려 하면 에러를 내뱉는다.")
    void setStatusFail() {
        EntityEntry entityEntry = new EntityEntry(EntityEntryStatus.SAVING);

        assertThrows(IllegalStateException.class, () -> entityEntry.setStatus(EntityEntryStatus.DELETED));
    }

    @Test
    @DisplayName("업데이트 불가한 상태인지 판별한다.")
    void testIsImmutable() {
        assertAll(
                () -> assertFalse(new EntityEntry(EntityEntryStatus.MANAGED).isImmutable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.SAVING).isImmutable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.LOADING).isImmutable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.READ_ONLY).isImmutable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.DELETED).isImmutable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.GONE).isImmutable())
        );
    }

    @Test
    @DisplayName("삭제 불가한 상태인지 판별한다.")
    void testIsUndeletable() {
        assertAll(
                () -> assertFalse(new EntityEntry(EntityEntryStatus.MANAGED).isUndeletable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.SAVING).isUndeletable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.LOADING).isUndeletable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.READ_ONLY).isUndeletable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.DELETED).isUndeletable()),
                () -> assertTrue(new EntityEntry(EntityEntryStatus.GONE).isUndeletable())
        );
    }
}
