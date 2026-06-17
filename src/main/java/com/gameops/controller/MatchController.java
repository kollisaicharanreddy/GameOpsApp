package com.gameops.controller;

import com.gameops.model.MatchResult;
import com.gameops.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/submit-score")
    public ResponseEntity<String> submitScore(@RequestBody MatchResult result) {
        matchService.saveMatchResult(result);
        return ResponseEntity.ok("Score submitted successfully");
    }
}