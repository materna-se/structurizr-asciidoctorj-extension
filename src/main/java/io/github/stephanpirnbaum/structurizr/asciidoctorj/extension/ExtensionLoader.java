package io.github.stephanpirnbaum.structurizr.asciidoctorj.extension;

import lombok.extern.slf4j.Slf4j;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

@Slf4j
public class ExtensionLoader  implements ExtensionRegistry {

    private static final StructurizrMacro macro = new StructurizrMacro();

    @Override
    public void register(Asciidoctor asciidoctor) {
        log.info("Loading Structurizr macro");
        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro(macro);
    }
}
