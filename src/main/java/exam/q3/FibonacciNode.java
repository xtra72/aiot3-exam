package exam.q3;

import exam.core.Message;
import exam.core.Node;

/**
 * ════════════════════════════════════════════════════
 * [Q3] 피보나치 수열 생성 노드 (20점)
 * ════════════════════════════════════════════════════
 *
 * execute() 한 번 호출 = 현재 피보나치 수 하나 emit → 상태 전진.
 * 루프 없음 — 반복은 asRunnable() 또는 테스트 코드가 담당한다.
 *
 * 수열: 1, 1, 2, 3, 5, 8, 13, 21, ...
 *
 * 동작 순서 (한 단계):
 *   1. Thread.sleep(intervalMs)
 *   2. 현재 값(a)을 Long으로 emit
 *   3. 다음 상태로 전진: [a, b] = [b, a+b]
 *
 * 초기 상태: a=1, b=1
 *   → 1회: emit(1), [1,1]→[1,2]
 *   → 2회: emit(1), [1,2]→[2,3]
 *   → 3회: emit(2), [2,3]→[3,5]  ...
 *
 * 사용 모드:
 *   [Q3 싱글 스레드] 테스트가 8회 직접 호출
 *   [Q5~Q6 멀티 스레드] Flow.start() → asRunnable()이 루프 호출
 *   [Q7 Flow.run()] 협력적 실행 루프가 호출
 */
public class FibonacciNode extends Node {

    private final long intervalMs;

    // TODO: 피보나치 상태 변수 선언 (초기값: a=1, b=1)
    private long a;
    private long b;

    public FibonacciNode(String id, long intervalMs) {
        super(id);
        this.intervalMs = intervalMs;
        // TODO: 초기화
    }

    public FibonacciNode(String id) {
        this(id, 1000L);
    }

    /**
     * 한 단계: sleep → emit(a) → [a,b]=[b,a+b].
     * while 루프 작성 금지.
     */
    @Override
    public void execute() throws InterruptedException {
        // TODO
    }
}
