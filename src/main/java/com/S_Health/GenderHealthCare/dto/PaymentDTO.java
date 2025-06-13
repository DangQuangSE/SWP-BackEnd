package com.S_Health.GenderHealthCare.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentDTO implements Serializable {
    private String status;
    private String message;
    private String URL;
}
