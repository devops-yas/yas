package com.yas.recommendation.vector.common.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class DefaultDocumentFormatterTest {

    @Test
    void shouldReplaceTemplateVariablesAndStripHtml() {
        DefaultDocumentFormatter formatter = new DefaultDocumentFormatter();
        Map<String, Object> values = new HashMap<>();
        values.put("name", "<b>Book</b>");
        String template = "Hello {name}";

        String result = formatter.format(values, template, new ObjectMapper());

        assertEquals("Hello Book", result);
    }
}
