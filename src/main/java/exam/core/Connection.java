package exam.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 노드와 노드를 잇는 메시지 통로.
 * 내부적으로 BlockingQueue를 사용하므로 스레드 안전.
 * 제공 클래스 — 수정 금지.
 */
public class Connection {

    private final String id;
    private final BlockingQueue<Message> queue;

    public Connection(String id) {
        this(id, 256);
    }

    public Connection(String id, int capacity) {
        this.id    = id;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * 메시지를 큐에 넣는다.
     * 큐가 가득 차면 공간이 생길 때까지 블로킹한다 (백프레셔).
     */
    public void send(Message msg) throws InterruptedException {
        queue.put(msg);
    }

    /**
     * 메시지를 꺼낸다.
     * 큐가 비어 있으면 메시지가 들어올 때까지 블로킹한다.
     */
    public Message receive() throws InterruptedException {
        return queue.take();
    }

    /**
     * 논블로킹으로 메시지를 꺼낸다.
     * 큐가 비어 있으면 null을 반환한다.
     */
    public Message tryReceive() {
        return queue.poll();
    }

    /**
     * 지정 시간 동안만 기다리는 receive.
     * 시간 내 메시지가 없으면 null 반환.
     */
    public Message tryReceive(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public boolean isEmpty()  { return queue.isEmpty(); }
    public int     size()     { return queue.size(); }
    public String  getId()    { return id; }

    @Override
    public String toString() { return "Connection(" + id + ")"; }
}
