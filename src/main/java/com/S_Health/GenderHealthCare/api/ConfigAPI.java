package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.entity.ConfigValue;
import com.S_Health.GenderHealthCare.service.configValue.ConfigValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@SecurityRequirement(name = "api")
public class ConfigAPI {

    @Autowired
    ConfigValueService configValueService;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả cấu hình", description = "Lấy danh sách tất cả các cấu hình hệ thống")
    public ResponseEntity<List<ConfigValue>> getAllConfigs() {
        return ResponseEntity.ok(configValueService.getAllConfigs());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật cấu hình", description = "Cập nhật giá trị cấu hình theo ID")
    public ResponseEntity<ConfigValue> updateConfig(@PathVariable Long id, @RequestParam Integer value) {
        ConfigValue updatedConfig = configValueService.updateConfig(id, value);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @PostMapping
    @Operation(summary = "Tạo cấu hình mới", description = "Tạo một cấu hình mới")
    public ResponseEntity<ConfigValue> createConfig(@RequestParam String name, @RequestParam Integer value) {
        ConfigValue savedConfig = configValueService.createConfig(name, value);
        return ResponseEntity.ok(savedConfig);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa cấu hình", description = "Xóa cấu hình theo ID")
    public ResponseEntity<String> deleteConfig(@PathVariable Long id) {
        configValueService.deleteConfig(id);
        return ResponseEntity.ok("Đã xóa cấu hình thành công");
    }
}
