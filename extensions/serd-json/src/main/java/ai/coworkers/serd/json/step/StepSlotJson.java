package ai.coworkers.serd.json.step;

import ai.coworkers.core.model.runtime.step.slot.StepSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.BranchSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.LoopSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.ParallelSlotData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData.TaskSlotData;
import ai.coworkers.serd.json.JsonFields;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 슬롯의 저장 형태와 JSON 노드 사이를 변환함 */
public final class StepSlotJson {

  private static final String KIND = "kind";
  private static final String ID = "id";
  private static final String TEMPLATE_ID = "templateId";
  private static final String TEMPLATE_VERSION = "templateVersion";
  private static final String NEXT = "next";
  private static final String WHEN_SATISFIED = "whenSatisfied";
  private static final String OTHERWISE = "otherwise";
  private static final String BODY = "body";
  private static final String EXIT = "exit";
  private static final String BRANCHES = "branches";
  private static final String JOIN = "join";

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonFields fields = new JsonFields(mapper, "slot");

  /**
   * 저장 형태를 JSON 노드로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 노드
   */
  public ObjectNode toNode(StepSlotData data) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ID, data.id().toString());
    node.put(TEMPLATE_ID, data.templateId().toString());
    node.put(TEMPLATE_VERSION, data.templateVersion());
    switch (data) {
      case TaskSlotData task -> {
        node.put(KIND, Kind.TASK.name());
        node.put(NEXT, textOf(task.next()));
      }
      case BranchSlotData branch -> {
        node.put(KIND, Kind.BRANCH.name());
        node.put(WHEN_SATISFIED, branch.whenSatisfied().toString());
        node.put(OTHERWISE, branch.otherwise().toString());
      }
      case LoopSlotData loop -> {
        node.put(KIND, Kind.LOOP.name());
        node.put(BODY, loop.body().toString());
        node.put(EXIT, loop.exit().toString());
      }
      case ParallelSlotData parallel -> {
        node.put(KIND, Kind.PARALLEL.name());
        ArrayNode branches = node.putArray(BRANCHES);
        parallel.branches().forEach(branch -> branches.add(branch.toString()));
        node.put(JOIN, parallel.join().toString());
      }
    }
    return node;
  }

  /**
   * JSON 노드를 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param node JSON 노드
   * @return 저장 형태
   */
  public StepSlotData fromNode(JsonNode node) {
    fields.object(node, "node");
    UUID id = fields.uuid(node, ID);
    UUID templateId = fields.uuid(node, TEMPLATE_ID);
    Long templateVersion = fields.integer(node, TEMPLATE_VERSION);
    return switch (fields.constant(node, KIND, Kind.class)) {
      case TASK ->
          new TaskSlotData(id, templateId, templateVersion, fields.nullableUuid(node, NEXT));
      case BRANCH ->
          new BranchSlotData(
              id,
              templateId,
              templateVersion,
              fields.uuid(node, WHEN_SATISFIED),
              fields.uuid(node, OTHERWISE));
      case LOOP ->
          new LoopSlotData(
              id, templateId, templateVersion, fields.uuid(node, BODY), fields.uuid(node, EXIT));
      case PARALLEL ->
          new ParallelSlotData(
              id,
              templateId,
              templateVersion,
              fields.uuids(node, BRANCHES),
              fields.uuid(node, JOIN));
    };
  }

  private static @Nullable String textOf(@Nullable UUID value) {
    return value == null ? null : value.toString();
  }

  /** JSON에 기록하는 슬롯 종류 */
  private enum Kind {
    TASK,
    BRANCH,
    LOOP,
    PARALLEL
  }
}
