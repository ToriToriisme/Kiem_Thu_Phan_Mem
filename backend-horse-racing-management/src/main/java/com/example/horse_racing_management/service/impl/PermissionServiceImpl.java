package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.dto.PermissionDTO;
import com.example.horse_racing_management.entity.Permission;
import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.repository.PermissionRepository;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    private PermissionDTO mapToDTO(Permission permission) {
        return new PermissionDTO(permission.getId(), permission.getTitle(), permission.getKey());
    }

    private Permission mapToEntity(PermissionDTO dto) {
        Permission permission = new Permission();
        permission.setId(dto.getId());
        permission.setTitle(dto.getTitle());
        permission.setKey(dto.getKey());
        return permission;
    }

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(String id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        return mapToDTO(permission);
    }

    @Override
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        // Check if key already exists
        if (permissionRepository.findAll().stream().anyMatch(p -> p.getKey().equals(permissionDTO.getKey()))) {
            throw new RuntimeException("Permission key already exists: " + permissionDTO.getKey());
        }
        Permission permission = mapToEntity(permissionDTO);
        permission.setId(null); // Let MongoDB generate ID
        return mapToDTO(permissionRepository.save(permission));
    }

    @Override
    public PermissionDTO updatePermission(String id, PermissionDTO permissionDTO) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        // Check if key already exists on another permission
        if (permissionRepository.findAll().stream()
                .anyMatch(p -> p.getKey().equals(permissionDTO.getKey()) && !p.getId().equals(id))) {
            throw new RuntimeException("Permission key already exists: " + permissionDTO.getKey());
        }

        existingPermission.setTitle(permissionDTO.getTitle());
        existingPermission.setKey(permissionDTO.getKey());
        return mapToDTO(permissionRepository.save(existingPermission));
    }

    @Override
    public void deletePermission(String id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        
        // Check if any role uses this permission
        List<Role> roles = roleRepository.findAll();
        boolean isUsed = roles.stream().anyMatch(role -> 
            role.getPermissions() != null && role.getPermissions().stream().anyMatch(p -> p.getId().equals(id))
        );
        
        if (isUsed) {
            throw new RuntimeException("Cannot delete permission. It is currently associated with one or more roles.");
        }
        
        permissionRepository.delete(permission);
    }
}
