package com.example.mail.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class UtilService {

    private final String DEFAULT_DATE_FORMAT = "MMMM d, yyyy";
    private final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public Date generateDateFromString(String dateString) throws ParseException{
        DateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);

        return format.parse(dateString);
    }
}