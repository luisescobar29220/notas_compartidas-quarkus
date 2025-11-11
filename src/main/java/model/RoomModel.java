package model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "ux_rooms_code",columnList = "code",unique = true)
})
public class RoomModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = true,length = 100)
    private String code;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoteModel> notes = new ArrayList<>();

    //constructores
    public RoomModel() {}

    public RoomModel(String code) {
        this.code = code;
    }

    @PrePersist
    protected void onCreate(){this.createdAt = Instant.now();}

    // Helpers para mantener la relación bidireccional
    public void addNote(NoteModel n) { notes.add(n); n.setRoom(this); }
    public void removeNote(NoteModel n) { notes.remove(n); n.setRoom(null); }

    //getter y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<NoteModel> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteModel> notes) {
        this.notes = notes;
    }
}
