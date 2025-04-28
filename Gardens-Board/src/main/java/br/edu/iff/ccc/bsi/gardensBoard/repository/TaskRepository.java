package br.edu.iff.ccc.bsi.gardensBoard.repository;
import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TaskRepository extends JpaRepository<TaskModel, UUID> {


 
    TaskModel findByTitle(String title);
    List<TaskModel> findByIdUserOwner(UUID idUserOwner);;
    List<TaskModel> findByPriority(String priority);
    
    //@Query("SELECT t FROM TaskModel t JOIN t.collaborators c WHERE c.id = :userId OR t.idUserOwner = :userId")
    //List<TaskModel> findByUserParticipation(@Param("userId") UUID userId);  
    
}

