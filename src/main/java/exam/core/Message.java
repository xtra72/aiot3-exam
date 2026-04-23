package exam.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 노드 간에 전달되는 메시지 객체 (불변).
 * 제공 클래스 — 수정 금지.
 */
public final class Message {

    private final Object payload;
    private final Map<String, Object> headers;

    private Message(Object payload, Map<String, Object> headers) {
        this.payload = payload;
        this.headers = Map.copyOf(headers);
    }

    public static Message of(Object payload) {
        return new Message(payload, Map.of());
    }

    public static Message of(Object payload, Map<String, Object> headers) {
        return new Message(payload, new HashMap<>(headers));
    }

    /** payload를 그대로 반환 */
    public Object getPayload() { return payload; }

    /** payload를 String으로 변환 */
    public String getPayloadAsString() { return String.valueOf(payload); }

    /** payload를 int로 변환 */
    public int getPayloadAsInt() { return ((Number) payload).intValue(); }

    /** 헤더 맵 전체 반환 */
    public Map<String, Object> getHeaders() { return headers; }

    /** 헤더 값 반환 */
    public Object getHeader(String key) { return headers.get(key); }

    /** 새 헤더를 추가한 새 Message 반환 */
    public Message withHeader(String key, Object value) {
        Map<String, Object> newHeaders = new HashMap<>(this.headers);
        newHeaders.put(key, value);
        return new Message(this.payload, newHeaders);
    }

    /** payload를 교체한 새 Message 반환 */
    public Message withPayload(Object newPayload) {
        return new Message(newPayload, this.headers);
    }

    @Override
    public String toString() {
        return "Message{payload=" + payload + ", headers=" + headers + "}";
    }
}
