package fr.gouv.vitam.metadata.core.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.SedaConstants;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileStatus;
import org.junit.Test;

import java.util.Optional;

import static fr.gouv.vitam.common.SedaConstants.TAG_ARCHIVE_UNIT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class UnitValidatorTest {

    private static final String AU_JSON_FILE = "archive-unit_OK.json";
    private static final String AU_JSON_MAIL_FILE = "au_mail.json";
    private static final String SCHEMA_JSON_MAIL_FILE = "schema_mail.json";
    private static final String OBJECT_BIRTH_PLACE_JSON_FILE = "object_birth_place_archive_unit.json";
    private static final String STRING_BIRTH_PLACE_JSON_FILE = "string_birth_place_archive_unit.json";
    private static final String COMPLEX_JSON_FILE = "complex_archive_unit.json";
    private static final String AU_SAME_DATES_JSON_FILE = "archive_unit_same_dates.json";
    private static final String AU_INVALID_JSON_FILE = "archive-unit_Invalid.json";
    private static final String SIMPLE_UNIT_JSON_FILE = "simple_unit.json";
    private static final String AU_INVALID_DATE_JSON_FILE = "archive-unit_date_Invalid.json";

    @Test
    public void givenComplexArchiveUnitJsonThenValidateJsonObjectBirthPlaceOK() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(OBJECT_BIRTH_PLACE_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        unitValidator.validateUnit(unitJson);
    }

    @Test
    public void givenComplexArchiveUnitJsonThenValidateJsonObjectBirthPlaceKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(STRING_BIRTH_PLACE_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.SCHEMA_VALIDATION_FAILURE);
    }

    @Test
    public void givenConstructorWithCorrectSchemaThenValidateJsonOK() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(AU_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        unitValidator.validateUnit(unitJson);
    }

    @Test
    public void givenComplexArchiveUnitJsonThenValidateJsonOK() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(COMPLEX_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        unitValidator.validateUnit(unitJson);
    }

    @Test
    public void givenInvalidJsonFileThenValidateKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(AU_INVALID_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .hasMessageContaining("Title_")
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.SCHEMA_VALIDATION_FAILURE);
    }

    @Test
    public void givenInvalidDateKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(AU_INVALID_DATE_JSON_FILE));

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .hasMessageContaining("EndDate is before StartDate")
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.INVALID_START_END_DATE);
    }

    @Test
    public void givenMissingTitleThenValidationKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(SIMPLE_UNIT_JSON_FILE));

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .hasMessageContaining("Title_")
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.SCHEMA_VALIDATION_FAILURE);
    }

    @Test
    public void givenSameDatesOK() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        CachedSchemaValidatorLoader schemaValidatorLoader = mock(CachedSchemaValidatorLoader.class);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(AU_SAME_DATES_JSON_FILE)).get(TAG_ARCHIVE_UNIT);

        // When / Then
        unitValidator.validateUnit(unitJson);
    }

    @Test
    public void givenEmptyArchiveUnitProfileThenValidationKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema("{}")
                .setStatus(ArchiveUnitProfileStatus.ACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("MyArchiveUnitProfile");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(COMPLEX_JSON_FILE)).get(TAG_ARCHIVE_UNIT);
        unitJson.put(SedaConstants.TAG_ARCHIVE_UNIT_PROFILE, "MyArchiveUnitProfile");

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.EMPTY_ARCHIVE_UNIT_PROFILE_SCHEMA);
    }

    @Test
    public void givenInactiveArchiveUnitProfileThenValidationKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema("{\n" +
                    "  \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\",\n" +
                    "  \"id\": \"http://example.com/root.json\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"properties\": {\n" +
                    "    \"_id\": {\n" +
                    "      \"type\": \"string\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n")
                .setStatus(ArchiveUnitProfileStatus.INACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("MyArchiveUnitProfile");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(COMPLEX_JSON_FILE)).get(TAG_ARCHIVE_UNIT);
        unitJson.put(SedaConstants.TAG_ARCHIVE_UNIT_PROFILE, "MyArchiveUnitProfile");

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.ARCHIVE_UNIT_PROFILE_SCHEMA_INACTIVE);
    }

    @Test
    public void givenArchiveUnitProfileAndValidUnitThenValidationOK() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema("{\n" +
                    "  \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\",\n" +
                    "  \"id\": \"http://example.com/root.json\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"properties\": {\n" +
                    "    \"_id\": {\n" +
                    "      \"type\": \"string\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n")
                .setStatus(ArchiveUnitProfileStatus.ACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("MyArchiveUnitProfile");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(COMPLEX_JSON_FILE)).get(TAG_ARCHIVE_UNIT);
        unitJson.put(SedaConstants.TAG_ARCHIVE_UNIT_PROFILE, "MyArchiveUnitProfile");

        // When / Then
        unitValidator.validateUnit(unitJson);
    }

    @Test
    public void givenArchiveUnitProfileAndInvalidUnitThenValidationKO() throws Exception {

        // Given
        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema("{\n" +
                    "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                    "  \"id\": \"http://example.com/root.json\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"additionalProperties\": false,\n" +
                    "  \"anyOf\": [\n" +
                    "    {\n" +
                    "      \"required\": [\n" +
                    "        \"_id\",\n" +
                    "        \"Title\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"properties\": {\n" +
                    "    \"_id\": {\n" +
                    "      \"type\": \"string\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}")
                .setStatus(ArchiveUnitProfileStatus.ACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("MyArchiveUnitProfile");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        ObjectNode unitJson = (ObjectNode) JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream(COMPLEX_JSON_FILE)).get(TAG_ARCHIVE_UNIT);
        unitJson.put(SedaConstants.TAG_ARCHIVE_UNIT_PROFILE, "MyArchiveUnitProfile");

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit(unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.ARCHIVE_UNIT_PROFILE_SCHEMA_VALIDATION_FAILURE);
    }

    @Test
    public void givenComplexArchiveUnitProfileAndValidUnitThenValidationOK() throws Exception {

        // Given
        JsonNode schemaMail =
            JsonHandler.getFromInputStream(PropertiesUtils.getResourceAsStream(SCHEMA_JSON_MAIL_FILE));

        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema(JsonHandler.unprettyPrint(schemaMail))
                .setStatus(ArchiveUnitProfileStatus.ACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("AUP-000001");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        JsonNode unitJson = JsonHandler.getFromInputStream(PropertiesUtils.getResourceAsStream(AU_JSON_MAIL_FILE));

        // When / Then
        unitValidator.validateUnit((ObjectNode)unitJson);
    }

    @Test
    public void givenComplexArchiveUnitProfileAndInvalidUnitThenValidationKO() throws Exception {

        // Given
        JsonNode schemaMail =
            JsonHandler.getFromInputStream(PropertiesUtils.getResourceAsStream(SCHEMA_JSON_MAIL_FILE));

        CachedArchiveUnitProfileLoader archiveUnitProfileLoader = mock(CachedArchiveUnitProfileLoader.class);
        doReturn(Optional.of(
            new ArchiveUnitProfileModel()
                .setControlSchema(JsonHandler.unprettyPrint(schemaMail))
                .setStatus(ArchiveUnitProfileStatus.ACTIVE)
        )).when(archiveUnitProfileLoader).loadArchiveUnitProfile("AUP-000001");

        CachedSchemaValidatorLoader schemaValidatorLoader = new CachedSchemaValidatorLoader(10, 6);
        UnitValidator unitValidator = new UnitValidator(archiveUnitProfileLoader, schemaValidatorLoader);

        JsonNode unitJson = JsonHandler.getFromInputStream(PropertiesUtils.getResourceAsStream(AU_JSON_MAIL_FILE));
        ((ObjectNode) unitJson).put("randomAddedField", "This field will be rejected");

        // When / Then
        assertThatThrownBy(() -> unitValidator.validateUnit((ObjectNode) unitJson))
            .isInstanceOf(MetadataValidationException.class)
            .extracting(e -> ((MetadataValidationException) e).getErrorCode())
            .isEqualTo(MetadataValidationErrorCode.ARCHIVE_UNIT_PROFILE_SCHEMA_VALIDATION_FAILURE);
    }
}
