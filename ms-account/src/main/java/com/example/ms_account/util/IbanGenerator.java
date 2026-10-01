package com.example.ms_account.util;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class IbanGenerator {

    public static String generate() {
        Iban randomIban = Iban.random(CountryCode.AZ);
        System.out.println("-----------------"+randomIban.toFormattedString());
        return randomIban.toFormattedString();
    }
}
