package exam.q1;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q1-B] 터미널 출력 노드 (5점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나 receive → 출력.
 * 루프 없음.
 *
 * 동작:
 *   inbound에서 Message를 receive()한 뒤
 *   "[OUTPUT] <payload>" 형식으로 System.out.println()한다.
 *   getLastOutput()이 해당 문자열을 반환해야 한다.
 */
public class ConsoleOutputNode extends Node {

    private String lastOutput;

    public ConsoleOutputNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive → 출력 → lastOutput 저장
    }

    /** 테스트 검증용 */
    public String getLastOutput() {
        return lastOutput;
    }
}

