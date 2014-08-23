#PDF Finish

PDF finishing touches tool to add metadata and table of contents.

Generated PDF files, from various programs, may not have complete metadata
and may not have the desired content in the navigation table of contents
available in online readers.

##Usage

There are two usage modes, show and update.

###Show mode

Show mode displays the metadata, table of contents, and fonts used for a PDF
document. The command is,

    java -jar pdf-finish -s -i example.pdf

###Update mode

Update mode uses a configuration file to generate a new PDF file. An example
command is,

    java -jar pdf-finish -i input.pdf -o output.pdf -c config.json

The input PDF will not be changed.

The output PDF will contain the changes based on the configuration options. If
a file with the same name already exists, it will be overwritten.

The configuration file is a JSON file, containing the following fields that
are used to add/update the metadata.

- title: document title, and table of contents header on most viewers
- author: document author
- subject: document subject, useful for search
- keywords: comma separated list of keywords, useful for search

The next section of the configuration file is the "toc" section, which
contains an array of font objects. These are used to find the elements to
place in the table of contents. This is independent of any table of contents
that may exist within the PDF document. Each font object contains,

- font: font name
- size: font size (floating point accepted)
- level: TOC hierarchy level to assign the element to

The font names and sizes can be determined by using the show mode, which
shows all font name/size combinations used in the PDF document.

###Example Configuration File

The following is an example, showing the metadata updates and a three level
table of contents.

    {
      "title":"Mousetrap Building",
      "author":"Jane Doe",
      "subject":"Building a better mousetrap",
      "keywords":"mousetrap, mouse, cheese",
      "toc":[
        { "font":"Optima-Bold", "size":24.0, "level":1 },
        { "font":"Optima-Bold", "size":16.0, "level":2 },
        { "font":"Optima-Bold", "size":14.0, "level":3 }
      ]
    }

##License

MIT
