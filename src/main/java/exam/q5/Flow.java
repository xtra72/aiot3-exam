package exam.q5;

import exam.core.Connection;
import exam.core.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ════════════════════════════════════════════════════
 * [Q5 / Q7] Flow 클래스 구현 (20점)
 * ════════════════════════════════════════════════════
 *
 * Flow는 두 가지 모드를 지원한다.
 *
 * ━━ 인스턴스 모드 (Q5, Q6) ━━━━━━━━━━━━━━━━━━━━━━━━
 *   addNode()로 노드를 직접 등록하고
 *   start()로 각 노드를 별도 daemon Thread에서 실행한다.
 *
 *   Flow flow = new Flow("fib-flow");
 *   flow.addNode(fibNode).addNode(colNode)
 *       .addConnection(conn).start();
 *
 * ━━ Runnable 모드 (Q7) ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *   Flow가 Runnable을 구현한다.
 *   addNodeFactory()로 노드 생성 팩토리를,
 *   setWiring()으로 연결 설정 함수를 등록한다.
 *
 *   run()이 호출될 때마다:
 *     1. 팩토리로 새 노드 인스턴스 목록 생성
 *     2. wiring.accept(nodes) 로 연결 및 추가 노드 설정
 *     3. 현재 스레드에서 모든 노드를 협력적으로 반복 실행
 *
 *   같은 Flow 인스턴스를 여러 Thread에 전달하면
 *   각 Thread가 run()을 호출 → 각자 독립된 파이프라인을 실행한다.
 *   팩토리가 매번 새 인스턴스를 반환하므로 상태가 완전히 분리된다.
 *
 *   Flow flow = new Flow("fib-flow");       // 인스턴스 하나
 *   flow.addNodeFactory(() -> new FibonacciNode("fib", 100))
 *       .setWiring(nodes -> {
 *           // 이 wiring은 run() 호출마다 실행됨
 *           // Thread.currentThread()로 호출 스레드 식별 가능
 *           CollectorNode col = new CollectorNode(...);
 *           Connection c = new Connection("c");
 *           nodes.get(0).setOutput(c);
 *           col.setInbound(c);
 *           nodes.add(col);          // 실행 목록에 포함
 *       });
 *
 *   Thread t1 = new Thread(flow, "pipeline-1"); // 같은 flow
 *   Thread t2 = new Thread(flow, "pipeline-2"); // 같은 flow
 *   t1.start(); t2.start();
 *   // t1: run() → 새 FibonacciNode A 생성 → 1,1,2,3,5,...
 *   // t2: run() → 새 FibonacciNode B 생성 → 1,1,2,3,5,...
 */
public class Flow implements Runnable {

    private final String name;

    // ── 인스턴스 모드 ──
    private final Map<String, Node>       nodes       = new LinkedHashMap<>();
    private final Map<String, Connection> connections = new LinkedHashMap<>();
    private final List<Thread>            threads     = new ArrayList<>();

    // ── Runnable 모드 ──
    private final List<Supplier<Node>>    nodeFactories = new ArrayList<>();
    private Consumer<List<Node>>          wiring;

    public Flow(String name) { this.name = name; }

    // ════════════════════════════════════════════
    // 인스턴스 모드 (Q5, Q6)
    // ════════════════════════════════════════════

    /** 노드 인스턴스 등록. 체이닝 지원. */
    public Flow addNode(Node node) {
        // TODO
        return this;
    }

    /** 커넥션 등록. 체이닝 지원. */
    public Flow addConnection(Connection connection) {
        // TODO
        return this;
    }

    /**
     * 등록된 모든 노드를 각각 daemon Thread로 실행.
     * 스레드 이름: "flow-<n>-<nodeId>"
     */
    public Flow start() {
        // TODO
        return this;
    }

    /** 모든 노드 stop() + 스레드 interrupt. */
    public Flow stop() {
        // TODO
        return this;
    }

    /** 모든 스레드 join(timeoutMs). */
    public void awaitAll(long timeoutMs) throws InterruptedException {
        // TODO
    }

    // ════════════════════════════════════════════
    // Runnable 모드 (Q7)
    // ════════════════════════════════════════════

    /**
     * 노드 생성 팩토리 등록.
     * run() 호출 시 팩토리마다 get()을 호출해 새 인스턴스를 만든다.
     */
    public Flow addNodeFactory(Supplier<Node> factory) {
        // TODO
        return this;
    }

    /**
     * 연결 설정 함수 등록.
     * run() 호출 시 신선한 노드 목록을 인자로 받아
     *   - Connection 생성 및 노드 간 연결
     *   - nodes.add(...)로 추가 노드(CollectorNode 등) 포함
     * 을 수행한다.
     */
    public Flow setWiring(Consumer<List<Node>> wiring) {
        // TODO
        return this;
    }

    /**
     * [Q7 핵심] 현재 스레드에서 독립 파이프라인을 생성하고 실행한다.
     *
     * 구현 순서:
     *   1. nodeFactories에서 새 노드 인스턴스 목록(freshNodes) 생성
     *   2. wiring.accept(freshNodes) 호출
     *   3. 협력적 실행 루프:
     *        Thread가 interrupt될 때까지
     *        freshNodes의 모든 노드를 순서대로 execute() 반복 호출
     *
     * 여러 Thread가 같은 Flow 인스턴스로 run()을 호출하면:
     *   - 각 Thread가 1~3 단계를 독립적으로 수행
     *   - factory.get()이 매번 새 인스턴스를 반환하므로 상태 분리
     *   - 결과: 각 Thread마다 독립적인 피보나치 수열
     */
    @Override
    public void run() {
        // TODO
    }

    // ════════════════════════════════════════════
    // 공통
    // ════════════════════════════════════════════

    public String getName()   { return name; }
    public int    nodeCount() { return nodes.size(); }
}
