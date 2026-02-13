package de.materna.structurizr.asciidoctorj.extension;

import de.materna.structurizr.renderer.Renderer;
import de.materna.structurizr.renderer.StructurizrRenderingException;
import de.materna.structurizr.renderer.WorkspaceRenderer;
import de.materna.structurizr.renderer.plantuml.PlantumlLayoutEngine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Macro for Asciidoc.
 *
 * @author Stephan Pirnbaum
 */
@Slf4j
@Name("structurizrc4")
public class StructurizrMacro extends BlockMacroProcessor {

    private final WorkspaceRenderer workspaceRenderer = new WorkspaceRenderer();

    public StructurizrMacro() {
        log.debug("Constructing Structurizr Macro");
    }

    @SneakyThrows
    @Override
    public StructuralNode process(StructuralNode structuralNode, String workspacePath, Map<String, Object> attributes) {
        String viewKey = resolveViewKey(attributes);
        String title = resolveTitle(attributes, viewKey);

        Path workspaceDslPath = Path.of((String) structuralNode.getDocument().getAttribute("docdir"), workspacePath);

        Path workspaceJsonPath = resolveWorkspaceJson(structuralNode, attributes);

        Path outDir = resolveOutdir(structuralNode);

        String renderer = (String) attributes.get("renderer");
        String plantumlLayoutEngine = (String) attributes.get("plantumlLayoutEngine");

        try {
            Map<String, Path> diagrams = this.workspaceRenderer.render(
                    workspaceDslPath,
                    workspaceJsonPath,
                    outDir,
                    viewKey,
                    renderer != null ? Renderer.valueOf(renderer.toUpperCase()) : null,
                    plantumlLayoutEngine != null ? PlantumlLayoutEngine.valueOf(plantumlLayoutEngine.toUpperCase()) : null);

            List<String> lines = Arrays.asList(
                    "." + title,
                    "image::" + diagrams.get(viewKey).getFileName().toString() + "[]"
            );

            parseContent(structuralNode, lines);
            return structuralNode;
        } catch (StructurizrRenderingException e) {
            throw new StructurizrException("Failed to render view with key " + viewKey, e);
        }
    }

    private static Path resolveWorkspaceJson(StructuralNode structuralNode, Map<String, Object> attributes) {
        String workspaceJson = (String) attributes.get("workspaceJson");
        Path workspaceJsonPath = null;
        if (StringUtils.isNotBlank(workspaceJson)) {
            workspaceJsonPath = Path.of((String) structuralNode.getDocument().getAttribute("docdir"), workspaceJson);
        }
        return workspaceJsonPath;
    }

    private static String resolveViewKey(Map<String, Object> attributes) {
        String viewKey = (String) attributes.get("viewKey");
        if (viewKey == null) {
            throw new StructurizrException("No viewKey specified for Structurizr diagram.");
        }
        return viewKey;
    }

    private static String resolveTitle(Map<String, Object> attributes, String viewKey) {
        return (String) attributes.getOrDefault("title", viewKey);
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
