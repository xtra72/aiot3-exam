package exam;

import exam.core.Connection;
import exam.core.Message;
import exam.core.Node;
import exam.q1.ConsoleInputNode;
import exam.q1.ConsoleOutputNode;
import exam.q2.IncrementNode;
import exam.q2.TypeRouterNode;
import exam.q2.UpperCaseNode;
import exam.q3.FibonacciNode;
import exam.q4.DelayNode;
import exam.q4.FanOutNode;
import exam.q4.PeriodicSourceNode;
import exam.q5.Flow;
import exam.q8.SingleThreadDelayNode;
import exam.q8.SingleThreadFlow;
import exam.q9.HttpListenerNode;
import exam.q9.HttpResponseNode;
import exam.util.Nodes;
import exam.util.Nodes.TimedEntry;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FBP 엔진 시험 — 부분 점수 채점 테스트
 *
 * Q1~Q3 : 싱글 스레드 — execute() 직접 호출
 * Q4~Q9 : 멀티 스레드
 *
 * 각 문제 안에서 클래스별 단위 테스트가 먼저 나오고,
 * 마지막에 통합 테스트가 있다.
 * 단위 테스트를 통과하면 부분 점수를 얻을 수 있다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExamTest {

    // ══════════════════════════════════════════════════════════════
    // Q1 — 싱글 스레드 단일 패스  [총 10점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q1 [10점] 싱글스레드 단일 패스")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q1Tests {

        /**
         * ConsoleInputNode 단독 검증.
         * execute() 한 번 호출 후 Connection에 메시지가 들어있어야 한다.
         */
        @Test @Order(1)
        @DisplayName("Q1-A [5점] ConsoleInputNode — execute() 후 Connection에 메시지 존재")
        void q1a_inputNodeEmit() throws InterruptedException {
            ConsoleInputNode input = new ConsoleInputNode("in", "Hello, FBP!");
            Connection conn = new Connection("c");
            input.setOutput(conn);

            input.execute();   // 단일 스텝

            Message msg = conn.tryReceive();
            assertNotNull(msg,
                    "execute() 호출 후 Connection 큐에 메시지가 있어야 합니다");
            assertEquals("Hello, FBP!", msg.getPayloadAsString(),
                    "payload가 'Hello, FBP!'여야 합니다");
        }

        /**
         * ConsoleOutputNode 단독 검증.
         * Connection에 메시지를 넣고 execute() 호출 시
         * getLastOutput()이 payload를 포함해야 한다.
         */
        @Test @Order(2)
        @DisplayName("Q1-B [5점] ConsoleOutputNode — execute() 후 getLastOutput() 반환")
        void q1b_outputNodeReceive() throws InterruptedException {
            ConsoleOutputNode output = new ConsoleOutputNode("out");
            Connection conn = new Connection("c");
            output.setInbound(conn);

            conn.send(Message.of("FBP Test"));
            output.execute();   // 단일 스텝

            assertNotNull(output.getLastOutput(),
                    "getLastOutput()이 null이면 안 됩니다");
            assertTrue(output.getLastOutput().contains("FBP Test"),
                    "getLastOutput()이 payload를 포함해야 합니다. 실제: "
                    + output.getLastOutput());
        }

        /** 통합: input → output 연결 단일 패스 */
        @Test @Order(3)
        @DisplayName("Q1-통합 — ConsoleInputNode → ConsoleOutputNode 연결")
        void q1c_integration() throws InterruptedException {
            ConsoleInputNode  input  = new ConsoleInputNode("in",  "Hello, FBP!");
            ConsoleOutputNode output = new ConsoleOutputNode("out");
            Connection conn = new Connection("conn");
            input.setOutput(conn);
            output.setInbound(conn);

            input.execute();
            output.execute();

            assertTrue(output.getLastOutput().contains("Hello, FBP!"));
            System.out.println("✅ Q1 통과: " + output.getLastOutput());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q2 — 싱글 스레드 2중 패스  [총 35점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q2 [35점] 싱글스레드 2중 패스")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q2Tests {

        /** TypeRouterNode: 숫자 판별 → "number" 포트 */
        @Test @Order(1)
        @DisplayName("Q2-A [8점] TypeRouterNode — 숫자는 number 포트로")
        void q2a_routerNumber() throws InterruptedException {
            TypeRouterNode router = new TypeRouterNode("r");
            Connection src = new Connection("src");
            Connection num = new Connection("num");
            Connection str = new Connection("str");
            router.setInbound(src);
            router.addOutput("number", num);
            router.addOutput("string", str);

            // 정수 문자열
            src.send(Message.of("42"));
            router.execute();
            assertNotNull(num.tryReceive(), "'42'은 number 포트로 가야 합니다");
            assertNull(str.tryReceive(),    "'42'이 string 포트로 가면 안 됩니다");

            // 음수 문자열
            src.send(Message.of("-7"));
            router.execute();
            assertNotNull(num.tryReceive(), "'-7'은 number 포트로 가야 합니다");

            // Integer 타입
            src.send(Message.of(100));
            router.execute();
            assertNotNull(num.tryReceive(), "Integer 타입은 number 포트로 가야 합니다");
        }

        /** TypeRouterNode: 문자열 판별 → "string" 포트 */
        @Test @Order(2)
        @DisplayName("Q2-B [7점] TypeRouterNode — 문자열은 string 포트로")
        void q2b_routerString() throws InterruptedException {
            TypeRouterNode router = new TypeRouterNode("r");
            Connection src = new Connection("src");
            Connection num = new Connection("num");
            Connection str = new Connection("str");
            router.setInbound(src);
            router.addOutput("number", num);
            router.addOutput("string", str);

            src.send(Message.of("hello"));
            router.execute();
            assertNull(num.tryReceive(),    "'hello'는 number 포트로 가면 안 됩니다");
            assertNotNull(str.tryReceive(), "'hello'는 string 포트로 가야 합니다");

            src.send(Message.of("world"));
            router.execute();
            assertNotNull(str.tryReceive(), "'world'는 string 포트로 가야 합니다");
        }

        /** IncrementNode: 정수 +1 */
        @Test @Order(3)
        @DisplayName("Q2-C [10점] IncrementNode — 정수 +1 변환")
        void q2c_increment() throws InterruptedException {
            IncrementNode inc = new IncrementNode("inc");
            Connection in  = new Connection("in");
            Connection out = new Connection("out");
            inc.setInbound(in);
            inc.setOutput(out);

            for (int[] pair : new int[][]{{0,1},{5,6},{-1,0},{99,100}}) {
                in.send(Message.of(String.valueOf(pair[0])));
                inc.execute();
                Message msg = out.tryReceive();
                assertNotNull(msg, pair[0] + " 입력 시 출력이 있어야 합니다");
                assertEquals(pair[1], Integer.parseInt(msg.getPayloadAsString()),
                        pair[0] + " + 1 = " + pair[1] + " 여야 합니다");
            }
        }

        /** UpperCaseNode: 대문자 변환 */
        @Test @Order(4)
        @DisplayName("Q2-D [10점] UpperCaseNode — 대문자 변환")
        void q2d_upperCase() throws InterruptedException {
            UpperCaseNode upper = new UpperCaseNode("up");
            Connection in  = new Connection("in");
            Connection out = new Connection("out");
            upper.setInbound(in);
            upper.setOutput(out);

            for (String[] pair : new String[][]{
                    {"hello","HELLO"}, {"world","WORLD"}, {"FbP","FBP"}}) {
                in.send(Message.of(pair[0]));
                upper.execute();
                Message msg = out.tryReceive();
                assertNotNull(msg);
                assertEquals(pair[1], msg.getPayloadAsString(),
                        pair[0] + " → " + pair[1] + " 여야 합니다");
            }
        }

        /** 전체 파이프라인 통합 */
        @Test @Order(5)
        @DisplayName("Q2-통합 — 전체 파이프라인")
        void q2e_fullPipeline() throws InterruptedException {
            TypeRouterNode router    = new TypeRouterNode("router");
            IncrementNode  increment = new IncrementNode("increment");
            UpperCaseNode  upperCase = new UpperCaseNode("upperCase");

            Connection srcConn     = new Connection("src");
            Connection routerToNum = new Connection("r->num");
            Connection routerToStr = new Connection("r->str");
            Connection numOut      = new Connection("num->out");
            Connection strOut      = new Connection("str->out");

            router.setInbound(srcConn);
            router.addOutput("number", routerToNum);
            router.addOutput("string", routerToStr);
            increment.setInbound(routerToNum); increment.setOutput(numOut);
            upperCase.setInbound(routerToStr); upperCase.setOutput(strOut);

            for (String s : List.of("42", "hello", "7", "world", "3"))
                srcConn.send(Message.of(s));

            List<String> numResults = new ArrayList<>();
            List<String> strResults = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                router.execute();
                if (!routerToNum.isEmpty()) {
                    increment.execute();
                    numResults.add(numOut.receive().getPayloadAsString());
                }
                if (!routerToStr.isEmpty()) {
                    upperCase.execute();
                    strResults.add(strOut.receive().getPayloadAsString());
                }
            }

            assertEquals(3, numResults.size());
            assertTrue(numResults.containsAll(List.of("43", "8", "4")));
            assertEquals(2, strResults.size());
            assertTrue(strResults.containsAll(List.of("HELLO", "WORLD")));
            System.out.println("✅ Q2 통과  숫자=" + numResults + "  문자=" + strResults);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q3 — 싱글 스레드 피보나치  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q3 [20점] 싱글스레드 피보나치")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q3Tests {

        /** 첫 번째 값 = 1 */
        @Test @Order(1)
        @DisplayName("Q3-A [5점] FibonacciNode — 첫 번째 emit 값 = 1")
        void q3a_firstValue() throws InterruptedException {
            FibonacciNode fib  = new FibonacciNode("fib", 10);
            Connection    conn = new Connection("c");
            fib.setOutput(conn);

            fib.execute();

            Message msg = conn.tryReceive();
            assertNotNull(msg, "execute() 후 메시지가 있어야 합니다");
            assertEquals(1L, Long.parseLong(msg.getPayloadAsString()),
                    "첫 번째 피보나치 수는 1이어야 합니다");
        }

        /** 호출 간 상태 유지 확인 */
        @Test @Order(2)
        @DisplayName("Q3-B [5점] FibonacciNode — 두 번째 값도 1 (상태 유지)")
        void q3b_statePersistence() throws InterruptedException {
            FibonacciNode fib  = new FibonacciNode("fib", 10);
            Connection    conn = new Connection("c");
            fib.setOutput(conn);

            fib.execute();
            fib.execute();

            conn.receive();   // 첫 번째 버림
            Message second = conn.receive();
            assertEquals(1L, Long.parseLong(second.getPayloadAsString()),
                    "두 번째 피보나치 수도 1이어야 합니다 (상태가 유지되어야 함)");
        }

        /** 8개 수열 전체 검증 */
        @Test @Order(3)
        @DisplayName("Q3-C [10점] FibonacciNode — 수열 1,1,2,3,5,8,13,21")
        void q3c_sequence() throws InterruptedException {
            FibonacciNode fib  = new FibonacciNode("fib", 10);
            Connection    conn = new Connection("c");
            fib.setOutput(conn);

            List<String> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                fib.execute();
                results.add(conn.receive().getPayloadAsString());
            }

            List<Long> expected = List.of(1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L);
            for (int i = 0; i < 8; i++) {
                assertEquals(expected.get(i), Long.parseLong(results.get(i)),
                        i + "번째 피보나치 불일치. 실제=" + results.get(i));
            }
            System.out.println("✅ Q3 통과  수열=" + results);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q4 — 멀티스레드 딜레이 팬아웃  [총 30점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q4 [30점] 멀티스레드 딜레이 팬아웃")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 15, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q4Tests {

        /** PeriodicSourceNode: 순번 emit, 상태 유지 */
        @Test @Order(1)
        @DisplayName("Q4-A [8점] PeriodicSourceNode — 순번(1,2,3) emit")
        void q4a_periodicSource() throws InterruptedException {
            PeriodicSourceNode src = new PeriodicSourceNode("src", 50);
            Connection out = new Connection("out");
            src.setOutput(out);

            for (int expected = 1; expected <= 3; expected++) {
                src.execute();
                Message msg = out.tryReceive();
                assertNotNull(msg, expected + "번째 emit이 없습니다");
                assertEquals(expected, Integer.parseInt(msg.getPayloadAsString()),
                        expected + "번째 순번이 " + expected + "여야 합니다");
            }
        }

        /** FanOutNode: 동일 메시지를 out1, out2 모두로 */
        @Test @Order(2)
        @DisplayName("Q4-B [8점] FanOutNode — out1, out2 모두에 emit")
        void q4b_fanOut() throws InterruptedException {
            FanOutNode fan = new FanOutNode("fan");
            Connection in   = new Connection("in");
            Connection out1 = new Connection("out1");
            Connection out2 = new Connection("out2");
            fan.setInbound(in);
            fan.addOutput("out1", out1);
            fan.addOutput("out2", out2);

            in.send(Message.of("ping"));
            fan.execute();

            Message m1 = out1.tryReceive();
            Message m2 = out2.tryReceive();
            assertNotNull(m1, "out1에 메시지가 있어야 합니다");
            assertNotNull(m2, "out2에 메시지가 있어야 합니다");
            assertEquals(m1.getPayloadAsString(), m2.getPayloadAsString(),
                    "out1과 out2의 payload가 동일해야 합니다");
        }

        /** DelayNode: delayMs 이상 대기 후 emit */
        @Test @Order(3)
        @DisplayName("Q4-C [8점] DelayNode — 지정 시간 후 emit (Thread.sleep)")
        void q4c_delayNode() throws InterruptedException {
            DelayNode  delay = new DelayNode("delay", 200);
            Connection in    = new Connection("in");
            Connection out   = new Connection("out");
            delay.setInbound(in);
            delay.setOutput(out);

            in.send(Message.of("delayed"));

            Thread t = new Thread(delay.asRunnable());
            t.setDaemon(true);
            long start = System.currentTimeMillis();
            t.start();

            Message msg = out.tryReceive(1000, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - start;
            t.interrupt();

            assertNotNull(msg, "200ms 후 메시지가 emit되어야 합니다");
            assertTrue(elapsed >= 150,
                    "최소 200ms 대기 필요. 실제=" + elapsed + "ms");
        }

        /** 전체 파이프라인 통합 + 타이밍 */
        @Test @Order(4)
        @DisplayName("Q4-통합 [6점] 전체 팬아웃 파이프라인 타이밍")
        void q4d_fullPipeline() throws InterruptedException {
            List<TimedEntry> rA = new CopyOnWriteArrayList<>();
            List<TimedEntry> rB = new CopyOnWriteArrayList<>();

            PeriodicSourceNode             source = new PeriodicSourceNode("source", 500);
            FanOutNode                     fanOut = new FanOutNode("fanOut");
            DelayNode                      delay  = new DelayNode("delay", 250);
            Nodes.TimestampedCollectorNode colA   = Nodes.timedCollector("colA", rA);
            Nodes.TimestampedCollectorNode colB   = Nodes.timedCollector("colB", rB);

            Connection s2f = new Connection("s->f");
            Connection f2a = new Connection("f->A");
            Connection f2d = new Connection("f->delay");
            Connection d2b = new Connection("d->B");

            source.setOutput(s2f);
            fanOut.setInbound(s2f);
            fanOut.addOutput("out1", f2a);
            fanOut.addOutput("out2", f2d);
            delay.setInbound(f2d); delay.setOutput(d2b);
            colA.setInbound(f2a); colB.setInbound(d2b);

            List<Node> nodes = List.of(source, fanOut, delay, colA, colB);
            List<Thread> threads = new ArrayList<>();
            for (Node n : nodes) {
                Thread t = new Thread(n.asRunnable(), "t-" + n.getId());
                t.setDaemon(true); t.start(); threads.add(t);
            }

            waitForTimedSize(rA, 3, 6000);
            waitForTimedSize(rB, 3, 6000);
            nodes.forEach(Node::stop);
            threads.forEach(Thread::interrupt);

            assertTrue(rA.size() >= 3 && rB.size() >= 3,
                    "A, B 각 3건 이상 필요");
            for (int i = 0; i < 3; i++) {
                long diff = rB.get(i).receivedAt() - rA.get(i).receivedAt();
                assertTrue(diff >= 150 && diff <= 450,
                        "A→B 차이 150~450ms. pair[" + i + "]=" + diff + "ms");
            }
            System.out.println("✅ Q4 통과");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q5 — Flow 클래스  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q5 [20점] Flow 클래스")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 15, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q5Tests {

        /** addNode() + nodeCount() */
        @Test @Order(1)
        @DisplayName("Q5-A [5점] addNode() — nodeCount() 반환값")
        void q5a_nodeCount() {
            Flow flow = new Flow("test");
            assertEquals(0, flow.nodeCount(), "초기 nodeCount()는 0");

            flow.addNode(new FibonacciNode("n1", 100));
            assertEquals(1, flow.nodeCount(), "addNode() 후 1");

            flow.addNode(Nodes.collector("n2", new ArrayList<>()));
            assertEquals(2, flow.nodeCount(), "addNode() 후 2");
        }

        /** start() — 노드가 실제로 실행되어 메시지 생성 */
        @Test @Order(2)
        @DisplayName("Q5-B [10점] start() — 노드가 Thread에서 실행됨")
        void q5b_start() throws InterruptedException {
            List<String> results = new CopyOnWriteArrayList<>();
            FibonacciNode       fib  = new FibonacciNode("fib", 100);
            Nodes.CollectorNode col  = Nodes.collector("col", results);
            Connection          conn = new Connection("c");
            fib.setOutput(conn); col.setInbound(conn);

            Flow flow = new Flow("test");
            flow.addNode(fib).addNode(col).addConnection(conn).start();

            waitForSize(results, 3, 3000);
            flow.stop(); flow.awaitAll(1000);

            assertTrue(results.size() >= 3,
                    "start() 후 노드가 실행되어 3개 이상 수집 필요. 실제: " + results.size());
        }

        /** stop() + awaitAll() — 중단 후 추가 메시지 없음 */
        @Test @Order(3)
        @DisplayName("Q5-C [5점] stop() + awaitAll() — 중단 동작")
        void q5c_stopAndAwait() throws InterruptedException {
            List<String> results = new CopyOnWriteArrayList<>();
            FibonacciNode       fib  = new FibonacciNode("fib", 100);
            Nodes.CollectorNode col  = Nodes.collector("col", results);
            Connection          conn = new Connection("c");
            fib.setOutput(conn); col.setInbound(conn);

            Flow flow = new Flow("test");
            flow.addNode(fib).addNode(col).addConnection(conn).start();

            waitForSize(results, 2, 3000);
            flow.stop();
            flow.awaitAll(1500);   // 스레드 종료 대기

            int countAtStop = results.size();
            Thread.sleep(300);     // 추가 실행 여부 확인

            assertTrue(results.size() <= countAtStop + 1,
                    "stop() 후 대량의 메시지가 추가되면 안 됩니다");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q6 — 다중 Flow 독립 실행  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q6 [20점] 다중 Flow 독립 실행")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 15, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q6Tests {

        /** 두 Flow가 동시에 실행되고 각 5건 수집 */
        @Test @Order(1)
        @DisplayName("Q6-A [10점] 두 Flow 인스턴스 동시 실행")
        void q6a_twoFlowsRun() throws InterruptedException {
            List<String> r1 = new CopyOnWriteArrayList<>();
            List<String> r2 = new CopyOnWriteArrayList<>();

            FibonacciNode f1 = new FibonacciNode("f1", 100);
            Nodes.CollectorNode c1 = Nodes.collector("c1", r1);
            Connection cn1 = new Connection("cn1");
            f1.setOutput(cn1); c1.setInbound(cn1);
            Flow flow1 = new Flow("flow-1");
            flow1.addNode(f1).addNode(c1).addConnection(cn1).start();

            FibonacciNode f2 = new FibonacciNode("f2", 150);
            Nodes.CollectorNode c2 = Nodes.collector("c2", r2);
            Connection cn2 = new Connection("cn2");
            f2.setOutput(cn2); c2.setInbound(cn2);
            Flow flow2 = new Flow("flow-2");
            flow2.addNode(f2).addNode(c2).addConnection(cn2).start();

            waitForSize(r1, 5, 4000);
            waitForSize(r2, 5, 4000);
            flow1.stop(); flow2.stop();
            flow1.awaitAll(1000); flow2.awaitAll(1000);

            assertTrue(r1.size() >= 5, "flow1 5건 이상. 실제: " + r1.size());
            assertTrue(r2.size() >= 5, "flow2 5건 이상. 실제: " + r2.size());
        }

        /** 두 Flow의 수열이 모두 올바른 피보나치 */
        @Test @Order(2)
        @DisplayName("Q6-B [10점] 두 Flow가 독립적으로 올바른 피보나치 수열 생성")
        void q6b_independentSequences() throws InterruptedException {
            List<String> r1 = new CopyOnWriteArrayList<>();
            List<String> r2 = new CopyOnWriteArrayList<>();

            for (int fi = 0; fi < 2; fi++) {
                FibonacciNode       fib  = new FibonacciNode("f" + fi, 100);
                Nodes.CollectorNode col  = Nodes.collector("c" + fi, fi == 0 ? r1 : r2);
                Connection          conn = new Connection("cn" + fi);
                fib.setOutput(conn); col.setInbound(conn);
                new Flow("flow-" + fi).addNode(fib).addNode(col)
                        .addConnection(conn).start();
            }

            waitForSize(r1, 5, 4000);
            waitForSize(r2, 5, 4000);

            List<Long> exp = List.of(1L, 1L, 2L, 3L, 5L);
            for (List<String> r : List.of(r1, r2)) {
                for (int i = 0; i < 5; i++) {
                    assertEquals(exp.get(i), Long.parseLong(r.get(i)),
                            i + "번째 피보나치 불일치. 실제=" + r.get(i));
                }
            }
            System.out.println("✅ Q6 통과");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q7 — Flow implements Runnable, 단일 인스턴스, 다중 Thread  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q7 [20점] Flow implements Runnable — 단일 인스턴스, 다중 Thread")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 15, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q7Tests {

        /** run()이 팩토리로 새 인스턴스를 생성하는지 확인 */
        @Test @Order(1)
        @DisplayName("Q7-A [10점] Flow.run() — 팩토리에서 새 노드 생성, 단일 스레드 동작")
        void q7a_runSingleThread() throws InterruptedException {
            AtomicInteger factoryCallCount = new AtomicInteger(0);
            List<String>  results         = new CopyOnWriteArrayList<>();

            Flow flow = new Flow("fib-flow");
            flow.addNodeFactory(() -> {
                factoryCallCount.incrementAndGet();
                return new FibonacciNode("fib", 50);
            }).setWiring(nodes -> {
                Nodes.CollectorNode col = Nodes.collector("col", results);
                Connection c = new Connection("c");
                nodes.get(0).setOutput(c);
                col.setInbound(c);
                nodes.add(col);
            });

            Thread t = new Thread(flow, "single-run");
            t.setDaemon(true);
            t.start();

            waitForSize(results, 5, 3000);
            t.interrupt(); t.join(1000);

            assertEquals(1, factoryCallCount.get(),
                    "run() 1회 호출 시 팩토리가 1번 호출되어야 합니다");
            assertTrue(results.size() >= 5, "5개 이상 수집 필요");

            List<Long> exp = List.of(1L, 1L, 2L, 3L, 5L);
            for (int i = 0; i < 5; i++)
                assertEquals(exp.get(i), Long.parseLong(results.get(i)),
                        i + "번째 피보나치 불일치");
        }

        /** 두 Thread가 같은 Flow 인스턴스로 각자 독립 수열 생성 */
        @Test @Order(2)
        @DisplayName("Q7-B [10점] 두 Thread — 각자 독립 피보나치 수열 1,1,2,3,5,...")
        void q7b_twoThreadsIndependent() throws InterruptedException {
            Map<String, List<String>> allResults = new ConcurrentHashMap<>();

            Flow flow = new Flow("fib-flow");
            flow.addNodeFactory(() -> new FibonacciNode("fib", 100))
                .setWiring(nodes -> {
                    String threadName = Thread.currentThread().getName();
                    List<String> results = new CopyOnWriteArrayList<>();
                    allResults.put(threadName, results);
                    Nodes.CollectorNode col =
                            Nodes.collector("col-" + threadName, results);
                    Connection c = new Connection("c-" + threadName);
                    nodes.get(0).setOutput(c);
                    col.setInbound(c);
                    nodes.add(col);
                });

            Thread t1 = new Thread(flow, "pipeline-1");
            Thread t2 = new Thread(flow, "pipeline-2");
            t1.setDaemon(true); t1.start();
            t2.setDaemon(true); t2.start();

            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline) {
                if (allResults.size() == 2
                        && allResults.values().stream().allMatch(r -> r.size() >= 5))
                    break;
                Thread.sleep(100);
            }
            t1.interrupt(); t2.interrupt();
            t1.join(1000);  t2.join(1000);

            assertEquals(2, allResults.size(),
                    "두 Thread 모두 파이프라인 실행 필요");

            List<Long> exp = List.of(1L, 1L, 2L, 3L, 5L);
            for (var entry : allResults.entrySet()) {
                List<String> r = entry.getValue();
                assertTrue(r.size() >= 5,
                        entry.getKey() + ": 5건 이상 필요. 실제=" + r.size());
                for (int i = 0; i < 5; i++)
                    assertEquals(exp.get(i), Long.parseLong(r.get(i)),
                            entry.getKey() + " " + i + "번째 피보나치 불일치");
            }
            System.out.println("✅ Q7 통과");
            allResults.forEach((t, r) ->
                    System.out.println("  " + t + " = " + r.subList(0, 5)));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q8 — 싱글스레드 딜레이 플로우  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q8 [20점] 싱글스레드 딜레이 플로우")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q8Tests {

        /** SingleThreadDelayNode: 빈 큐에서 즉시 반환 (tryReceive), 메시지 있으면 지연 emit */
        @Test @Order(1)
        @DisplayName("Q8-A [10점] SingleThreadDelayNode — 논블로킹 동작 검증")
        void q8a_nonBlocking() throws InterruptedException {
            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();
            SingleThreadDelayNode delay = new SingleThreadDelayNode("d", 200, scheduler);
            Connection in  = new Connection("in");
            Connection out = new Connection("out");
            delay.setInbound(in);
            delay.setOutput(out);

            // 빈 큐: execute()는 즉시 반환해야 함
            long start = System.currentTimeMillis();
            delay.execute();
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 50,
                    "빈 큐에서 execute()는 즉시 반환해야 합니다 (tryReceive 사용). " +
                    "실제=" + elapsed + "ms — receive() 블로킹을 사용하면 안 됩니다");

            // 메시지 있음: schedule() 후 200ms 뒤 emit
            in.send(Message.of("delayed"));
            delay.execute();
            Message msg = out.tryReceive(500, TimeUnit.MILLISECONDS);
            assertNotNull(msg, "200ms 후 메시지가 emit되어야 합니다");

            scheduler.shutdown();
        }

        /** SingleThreadFlow: 단일 스케줄러로 전체 파이프라인 타이밍 */
        @Test @Order(2)
        @DisplayName("Q8-B [10점] SingleThreadFlow — 타이밍 검증")
        void q8b_fullFlow() throws InterruptedException {
            List<TimedEntry> rA = new CopyOnWriteArrayList<>();
            List<TimedEntry> rB = new CopyOnWriteArrayList<>();

            SingleThreadFlow stFlow = new SingleThreadFlow("st");

            PeriodicSourceNode             src   = new PeriodicSourceNode("src", 500);
            FanOutNode                     fan   = new FanOutNode("fan");
            SingleThreadDelayNode          delay = new SingleThreadDelayNode(
                                                       "delay", 250, stFlow.getScheduler());
            Nodes.TimestampedCollectorNode cA    = Nodes.timedCollector("cA", rA);
            Nodes.TimestampedCollectorNode cB    = Nodes.timedCollector("cB", rB);

            Connection s2f = new Connection("s->f");
            Connection f2a = new Connection("f->A");
            Connection f2d = new Connection("f->d");
            Connection d2b = new Connection("d->B");

            src.setOutput(s2f);
            fan.setInbound(s2f);
            fan.addOutput("out1", f2a);
            fan.addOutput("out2", f2d);
            delay.setInbound(f2d); delay.setOutput(d2b);
            cA.setInbound(f2a);  cB.setInbound(d2b);

            stFlow.addNode(src, 500).addNode(fan, 50)
                  .addNode(delay, 50).addNode(cA, 50).addNode(cB, 50)
                  .start();

            waitForTimedSize(rA, 3, 8000);
            waitForTimedSize(rB, 3, 8000);
            stFlow.stop();

            assertTrue(rA.size() >= 3 && rB.size() >= 3, "A, B 각 3건 이상");
            for (int i = 0; i < 3; i++) {
                long diff = rB.get(i).receivedAt() - rA.get(i).receivedAt();
                assertTrue(diff >= 150 && diff <= 500,
                        "A→B 150~500ms. pair[" + i + "]=" + diff + "ms");
            }
            System.out.println("✅ Q8 통과");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Q9 — HTTP Server 플로우  [총 20점]
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Q9 [20점] HTTP Server 플로우")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Timeout(value = 15, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    class Q9Tests {

        private static final int PORT = 18080;

        /** HTTP 200 상태코드 반환 */
        @Test @Order(1)
        @DisplayName("Q9-A [10점] HTTP 응답 상태코드 200")
        void q9a_statusCode() throws Exception {
            HttpListenerNode listener  = new HttpListenerNode("in",  PORT, "/");
            HttpResponseNode responder = new HttpResponseNode("out");
            Connection conn = new Connection("http-c");
            listener.setOutput(conn); responder.setInbound(conn);

            Thread t1 = new Thread(listener.asRunnable(),  "t-http-in");
            Thread t2 = new Thread(responder.asRunnable(), "t-http-out");
            t1.setDaemon(true); t2.setDaemon(true);
            t1.start(); t2.start();
            Thread.sleep(300);

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + PORT + "/test"))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString());

            listener.stop(); responder.stop();
            t1.interrupt(); t2.interrupt();

            assertEquals(200, resp.statusCode(),
                    "HTTP 상태코드 200이어야 합니다. 실제: " + resp.statusCode());
        }

        /** 요청 경로가 응답 body에 포함 */
        @Test @Order(2)
        @DisplayName("Q9-B [10점] 요청 경로 echo — body에 경로 포함")
        void q9b_echoPath() throws Exception {
            HttpListenerNode listener  = new HttpListenerNode("in",  PORT + 1, "/");
            HttpResponseNode responder = new HttpResponseNode("out");
            Connection conn = new Connection("http-c2");
            listener.setOutput(conn); responder.setInbound(conn);

            Thread t1 = new Thread(listener.asRunnable(),  "t-http-in2");
            Thread t2 = new Thread(responder.asRunnable(), "t-http-out2");
            t1.setDaemon(true); t2.setDaemon(true);
            t1.start(); t2.start();
            Thread.sleep(300);

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> r1 = client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + (PORT+1) + "/hello")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> r2 = client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + (PORT+1) + "/world")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

            listener.stop(); responder.stop();
            t1.interrupt(); t2.interrupt();

            assertTrue(r1.body().contains("/hello"),
                    "body에 '/hello' 포함 필요. 실제: " + r1.body());
            assertTrue(r2.body().contains("/world"),
                    "body에 '/world' 포함 필요. 실제: " + r2.body());
            System.out.println("✅ Q9 통과");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 헬퍼
    // ══════════════════════════════════════════════════════════════

    private void waitForSize(List<String> list, int target, long ms)
            throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        synchronized (list) {
            while (list.size() < target && System.currentTimeMillis() < end)
                list.wait(100);
        }
    }

    private void waitForTimedSize(List<TimedEntry> list, int target, long ms)
            throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        synchronized (list) {
            while (list.size() < target && System.currentTimeMillis() < end)
                list.wait(100);
        }
    }
}
