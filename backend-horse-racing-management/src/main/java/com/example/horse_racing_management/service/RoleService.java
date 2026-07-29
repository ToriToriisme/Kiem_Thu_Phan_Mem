package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.RoleDTO;
import java.util.List;

public interface RoleService {
    List<RoleDTO> getAllRoles();
    RoleDTO getRoleById(String id);
    RoleDTO createRole(RoleDTO roleDTO);
    RoleDTO updateRole(String id, RoleDTO roleDTO);
    void deleteRole(String id);
}
