package com.example.horse_racing_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private String id;
    private String title;
    private String key;
    private List<String> permissionIds;
    private List<PermissionDTO> permissions; // for viewing
}
