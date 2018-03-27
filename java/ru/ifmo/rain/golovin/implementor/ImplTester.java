package ru.ifmo.rain.golovin.implementor;

import info.kgeorgiy.java.advanced.implementor.standard.basic.Accessible;
import info.kgeorgiy.java.advanced.implementor.standard.full.BMPImageWriteParam;
import info.kgeorgiy.java.advanced.implementor.standard.full.LdapReferralException;
import info.kgeorgiy.java.advanced.implementor.standard.full.SDeprecated;

import java.nio.file.Paths;

public class ImplTester {
    private static Implementor hero = new Implementor();

    public static void main(String[] args) {
        try {
            hero.implement(SDeprecated.class, Paths.get("/media/pavel/DATA/java-course/java/ru/ifmo/rain/golovin/implementor/temp/"));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
