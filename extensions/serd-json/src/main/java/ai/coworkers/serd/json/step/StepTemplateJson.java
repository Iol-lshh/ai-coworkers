package ai.coworkers.serd.json.step;

import ai.coworkers.core.model.workflow.step.template.StepTemplateData;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.BranchData;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.LoopData;
import ai.coworkers.core.model.workflow.step.template.StepTemplateData.ParallelData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 템플릿의 저장 형태와 JSON 문자열 사이를 변환함. 조건 트리는 {@link StepConditionJson}에 위임함. */
public final class StepTemplateJson {

  private static final String KIND = "kind";
  private static final String ID = "id";
  private static final String VERSION = "version";
  private static final String CONDITION = "condition";
  private static final String WHEN_SATISFIED = "whenSatisfied";
  private static final String OTHERWISE = "otherwise";
  private static final String BODY = "body";
  private static final String EXIT = "exit";
  private static final String BRANCHES = "branches";
  private static final String JOIN = "join";

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final StepConditionJson conditionJson = new StepConditionJson();

  /**
   * 저장 형태를 JSON 문자열로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 문자열
   */
  public String write(StepTemplateData data) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ID, data.id().toString());
    node.put(VERSION, data.version());
    switch (data) {
      case BranchData branch -> {
        node.put(KIND, Kind.BRANCH.name());
        node.set(CONDITION, conditionJson.toNode(branch.condition()));
        node.put(WHEN_SATISFIED, branch.whenSatisfied().toString());
        node.put(OTHERWISE, branch.otherwise().toString());
      }
      case LoopData loop -> {
        node.put(KIND, Kind.LOOP.name());
        node.set(CONDITION, conditionJson.toNode(loop.condition()));
        node.put(BODY, loop.body().toString());
        node.put(EXIT, loop.exit().toString());
      }
      case ParallelData parallel -> {
        node.put(KIND, Kind.PARALLEL.name());
        ArrayNode branches = node.putArray(BRANCHES);
        parallel.branches().forEach(branch -> branches.add(branch.toString()));
        node.put(JOIN, parallel.join().toString());
      }
    }
    return mapper.writeValueAsString(node);
  }

  /**
   * JSON 문자열을 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param json JSON 문자열
   * @return 저장 형태
   */
  public StepTemplateData read(String json) {
    JsonNode root;
    try {
      root = mapper.readTree(json);
    } catch (JacksonException e) {
      throw new IllegalArgumentException("invalid template json", e);
    }
    if (!root.isObject()) {
      throw invalid("root must be an object");
    }
    UUID id = uuidOf(root, ID);
    Long version = versionOf(root);
    return switch (kindOf(root)) {
      case BRANCH ->
          new BranchData(
              id,
              version,
              conditionJson.fromNode(require(root, CONDITION)),
              uuidOf(root, WHEN_SATISFIED),
              uuidOf(root, OTHERWISE));
      case LOOP ->
          new LoopData(
              id,
              version,
              conditionJson.fromNode(require(root, CONDITION)),
              uuidOf(root, BODY),
              uuidOf(root, EXIT));
      case PARALLEL -> new ParallelData(id, version, branchesOf(root), uuidOf(root, JOIN));
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

  private static Long versionOf(JsonNode node) {
    JsonNode version = require(node, VERSION);
    if (!version.isIntegralNumber() || !version.canConvertToLong()) {
      throw invalid(VERSION + " must be an integer");
    }
    return version.longValue();
  }

  private static List<UUID> branchesOf(JsonNode node) {
    JsonNode branches = require(node, BRANCHES);
    if (!branches.isArray()) {
      throw invalid(BRANCHES + " must be an array");
    }
    List<UUID> result = new ArrayList<>();
    for (JsonNode branch : branches.values()) {
      result.add(toUuid(branch, BRANCHES));
    }
    return result;
  }

  private static UUID uuidOf(JsonNode node, String field) {
    return toUuid(require(node, field), field);
  }

  private static UUID toUuid(JsonNode value, String field) {
    String text = stringOf(value, field);
    try {
      return UUID.fromString(text);
    } catch (IllegalArgumentException e) {
      throw invalid(field + " must be a uuid");
    }
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
    return new IllegalArgumentException("invalid template json: " + reason);
  }

  /** JSON에 기록하는 템플릿 종류 */
  private enum Kind {
    BRANCH,
    LOOP,
    PARALLEL
  }
}
