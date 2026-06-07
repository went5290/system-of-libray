package com.humber.library.fine;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fines")
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @PostMapping("/{fineId}/payments")
    public FinePaymentResponse pay(@PathVariable long fineId, @Valid @RequestBody FinePaymentRequest request) {
        return fineService.pay(fineId, request);
    }
}
