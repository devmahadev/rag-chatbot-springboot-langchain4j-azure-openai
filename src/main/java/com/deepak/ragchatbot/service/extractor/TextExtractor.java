package com.deepak.ragchatbot.service.extractor;

import java.io.InputStream;
import java.util.Optional;

public interface TextExtractor {
    boolean supports(String filename);
    Optional<String> extract(InputStream inputStream);
}
