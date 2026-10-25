package ai.coworkers.serd.json.step;

import ai.coworkers.core.model.template.step.condition.StepConditionData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.AllOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.AnyOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.NotData;
import ai.coworkers.serd.json.JsonFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final JsonFields fields = new JsonFields(mapper, "condition");

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
    return fromNode(fields.parse(json));
  }

  /**
   * 저장 형태를 JSON 노드로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 노드
   */
  public ObjectNode toNode(StepConditionData data) {
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

  /**
   * JSON 노드를 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param node JSON 노드
   * @return 저장 형태
   */
  public StepConditionData fromNode(JsonNode node) {
    fields.object(node, "node");
    return switch (fields.constant(node, KIND, Kind.class)) {
      case ALL_OF -> new AllOfData(childrenOf(node));
      case ANY_OF -> new AnyOfData(childrenOf(node));
      case NOT -> new NotData(fromNode(fields.require(node, CHILD)));
      case LEAF -> new LeafData(fields.string(node, CODE), configOf(node));
    };
  }

  private List<StepConditionData> childrenOf(JsonNode node) {
    List<StepConditionData> result = new ArrayList<>();
    for (JsonNode child : fields.array(node, CHILDREN)) {
      result.add(fromNode(child));
    }
    return result;
  }

  private Map<String, String> configOf(JsonNode node) {
    JsonNode config = fields.object(fields.require(node, CONFIG), CONFIG);
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, JsonNode> entry : config.properties()) {
      result.put(entry.getKey(), fields.stringOf(entry.getValue(), CONFIG + "." + entry.getKey()));
    }
    return result;
  }

  /** JSON에 기록하는 노드 종류 */
  private enum Kind {
    ALL_OF,
    ANY_OF,
    NOT,
    LEAF
  }
}
