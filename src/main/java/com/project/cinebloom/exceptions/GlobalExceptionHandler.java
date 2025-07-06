package com.project.cinebloom.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            MovieNotFoundException.class,
            ReviewNotFoundException.class,
            CategoryNotFoundException.class
    })
    public ModelAndView handleNotFoundExceptions(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView("notFoundException");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }
}