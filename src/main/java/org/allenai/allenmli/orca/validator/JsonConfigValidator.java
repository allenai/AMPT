/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.allenai.allenmli.orca.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.allenai.allenmli.orca.helpers.ConfigurationLoader;
import org.allenai.allenmli.orca.helpers.JsonConfigurationFile;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** A validator for JSON configuration files. */
public class JsonConfigValidator {
  private final JsonSchemaFactory jsonSchemaFactory;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Logger logger = new StderrLogService();

  public JsonConfigValidator() throws JsonValidationException {
    final JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
    final JsonSchemaFactory.Builder builder =
        JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909))
            .defaultMetaSchemaURI(metaSchema.getUri())
            .addMetaSchema(metaSchema)
            .addUriMappings(getUriMappings());
    jsonSchemaFactory = builder.build();
  }

  /**
   * Validates all configuration files in the user's preference directory.
   *
   * @throws JsonValidationException If any validation errors occur.
   */
  public void validateAllConfigs() throws JsonValidationException {
    for (final JsonConfigurationFile jsonConfigFile : JsonConfigurationFile.values()) {
      final Path configPath =
          ConfigurationLoader.getAbsoluteConfigurationPath(jsonConfigFile.getFilename());
      validateConfig(configPath, jsonConfigFile.getSchemaFilename());
    }
  }

  /**
   * Validates a given configuration file against a given schema.
   *
   * @param configPath The configuration file to validate.
   * @param schemaFileName The schema file name (last part of the path) to use.
   * @throws JsonValidationException If any validation errors occur.
   */
  public void validateConfig(Path configPath, String schemaFileName)
      throws JsonValidationException {
    logger.info("Validating config: " + configPath);

    final JsonSchema schema = createSchema(schemaFileName);
    final JsonNode node;
    try {
      node = mapper.readTree(configPath.toFile());
    } catch (IOException e) {
      throw new JsonValidationException("Unable to read config file: " + configPath, e);
    }

    validateConfig(node, schema);
  }

  /**
   * Validates a given configuration input stream against a given schema.
   *
   * @param configInputStream The configuration input stream to validate.
   * @param schemaFileName The schema file name (last part of the path) to use.
   * @throws JsonValidationException If any validation errors occur.
   */
  public void validateConfig(InputStream configInputStream, String schemaFileName)
      throws JsonValidationException {
    final JsonSchema schema = createSchema(schemaFileName);
    final JsonNode node;
    try {
      node = mapper.readTree(configInputStream);
    } catch (IOException e) {
      throw new JsonValidationException("Unable to read config from input stream", e);
    }

    validateConfig(node, schema);
  }

  /**
   * Creates a {@link JsonSchema} object from the given schema file name.
   *
   * @param schemaFileName The schema file name (last par5t of the path) to use.
   * @return A schema object for the given schema file name.
   * @throws JsonValidationException If the schema file cannot be opened properly.
   */
  private JsonSchema createSchema(String schemaFileName) throws JsonValidationException {
    try (InputStream schemaInputStream =
        JsonConfigValidator.class.getResourceAsStream(
            String.format("/schema/%s", schemaFileName))) {
      return jsonSchemaFactory.getSchema(schemaInputStream);
    } catch (IOException e) {
      throw new JsonValidationException("Failed to open schema file: " + schemaFileName, e);
    }
  }

  /**
   * Validates the given {@link JsonNode} against the the given {@link JsonSchema}.
   *
   * @param node The node to validate.
   * @param schema The schema to use for validation.
   * @throws JsonValidationException If any validation errors occur.
   */
  private void validateConfig(JsonNode node, JsonSchema schema) throws JsonValidationException {
    final Set<ValidationMessage> errors = schema.validate(node);
    if (!errors.isEmpty()) {
      final String errorMessage =
          errors.stream()
              .map(ValidationMessage::getMessage)
              .collect(Collectors.joining(System.lineSeparator()));
      throw new JsonValidationException(errorMessage);
    }
  }

  /**
   * Returns a mapping from internet-accessible schema URLs to alternate local locations.
   *
   * <p>This allows for offline validation of schemas that refer to public URLs. The mapping is
   * sourced from the schema/schema-map.json resource.
   *
   * @return A mapping from public URLs to local URLs.
   * @throws JsonValidationException If any errors occur creating the mapping.
   */
  private Map<String, String> getUriMappings() throws JsonValidationException {
    final HashMap<String, String> map = new HashMap<>();
    try (InputStream inputStream =
        JsonConfigValidator.class.getResourceAsStream("/schema/schema-map.json")) {
      for (JsonNode mapping : mapper.readTree(inputStream)) {
        map.put(mapping.get("publicURL").asText(), mapping.get("localURL").asText());
      }
    } catch (IOException e) {
      throw new JsonValidationException("Failed to read schema mappings", e);
    }
    return map;
  }
}
