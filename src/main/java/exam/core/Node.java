package exam.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 모든 노드의 추상 기반 클래스.
 *
 * <pre>
 * 포트 구조
 *   inbound   : 단일 입력 Connection (소스 노드는 null)
 *   outputs   : 이름 있는 출력 Connection 맵
 *                "out"  — 기본 단일 출력 (setOutput 사용)
 *                임의 이름 — 분기 출력 (addOutput 사용)
 * </pre>
 *
 * 제공 클래스 — 수정 금지.
 */
public abstract class Node {

    protected final String id;

    private Connection            inbound;
    private final Map<String, Connection> outputs = new LinkedHashMap<>();
    private volatile boolean      running = true;

    public Node(String id) { this.id = id; }

    // ──────────────────────────────────────────────
    // 연결 설정 (Flow 또는 테스트 코드에서 호출)
    // ──────────────────────────────────────────────

    /** 단일 입력 포트 연결 */
    public void setInbound(Connection c)               { this.inbound = c; }

    /** 기본 출력 포트 "out" 연결 */
    public void setOutput(Connection c)                { outputs.put("out", c); }

    /** 이름 있는 출력 포트 추가 */
    public void addOutput(String port, Connection c)   { outputs.put(port, c); }

    /** 출력 포트 Connection 조회 */
    public Connection getOutput(String port)           { return outputs.get(port); }

    // ──────────────────────────────────────────────
    // 실행 제어
    // ──────────────────────────────────────────────

    /** 노드 실행을 중단하도록 플래그 설정 */
    public void stop()               { running = false; }
    protected boolean isRunning()    { return running; }

    /**
     * Flow / Thread 에서 노드를 실행할 때 사용하는 Runnable 래퍼.
     * execute()를 isRunning() && !interrupted 조건으로 반복 호출한다.
     *
     * 단일 스레드 모드(Q1~Q3)에서는 asRunnable()을 사용하지 않고
     * execute()를 직접 호출한다.
     */
    public final Runnable asRunnable() {
        return () -> {
            try {
                while (isRunning() && !Thread.currentThread().isInterrupted()) {
                    execute();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[" + id + "] 오류: " + e.getMessage());
            }
        };
    }

    // ──────────────────────────────────────────────
    // 메시지 송수신 헬퍼 (서브클래스에서 사용)
    // ──────────────────────────────────────────────

    /** inbound에서 메시지를 블로킹 수신 */
    protected Message receive() throws InterruptedException {
        if (inbound == null)
            throw new IllegalStateException(id + ": inbound 연결이 설정되지 않았습니다");
        return inbound.receive();
    }

    /** inbound에서 논블로킹 수신 (없으면 null) */
    protected Message tryReceive() {
        return (inbound == null) ? null : inbound.tryReceive();
    }

    /** "out" 기본 포트로 emit */
    protected void emit(Message msg) throws InterruptedException {
        emit("out", msg);
    }

    /** 지정 포트로 emit */
    protected void emit(String port, Message msg) throws InterruptedException {
        Connection c = outputs.get(port);
        if (c == null)
            throw new IllegalStateException(id + ": output port '" + port + "' 연결 없음");
        c.send(msg);
    }

    public String getId() { return id; }

    // ──────────────────────────────────────────────
    // 서브클래스 구현 대상
    // ──────────────────────────────────────────────

    /**
     * 노드 실행의 한 단계(step).
     *
     * <b>설계 원칙: 루프를 직접 작성하지 않는다.</b>
     * - 한 번 호출될 때마다 메시지 하나를 처리하고 반환한다.
     * - 반복은 asRunnable()이 외부에서 담당한다.
     *
     * 단일 스레드 모드(Q1~Q3):
     *   테스트 코드가 execute()를 직접 반복 호출한다.
     *
     * 멀티 스레드 모드(Q4~):
     *   asRunnable()이 execute()를 루프로 호출한다.
     */
    public abstract void execute() throws InterruptedException;
}
