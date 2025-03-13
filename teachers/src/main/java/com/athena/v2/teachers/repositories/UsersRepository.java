package com.athena.v2.teachers.repositories;

import com.athena.v2.libraries.enums.UserRoles;
import com.athena.v2.libraries.enums.UserStatus;
import com.athena.v2.users.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    boolean existsByEmailOrUsername(String email, String username);
    Optional<Users> findUsersByUserId(String userId);
    List<Users> getUsersByUserStatus(UserStatus userStatus);
    List<Users> findAllByUserRoles(UserRoles userRoles);
}
