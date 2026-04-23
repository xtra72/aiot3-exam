package exam.q8;

import exam.core.Connection;
import exam.core.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ════════════════════════════════════════════════════
 * [Q8] 싱글스레드 플로우 (Q8 나머지 부분, 20점 합산)
 * ════════════════════════════════════════════════════
 *
 * ScheduledExecutorService(단일 스레드)로 모든 노드를 실행한다.
 *
 * 구조:
 *   - 내부적으로 newSingleThreadScheduledExecutor() 하나만 사용한다.
 *   - 각 노드의 execute()를 scheduler.scheduleAtFixedRate()로 등록한다.
 *     (폴링 주기: 50ms — 충분히 짧아 응답성 확보)
 *   - PeriodicSourceNode 같은 소스 노드는 scheduleAtFixedRate로 intervalMs마다 실행.
 *   - SingleThreadDelayNode는 50ms마다 폴링.
 *
 * 요구사항:
 *   1. addNode(node, periodMs) — 노드를 주기 periodMs(ms)로 등록
 *   2. start()  — 스케줄러 시작
 *   3. stop()   — 스케줄러 종료 (awaitTermination 1초)
 *
 * 힌트:
 *   scheduler.scheduleAtFixedRate(node.asRunnable(), 0, periodMs, TimeUnit.MILLISECONDS)
 */
public class SingleThreadFlow {

    private final String                   name;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "single-flow-thread");
                t.setDaemon(true);
                return t;
            });

    private final List<Node>             nodes    = new ArrayList<>();
    private final List<ScheduledFuture<?>> futures  = new ArrayList<>();

    // 노드별 실행 주기 기록
    private final List<long[]> schedule = new ArrayList<>();  // [nodeIndex, periodMs]

    public SingleThreadFlow(String name) {
        this.name = name;
    }

    /**
     * 노드를 등록한다.
     * periodMs: 이 노드의 execute()를 호출하는 주기(밀리초)
     */
    public SingleThreadFlow addNode(Node node, long periodMs) {
        // TODO
        return this;
    }

    /** 스케줄러를 시작한다. */
    public SingleThreadFlow start() {
        // TODO
        return this;
    }

    /** 스케줄러를 종료하고 1초간 대기한다. */
    public void stop() throws InterruptedException {
        // TODO
    }

    /** 테스트용: 스케줄러 접근자 */
    public ScheduledExecutorService getScheduler() { return scheduler; }
    public String getName() { return name; }
}
