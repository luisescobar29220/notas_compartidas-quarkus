package resource;

import dto.CreateNoteDTO;
import dto.NoteDTO;
import dto.UpdateNoteDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import model.NoteModel;
import repository.NoteRepository;
import service.NoteService;
import webSocket.NotesSocket;

import java.net.URI;
import java.util.List;
@Path("/rooms/{roomCode}/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotesResource {

    @Inject NoteService noteService;
    @Inject NoteRepository noteRepository;

    // opcional: si tienes el endpoint WS
    @Inject
    NotesSocket notesSocket;

    @GET
    public Response list(@PathParam("roomCode") String roomCode,
                         @HeaderParam("If-None-Match") String ifNoneMatch) {
        String etag = noteService.computeRoomETag(roomCode);
        if (etag != null && etag.equals(ifNoneMatch)) {
            return Response.notModified().tag(etag).build();
        }
        List<NoteDTO> out = noteService.listNotesByRoom(roomCode).stream()
                .map(n -> new NoteDTO(
                        n.getId(),
                        n.getRoom() != null ? n.getRoom().getId() : null,
                        n.getContent(),
                        n.getUpdatedAt(),
                        n.getVersion()))
                .toList();
        return Response.ok(out).tag(etag).build();
    }

    @POST
    public Response create(@PathParam("roomCode") String roomCode,
                           @Valid CreateNoteDTO body,
                           @Context UriInfo uriInfo) {
        NoteModel n = noteService.createNote(roomCode, body.content());
        // WS (si lo tienes)
        if (notesSocket != null) notesSocket.broadcastNoteEvent(roomCode, "note-created", n.getId());

        NoteDTO dto = new NoteDTO(
                n.getId(),
                n.getRoom() != null ? n.getRoom().getId() : null,
                n.getContent(),
                n.getUpdatedAt(),
                n.getVersion());

        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(n.getId())).build();
        return Response.created(location).entity(dto).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("roomCode") String roomCode,
                           @PathParam("id") Long id,
                           @Valid UpdateNoteDTO body,
                           @HeaderParam("If-Match") String ifMatch) {
        NoteModel existing = noteRepository.findById(id);
        if (existing == null || existing.getRoom() == null
                || !roomCode.equals(existing.getRoom().getCode())) {
            throw new NotFoundException();
        }
        if (ifMatch != null && !ifMatch.isBlank()) {
            String cleaned = ifMatch.trim();
            if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            try {
                long expectedVersion = Long.parseLong(cleaned);
                if (expectedVersion != existing.getVersion()) {
                    return Response.status(Response.Status.PRECONDITION_FAILED)
                            .entity("{\"error\":\"ETag mismatch. Refresh and retry.\"}")
                            .build();
                }
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity("{\"error\":\"Invalid If-Match header.\"}")
                        .build();
            }
        }

        NoteModel updated = noteService.updateNote(roomCode, id, body.content());
        if (updated == null) throw new NotFoundException();

        // WS (si lo tienes)
        if (notesSocket != null) notesSocket.broadcastNoteEvent(roomCode, "note-updated", updated.getId());

        NoteDTO dto = new NoteDTO(
                updated.getId(),
                updated.getRoom() != null ? updated.getRoom().getId() : null,
                updated.getContent(),
                updated.getUpdatedAt(),
                updated.getVersion());
        return Response.ok(dto).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("roomCode") String roomCode,
                           @PathParam("id") Long id) {
        boolean ok = noteService.deleteNote(roomCode, id);
        if (!ok) throw new NotFoundException();

        // WS (si lo tienes)
        if (notesSocket != null) notesSocket.broadcastNoteEvent(roomCode, "note-deleted", id);

        return Response.noContent().build();
    }
}
