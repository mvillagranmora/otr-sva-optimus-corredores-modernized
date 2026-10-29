package com.itextpdf.text;

/** ORACLE SHIM of iText 5 Chunk: only NEWLINE. */
public class Chunk implements Element {
    public static final Chunk NEWLINE = new Chunk("\n");
    public final String content;

    public Chunk(String content) {
        this.content = content;
    }
}
