package com.careerfolio.careerfolio.resume.service;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import com.careerfolio.careerfolio.resume.dto.ResumeDto;
import com.careerfolio.careerfolio.resume.entity.Resume;
import com.careerfolio.careerfolio.resume.repository.ResumeRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    private List<Map<String, Object>> safeJsonToList(String json) {
        try {
            if (json == null || json.trim().equals("") || json.equals("[]")) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public Long save(ResumeDto dto, String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Resume resume = Resume.builder()
                .member(member)
                .title(dto.getTitle())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .birthday(dto.getBirthday())
                .address(dto.getAddress())
                .photoPath(null)
                .skills(dto.getSkills())
                .certificates(dto.getCertificates())
                .educations(dto.getEducations())
                .growth(dto.getGrowth())
                .motivation(dto.getMotivation())
                .personality(dto.getPersonality())
                .future(dto.getFuture())
                .build();

        resumeRepository.save(resume);
        return resume.getId();
    }

    @Transactional
    public Long saveWithPhoto(ResumeDto dto, MultipartFile photoFile, String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        String photoPath = null;

        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                photoPath = saveMultipartPhoto(photoFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("사진 저장 실패: " + e.getMessage());
        }

        Resume resume = Resume.builder()
                .member(member)
                .title(dto.getTitle())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .birthday(dto.getBirthday())
                .address(dto.getAddress())
                .photoPath(photoPath)
                .skills(dto.getSkills())
                .certificates(dto.getCertificates())
                .educations(dto.getEducations())
                .growth(dto.getGrowth())
                .motivation(dto.getMotivation())
                .personality(dto.getPersonality())
                .future(dto.getFuture())
                .build();

        resumeRepository.save(resume);
        return resume.getId();
    }

    private String saveMultipartPhoto(MultipartFile photoFile) throws IOException {

        String folderPath = "C:/careerfolio/uploads/resume/";

        File dir = new File(folderPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + photoFile.getOriginalFilename();

        File dest = new File(folderPath + fileName);

        photoFile.transferTo(dest);
        return "/uploads/resume/" + fileName;
    }

    public Resume getResume(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이력서 없음"));
    }

    public void generatePdf(Long id, HttpServletResponse response) throws Exception {

        Resume resume = getResume(id);

        List<Map<String, Object>> skills = safeJsonToList(resume.getSkills());
        List<Map<String, Object>> certs  = safeJsonToList(resume.getCertificates());
        List<Map<String, Object>> edus   = safeJsonToList(resume.getEducations());

        Context context = new Context();
        context.setVariable("resume", resume);
        context.setVariable("skills", skills);
        context.setVariable("certs", certs);
        context.setVariable("edus", edus);

        String html = templateEngine.process("resume/pdf", context);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=resume_" + id + ".pdf");

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();

        OutputStream out = response.getOutputStream();
        out.write(os.toByteArray());
        out.flush();
        out.close();
    }


    public List<Resume> getList(String username) {
        return resumeRepository.findByMember_Username(username);
    }

    @Transactional
    public void deleteResume(Long id, String username) {

        Resume resume = getResume(id);

        if (!resume.getMember().getUsername().equals(username)) {
            throw new SecurityException("본인이 작성한 이력서만 삭제 가능");
        }

        resumeRepository.delete(resume);
    }


    @Transactional
    public void updateResumeWithPhoto(
            Long id, ResumeDto dto,
            MultipartFile photoFile,
            String deletePhotoFlag,
            String username
    ) {

        Resume resume = getResume(id);

        if (!resume.getMember().getUsername().equals(username)) {
            throw new SecurityException("본인이 작성한 이력서만 수정 가능");
        }

        if ("true".equals(deletePhotoFlag)) {
            resume.setPhotoPath(null);
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                String path = saveMultipartPhoto(photoFile);
                resume.setPhotoPath(path);
            } catch (Exception e) {
                throw new RuntimeException("사진 업로드 실패: " + e.getMessage());
            }
        }

        updateResumeFields(resume, dto);
        resumeRepository.save(resume);
    }

    @Transactional
    public void updateResumeJson(Long id, ResumeDto dto, String username) {

        Resume resume = getResume(id);

        if (!resume.getMember().getUsername().equals(username)) {
            throw new SecurityException("본인이 작성한 이력서만 수정 가능");
        }

        updateResumeFields(resume, dto);
        resumeRepository.save(resume);
    }

    public List<Map<String, Object>> parseSkills(String json) {
        return safeJsonToList(json);
    }

    public List<Map<String, Object>> parseCerts(String json) {
        return safeJsonToList(json);
    }

    public List<Map<String, Object>> parseEdus(String json) {
        return safeJsonToList(json);
    }

    private void updateResumeFields(Resume resume, ResumeDto dto) {

        resume.setTitle(dto.getTitle());
        resume.setName(dto.getName());
        resume.setEmail(dto.getEmail());
        resume.setPhone(dto.getPhone());
        resume.setBirthday(dto.getBirthday());
        resume.setAddress(dto.getAddress());

        resume.setSkills(dto.getSkills());
        resume.setCertificates(dto.getCertificates());
        resume.setEducations(dto.getEducations());

        resume.setGrowth(dto.getGrowth());
        resume.setMotivation(dto.getMotivation());
        resume.setPersonality(dto.getPersonality());
        resume.setFuture(dto.getFuture());
    }
}
