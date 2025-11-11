package model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(name = "notes", indexes = {
        @Index(name = "ix_notes_room_id", columnList = "room_id"),
        @Index(name = "ix_notes_updated_at", columnList = "updatedAt")
})
public class NoteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomModel room;

    @NotBlank
    @Column(nullable = false,length = 2000)
    private String content;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;



    //constructores

    public NoteModel() {
    }

    public NoteModel(RoomModel room, String content) {
        this.room = room;
        this.content = content;
    }

    @PrePersist
    protected void onCreate(){this.updatedAt = Instant.now();}

    @PreUpdate
    protected void onUpdate(){this.updatedAt = Instant.now();}

    //getter y setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoomModel getRoom() {
        return room;
    }

    public void setRoom(RoomModel room) {
        this.room = room;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updateAt) {
        this.updatedAt = updateAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
