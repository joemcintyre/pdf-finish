/**
 * Text finder for PDF content. Find text that matches one of a set of fonts.
 * 
 * Copyright (c) 2014, Joe McIntyre
 * License: MIT
 */
package com.github.joemcintyre.pdffinish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;

/**
 * Find text in the PDF document that matches one of a set of fonts
 * indicated, making the list of text elements available.
 */
public class PDFTextFinder extends PDFTextStripper {
    private List<PDFTextFinder.PDFText> textList = new ArrayList<PDFTextFinder.PDFText> ();
    private List<PDFTextFinder.Font> fontFilterList = null;
    private PDPage currentPage = null;

    /**
     * Font name and size;
     */
    public static class Font {
        public String name;
        public float size;
        public int tag;

        /**
         * Constructor.
         * 
         * @param name PDF font name, style.
         * @param size Font size in points.
         * @param tag Tag to associate with text with this font.
         */
        public Font (String name, float size, int tag) {
            this.name = name;
            this.size = size;
            this.tag = tag;
        }
    }

    /**
     * Text element: text, page, position and tag. The tag allows the text
     * to be identified within a grouping based on its font, allowing
     * multiple font matches to be grouped, or tags to be used to indicate
     * hierarchical relationships.
     */
    public class PDFText {
        public PDPage page;
        public TextPosition metadata;
        public String text;
        public int tag;
        public String font;
        public float fontSize;

        /**
         * Constructor.
         * 
         * @param page Page heading belongs to.
         * @param metadata Text metadata, including position.
         * @param text Heading text.
         * @param font Font of text.
         * @param fontSize Size of font.
         */
        private PDFText (PDPage page, TextPosition metadata, String text, String font, float fontSize) {
            this.page = page;
            this.metadata = metadata;
            this.text = text;
            this.tag = 0;
            this.font = font;
            this.fontSize = fontSize;
        }
    }

    /**
     * Constructor. Create the instance with the font list to process with.
     * @param findFontList List of Font objects.
     * @throws IOException On error accessing PDF content.
     */
    public PDFTextFinder (List<Font> fontFilterList) throws IOException {
        super.setSortByPosition (true);
        this.fontFilterList = fontFilterList;
    }

    /**
     * Get the text element list from the specified PDF document.
     * @param document PDF document object.
     * @return List of text elements.
     * @throws IOException On error accessing PDF content.
     */
    public List<PDFText> getTextList (PDDocument document) throws IOException {
        getText (document);
        return (textList);
    }

    /**
     * Record current page when text processor starts a new page.
     * 
     * @param page Page object for new page.
     */
    protected void startPage (PDPage page) {
        currentPage = page;
    }

    /**
     * Override the writeString method to capture the text elements, and their
     * associated information. For those that fit the font matching criteria,
     * record these in the text element list, with their associated tag.
     */
    protected void writeString (String fullText, List<TextPosition> textPositions) throws IOException {
        String baseFont = "";
        float fontSize = 0.0f;
        String lastBaseFont = null;
        float lastFontSize = 0.0f;

        ArrayList<PDFText> fragments = new ArrayList<PDFText> ();
        StringBuilder text = new StringBuilder ();
        TextPosition startPosition = null;

        // for all characters in text
        for (TextPosition position : textPositions) {
            // hold start of text position for text element
            if (startPosition == null) {
                startPosition = position;
            }

            // get font face and style, separate away subset if present
            baseFont = position.getFont ().getBaseFont ();
            int plus = baseFont.indexOf ('+');
            if (plus > -1) {
                baseFont = baseFont.substring (plus + 1);
            }
            fontSize = position.getFontSizeInPt ();

            // if end of a text fragment in the current font, record the text
            // fragment and start the next text fragment
            if (baseFont != null) {
                if ((baseFont.equals (lastBaseFont) == false) || (fontSize != lastFontSize)) {
                    lastBaseFont = baseFont;
                    lastFontSize = fontSize;
                    
                    if (text.length () > 0) {
                        fragments.add (new PDFText (currentPage, startPosition, text.toString (), baseFont, fontSize));
                        text.setLength (0);
                        startPosition = null;
                    }
                }
                text.append (position.getCharacter ());
            }
        }

        // if loop ended with a text fragment, record it
        if (text.length () > 0) {
            fragments.add (new PDFText (currentPage, startPosition, text.toString (), baseFont, fontSize));
        }    

        // for each fragment, if it is a matching font, add the tag and the fragment to the result list
        for (PDFText fragment : fragments) {
            if ((fontFilterList == null) || (fontFilterList.size () == 0)) {
                textList.add (fragment);
            } else {
                for (Font font : fontFilterList) {
                    if ((fragment.font.equals (font.name)) && (fragment.fontSize == font.size)) {
                        fragment.tag = font.tag;
                        textList.add (fragment);
                    }
                }
            }
        }
        // parent call to record the text string for its own processing
        writeString (fullText);
    }
}
