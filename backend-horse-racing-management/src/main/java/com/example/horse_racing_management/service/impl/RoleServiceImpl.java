package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.dto.PermissionDTO;
import com.example.horse_racing_management.dto.RoleDTO;
import com.example.horse_racing_management.entity.Permission;
import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.repository.PermissionRepository;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;

    private RoleDTO mapToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setTitle(role.getTitle());
        dto.setKey(role.getKey());
        
        List<String> permissionIds = new ArrayList<>();
        List<PermissionDTO> permissions = new ArrayList<>();
        
        if (role.getPermissions() != null) {
            for (Permission p : role.getPermissions()) {
                permissionIds.add(p.getId());
                permissions.add(new PermissionDTO(p.getId(), p.getTitle(), p.getKey()));
            }
        }
        
        dto.setPermissionIds(permissionIds);
        dto.setPermissions(permissions);
        return dto;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return mapToDTO(role);
    }

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.findByKey(roleDTO.getKey()).isPresent()) {
            throw new RuntimeException("Role key already exists: " + roleDTO.getKey());
        }

        Role role = new Role();
        role.setTitle(roleDTO.getTitle());
        role.setKey(roleDTO.getKey());
        
        Set<Permission> permissions = new HashSet<>();
        if (roleDTO.getPermissionIds() != null) {
            for (String pid : roleDTO.getPermissionIds()) {
                permissionRepository.findById(pid).ifPresent(permissions::add);
            }
        }
        role.setPermissions(permissions);
        
        return mapToDTO(roleRepository.save(role));
    }

    @Override
    public RoleDTO updateRole(String id, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        if (!existingRole.getKey().equals(roleDTO.getKey()) && roleRepository.findByKey(roleDTO.getKey()).isPresent()) {
            throw new RuntimeException("Role key already exists: " + roleDTO.getKey());
        }

        existingRole.setTitle(roleDTO.getTitle());
        existingRole.setKey(roleDTO.getKey());
        
        Set<Permission> permissions = new HashSet<>();
        if (roleDTO.getPermissionIds() != null) {
            for (String pid : roleDTO.getPermissionIds()) {
                permissionRepository.findById(pid).ifPresent(permissions::add);
            }
        }
        existingRole.setPermissions(permissions);
        
        return mapToDTO(roleRepository.save(existingRole));
    }

    @Override
    public void deleteRole(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
                
        // Check if any user is assigned this role
        boolean isUsed = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() != null && u.getRole().getId().equals(id));
                
        if (isUsed) {
            throw new RuntimeException("Cannot delete role. It is currently assigned to one or more users.");
        }
        
        roleRepository.delete(role);
    }
}
