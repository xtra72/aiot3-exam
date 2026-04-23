package exam.q4;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q4-C] 딜레이 노드 — 멀티스레드용 (10점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = receive → sleep(delayMs) → emit.
 * 루프 없음.
 *
 * Thread.sleep()으로 대기하므로 해당 스레드를 점유한다.
 * (Q8의 SingleThreadDelayNode와 대비됨)
 */
public class DelayNode extends Node {

    private final long delayMs;

    public DelayNode(String id, long delayMs) {
        super(id);
        this.delayMs = delayMs;
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: receive → sleep(delayMs) → emit
    }
}
