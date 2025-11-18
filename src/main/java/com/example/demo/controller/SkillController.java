package com.example.demo.controller;

import com.example.demo.domain.Skill;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.SkillRepository;
import com.example.demo.service.SkillService;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;


@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }


    @PostMapping
    @ApiMessage("Create a new skill under a Job Profession")
    public ResponseEntity<Skill> createSkill(
            @Valid @RequestBody Skill skill,
            @RequestParam Long professionId) {
        Skill created = skillService.create(skill, professionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{id}")
    @ApiMessage("Update skill")
    public ResponseEntity<Skill> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody Skill skill) {
        Skill updated = skillService.update(id, skill);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    @ApiMessage("Delete a skill")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Get Skill by Id
    @GetMapping("/{id}")
    @ApiMessage("Get skill by Id")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        Skill skill = skillService.getById(id);
        return ResponseEntity.ok(skill);
    }

    // Get all Skills (paginate + filter)
    @GetMapping
    @ApiMessage("Fetch all skills with pagination and filter")
    public ResponseEntity<ResultPaginationDTO> getAllSkills(
            @Filter Specification<Skill> spec,
            Pageable pageable) {
        ResultPaginationDTO rs = skillService.getAll(spec, pageable);
        return ResponseEntity.ok(rs);
    }
    @GetMapping("/by-profession/{professionId}")
    @ApiMessage("Get skills by Job Profession")
    public ResponseEntity<List<Skill>> getSkillsByProfession(@PathVariable Long professionId) {
        List<Skill> skills = skillService.getByProfessionId(professionId);
        return ResponseEntity.ok(skills);
    }
}
