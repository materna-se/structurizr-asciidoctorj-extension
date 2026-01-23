# structurizr-asciidoctorj-extension
Support the rendering of Structurizr DSL views in AsciiDoc using different rendering algorithms including the same as provided by the Structurizr UI.

## Usage in the IntelliJ AsciiDoc plugin

This extension can also be used in the IntelliJ AsciiDoc plugin so that the diagrams are rendered also in the preview view. 
For this, download the Shaded jar which is provided in this repository under the latest release. 

Next, when having IntelliJ open, create following folder in the root directory: `.asciidoctor/lib`.

Now copy the downloaded shaded jar into the just created folder and reopen the Asciidoc file. After a few seconds, a notification will be displayed saying that an extension folder has been identified. In order to activate the extension, confirm that the folder should be used. 
