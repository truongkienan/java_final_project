package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.entity.Unit;
import com.ecommerce.catalog.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    @Autowired
    private UnitRepository unitRepository;

    @GetMapping
    public List<Unit> getUnits() {
        return unitRepository.findAll();
    }

    @GetMapping("/{id}")
    public Unit getUnit(@PathVariable("id") Short id) {
        return unitRepository.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> createUnit(@RequestBody Unit unit) {
        if (unit.getCategory() == null || unit.getCategory().getCategoryId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
        }
        return ResponseEntity.ok(unitRepository.save(unit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUnit(@PathVariable("id") Short id, @RequestBody Unit unit) {
        if (unit.getCategory() == null || unit.getCategory().getCategoryId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
        }
        unit.setUnitId(id);
        return ResponseEntity.ok(unitRepository.save(unit));
    }

    @DeleteMapping("/{id}")
    public void deleteUnit(@PathVariable("id") Short id) {
        unitRepository.deleteById(id);
    }
}
