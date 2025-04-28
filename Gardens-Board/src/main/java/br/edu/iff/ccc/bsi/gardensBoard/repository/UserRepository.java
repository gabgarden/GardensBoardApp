package br.edu.iff.ccc.bsi.gardensBoard.repository;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserModel, UUID>  {

    UserModel findByUsername(String username);
    boolean existsByUsername(String username);
    

  

}
