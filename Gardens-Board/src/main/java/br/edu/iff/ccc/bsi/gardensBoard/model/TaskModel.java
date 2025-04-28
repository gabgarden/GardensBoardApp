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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Entity(name = "tb_tasks")
public class TaskModel {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToMany
    @JoinTable(
    name = "tb_task_collaborators",
    joinColumns = @JoinColumn(name = "task_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserModel> collaborators = new ArrayList<>();


    @NotBlank(message = "The title cannot be blank")
    @Size(max = 100, message = "The title must be at most 100 characters")
    @Column(nullable = false, length = 100) 
    private String title;


    @Size(max = 500, message = "The descrkçiption must be at most 500 characters")
    @Column(length = 500)
    private String description;



    private LocalDateTime startAt;



    private LocalDateTime endAt;


    @Pattern(regexp = "High|Medium|Low", message = "The priority must be High, Medium or Low")
    private String priority;



    private UUID idUserOwner;
    


    @CreationTimestamp
    private LocalDateTime createdAt;

    

}
