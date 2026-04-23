package exam.q9;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exam.core.Message;
import exam.core.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════
 * [Q9-A] HTTP 리스너 노드 (20점 합산)
 * ════════════════════════════════════════════════════
 *
 * 플로우 구조:
 *
 *   [HTTP Client] ──HTTP 요청──▶ HttpListenerNode ──▶ HttpResponseNode ──▶ [HTTP Client]
 *
 * 동작:
 *   지정 포트에서 HTTP 서버(com.sun.net.httpserver.HttpServer)를 시작한다.
 *   요청이 오면 Message를 생성해 "out" 포트로 emit한다.
 *
 * 생성되는 Message:
 *   payload : 요청 body 문자열 (없으면 빈 문자열)
 *   headers :
 *     "method"   → HTTP 메서드 (GET, POST, ...)
 *     "path"     → 요청 경로 (/hello 등)
 *     "exchange" → HttpExchange 객체 (HttpResponseNode가 응답 전송에 사용)
 *
 * 생성자 파라미터:
 *   id      : 노드 식별자
 *   port    : 리스닝 포트
 *   context : 등록할 URL 컨텍스트 (예: "/")
 *
 * 힌트:
 *   HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
 *   server.createContext(context, exchange -> { ... emit(...) ... });
 *   server.start();
 *
 * 주의:
 *   - execute()에서 서버를 시작하고 isRunning() 동안 대기(Thread.sleep 루프)한다.
 *   - stop() 시 server.stop(0)을 호출한다.
 *   - HttpServer 핸들러는 별도 스레드에서 호출되므로 emit()은 스레드 안전하다.
 */
public class HttpListenerNode extends Node {

    private final int    port;
    private final String context;
    private HttpServer   server;

    public HttpListenerNode(String id, int port, String context) {
        super(id);
        this.port    = port;
        this.context = context;
    }

    @Override
    public void execute() throws InterruptedException {
        // TODO: HttpServer 시작 + isRunning() 루프 + stop 시 server.stop(0)
    }

    @Override
    public void stop() {
        super.stop();
        if (server != null) server.stop(0);
    }

    /** 요청 body를 문자열로 읽는 헬퍼 */
    private String readBody(InputStream in) throws IOException {
        return new String(in.readAllBytes());
    }
}
