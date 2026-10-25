package ai.coworkers.serd.json;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** JSON 노드에서 필드를 꺼내는 도구. 형식이 맞지 않으면 대상 이름을 붙여 거부함. */
public final class JsonFields {

  private final JsonMapper mapper;
  private final String subject;

  /**
   * 파싱에 쓸 매퍼와 거부 메시지에 붙일 대상 이름을 받음.
   *
   * @param mapper JSON 매퍼
   * @param subject 대상 이름
   */
  public JsonFields(JsonMapper mapper, String subject) {
    this.mapper = mapper;
    this.subject = subject;
  }

  /**
   * JSON 문자열을 노드로 읽음. JSON이 아니면 거부함.
   *
   * @param json JSON 문자열
   * @return JSON 노드
   */
  public JsonNode parse(String json) {
    try {
      return mapper.readTree(json);
    } catch (JacksonException e) {
      throw new IllegalArgumentException("invalid " + subject + " json", e);
    }
  }

  /**
   * 노드가 객체인지 확인함. 객체가 아니면 거부함.
   *
   * @param node JSON 노드
   * @param name 거부 메시지에 쓸 노드 이름
   * @return 같은 노드
   */
  public JsonNode object(JsonNode node, String name) {
    if (!node.isObject()) {
      throw invalid(name + " must be an object");
    }
    return node;
  }

  /**
   * 필드 값을 꺼냄. 필드가 없으면 거부함.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return 필드 값
   */
  public JsonNode require(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null) {
      throw invalid("missing field: " + field);
    }
    return value;
  }

  /**
   * 문자열 필드 값을 꺼냄.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return 문자열
   */
  public String string(JsonNode node, String field) {
    return stringOf(require(node, field), field);
  }

  /**
   * 값이 문자열인지 확인하고 꺼냄. 문자열이 아니면 거부함.
   *
   * @param value JSON 값
   * @param field 거부 메시지에 쓸 필드 이름
   * @return 문자열
   */
  public String stringOf(JsonNode value, String field) {
    if (!value.isString()) {
      throw invalid(field + " must be a string");
    }
    return value.stringValue();
  }

  /**
   * 정수 필드 값을 꺼냄. 정수가 아니면 거부함.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return 정수
   */
  public Long integer(JsonNode node, String field) {
    JsonNode value = require(node, field);
    if (!value.isIntegralNumber() || !value.canConvertToLong()) {
      throw invalid(field + " must be an integer");
    }
    return value.longValue();
  }

  /**
   * UUID 필드 값을 꺼냄.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return UUID
   */
  public UUID uuid(JsonNode node, String field) {
    return uuidOf(require(node, field), field);
  }

  /**
   * 널을 허용하는 UUID 필드 값을 꺼냄. 필드는 있어야 함.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return UUID. 값이 널이면 없음
   */
  public @Nullable UUID nullableUuid(JsonNode node, String field) {
    JsonNode value = require(node, field);
    return value.isNull() ? null : uuidOf(value, field);
  }

  /**
   * 값이 UUID 문자열인지 확인하고 꺼냄. 아니면 거부함.
   *
   * @param value JSON 값
   * @param field 거부 메시지에 쓸 필드 이름
   * @return UUID
   */
  public UUID uuidOf(JsonNode value, String field) {
    String text = stringOf(value, field);
    try {
      return UUID.fromString(text);
    } catch (IllegalArgumentException e) {
      throw invalid(field + " must be a uuid");
    }
  }

  /**
   * UUID 배열 필드 값을 꺼냄.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return UUID 목록
   */
  public List<UUID> uuids(JsonNode node, String field) {
    List<UUID> result = new ArrayList<>();
    for (JsonNode value : array(node, field)) {
      result.add(uuidOf(value, field));
    }
    return result;
  }

  /**
   * 배열 필드의 원소를 꺼냄. 배열이 아니면 거부함.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @return 원소 목록
   */
  public List<JsonNode> array(JsonNode node, String field) {
    JsonNode value = require(node, field);
    if (!value.isArray()) {
      throw invalid(field + " must be an array");
    }
    return List.copyOf(value.values());
  }

  /**
   * 문자열 필드 값을 열거 상수로 꺼냄. 모르는 이름이면 거부함.
   *
   * @param node JSON 노드
   * @param field 필드 이름
   * @param type 열거 타입
   * @param <E> 열거 타입
   * @return 열거 상수
   */
  public <E extends Enum<E>> E constant(JsonNode node, String field, Class<E> type) {
    String name = string(node, field);
    try {
      return Enum.valueOf(type, name);
    } catch (IllegalArgumentException e) {
      throw invalid("unknown " + field + ": " + name);
    }
  }

  /**
   * 대상 이름을 붙인 거부 예외를 만듦.
   *
   * @param reason 거부 사유
   * @return 거부 예외
   */
  public IllegalArgumentException invalid(String reason) {
    return new IllegalArgumentException("invalid " + subject + " json: " + reason);
  }
}
