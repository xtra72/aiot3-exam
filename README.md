# FBP 엔진 구현 시험

---

## 시험 안내

| 항목 | 내용 |
|------|------|
| 언어 | Java 21 |
| 빌드 | Maven (`mvn test`) |

### 제공 파일 (수정 금지)

```
exam/src/main/java/exam/
  core/
    Message.java        ← 메시지 객체 (불변)
    Connection.java     ← BlockingQueue 기반 채널
    Node.java           ← 노드 추상 기반 클래스
  util/
    Nodes.java          ← CollectorNode, TimestampedCollectorNode (테스트용)
```

### 구현 대상 파일

```
exam/src/main/java/exam/
  q1/  ConsoleInputNode.java, ConsoleOutputNode.java
  q2/  TypeRouterNode.java, IncrementNode.java, UpperCaseNode.java
  q3/  FibonacciNode.java
  q4/  PeriodicSourceNode.java, FanOutNode.java, DelayNode.java
  q5/  Flow.java
  q8/  SingleThreadDelayNode.java, SingleThreadFlow.java
  q9/  HttpListenerNode.java,  HttpResponseNode.java
```

### execute() 설계 원칙

> **execute()는 한 단계(step)만 처리하고 반환한다. 루프를 작성하지 않는다.**

반복은 외부에서 담당한다.

| 실행 모드 | 담당자 |
|-----------|--------|
| Q1~Q3 싱글스레드 | 테스트 코드가 직접 반복 호출 |
| Q4~Q7 멀티스레드 | `asRunnable()`이 `while(!interrupted)` 루프로 반복 |
| Q7 협력적 실행 | `Flow.run()` 안의 루프가 노드를 순서대로 반복 |
| Q8 싱글스레드 스케줄러 | `scheduleAtFixedRate`가 반복 호출 |

### 핵심 API 요약

```java
// ── Node 서브클래스에서 사용하는 메서드 ──
Message receive()               // inbound에서 블로킹 수신
Message tryReceive()            // 논블로킹 수신 (없으면 null)
void emit(Message msg)          // "out" 포트로 emit
void emit(String port, Message) // 지정 포트로 emit

// ── 연결 설정 (외부에서 호출) ──
node.setInbound(Connection)
node.setOutput(Connection)
node.addOutput("portName", Connection)

// ── Message 생성 ──
Message.of(Object payload)
Message.of(Object payload, Map<String,Object> headers)
```

---

## Q1 — 단일 패스 기본 구성 (10점)

**구현 파일:** `q1/ConsoleInputNode.java`, `q1/ConsoleOutputNode.java`

```
ConsoleInputNode ──conn──▶ ConsoleOutputNode
```

### ConsoleInputNode

- `execute()` 한 번 호출 = `inputText`를 `Message.of(inputText)`로 emit 후 반환

### ConsoleOutputNode

- `execute()` 한 번 호출 = `receive()` 후 `"[OUTPUT] <payload>"` 출력 후 반환
- `getLastOutput()`: 마지막 출력 문자열 반환

### 싱글스레드 실행 방식

```java
input.execute();   // conn 큐에 적재
output.execute();  // conn 큐에서 꺼내 출력
assertTrue(output.getLastOutput().contains("Hello, FBP!"));
```

---

## Q2 — 2중 패스 타입 분기 (35점)

**구현 파일:** `q2/TypeRouterNode.java`, `q2/IncrementNode.java`, `q2/UpperCaseNode.java`

```
              ┌─ "number" ──▶ IncrementNode ──▶ 숫자 결과
srcConn ──▶ TypeRouterNode
              └─ "string" ──▶ UpperCaseNode ──▶ 문자 결과
```

### TypeRouterNode

- `execute()` 한 번 = `receive()` 하나 → 숫자면 `emit("number", msg)`, 아니면 `emit("string", msg)`
- 숫자 판단: `Number` 타입이거나 `Integer.parseInt()` 성공

### IncrementNode

- `execute()` 한 번 = `receive()` → 정수로 파싱 → `+1` → `emit()`

### UpperCaseNode

- `execute()` 한 번 = `receive()` → `.toUpperCase()` → `emit()`

### 싱글스레드 실행 방식

```java
// 입력 5개를 모두 srcConn에 적재한 뒤
for (int i = 0; i < 5; i++) {
    router.execute();                         // 1개 라우팅
    if (!routerToNum.isEmpty()) increment.execute();
    if (!routerToStr.isEmpty()) upperCase.execute();
}
```

검증: 입력 `["42","hello","7","world","3"]` → 숫자 `["43","8","4"]`, 문자 `["HELLO","WORLD"]`

---

## Q3 — 피보나치 수열 (20점)

**구현 파일:** `q3/FibonacciNode.java`

### FibonacciNode

- `execute()` 한 번 = `Thread.sleep(intervalMs)` → 현재 피보나치 수 `emit` → 상태 전진
- 내부 상태 변수 2개 (`a`, `b`) — `execute()` 호출 간 유지됨
- 수열: `1, 1, 2, 3, 5, 8, 13, 21, ...`
- emit 순서: **현재 값을 먼저 emit한 뒤 상태를 전진**시킨다

### 싱글스레드 실행 방식

```java
for (int i = 0; i < 8; i++) {
    fib.execute();                              // 한 수 emit
    results.add(conn.receive().getPayloadAsString());  // 한 수 수집
}
// 결과: ["1","1","2","3","5","8","13","21"]
```

### 멀티스레드 / Flow 실행 방식 (Q5~Q7)

```java
// asRunnable()이 while 루프로 execute()를 반복 호출
flow.addNode(fib).start();
```

---

## Q4 — 멀티스레드 딜레이 팬아웃 (30점)

**구현 파일:** `q4/PeriodicSourceNode.java`, `q4/FanOutNode.java`, `q4/DelayNode.java`

```
PeriodicSource(500ms) ──▶ FanOutNode ──[out1]──▶ CollectorA (즉시)
                                      └─[out2]──▶ DelayNode(250ms) ──▶ CollectorB (250ms 후)
```

노드마다 별도 Thread. `asRunnable()`이 `execute()`를 루프로 반복 호출한다.

### PeriodicSourceNode

- `execute()` 한 번 = `Thread.sleep(intervalMs)` → 순번(1, 2, 3...) emit

### FanOutNode

- `execute()` 한 번 = `receive()` → `emit("out1", msg)` + `emit("out2", msg)`

### DelayNode

- `execute()` 한 번 = `receive()` → `Thread.sleep(delayMs)` → `emit(msg)`
- `Thread.sleep()` 사용 — 해당 Thread를 점유 (Q8의 논블로킹 방식과 대비)

### 검증

A와 B 각 3건 이상, B 수신 시각 − A 수신 시각 = 150~450ms

---

## Q5 — Flow 클래스 구현 (20점)

**구현 파일:** `q5/Flow.java`

`Flow implements Runnable` 이어야 한다 (Q7에서 사용).

```java
Flow flow = new Flow("my-flow");
flow.addNode(nodeA).addNode(nodeB).addConnection(conn).start();
Thread.sleep(3000);
flow.stop(); flow.awaitAll(1000);
```

### 구현 요구사항

| 메서드 | 동작 |
|--------|------|
| `addNode(Node)` | id 기준으로 내부 Map에 등록, `return this` |
| `addConnection(Connection)` | id 기준으로 등록, `return this` |
| `start()` | 모든 노드를 daemon Thread로 실행, 이름: `"flow-<name>-<nodeId>"` |
| `stop()` | 모든 노드 `stop()` + 스레드 `interrupt()` |
| `awaitAll(long ms)` | 각 스레드 `join(ms)` |
| `nodeCount()` | 등록된 노드 수 반환 |
| `addNodeFactory(Supplier<Node>)` | Q7용 팩토리 등록 |
| `setWiring(Consumer<List<Node>>)` | Q7용 연결 설정 함수 등록 |
| `run()` | Q7용 — 팩토리로 노드 생성 후 협력적 실행 |

---

## Q6 — 다중 Flow 독립 실행 (20점)

**구현 파일:** Q5의 `Flow` 클래스 재사용

2개의 독립된 Flow 인스턴스를 동시에 실행한다.

```java
Flow flow1 = new Flow("flow-1");
flow1.addNode(fib1).addNode(col1).addConnection(c1).start();

Flow flow2 = new Flow("flow-2");
flow2.addNode(fib2).addNode(col2).addConnection(c2).start();
```

### 검증

- 두 Flow 모두 피보나치 수열 5건 이상 수집
- 두 수열 모두 `[1, 1, 2, 3, 5, ...]` 로 시작

---

## Q7 — Flow implements Runnable, 단일 인스턴스, 다중 Thread (20점)

**구현 파일:** Q5의 `Flow` 클래스 재사용 (`run()` 구현)

Flow 인스턴스 하나를 여러 Thread에 전달한다.  
각 Thread가 `run()`을 호출하면 팩토리로 새 노드 인스턴스를 생성하여  
현재 스레드에서 협력적으로 실행한다.

```java
Flow flow = new Flow("fib-flow");       // 인스턴스 하나
flow.addNodeFactory(() -> new FibonacciNode("fib", 100))
    .setWiring(nodes -> {
        // run() 호출마다 실행 — Thread.currentThread()로 호출 스레드 식별 가능
        String threadName = Thread.currentThread().getName();
        Nodes.CollectorNode col = Nodes.collector("col-" + threadName, results);
        Connection c = new Connection("c-" + threadName);
        nodes.get(0).setOutput(c);   // FibonacciNode → connection
        col.setInbound(c);           // connection → CollectorNode
        nodes.add(col);              // 협력적 루프에 포함
    });

Thread t1 = new Thread(flow, "pipeline-1");  // 같은 flow
Thread t2 = new Thread(flow, "pipeline-2");  // 같은 flow
t1.start(); t2.start();
// t1: run() → 새 FibonacciNode A → 1,1,2,3,5,...
// t2: run() → 새 FibonacciNode B → 1,1,2,3,5,...
```

### run() 구현 포인트

```java
@Override
public void run() {
    // 1. 팩토리로 신선한 노드 인스턴스 생성
    List<Node> freshNodes = nodeFactories.stream()
                                         .map(Supplier::get)
                                         .collect(toList());
    // 2. 연결 설정 (wiring에서 nodes.add()로 추가 노드 포함 가능)
    wiring.accept(freshNodes);

    // 3. 협력적 실행 루프 — execute()가 단일 스텝이므로 가능
    while (!Thread.currentThread().isInterrupted()) {
        for (Node node : freshNodes) node.execute();
    }
}
```

### 검증

- 두 Thread 모두 결과 생성 (파이프라인 2개)
- 각 수열이 `1, 1, 2, 3, 5` 로 시작

---

## Q8 — 싱글스레드 딜레이 플로우 (20점)

**구현 파일:** `q8/SingleThreadDelayNode.java`, `q8/SingleThreadFlow.java`

Q4와 동일한 구조를 **단일 ScheduledExecutorService 스레드**로 구현한다.

### Q4 vs Q8

| | Q4 멀티스레드 | Q8 싱글스레드 |
|-|--------------|--------------|
| 실행 모델 | 노드마다 Thread | 단일 ScheduledExecutorService |
| 메시지 수신 | `receive()` 블로킹 | `tryReceive()` 논블로킹 |
| 대기 방법 | `Thread.sleep()` 스레드 점유 | `scheduler.schedule()` 콜백 예약 |

### SingleThreadDelayNode

```java
@Override
public void execute() throws InterruptedException {
    Message msg = tryReceive();   // 논블로킹 — 없으면 즉시 return
    if (msg == null) return;
    scheduler.schedule(           // 스레드를 점유하지 않음
        () -> { try { emit(msg); } catch (InterruptedException e) {} },
        delayMs, TimeUnit.MILLISECONDS
    );
}
```

### SingleThreadFlow

```java
SingleThreadFlow stFlow = new SingleThreadFlow("st-flow");
stFlow.addNode(source, 500)   // 500ms마다 execute() 호출
      .addNode(fanOut, 50)    // 50ms마다 폴링
      .addNode(delay,  50)
      .addNode(colA,   50)
      .addNode(colB,   50)
      .start();
```

내부: `scheduler.scheduleAtFixedRate(node.asRunnable(), 0, periodMs, MILLISECONDS)`

---

## Q9 — HTTP Server 플로우 (20점)

**구현 파일:** `q9/HttpListenerNode.java`, `q9/HttpResponseNode.java`

```
[HTTP Client] ──GET /path──▶ HttpListenerNode ──▶ HttpResponseNode ──▶ [HTTP Client]
```

### HttpListenerNode

```java
// execute() 안에서 서버 시작 후 isRunning() 동안 sleep(100) 대기
HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
server.createContext(context, exchange -> {
    String path = exchange.getRequestURI().getPath();
    emit(Message.of("echo: " + path, Map.of(
        "method", exchange.getRequestMethod(),
        "path",   path,
        "exchange", exchange    // HttpResponseNode가 응답 전송에 사용
    )));
});
server.start();
// stop() 오버라이드: super.stop() + server.stop(0)
```

### HttpResponseNode

```java
HttpExchange ex    = (HttpExchange) msg.getHeader("exchange");
byte[]       bytes = msg.getPayloadAsString().getBytes(StandardCharsets.UTF_8);
ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
ex.sendResponseHeaders(200, bytes.length);
try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
```

### 검증

```
GET /hello → 200 OK, body contains "/hello"
GET /world → 200 OK, body contains "/world"
```

---

## 채점 기준 요약

| 문제 | 구현 파일 | 점수 | 서브테스트 |
|------|-----------|------|-----------|
| Q1 | ConsoleInputNode, ConsoleOutputNode | 10점 | Q1-A(5), Q1-B(5) |
| Q2 | TypeRouterNode, IncrementNode, UpperCaseNode | 35점 | Q2-A(8), Q2-B(7), Q2-C(10), Q2-D(10) |
| Q3 | FibonacciNode | 20점 | Q3-A(5), Q3-B(5), Q3-C(10) |
| Q4 | PeriodicSourceNode, FanOutNode, DelayNode | 30점 | Q4-A(8), Q4-B(8), Q4-C(8), Q4-통합(6) |
| Q5 | Flow | 20점 | Q5-A(5), Q5-B(10), Q5-C(5) |
| Q6 | Flow 재사용 | 20점 | Q6-A(10), Q6-B(10) |
| Q7 | Flow.run() 구현 | 20점 | Q7-A(10), Q7-B(10) |
| Q8 | SingleThreadDelayNode, SingleThreadFlow | 20점 | Q8-A(10), Q8-B(10) |
| Q9 | HttpListenerNode, HttpResponseNode | 20점 | Q9-A(10), Q9-B(10) |
| **합계** | | **175점** | |

---

## 실행 방법

```bash
# 전체 테스트
mvn test

# 문제별 Nested 클래스 실행
mvn test -Dtest="ExamTest\$Q1Tests"
mvn test -Dtest="ExamTest\$Q2Tests"
mvn test -Dtest="ExamTest\$Q3Tests"
mvn test -Dtest="ExamTest\$Q4Tests"
mvn test -Dtest="ExamTest\$Q5Tests"
mvn test -Dtest="ExamTest\$Q6Tests"
mvn test -Dtest="ExamTest\$Q7Tests"
mvn test -Dtest="ExamTest\$Q8Tests"
mvn test -Dtest="ExamTest\$Q9Tests"

# 특정 서브테스트
mvn test -Dtest="ExamTest\$Q3Tests#q3c_sequence"
mvn test -Dtest="ExamTest\$Q8Tests#q8a_nonBlocking"
```
