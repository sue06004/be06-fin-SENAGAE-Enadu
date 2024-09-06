package org.example.backend.Answer.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerComment {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @Column(name = "answer_id", nullable = false)
        private Answer answer;

//        @ManyToOne(fetch = FetchType.LAZY)
//        @Column(name = "user_id", nullable = false)
//        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "super_comment_id", nullable = false)
        private AnswerComment answerComment;

        @Column(name = "content", columnDefinition = "TEXT", nullable = false)
        private String content;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "enable", nullable = false)
        private boolean enable;

}