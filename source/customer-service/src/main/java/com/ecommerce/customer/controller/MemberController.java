package com.ecommerce.customer.controller;

import com.ecommerce.customer.entity.Member;
import com.ecommerce.customer.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Member> getByUsername(@PathVariable("username") String username) {
        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(member);
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        if (memberRepository.existsByUsername(member.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username đã tồn tại!");
        }
        return ResponseEntity.ok(memberRepository.save(member));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        Optional<Member> memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Member member = memberOpt.get();
        if (!member.getPassword().equals(body.get("oldPassword"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mật khẩu cũ không đúng!");
        }
        member.setPassword(body.get("newPassword"));
        memberRepository.save(member);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}