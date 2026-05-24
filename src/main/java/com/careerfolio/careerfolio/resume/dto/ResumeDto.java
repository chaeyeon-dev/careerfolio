package com.careerfolio.careerfolio.resume.dto;

import lombok.Data;

@Data
public class ResumeDto {

    private Long id;

    private String title;
    private String name;
    private String email;
    private String phone;
    private String birthday;
    private String address;

    private String photoBase64;

    private String skills;
    private String certificates;
    private String educations;

    private String growth;
    private String motivation;
    private String personality;
    private String future;
}
