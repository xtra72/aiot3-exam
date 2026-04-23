package exam.q8;

import exam.core.Message;
import exam.core.Node;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ════════════════════════════════════════════════════
 * [Q8] 딜레이 노드 — 싱글스레드용 (20점)
 * ════════════════════════════════════════════════════
 *
 * Q4와 동일한 플로우를 단일 스레드(ScheduledExecutorService)로 구현한다.
 *
 * Q4 vs Q8 차이:
 *   Q4  Thread per node, Thread.sleep()으로 블로킹 대기
 *   Q8  단일 스레드 스케줄러, 논블로킹 tryReceive() + schedule()로 지연
 *
 * 동작:
 *   tryReceive()로 메시지를 논블로킹으로 확인한다.
 *   메시지가 있으면 scheduler.schedule()을 사용해 delayMs 후에 emit한다.
 *   메시지가 없으면 즉시 리턴한다.
 *
 *   이 노드는 execute() 자체가 루프가 아니라 단일 체크 단위이다.
 *   (SingleThreadFlow가 주기적으로 호출함)
 *
 * 생성자 파라미터:
 *   id         : 노드 식별자
 *   delayMs    : 지연 시간 (밀리초)
 *   scheduler  : 외부에서 주입받는 단일 스레드 ScheduledExecutorService
 */
public class SingleThreadDelayNode extends Node {

    private final long                     delayMs;
    private final ScheduledExecutorService scheduler;

    public SingleThreadDelayNode(String id, long delayMs, ScheduledExecutorService scheduler) {
        super(id);
        this.delayMs   = delayMs;
        this.scheduler = scheduler;
    }

    /**
     * 논블로킹으로 한 번 확인하고 리턴한다.
     * SingleThreadFlow가 주기적으로 이 메서드를 호출한다.
     */
    @Override
    public void execute() throws InterruptedException {
        // TODO: tryReceive()로 메시지를 꺼내고
        //       scheduler.schedule()로 delayMs 후 emit한다
    }
}
