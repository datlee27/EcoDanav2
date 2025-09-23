package com.ecodana.evodanavn1.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RoleService {
    
    // Valid role IDs from database (these should match the actual UUIDs in database)
    private static final List<String> VALID_ROLE_IDS = Arrays.asList(
        "customer-role-id",
        "staff-role-id", 
        "admin-role-id"
    );
    
    // Valid role names
    private static final List<String> VALID_ROLE_NAMES = Arrays.asList(
        "Customer",
        "Staff",
        "Admin"
    );
    
    /**
     * Validates if the given role ID is valid
     * @param roleId the role ID to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidRoleId(String roleId) {
        if (roleId == null || roleId.trim().isEmpty()) {
            return false;
        }
        return VALID_ROLE_IDS.contains(roleId.trim());
    }
    
    /**
     * Validates if the given role name is valid
     * @param roleName the role name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        return VALID_ROLE_NAMES.contains(roleName.trim());
    }
    
    /**
     * Gets the default role ID for customers
     * @return the default customer role ID
     */
    public String getDefaultCustomerRoleId() {
        return "customer-role-id";
    }
    
    /**
     * Gets the default role ID for staff
     * @return the default staff role ID
     */
    public String getDefaultStaffRoleId() {
        return "staff-role-id";
    }
    
    /**
     * Gets the default role ID for admin
     * @return the default admin role ID
     */
    public String getDefaultAdminRoleId() {
        return "admin-role-id";
    }
    
    /**
     * Gets all valid role IDs
     * @return list of valid role IDs
     */
    public List<String> getAllValidRoleIds() {
        return VALID_ROLE_IDS;
    }
    
    /**
     * Gets all valid role names
     * @return list of valid role names
     */
    public List<String> getAllValidRoleNames() {
        return VALID_ROLE_NAMES;
    }
}
