package com.S_Health.GenderHealthCare.service.configValue;

import com.S_Health.GenderHealthCare.entity.ConfigValue;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.ConfigValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigValueService {

    @Autowired
    private ConfigValueRepository configValueRepository;

    /**
     * Lấy tất cả cấu hình
     */
    public List<ConfigValue> getAllConfigs() {
        return configValueRepository.findAll();
    }

    /**
     * Tạo cấu hình mới
     */
    public ConfigValue createConfig(String name, Integer value) {
        if (configValueRepository.existsByName(name)) {
            throw new AppException("Cấu hình đã tồn tại");
        }
        ConfigValue config = ConfigValue.builder()
                .name(name)
                .value(value)
                .build();
        return configValueRepository.save(config);
    }

    /**
     * Cập nhật cấu hình
     */
    public ConfigValue updateConfig(Long id, Integer value) {
        ConfigValue config = configValueRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy cấu hình"));

        config.setValue(value);
        return configValueRepository.save(config);
    }

    /**
     * Xóa cấu hình
     */
    public void deleteConfig(Long id) {
        if (!configValueRepository.existsById(id)) {
            throw new AppException("Không tìm thấy cấu hình");
        }

        configValueRepository.deleteById(id);
    }

    /**
     * Lấy giá trị cấu hình theo tên
     */
    public Integer getConfigValue(String name, Integer defaultValue) {
        return configValueRepository.findByName(name)
                .map(ConfigValue::getValue)
                .orElse(defaultValue);
    }
}
