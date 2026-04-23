package exam.q2;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q2-C] 대문자 변환 노드 (10점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나를 receive → 대문자 변환 후 emit.
 * 루프 없음.
 */
public class UpperCaseNode extends Node {

    public UpperCaseNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive → toUpperCase → emit
    }
}

