package com.itextpdf.text;

/** ORACLE SHIM of iText 5 Element (alignment constants with the real iText values). */
public interface Element {
    int ALIGN_LEFT = 0;
    int ALIGN_CENTER = 1;
    int ALIGN_RIGHT = 2;
    int ALIGN_JUSTIFIED = 3;
    int ALIGN_TOP = 4;
    int ALIGN_MIDDLE = 5;
    int ALIGN_BOTTOM = 6;

    static String name(int a) {
        return switch (a) {
            case 0 -> "LEFT";
            case 1 -> "CENTER";
            case 2 -> "RIGHT";
            case 3 -> "JUSTIFIED";
            case 4 -> "TOP";
            case 5 -> "MIDDLE";
            case 6 -> "BOTTOM";
            default -> String.valueOf(a);
        };
    }
}
