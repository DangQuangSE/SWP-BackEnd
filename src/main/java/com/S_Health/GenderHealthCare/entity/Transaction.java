package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String transactionCode;
    String requestId;
    String orderId;
    String responseMessage;
    int resultCode;
    String payUrl;
    LocalDateTime responseTime;


    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    Payment payment;
}
