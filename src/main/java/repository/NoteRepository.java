package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import model.NoteModel;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class NoteRepository implements PanacheRepository<NoteModel> {

    public List<NoteModel> listByRoomCode(String roomCode){
        return find("room.code = ?1 order by updatedAt desc",roomCode).list();
    }

    // Helpers para ETag/estadísticas

    public long countByRoomCode(String roomCode){
        return count("room.code",roomCode);
    }

    public long sumVersionByRoomCode(String roomCode){
        // Panache no tiene SUM directo: usa query HQL
        Long s = getEntityManager().createQuery(
                "SELECT COALESCE(SUM(n.version),0) FROM NoteModel n WHERE n.room.code = :code", Long.class)
                .setParameter("code",roomCode)
                .getSingleResult();
        return s == null ? 0L : s;
    }

    public Instant maxUpdatedAtByRoomCode(String roomCode){
        return getEntityManager().createQuery(
                "SELECT MAX(n.updatedAt) FROM NoteModel n WHERE n.room.code = :code", Instant.class)
                .setParameter("code",roomCode)
                .getSingleResult();
    }

}
