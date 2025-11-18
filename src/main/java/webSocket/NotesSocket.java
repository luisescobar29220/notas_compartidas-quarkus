package webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint("/ws/notes/{roomCode}")
public class NotesSocket {
    private static final Map<String, Set<Session>> ROOMS = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper mapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("roomCode") String roomCode) {
        ROOMS.computeIfAbsent(roomCode, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("roomCode") String roomCode) {
        broadcast(roomCode, message);
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomCode") String roomCode) {
        Set<Session> set = ROOMS.get(roomCode);
        if (set != null) set.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) { t.printStackTrace(); }

    public void broadcastNoteEvent(String roomCode, String type, Long noteId) {
        try {
            String json = mapper.createObjectNode()
                    .put("type", type)
                    .put("room", roomCode)
                    .put("noteId", noteId == null ? null : noteId)
                    .toString();
            broadcast(roomCode, json);
        } catch (Exception ignored) { }
    }

    private void broadcast(String roomCode, String payload) {
        Set<Session> set = ROOMS.get(roomCode);
        if (set == null) return;
        set.forEach(s -> {
            if (s.isOpen()) {
                try { s.getBasicRemote().sendText(payload); }
                catch (IOException ignored) {}
            }
        });
    }
}
