package dto;

import java.time.Instant;

public record RoomDTO(
        Long id,
        String code,
        Instant createdAt
) {
}
