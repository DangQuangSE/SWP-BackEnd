package com.S_Health.GenderHealthCare.exception.exceptions;

import javassist.NotFoundException;

public class NullExpcept extends NotFoundException {
    public NullExpcept(String message) {
        super(message);
    }
}
