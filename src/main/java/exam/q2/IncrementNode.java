package exam.q2;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q2-B] 숫자 +1 변환 노드 (10점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나를 receive → +1 후 emit.
 * 루프 없음.
 *
 * 동작:
 *   payload를 정수로 파싱하여 +1한 결과(Integer)를 emit한다.
 */
public class IncrementNode extends Node {

    public IncrementNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive → parseInt → +1 → emit
    }
}

