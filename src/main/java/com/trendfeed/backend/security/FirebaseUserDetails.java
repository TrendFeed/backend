package com.trendfeed.backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUserDetails {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
}
