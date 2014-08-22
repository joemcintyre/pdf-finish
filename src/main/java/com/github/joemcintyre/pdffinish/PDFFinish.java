/**
 * PDF Finish, updates PDF metadata and table of contents.
 * 
 * @author Joe McIntyre 
 * 
 * Copyright (c) 2014, Joe McIntyre
 * License: MIT
 */
package com.github.joemcintyre.pdffinish;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.grack.nanojson.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.*;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * PDF Finish.
 * Update metadata and create table of contents for PDF files.
 */
public class PDFFinish {
    private String title;
    private String author;
    private String subject;
    private String keywords;
    ArrayList<PDFTextFinder.Font> fontList = null;

    /**
     * Constructor.
     * 
     * @param show Flag, show metadata and ToC only.
     * @param fileConfig Configuration file.
     * @param fileInput PDF input file.
     * @param filenameOutput PDF output file name.
     */
    public PDFFinish (boolean show, File fileConfig, File fileInput, String filenameOutput) {
        PDDocument document = null;
        try {
            document = PDDocument.load (fileInput);
        } catch (IOException e) {
            System.out.println ("Error reading input PDF: " + e);
            System.exit (1);
        }

        if (show) {
            try {
                showMetadata (document);
            } catch (IOException e) {
                System.out.println ("Error reading metadata: " + e);
                System.exit (1);
            }
            showTOC (document);
            showFonts (document);
        } else {
            JsonObject config = null;

            File fileOutput = new File (filenameOutput);

            try {
                FileInputStream fis = new FileInputStream (fileConfig);
                byte data[] = new byte[(int) fileConfig.length ()];
                fis.read (data);
                fis.close ();

                String content = new String (data, "UTF-8");
                config = JsonParser.object ().from (content);
            } catch (Exception e) {
                System.out.println ("Error reading configuration file: " + e);
                System.exit (1);
            }

            processConfig (config);
            processPDF (fileInput, fileOutput);
        }
    }

    /**
     * Get configuration information from file.
     * 
     * @param config JSON configuration object.
     */
    private void processConfig (JsonObject config) {
        title = config.getString ("title");
        author = config.getString ("author");
        subject = config.getString ("subject");
        keywords = config.getString ("keywords");

        // heading fonts
        JsonArray headings = config.getArray ("toc");
        if (headings != null) {
            fontList = new ArrayList<PDFTextFinder.Font> ();
            for (Object heading : headings) {
                JsonObject h = (JsonObject) heading;
                String font = h.getString ("font");
                float size = h.getFloat ("size");
                int level = h.getInt ("level");
                fontList.add (new PDFTextFinder.Font (font, size, level));
            }
        }
    }

    /**
     * Process the PDF input file, producing the output file.
     * 
     * @param fileInput PDF input file.
     * @param fileOutput PDF output file.
     */
    private void processPDF (File fileInput, File fileOutput) {
        PDDocument document = null;
        try {
            document = PDDocument.load (fileInput);
        } catch (IOException e) {
            System.out.println ("Error reading PDF: " + e);
        }

        if (document != null) {
            boolean save = true;
            try {
                updateMetadata (document);
                if (fontList != null) {
                    updateTOC (document);
                }
            } catch (IOException e) {
                System.out.println ("Error processing PDF: " + e);
                save = false;
            }

            if (save) {
                try {
                    document.save (fileOutput);
                    System.out.println ("Write complete");
                } catch (Exception e) {
                    System.out.println ("Error writing PDF: " + e);
                }
            }

            try {
                document.close ();
            } catch (Exception e) {
                System.out.println ("Error closing document: " + e);
            }
        }
    }

    /**
     * Show metadata from PDF document.
     * 
     * @param document Loaded PDF document.
     */
    private static void showMetadata (PDDocument document) throws IOException {
        PDDocumentInformation info = document.getDocumentInformation ();
        System.out.println ("Title: " + info.getTitle ());
        System.out.println ("Author: " + info.getAuthor ());
        System.out.println ("Subject: " + info.getSubject ());
        System.out.println ("Keywords: " + info.getKeywords ());
        System.out.println ("Creator: " + info.getCreator ());
        System.out.println ("Producer: " + info.getProducer ());
        System.out.println ("Creation Date: " + info.getCreationDate ());
        System.out.println ("Modification Date: " + info.getModificationDate ());
    }

    /**
     * Update metadata.
     * 
     * @param document Loaded PDF document.
     * @throws IOException
     */
    private void updateMetadata (PDDocument document) throws IOException {
        PDDocumentInformation info = document.getDocumentInformation ();
        if (title != null) {
            info.setTitle (title);
        }
        if (author != null) {
            info.setAuthor (author);
        }
        if (subject != null) {
            info.setSubject (subject);
        }
        if (keywords != null) {
            info.setKeywords (keywords);
        }
    }

    /**
     * Show Table of Contents in document.
     * 
     * @param document Loaded PDF document.
     */
    private static void showTOC (PDDocument document) {
        System.out.println ("Table of Contents");
        PDDocumentOutline outline = document.getDocumentCatalog ().getDocumentOutline ();
        if (outline != null) {
            showEntry (outline, "");
        }
    }

    /**
     * Show TOC entries at current hierarchy level, and sub-levels using recursive
     * call.
     * 
     * @param entry Starting node.
     * @param indent Spaces to precede output text.
     */
    private static void showEntry (PDOutlineNode entry, String spaces) {
        PDOutlineItem node = entry.getFirstChild ();
        while (node != null) {
            System.out.println (spaces + node.getTitle ());
            showEntry (node, spaces + "  ");
            node = node.getNextSibling ();
        }
    }

    /**
     * Update table of contents in destination document.
     * 
     * @param document PDF document to update.
     */
    private void updateTOC (PDDocument document) {
        PDDocumentOutline outline = new PDDocumentOutline ();
        document.getDocumentCatalog ().setDocumentOutline (outline);
        PDOutlineItem topItem = new PDOutlineItem ();
        topItem.setTitle (title);
        outline.appendChild (topItem);

        try {
            PDFTextFinder finder = new PDFTextFinder (fontList);
            List<PDFTextFinder.PDFText> headings = finder.getTextList (document);
            PDOutlineItem level[] = { topItem, null, null, null };
            for (PDFTextFinder.PDFText heading : headings) {
                PDPageXYZDestination dest = new PDPageXYZDestination ();
                dest.setPage (heading.page);

                PDOutlineItem bookmark = new PDOutlineItem ();
                bookmark.setDestination (dest);
                bookmark.setTitle (heading.text);
                level[heading.tag - 1].appendChild (bookmark);
                level[heading.tag] = bookmark;
            }
        } catch (IOException e) {
            System.out.println ("Error :" + e);
        }

        topItem.openNode ();
        outline.openNode ();
    }

    /**
     * Show list of fonts used in PDF document.
     * 
     * @param document PDF document.
     */
    private void showFonts (PDDocument document) {
        try {
            PDFTextFinder finder = new PDFTextFinder (null);
            List<PDFTextFinder.PDFText> elements = finder.getTextList (document);
            
            ArrayList<String> fontList = new ArrayList<String> ();
            for (PDFTextFinder.PDFText element : elements) {
                String font = element.font + ":" + element.fontSize;
                if (fontList.indexOf (font) == -1) {
                    fontList.add (font);
                }
            }
            
            System.out.println ("\nFonts\n");
            java.util.Collections.sort (fontList);
            for (String font : fontList) {
                System.out.println (font);
            }
        } catch (IOException e) {
            System.out.println ("Error :" + e);
        }
    }
}
