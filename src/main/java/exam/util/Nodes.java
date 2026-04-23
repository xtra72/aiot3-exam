package exam.util;

import exam.core.Connection;
import exam.core.Message;
import exam.core.Node;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 테스트 및 디버그용 제공 유틸리티 노드.
 * 수정 금지 — 시험에서 그대로 사용할 것.
 *
 * ─────────────────────────────────────────────────────
 * execute() 설계 원칙: 한 단계, 루프 없음
 * ─────────────────────────────────────────────────────
 * 멀티스레드 모드 : asRunnable()이 외부에서 루프
 * 단일스레드 모드 : 테스트 코드가 외부에서 루프
 * Flow.run()     : 협력적 실행 루프가 외부에서 루프
 */
public class Nodes {

    // ──────────────────────────────────────
    // CollectorNode : 한 번 호출에 메시지 하나 수집
    // ──────────────────────────────────────

    /**
     * receive() 한 번 → payload를 results에 추가.
     * 루프 없음 — 반복은 호출자가 담당한다.
     */
    public static class CollectorNode extends Node {
        private final List<String> results;

        public CollectorNode(String id, List<String> results) {
            super(id);
            this.results = results;
        }

        @Override
        public void execute() throws InterruptedException {
            Message msg = receive();                  // 한 번만 receive
            String val  = msg.getPayloadAsString();
            synchronized (results) {
                results.add(val);
                results.notifyAll();
            }
            System.out.println("[" + id + "] " + val);
        }
    }

    // ──────────────────────────────────────
    // TimestampedCollectorNode : 수신 시각 기록
    // ──────────────────────────────────────

    public record TimedEntry(String value, long receivedAt) {}

    /**
     * receive() 한 번 → (value, 수신시각)을 results에 추가.
     * 루프 없음.
     */
    public static class TimestampedCollectorNode extends Node {
        private final List<TimedEntry> results;

        public TimestampedCollectorNode(String id, List<TimedEntry> results) {
            super(id);
            this.results = results;
        }

        @Override
        public void execute() throws InterruptedException {
            Message msg  = receive();                 // 한 번만 receive
            long    now  = System.currentTimeMillis();
            TimedEntry e = new TimedEntry(msg.getPayloadAsString(), now);
            synchronized (results) {
                results.add(e);
                results.notifyAll();
            }
            System.out.println("[" + id + "] " + e.value() + " @" + now);
        }
    }

    // ──────────────────────────────────────
    // 팩토리 헬퍼
    // ──────────────────────────────────────

    public static CollectorNode collector(String id, List<String> results) {
        return new CollectorNode(id, results);
    }

    public static TimestampedCollectorNode timedCollector(String id, List<TimedEntry> results) {
        return new TimestampedCollectorNode(id, results);
    }

    /** 노드 단방향 연결 편의 메서드 */
    public static Connection wire(Node from, Node to) {
        return wire(from, "out", to);
    }

    public static Connection wire(Node from, String port, Node to) {
        Connection conn = new Connection(from.getId() + "." + port + "->" + to.getId());
        from.addOutput(port, conn);
        to.setInbound(conn);
        return conn;
    }
}
