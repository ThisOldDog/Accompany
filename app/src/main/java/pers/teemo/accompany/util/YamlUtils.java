package pers.teemo.accompany.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class YamlUtils {
    private static final Logger logger = LoggerFactory.getLogger(YAMLMapper.class);
    private static final YAMLMapper YAML_MAPPER;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        YAML_MAPPER = new YAMLMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
                String dateString = parser.getValueAsString();
                if (StringUtils.isEmpty(dateString)) {
                    return null;
                }
                try {
                    return LocalDate.parse(dateString, DATE_FORMATTER);
                } catch (Exception e) {
                    logger.error("Parsing time format error.", e);
                    return null;
                }
            }
        });
        YAML_MAPPER.registerModule(javaTimeModule);
    }

    public static <T> T parser(InputStream inputStream, Class<T> entityClass) throws IOException {
        return YAML_MAPPER.createParser(inputStream).readValueAs(entityClass);
    }
}
