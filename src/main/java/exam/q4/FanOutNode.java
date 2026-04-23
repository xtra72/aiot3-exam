package exam.q4;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q4-B] 팬아웃 노드 (10점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 메시지 하나를 receive → out1, out2 모두 emit.
 * 루프 없음.
 */
public class FanOutNode extends Node {

    public FanOutNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive → emit("out1", msg) + emit("out2", msg)
    }
}
