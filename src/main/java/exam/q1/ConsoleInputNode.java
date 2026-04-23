package exam.q1;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q1-A] 터미널 입력 노드 (5점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나 emit.
 * 루프 없음 — 반복은 호출자(테스트 또는 asRunnable)가 담당한다.
 *
 * 동작:
 *   생성자에서 받은 inputText를 Message.of(inputText)로 감싸
 *   "out" 포트로 emit하고 반환한다.
 */
public class ConsoleInputNode extends Node {

    private final String inputText;

    public ConsoleInputNode(String id, String inputText) {
        super(id);
        this.inputText = inputText;
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: inputText를 Message로 감싸 emit
    }
}

