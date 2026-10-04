package ru.ilesh.resourceserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ilesh.resourceserver.entity.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {
    boolean existsByUsernameAndUuidNot(String username, String uuid);

    UserEntity findByUsername(String username, String uuid);

    UserEntity findByUuid(String uuid);
}
