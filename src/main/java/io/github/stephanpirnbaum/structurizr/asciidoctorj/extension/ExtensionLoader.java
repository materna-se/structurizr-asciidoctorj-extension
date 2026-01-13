package io.github.stephanpirnbaum.structurizr.asciidoctorj.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class ExtensionLoader  implements ExtensionRegistry {
    @Override
    public void register(Asciidoctor asciidoctor) {
        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro(StructurizrMacro.class);
    }
}
