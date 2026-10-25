package ai.coworkers.serd.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("JsonFields")
class JsonFieldsTest {

  private static final JsonFields FIELDS = new JsonFields(JsonMapper.builder().build(), "sample");
  private static final UUID ID = UUID.fromString("0190a000-0000-7000-8000-000000000001");

  private enum Color {
    RED
  }

  private static JsonNode node(String json) {
    return FIELDS.parse(json);
  }

  @Nested
  @DisplayName("parse")
  class Parse {

    @Test
    @DisplayName("JSON이 아니면 대상 이름을 붙여 거부함")
    void rejectsNonJson() {
      assertThatThrownBy(() -> FIELDS.parse("not json"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json");
    }
  }

  @Nested
  @DisplayName("object")
  class ObjectMethod {

    @Test
    @DisplayName("객체가 아니면 거부함")
    void rejectsNonObject() {
      assertThatThrownBy(() -> FIELDS.object(node("[]"), "root"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json: root must be an object");
    }
  }

  @Nested
  @DisplayName("integer")
  class Integer {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        delimiter = '|',
        value = {
          "{}|missing field: v",
          "{\"v\":\"1\"}|v must be an integer",
          "{\"v\":1.5}|v must be an integer"
        })
    @DisplayName("없거나 정수가 아니면 거부함")
    void rejectsNonInteger(String json, String reason) {
      assertThatThrownBy(() -> FIELDS.integer(node(json), "v"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json: " + reason);
    }
  }

  @Nested
  @DisplayName("nullableUuid")
  class NullableUuid {

    @Test
    @DisplayName("널이면 빈 값, UUID 문자열이면 UUID를 반환함")
    void readsNullOrUuid() {
      assertThat(FIELDS.nullableUuid(node("{\"v\":null}"), "v")).isNull();
      assertThat(FIELDS.nullableUuid(node("{\"v\":\"" + ID + "\"}"), "v")).isEqualTo(ID);
    }

    @Test
    @DisplayName("필드가 없으면 거부함")
    void rejectsMissing() {
      assertThatThrownBy(() -> FIELDS.nullableUuid(node("{}"), "v"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json: missing field: v");
    }
  }

  @Nested
  @DisplayName("uuids")
  class Uuids {

    @Test
    @DisplayName("순서대로 UUID 목록을 반환함")
    void readsInOrder() {
      assertThat(FIELDS.uuids(node("{\"v\":[\"" + ID + "\"]}"), "v")).isEqualTo(List.of(ID));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        delimiter = '|',
        value = {"{\"v\":\"x\"}|v must be an array", "{\"v\":[\"x\"]}|v must be a uuid"})
    @DisplayName("배열이 아니거나 원소가 UUID가 아니면 거부함")
    void rejectsInvalid(String json, String reason) {
      assertThatThrownBy(() -> FIELDS.uuids(node(json), "v"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json: " + reason);
    }
  }

  @Nested
  @DisplayName("constant")
  class Constant {

    @Test
    @DisplayName("이름에 맞는 열거 상수를 반환함")
    void readsConstant() {
      assertThat(FIELDS.constant(node("{\"kind\":\"RED\"}"), "kind", Color.class))
          .isSameAs(Color.RED);
    }

    @Test
    @DisplayName("모르는 이름이면 거부함")
    void rejectsUnknown() {
      assertThatThrownBy(() -> FIELDS.constant(node("{\"kind\":\"BLUE\"}"), "kind", Color.class))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid sample json: unknown kind: BLUE");
    }
  }
}
