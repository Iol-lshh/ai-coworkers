package ai.coworkers.serd.json.workflow;

import ai.coworkers.core.model.template.workflow.WorkflowTemplateData;
import ai.coworkers.serd.json.JsonFields;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 워크플로우 틀의 저장 형태와 JSON 문자열 사이를 변환함 */
public final class WorkflowTemplateJson {

  private static final String ID = "id";
  private static final String VERSION = "version";
  private static final String START = "start";
  private static final String STEPS = "steps";

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonFields fields = new JsonFields(mapper, "workflow template");

  /**
   * 저장 형태를 JSON 문자열로 바꿈.
   *
   * @param data 저장 형태
   * @return JSON 문자열
   */
  public String write(WorkflowTemplateData data) {
    ObjectNode node = mapper.createObjectNode();
    node.put(ID, data.id().toString());
    node.put(VERSION, data.version());
    node.put(START, data.start().toString());
    ArrayNode steps = node.putArray(STEPS);
    data.steps().forEach(step -> steps.add(step.toString()));
    return mapper.writeValueAsString(node);
  }

  /**
   * JSON 문자열을 저장 형태로 바꿈. 형식이 맞지 않으면 거부함.
   *
   * @param json JSON 문자열
   * @return 저장 형태
   */
  public WorkflowTemplateData read(String json) {
    JsonNode root = fields.object(fields.parse(json), "root");
    return new WorkflowTemplateData(
        fields.uuid(root, ID),
        fields.integer(root, VERSION),
        fields.uuid(root, START),
        fields.uuids(root, STEPS));
  }
}
