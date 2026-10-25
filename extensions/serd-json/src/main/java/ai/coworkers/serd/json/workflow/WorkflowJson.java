package ai.coworkers.serd.json.workflow;

import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.step.slot.StepSlotData;
import ai.coworkers.core.model.runtime.workflow.WorkflowData;
import ai.coworkers.core.model.runtime.workflow.WorkflowData.SagaJoinData;
import ai.coworkers.serd.json.JsonFields;
import ai.coworkers.serd.json.saga.SagaJson;
import ai.coworkers.serd.json.step.StepSlotJson;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 워크플로우 실행의 저장 형태와 JSON 문자열 사이를 변환함. 슬롯과 사가는 {@link StepSlotJson}, {@link SagaJson}에 위임함. */
public final class WorkflowJson {

  private static final String ID = "id";
  private static final String TEMPLATE_ID = "templateId";
  private static final String TEMPLATE_VERSION = "templateVersion";
  private static final String SLOTS = "slots";
  private static final String SAGAS = "sagas";
  private static final String JOINS = "joins";
  private static final String ORIGIN = "origin";
  private static final String BRANCHES = "branches";
  private static final String JOIN = "join";
  private static final String JOINED = "joined";

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonFields fields = new JsonFields(mapper, "workflow");
  private final StepSlotJson slotJson = new StepSlotJson();
  private final SagaJson sagaJson = new SagaJson();

  /**
   * 저장 형태를 JSON 문자열로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 문자열
   */
  public String write(WorkflowData data) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ID, data.id().toString());
    node.put(TEMPLATE_ID, data.templateId().toString());
    node.put(TEMPLATE_VERSION, data.templateVersion());
    ArrayNode slots = node.putArray(SLOTS);
    data.slots().forEach(slot -> slots.add(slotJson.toNode(slot)));
    ArrayNode sagas = node.putArray(SAGAS);
    data.sagas().forEach(saga -> sagas.add(sagaJson.toNode(saga)));
    ArrayNode joins = node.putArray(JOINS);
    data.joins().forEach(join -> joins.add(joinNode(join)));
    return mapper.writeValueAsString(node);
  }

  /**
   * JSON 문자열을 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param json JSON 문자열
   * @return 저장 형태
   */
  public WorkflowData read(String json) {
    JsonNode root = fields.object(fields.parse(json), "root");
    List<StepSlotData> slots = new ArrayList<>();
    for (JsonNode slot : fields.array(root, SLOTS)) {
      slots.add(slotJson.fromNode(slot));
    }
    List<SagaData> sagas = new ArrayList<>();
    for (JsonNode saga : fields.array(root, SAGAS)) {
      sagas.add(sagaJson.fromNode(saga));
    }
    List<SagaJoinData> joins = new ArrayList<>();
    for (JsonNode join : fields.array(root, JOINS)) {
      joins.add(joinOf(fields.object(join, JOINS)));
    }
    return new WorkflowData(
        fields.uuid(root, ID),
        fields.uuid(root, TEMPLATE_ID),
        fields.integer(root, TEMPLATE_VERSION),
        slots,
        sagas,
        joins);
  }

  private ObjectNode joinNode(SagaJoinData join) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ORIGIN, join.origin().toString());
    putUuids(node, BRANCHES, join.branches());
    node.put(JOIN, join.join().toString());
    putUuids(node, JOINED, join.joined());
    return node;
  }

  private SagaJoinData joinOf(JsonNode node) {
    return new SagaJoinData(
        fields.uuid(node, ORIGIN),
        fields.uuids(node, BRANCHES),
        fields.uuid(node, JOIN),
        fields.uuids(node, JOINED));
  }

  private static void putUuids(ObjectNode node, String field, List<UUID> values) {
    ArrayNode array = node.putArray(field);
    values.forEach(value -> array.add(value.toString()));
  }
}
