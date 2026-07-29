package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.PermissionDTO;
import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();
    PermissionDTO getPermissionById(String id);
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO updatePermission(String id, PermissionDTO permissionDTO);
    void deletePermission(String id);
}
