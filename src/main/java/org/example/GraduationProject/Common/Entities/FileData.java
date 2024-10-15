package org.example.GraduationProject.Common.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "fileData")
public class FileData extends BaseEntity {

    private String name;
    private String type;
    private String filePath;

    @Transient
    private byte[] data;

}
