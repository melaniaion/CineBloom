package com.project.cinebloom.logging;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogAdminAction {
    String action();
}

