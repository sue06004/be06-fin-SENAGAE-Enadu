package org.example.backend.ErrorArchive.Model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.backend.User.Model.Entity.User;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "error_archive_id", nullable = false)
    private ErrorArchive errorArchive;



    @Column(nullable = false)
    private boolean state;
}