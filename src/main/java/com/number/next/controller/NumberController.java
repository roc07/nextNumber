package com.number.next.controller;

import com.number.next.exception.InvalidListSize;
import com.number.next.exception.InvalidParametersException;
import com.number.next.model.Parameters;
import com.number.next.service.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.List;

@RestController("/")
@RequiredArgsConstructor
public class NumberController {

    private final NumberService numberService;

    @PostMapping("/provideNumbers")
    public ResponseEntity<String> provideNumbers(@RequestBody List<Integer> numbers) {
        if (numbers == null || numbers.size() < 2) {
            return ResponseEntity.badRequest().body("Provide at least two numbers");
        }
        List<Double> doubles = numberService.prepareNumbers(numbers);
        return ResponseEntity.ok("Expected chance for numbers to appear: " + doubles);
    }

    @PostMapping("/provideNumbersAndProbabilities")
    public ResponseEntity<String> provideNumbersAndProbabilities(@RequestBody Parameters parameters) {
        try {
            List<Double> doubles = numberService.prepareProbabilities(parameters);
            return ResponseEntity.ok("Expected chance for numbers to appear: " + doubles);
        } catch (InvalidParametersException | InvalidListSize e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/singleNumber")
    public ResponseEntity<?> getSingleNumber() {
        try {
            return ResponseEntity.ok(numberService.getSingleNumber());
        } catch (InvalidListSize e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/multipleNumbers")
    public ResponseEntity<?> getMultipleNumbers(@RequestParam int numberOfRequests) {
        try {
            if (numberOfRequests < 1) {
                throw new InvalidParameterException("Number of requests must greater than 0");
            }
            return ResponseEntity.ok(numberService.getNumbersWithAppearances(numberOfRequests));
        } catch (InvalidListSize | InvalidParameterException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
