package ai.coworkers.serd.json.step;

import ai.coworkers.core.model.workflow.step.condition.StepConditionData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.AllOfData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.AnyOfData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.workflow.step.condition.StepConditionData.NotData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 조건 트리의 저장 형태와 JSON 문자열 사이를 변환함 */
public final class StepConditionJson {

  private static final String KIND = "kind";
  private static final String CHILDREN = "children";
  private static final String CHILD = "child";
  private static final String CODE = "code";
  private static final String CONFIG = "config";

  private final JsonMapper mapper = JsonMapper.builder().build();

  /**
   * 저장 형태를 JSON 문자열로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 문자열
   */
  public String write(StepConditionData data) {
    return mapper.writeValueAsString(toNode(data));
  }

  /**
   * JSON 문자열을 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param json JSON 문자열
   * @return 저장 형태
   */
  public StepConditionData read(String json) {
    JsonNode root;
    try {
      root = mapper.readTree(json);
    } catch (JacksonException e) {
      throw new IllegalArgumentException("invalid condition json", e);
    }
    return fromNode(root);
  }

  private ObjectNode toNode(StepConditionData data) {
    ObjectNode node = mapper.createObjectNode();
    switch (data) {
      case AllOfData allOf -> {
        node.put(KIND, Kind.ALL_OF.name());
        putChildren(node, allOf.children());
      }
      case AnyOfData anyOf -> {
        node.put(KIND, Kind.ANY_OF.name());
        putChildren(node, anyOf.children());
      }
      case NotData not -> {
        node.put(KIND, Kind.NOT.name());
        node.set(CHILD, toNode(not.child()));
      }
      case LeafData leaf -> {
        node.put(KIND, Kind.LEAF.name());
        node.put(CODE, leaf.code());
        ObjectNode config = node.putObject(CONFIG);
        leaf.config().forEach(config::put);
      }
    }
    return node;
  }

  private void putChildren(ObjectNode node, List<StepConditionData> children) {
    ArrayNode array = node.putArray(CHILDREN);
    children.forEach(child -> array.add(toNode(child)));
  }

  private StepConditionData fromNode(JsonNode node) {
    if (!node.isObject()) {
      throw invalid("node must be an object");
    }
    return switch (kindOf(node)) {
      case ALL_OF -> new AllOfData(childrenOf(node));
      case ANY_OF -> new AnyOfData(childrenOf(node));
      case NOT -> new NotData(fromNode(require(node, CHILD)));
      case LEAF -> new LeafData(stringOf(require(node, CODE), CODE), configOf(node));
    };
  }

  private Kind kindOf(JsonNode node) {
    String kind = stringOf(require(node, KIND), KIND);
    try {
      return Kind.valueOf(kind);
    } catch (IllegalArgumentException e) {
      throw invalid("unknown kind: " + kind);
    }
  }

  private List<StepConditionData> childrenOf(JsonNode node) {
    JsonNode children = require(node, CHILDREN);
    if (!children.isArray()) {
      throw invalid(CHILDREN + " must be an array");
    }
    List<StepConditionData> result = new ArrayList<>();
    for (JsonNode child : children.values()) {
      result.add(fromNode(child));
    }
    return result;
  }

  private Map<String, String> configOf(JsonNode node) {
    JsonNode config = require(node, CONFIG);
    if (!config.isObject()) {
      throw invalid(CONFIG + " must be an object");
    }
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, JsonNode> entry : config.properties()) {
      result.put(entry.getKey(), stringOf(entry.getValue(), CONFIG + "." + entry.getKey()));
    }
    return result;
  }

  private static JsonNode require(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null) {
      throw invalid("missing field: " + field);
    }
    return value;
  }

  private static String stringOf(JsonNode node, String field) {
    if (!node.isString()) {
      throw invalid(field + " must be a string");
    }
    return node.stringValue();
  }

  private static IllegalArgumentException invalid(String reason) {
    return new IllegalArgumentException("invalid condition json: " + reason);
  }

  /** JSON에 기록하는 노드 종류 */
  private enum Kind {
    ALL_OF,
    ANY_OF,
    NOT,
    LEAF
  }
}
