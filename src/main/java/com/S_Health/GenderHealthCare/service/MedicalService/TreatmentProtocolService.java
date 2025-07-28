package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.request.TreatmentProtocolRequest;
import com.S_Health.GenderHealthCare.dto.response.TreatmentProtocolResponse;
import com.S_Health.GenderHealthCare.entity.TreatmentProtocol;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.TreatmentProtocolRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TreatmentProtocolService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TreatmentProtocolRepository treatmentProtocolRepository;


    public TreatmentProtocolResponse create (TreatmentProtocolRequest request){
        TreatmentProtocol treatmentProtocol = modelMapper.map(request, TreatmentProtocol.class);
        treatmentProtocolRepository.save(treatmentProtocol);
        return modelMapper.map(treatmentProtocol, TreatmentProtocolResponse.class);

    }

    public List<TreatmentProtocolResponse> getAll (){
        List<TreatmentProtocol> treatmentProtocol = treatmentProtocolRepository.findAll();
        return treatmentProtocol.stream().map(x
                -> modelMapper.map(x, TreatmentProtocolResponse.class))
                .collect(Collectors.toList());

    }

    public TreatmentProtocolResponse getById (Long id){
        TreatmentProtocol treatmentProtocol = treatmentProtocolRepository.findById(id)
                .orElseThrow(() -> new AppException("không tìm thấy ID"));
        return modelMapper.map(treatmentProtocol, TreatmentProtocolResponse.class);

    }
    @Transactional
    public TreatmentProtocolResponse update(Long id, TreatmentProtocolRequest request){
        TreatmentProtocol treatmentProtocol = treatmentProtocolRepository.findById(id)
                .orElseThrow(() -> new AppException("không tìm thấy ID"));

        modelMapper.map(request, treatmentProtocol);

        treatmentProtocolRepository.save(treatmentProtocol);
        return modelMapper.map(treatmentProtocol, TreatmentProtocolResponse.class);
    }
}
