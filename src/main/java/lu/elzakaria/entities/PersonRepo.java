package lu.elzakaria.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepo extends JpaRepository<Person, Long>{
    // repo for person entity
}
