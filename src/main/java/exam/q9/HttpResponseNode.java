package exam.q9;

import com.sun.net.httpserver.HttpExchange;
import exam.core.Message;
import exam.core.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ════════════════════════════════════════════════════
 * [Q9-B] HTTP 응답 노드 (Q9 합산)
 * ════════════════════════════════════════════════════
 *
 * 동작:
 *   isRunning() 동안 receive()한 Message를 HTTP 응답으로 전송한다.
 *
 *   1. Message header "exchange" 에서 HttpExchange를 꺼낸다.
 *   2. Message payload를 응답 body 문자열로 사용한다.
 *   3. HTTP 상태코드 200, Content-Type: text/plain; charset=utf-8 로 응답한다.
 *   4. exchange.close()를 호출한다.
 *
 * 힌트:
 *   HttpExchange ex = (HttpExchange) msg.getHeader("exchange");
 *   byte[] body = responseText.getBytes(StandardCharsets.UTF_8);
 *   ex.sendResponseHeaders(200, body.length);
 *   try (OutputStream os = ex.getResponseBody()) { os.write(body); }
 */
public class HttpResponseNode extends Node {

    public HttpResponseNode(String id) {
        super(id);
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO
    }
}
