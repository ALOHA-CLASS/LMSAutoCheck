package com.aloha.domain;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;                      // 아이디
    private String pw;                      // 비밀번호
    private String subDomain;               // 서브도메인
    private Map<String, String> courseMap;  // 과정코드, 과정명
    private String selectedCourseCode;      // 선택한 과정코드
}
