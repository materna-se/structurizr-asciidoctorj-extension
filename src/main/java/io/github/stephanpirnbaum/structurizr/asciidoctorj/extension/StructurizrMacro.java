package io.github.stephanpirnbaum.structurizr.asciidoctorj.extension;

import io.github.stephanpirnbaum.structurizr.renderer.AbstractDiagramExporter;
import io.github.stephanpirnbaum.structurizr.renderer.StructurizrRenderingException;
import io.github.stephanpirnbaum.structurizr.renderer.mermaid.MermaidExporter;
import io.github.stephanpirnbaum.structurizr.renderer.plantuml.PlantUMLExporter;
import io.github.stephanpirnbaum.structurizr.renderer.plantuml.PlantumlLayoutEngine;
import io.github.stephanpirnbaum.structurizr.renderer.structurizr.StructurizrExporter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Name("structurizrc4")
public class StructurizrMacro extends BlockMacroProcessor {

    // Cache expensive exporters (Playwright installation)
    private StructurizrExporter structurizrExporter;

    public StructurizrMacro() {
        log.info("Constructing Structurizr Macro");
    }

    @SneakyThrows
    @Override
    public StructuralNode process(StructuralNode structuralNode, String workspacePath, Map<String, Object> attributes) {
        String viewKey = (String) attributes.get("viewKey");
        if (viewKey == null) {
            throw new StructurizrException("No viewKey specified.");
        }

        String diagramRenderer = (String) attributes.getOrDefault("renderer", "structurizr");
        PlantumlLayoutEngine plantumlLayoutEngine = PlantumlLayoutEngine.valueOf(((String) attributes.getOrDefault("plantumlLayoutEngine", "graphviz")).toUpperCase());

        AbstractDiagramExporter diagramExporter = switch (diagramRenderer) {
            case "plantuml-c4" -> new PlantUMLExporter(plantumlLayoutEngine);
            case "mermaid" -> new MermaidExporter();
            case "structurizr" -> {
                if (this.structurizrExporter == null){
                    this.structurizrExporter = new StructurizrExporter(true);
                }
                yield this.structurizrExporter;
            }
            default -> throw new StructurizrException("Unknown diagram renderer specified: " + diagramRenderer);
        };

        log.info("Rendering view with key {} using engine {}", viewKey, diagramRenderer);

        Path workspaceDslPath = Path.of((String) structuralNode.getDocument().getAttribute("docdir"), workspacePath);

        String workspaceJson = (String) attributes.get("workspaceJson"); // todo directly workspace with different options?
        Path workspaceJsonPath = null;
        if (workspaceJson != null && !workspaceJson.isEmpty()) {
            workspaceJsonPath = Path.of((String) structuralNode.getDocument().getAttribute("docdir"), workspaceJson);
        }

        Path outDir = resolveOutdir(structuralNode);

        // todo everytime only one diagram is requested, but all are rendered. Some caching would be good as the same diagram could be embedded multiple times. need to hash file to register changes.
        try {
            Map<String, Path> diagrams = diagramExporter.export(workspaceDslPath, workspaceJsonPath, outDir.toFile(), viewKey);
            Map<String, Object> imageAttributes = new java.util.HashMap<>();

            imageAttributes.put("target", Path.of(diagramRenderer, viewKey + ".svg").toString());
            imageAttributes.put("title", viewKey);
            return createBlock(structuralNode, "image", "", imageAttributes);
        } catch (StructurizrRenderingException e) {
            throw new RuntimeException(e);
        }
    }

    private Path resolveOutdir(StructuralNode structuralNode) {
        /* Order of directories to be resolved as done by Asciidoctor:
         * {imagesoutdir} if the imagesoutdir attribute has been specified
         * {outdir}/{imagesdir} if the outdir attribute has been specified
         * {to_dir}/{imagesdir} if the to_dir attribute has been specified
         * {base_dir}/{imagesdir}
         */
        Map<String, Object> attributes = structuralNode.getDocument().getAttributes();
        Map<Object, Object> options = structuralNode.getDocument().getOptions();

        // Resolve imagesdir
        String imagesdir = (String) options.getOrDefault("imagesdir", attributes.getOrDefault("imagesdir", "./images"));

        // First, based on document attributes
        if (attributes.containsKey("imagesoutdir")) {
            return Path.of((String) attributes.get("imagesoutdir"));
        } else if (attributes.containsKey("outdir")) {
            return Path.of((String) attributes.get("outdir"), imagesdir);
        } else if (attributes.containsKey("to_dir")) {
            return Path.of((String) attributes.get("to_dir"), imagesdir);
        } else if (attributes.containsKey("base_dir")) {
            return Path.of((String) attributes.get("base_dir"), imagesdir);
        }

        // Second, based on global options
        if (options.containsKey("imagesoutdir")) {
            return Path.of((String) options.get("imagesoutdir"));
        } else if (options.containsKey("outdir")) {
            return Path.of((String) options.get("outdir"), imagesdir);
        } else if (options.containsKey("to_dir")) {
            return Path.of((String) options.get("to_dir"), imagesdir);
        } else if (options.containsKey("base_dir")) {
            return Path.of((String) options.get("base_dir"), imagesdir);
        }

        throw new StructurizrException("Could not resolve configuration for images out dir");
    }
}
