package com.videoplatform.dto;

import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListDTO {
    private Long id;
    private String name;
    private String description;
    private Set<String> memberUsernames;
}

