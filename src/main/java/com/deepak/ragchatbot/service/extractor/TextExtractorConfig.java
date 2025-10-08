package com.deepak.ragchatbot.service.extractor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextExtractorConfig {

    @Bean
    public TextExtractor pdfTextExtractor(){
        return new PdfTextExtractor();
    }

    @Bean
    public TextExtractor docxTextExtractor(){
        return new DocxTextExtractor();
    }
}
