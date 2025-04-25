package com.aloha.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String pw;
    private String link;

    
}
