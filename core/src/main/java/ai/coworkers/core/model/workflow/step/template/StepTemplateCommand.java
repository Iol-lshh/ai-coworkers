package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.common.PageCommand;
import ai.coworkers.core.model.workflow.step.condition.StepCondition;
import java.util.List;

/** 템플릿 요청 */
public sealed interface StepTemplateCommand {

  /** 템플릿 등록 요청. 식별자와 버전이 정해지면 템플릿을 만듦. */
  non-sealed interface Create extends StepTemplateCommand {

    /**
     * 식별자와 버전을 붙여 템플릿을 만듦.
     *
     * @param id 템플릿 식별자
     * @param version 템플릿 버전
     * @return 만든 템플릿
     */
    StepTemplate toTemplate(StepTemplateId id, Long version);
  }

  /**
   * 분기 템플릿 등록 요청
   *
   * @param condition 판정 조건
   * @param whenSatisfied 조건을 만족할 때 이어지는 템플릿
   * @param otherwise 조건을 만족하지 않을 때 이어지는 템플릿
   */
  record CreateBranch(
      StepCondition condition, StepTemplateId whenSatisfied, StepTemplateId otherwise)
      implements Create {
    @Override
    public BranchStepTemplate toTemplate(StepTemplateId id, Long version) {
      return new BranchStepTemplate(id, version, condition, whenSatisfied, otherwise);
    }
  }

  /**
   * 반복 템플릿 등록 요청
   *
   * @param condition 반복 조건
   * @param body 반복할 때 이어지는 템플릿
   * @param exit 반복을 끝낼 때 이어지는 템플릿
   */
  record CreateLoop(StepCondition condition, StepTemplateId body, StepTemplateId exit)
      implements Create {
    @Override
    public LoopStepTemplate toTemplate(StepTemplateId id, Long version) {
      return new LoopStepTemplate(id, version, condition, body, exit);
    }
  }

  /**
   * 병렬 템플릿 등록 요청
   *
   * @param branches 동시에 시작하는 템플릿
   * @param join 모든 갈래가 끝난 뒤 이어지는 템플릿
   */
  record CreateParallel(List<StepTemplateId> branches, StepTemplateId join) implements Create {

    public CreateParallel {
      branches = List.copyOf(branches);
    }

    @Override
    public ParallelStepTemplate toTemplate(StepTemplateId id, Long version) {
      return new ParallelStepTemplate(id, version, branches, join);
    }
  }

  /**
   * 템플릿 수정 요청
   *
   * @param id 수정할 템플릿 식별자
   * @param expectedVersion 수정 기준으로 삼은 현재본 버전
   * @param definition 수정한 정의
   */
  record Update(StepTemplateId id, Long expectedVersion, Create definition)
      implements StepTemplateCommand {}

  /**
   * 템플릿 페이지 조회 요청
   *
   * @param page 0부터 시작하는 페이지 번호
   * @param size 페이지 크기
   */
  record FindPage(long page, long size) implements StepTemplateCommand, PageCommand {}
}
