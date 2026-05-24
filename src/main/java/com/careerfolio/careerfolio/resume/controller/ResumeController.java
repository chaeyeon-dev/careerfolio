package com.careerfolio.careerfolio.resume.controller;

import com.careerfolio.careerfolio.resume.dto.ResumeDto;
import com.careerfolio.careerfolio.resume.entity.Resume;
import com.careerfolio.careerfolio.resume.service.ResumeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/resume")
public class ResumeController {
        private void loadResumeDetailToModel(Resume resume, Model model) throws Exception {

        List<Map<String, Object>> skills =
                objectMapper.readValue(resume.getSkills(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> certs =
                objectMapper.readValue(resume.getCertificates(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> edus =
                objectMapper.readValue(resume.getEducations(), new TypeReference<List<Map<String, Object>>>() {});

        model.addAttribute("resume", resume);
        model.addAttribute("skills", skills);
        model.addAttribute("certs", certs);
        model.addAttribute("edus", edus);
    }

    private final ResumeService resumeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ============================================================
       ⭐ CREATE — FormData 방식 (신규)
    ============================================================ */
    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> createResume(

            @RequestParam("title") String title,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("birthday") String birthday,
            @RequestParam("address") String address,

            @RequestParam("skills") String skills,
            @RequestParam("certificates") String certificates,
            @RequestParam("educations") String educations,

            @RequestParam("growth") String growth,
            @RequestParam("motivation") String motivation,
            @RequestParam("personality") String personality,
            @RequestParam("future") String future,

            @RequestPart(value = "photoFile", required = false) MultipartFile photoFile,
            Principal principal
    ) {

        if (principal == null)
            return ResponseEntity.badRequest().body("로그인이 필요합니다.");

        ResumeDto dto = new ResumeDto();
        dto.setTitle(title);
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setBirthday(birthday);
        dto.setAddress(address);

        dto.setSkills(skills);
        dto.setCertificates(certificates);
        dto.setEducations(educations);

        dto.setGrowth(growth);
        dto.setMotivation(motivation);
        dto.setPersonality(personality);
        dto.setFuture(future);

        Long id = resumeService.saveWithPhoto(dto, photoFile, principal.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);

        return ResponseEntity.ok(result);
    }

    /* ============================================================
       작성 폼
    ============================================================ */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("resume", new Resume());
        return "resume/resume_form";
    }

    /* ============================================================
       단일 VIEW (READ)
    ============================================================ */
    @GetMapping("/view/{id}")
    public String viewResume(@PathVariable Long id, Model model) throws Exception {
        Resume resume = resumeService.getResume(id);
        loadResumeDetailToModel(resume, model);
        return "resume/view";
    }

    @GetMapping("/print/{id}")
    public String printResume(@PathVariable Long id, Model model) throws Exception {
        Resume resume = resumeService.getResume(id);
        loadResumeDetailToModel(resume, model);
        return "resume/print";
    }


    @GetMapping("/pdf/{id}")
    public String pdfResume(@PathVariable Long id, Model model) throws Exception {
        Resume resume = resumeService.getResume(id);
        loadResumeDetailToModel(resume, model);
        return "resume/pdf";
    }


    /* ============================================================
       내 이력서 목록
    ============================================================ */
    @GetMapping("/my")
    public String myResumes(Model model, Principal principal) {

        if (principal == null) return "redirect:/member/login";

        model.addAttribute("resumeList", resumeService.getList(principal.getName()));
        return "resume/my";
    }

    /* ============================================================
       이력서 삭제
    ============================================================ */
    @PostMapping("/delete/{id}")
    public String deleteResume(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/member/login";

        resumeService.deleteResume(id, principal.getName());
        return "redirect:/resume/my";
    }

    /* ============================================================
       이력서 수정 페이지 (GET)
    ============================================================ */
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        if (principal == null) return "redirect:/member/login";

        Resume resume = resumeService.getResume(id);

        if (!resume.getMember().getUsername().equals(principal.getName()))
            throw new SecurityException("본인만 수정 가능합니다.");

        model.addAttribute("resume", resume);
        return "resume/edit";
    }

    /* ============================================================
       ⭐ 최종 수정 — FormData 100% 호환
    ============================================================ */
    @PostMapping(
            value = "/edit/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> updateResume(

            @PathVariable Long id,

            @RequestParam("title") String title,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("birthday") String birthday,
            @RequestParam("address") String address,

            @RequestParam("skills") String skills,
            @RequestParam("certificates") String certificates,
            @RequestParam("educations") String educations,

            @RequestParam("growth") String growth,
            @RequestParam("motivation") String motivation,
            @RequestParam("personality") String personality,
            @RequestParam("future") String future,

            @RequestPart(value = "photoFile", required = false) MultipartFile photoFile,
            @RequestParam("deletePhotoFlag") String deletePhotoFlag,

            Principal principal
    ) {
        if (principal == null)
            return ResponseEntity.badRequest().body("로그인이 필요합니다.");

        ResumeDto dto = new ResumeDto();
        dto.setTitle(title);
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setBirthday(birthday);
        dto.setAddress(address);
        dto.setSkills(skills);
        dto.setCertificates(certificates);
        dto.setEducations(educations);
        dto.setGrowth(growth);
        dto.setMotivation(motivation);
        dto.setPersonality(personality);
        dto.setFuture(future);

        resumeService.updateResumeWithPhoto(
                id,
                dto,
                photoFile,
                deletePhotoFlag,
                principal.getName()
        );

        return ResponseEntity.ok("수정 완료");
    }
}
