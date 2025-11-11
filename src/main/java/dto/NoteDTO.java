package dto;

import java.time.Instant;

public record NoteDTO(
        Long id,
        Long roomId,
        String content,
        Instant updatedAt,
        long version
) {
}
