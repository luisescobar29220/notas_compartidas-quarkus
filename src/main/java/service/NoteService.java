package service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Index;
import jakarta.transaction.Transactional;
import model.NoteModel;
import model.RoomModel;
import repository.NoteRepository;
import repository.RoomRepository;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class NoteService {

    private static final String CACHE_NOTES_BY_ROOM = "notes-by-room";

    @Inject
    NoteRepository noteRepository;
    @Inject
    RoomRepository roomRepository;

    // -------- Lecturas (cacheadas) --------
    @CacheResult(cacheName = CACHE_NOTES_BY_ROOM)
    public List<NoteModel> listNotesByRoom(@CacheKey String roomCode){
        return noteRepository.listByRoomCode(roomCode);
    }

    // ETag débil para la sala (no invalida cache; se basa en agregados)
    public String computeRoomETag(String roomCode) {
        long count = noteRepository.countByRoomCode(roomCode);
        long sumVersion = noteRepository.sumVersionByRoomCode(roomCode);
        Instant maxUpdated = noteRepository.maxUpdatedAtByRoomCode(roomCode);
        long maxTs = (maxUpdated == null) ? 0L : maxUpdated.toEpochMilli();
        // Formato: W/"count-sumVersion-maxTs"
        return "W/\"" + count + "-" + sumVersion + "-" + maxTs + "\"";
    }

    // -------- Escrituras (invalidan cache) --------
    @Transactional
    @CacheInvalidate(cacheName = CACHE_NOTES_BY_ROOM)
    public NoteModel createNote(@CacheKey String roomCode, String content) {
        RoomModel room = roomRepository.findByCode(roomCode);
        if (room == null) {
            room = new RoomModel();
            room.setCode(roomCode);
            roomRepository.persist(room);
        }

        NoteModel note = new NoteModel(room, content);
        noteRepository.persist(note);
        return note;
    }

    @Transactional
    @CacheInvalidate(cacheName = CACHE_NOTES_BY_ROOM)
    public NoteModel updateNote(@CacheKey String roomCode, Long noteId, String content) {
        NoteModel note = noteRepository.findById(noteId);
        if (note == null || note.getRoom() == null || !roomCode.equals(note.getRoom().getCode())) {
            return null;
        }
        note.setContent(content); // @PreUpdate actualizará updatedAt
        return note;
    }

    @Transactional
    @CacheInvalidate(cacheName = CACHE_NOTES_BY_ROOM)
    public boolean deleteNote(@CacheKey String roomCode, Long noteId) {
        NoteModel note = noteRepository.findById(noteId);
        if (note == null || note.getRoom() == null || !roomCode.equals(note.getRoom().getCode())) {
            return false;
        }
        noteRepository.delete(note);
        return true;
    }


}
