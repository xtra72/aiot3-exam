package exam.q2;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q2-A] 타입 분기 노드 (15점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나를 receive → 분기 emit.
 * 루프 없음.
 *
 * 플로우 구조:
 *
 *              ┌─ "number" ──▶ IncrementNode ──▶ Collector(숫자)
 *   srcConn ──▶ TypeRouterNode
 *              └─ "string"  ──▶ UpperCaseNode ──▶ Collector(문자)
 *
 * 숫자 판단 기준:
 *   - payload가 Number 타입이면 숫자
 *   - payload가 String이고 Integer.parseInt()가 성공하면 숫자
 *   - 그 외는 문자열
 *
 * 단일 스레드 테스트 예:
 *   for (String s : inputs) srcConn.send(Message.of(s));
 *   for (int i = 0; i < inputs.size(); i++) router.execute();
 */
public class TypeRouterNode extends Node {

    public TypeRouterNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive 하나 → isNumber 판단 → "number" 또는 "string" 포트로 emit
    }

    private boolean isNumber(Object payload) {
        // TODO: Number 타입 또는 parseInt 성공 여부 반환
        return false;
    }
}

