package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    /**
     * Find users by role name
     * @param roleName the role name
     * @return list of users with the role
     */
    @Query(value = "SELECT u.* FROM Users u " +
           "JOIN Roles r ON u.RoleId = r.RoleId " +
           "WHERE r.RoleName = :roleName", nativeQuery = true)
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find recent users
     * @return list of recent users
     */
    @Query("SELECT u FROM User u ORDER BY u.createdDate DESC")
    List<User> findRecentUsers();
    
    /**
     * Search users by keyword (username, email, or full name)
     * @param keyword the search keyword
     * @return list of matching users
     */
    @Query(value = "SELECT u.* FROM Users u WHERE " +
           "LOWER(u.Username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.Email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(CONCAT(u.FirstName, ' ', u.LastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<User> searchUsers(@Param("keyword") String keyword);
    
    /**
     * Find all users with their roles loaded
     * @return list of users with roles
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role ORDER BY u.createdDate DESC")
    List<User> findAllWithRoles();
}
