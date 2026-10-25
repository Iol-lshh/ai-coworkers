package ai.coworkers.serd.json.saga;

import ai.coworkers.core.model.runtime.saga.SagaData;
import ai.coworkers.core.model.runtime.saga.SagaData.NodeData;
import ai.coworkers.serd.json.JsonFields;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** 사가의 저장 형태와 JSON 문자열 사이를 변환함 */
public final class SagaJson {

  private static final String ID = "id";
  private static final String STATE = "state";
  private static final String NODE = "node";
  private static final String SLOT_ID = "slotId";

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonFields fields = new JsonFields(mapper, "saga");

  /**
   * 저장 형태를 JSON 문자열로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 문자열
   */
  public String write(SagaData data) {
    return mapper.writeValueAsString(toNode(data));
  }

  /**
   * JSON 문자열을 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param json JSON 문자열
   * @return 저장 형태
   */
  public SagaData read(String json) {
    return fromNode(fields.parse(json));
  }

  /**
   * 저장 형태를 JSON 노드로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 노드
   */
  public ObjectNode toNode(SagaData data) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ID, data.id().toString());
    node.put(STATE, data.state());
    NodeData active = data.node();
    if (active == null) {
      node.putNull(NODE);
    } else {
      ObjectNode activeNode = node.putObject(NODE);
      activeNode.put(ID, active.id().toString());
      activeNode.put(STATE, active.state());
      activeNode.put(SLOT_ID, active.slotId().toString());
    }
    return node;
  }

  /**
   * JSON 노드를 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param node JSON 노드
   * @return 저장 형태
   */
  public SagaData fromNode(JsonNode node) {
    fields.object(node, "node");
    JsonNode active = fields.require(node, NODE);
    return new SagaData(
        fields.uuid(node, ID),
        fields.string(node, STATE),
        active.isNull() ? null : nodeDataOf(fields.object(active, NODE)));
  }

  private NodeData nodeDataOf(JsonNode node) {
    return new NodeData(
        fields.uuid(node, ID), fields.string(node, STATE), fields.uuid(node, SLOT_ID));
  }
}
