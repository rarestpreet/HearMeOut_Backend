package com.project.hearmeout_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseModel{

    @Column(nullable = false, unique = true, length = 15)
    private String name;

    @Lob
    @Column(length = 100)
    private String description;
}
