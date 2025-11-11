package dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteDTO(
        @NotBlank String content
) {
}
