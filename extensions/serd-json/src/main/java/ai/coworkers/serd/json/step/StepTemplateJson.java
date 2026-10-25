package ai.coworkers.serd.json.step;

import ai.coworkers.core.model.template.step.StepTemplateData;
import ai.coworkers.core.model.template.step.StepTemplateData.BranchData;
import ai.coworkers.core.model.template.step.StepTemplateData.LoopData;
import ai.coworkers.core.model.template.step.StepTemplateData.ParallelData;
import ai.coworkers.serd.json.JsonFields;
import java.util.UUID;
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
  private final JsonFields fields = new JsonFields(mapper, "template");

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
    JsonNode root = fields.object(fields.parse(json), "root");
    UUID id = fields.uuid(root, ID);
    Long version = fields.integer(root, VERSION);
    return switch (fields.constant(root, KIND, Kind.class)) {
      case BRANCH ->
          new BranchData(
              id,
              version,
              conditionJson.fromNode(fields.require(root, CONDITION)),
              fields.uuid(root, WHEN_SATISFIED),
              fields.uuid(root, OTHERWISE));
      case LOOP ->
          new LoopData(
              id,
              version,
              conditionJson.fromNode(fields.require(root, CONDITION)),
              fields.uuid(root, BODY),
              fields.uuid(root, EXIT));
      case PARALLEL ->
          new ParallelData(id, version, fields.uuids(root, BRANCHES), fields.uuid(root, JOIN));
    };
  }

  /** JSON에 기록하는 템플릿 종류 */
  private enum Kind {
    BRANCH,
    LOOP,
    PARALLEL
  }
}
