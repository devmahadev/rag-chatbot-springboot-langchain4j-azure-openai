module com.deepak.aiassistant {

    // LangChain4j modules
    requires langchain4j;
    requires langchain4j.core;
    requires langchain4j.pgvector;
    requires langchain4j.azure.open.ai;
    requires langchain4j.spring.boot.starter;

    // Apache PDFBox (if used)
    requires org.apache.pdfbox;

    // Apache POI for Word document handling
    requires org.apache.poi.ooxml;

    // SLF4J for logging
    requires org.slf4j;

    // Spring Framework modules
    requires spring.core;
    requires spring.web;
    requires spring.context;
    requires spring.boot;
    requires spring.beans;
    requires spring.boot.autoconfigure;

    // Java base modules
    requires java.base;
    requires java.logging;
    requires java.xml;
    requires reactor.core;
    requires jakarta.validation;
}
