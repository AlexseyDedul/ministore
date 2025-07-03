package by.alexdedul.managerapp.repository;

import by.alexdedul.managerapp.entity.MinistoreUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MiniStoreUserRepository extends CrudRepository<MinistoreUser, Integer> {

    Optional<MinistoreUser> findByUsername(String username);
}
