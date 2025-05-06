package br.edu.iff.ccc.bsi.gardensBoard.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity(name = "tb_users")
public class UserModel {
    
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @NotBlank(message = "The username cannot be blank")
    @Size(min = 3, max = 20, message = "The username must be between 4 and 20 characters")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "The name cannot be blank")
    @Size(min = 4, max = 20, message = "The name must be between 4 and 20 characters")
    private String name;    

    @NotBlank(message = "The password cannot be blank")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 20 characters")
    @Column(length = 100)
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "collaborators")
    private List<TaskModel> tasksAsCollaborator = new ArrayList<>();

}
