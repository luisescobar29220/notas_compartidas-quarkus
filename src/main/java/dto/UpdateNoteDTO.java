package dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoteDTO(
        @NotBlank String content
) {
}
