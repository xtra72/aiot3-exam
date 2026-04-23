package exam.q4;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q4-A] 주기적 소스 노드 (10점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = sleep(intervalMs) → 순번 하나 emit.
 * 루프 없음 — asRunnable()이 반복 호출한다.
 *
 * 플로우 구조:
 *   PeriodicSource(500ms) ──▶ FanOutNode ──[out1]──▶ CollectorA
 *                                         └─[out2]──▶ DelayNode(250ms) ──▶ CollectorB
 *
 * 동작:
 *   Thread.sleep(intervalMs) 후 순번(1, 2, 3, ...)을 Integer로 emit한다.
 *   seq 필드로 순번을 추적한다.
 */
public class PeriodicSourceNode extends Node {

    private final long intervalMs;
    private int seq = 0;

    public PeriodicSourceNode(String id, long intervalMs) {
        super(id);
        this.intervalMs = intervalMs;
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: sleep → seq 증가 → emit
    }
}
