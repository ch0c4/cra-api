package org.johan.cra.helpers;

import jakarta.inject.Singleton;

import java.time.LocalDate;

@Singleton
public class TimeHelper {

    public LocalDate now() {
        return LocalDate.now();
    }

}
