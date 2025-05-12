package br.edu.iff.ccc.bsi.gardensBoard.repository;
import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskRepository extends JpaRepository<TaskModel, UUID> {



    TaskModel findByTitle(String title);
    List<TaskModel> findByIdUserOwner(UUID idUserOwner);;
    List<TaskModel> findByPriority(String priority);
    
   
    
}

