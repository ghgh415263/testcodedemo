
# 테스트 코드 작성 가이드 (with Examples)

* 각각의 테스트 메서드는 **독립적으로 동작**해야 합니다.
* 테스트는 외부 환경(API, DB, 파일 등)에 영향을 받지 않아야 합니다.
* 실행 순서와 무관하게 항상 **같은 결과**가 나와야 합니다.

<br>


# 테스트 대역(Test Double) 종류

| 종류   | 설명                      | 예시               |
| ---- | ----------------------- | ---------------- |
| Stub | 미리 정해진 값을 반환하는 객체       | 고정된 값 반환 Stub    |
| Fake | 단순 로직이 포함된 실제 동작 객체     | 인메모리 Repository  |
| Spy  | 메서드 호출 여부/횟수 검증         | 이메일 발송 여부 검증 Spy |
| Mock | 행위 기반 검증 객체 (과도한 사용 지양) | Mockito mock     |

> 💡 **Mock는 최소한으로 사용**하고, 가능한 경우 **Stub → Fake → Spy** 순으로 대체하는 게 유지보수에 더 유리함.

<br>

## Test Double 예시 코드

### ✔ Stub 예시

```java
class UserPointStub implements UserPointRepository {
    @Override
    public int getPoint(Long userId) {
        return 100; // 항상 100 리턴
    }
}
```



### ✔ Fake 예시 (인메모리 Repository)

```java
class FakeUserRepository implements UserRepository {

    private final Map<Long, User> store = new HashMap<>();

    @Override
    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
```

### ✔ Spy 예시

```java
class EmailSenderSpy implements EmailSender {

    private int sendCount = 0;
    private String lastEmail;

    @Override
    public void send(String email) {
        sendCount++;
        lastEmail = email;
    }

    public void assertSent(String expectedEmail) {
        assertEquals(1, sendCount);
        assertEquals(expectedEmail, lastEmail);
    }
}
```

사용 예:

```java
@Test
void email_is_sent_when_order_complete() {
    EmailSenderSpy spy = new EmailSenderSpy();
    OrderService service = new OrderService(spy);

    service.completeOrder("user@test.com");

    spy.assertSent("user@test.com");
}
```

### ✔ Mock 예시 (Mockito)

```java
@Test
void mockito_example() {
    EmailSender sender = mock(EmailSender.class);
    OrderService service = new OrderService(sender);

    service.completeOrder("user@test.com");

    verify(sender, times(1)).send("user@test.com");
}
```
<br>

# 🔧 테스트하기 어려운 코드와 해결 방법

| 문제 상황                   | 해결 방법                              |
| ----------------------- | ---------------------------------- |
| 의존 객체를 클래스 내부에서 new     | → DI(Dependency Injection) 로 외부 주입 |
| 실행 시점에 따라 결과 달라지는 now() | → Clock/Provider 로 분리              |
| 하나의 클래스가 여러 책임을 가짐      | → 단일 책임 원칙(SRP) 적용                 |
| 외부 라이브러리에 직접 의존         | → Adapter/Wrapper 로 감싸기            |

## 🕒 now() 테스트 가능하게 만들기

### ❌ 나쁜 예

```java
public LocalDateTime now() {
    return LocalDateTime.now();
}
```

### ✔ 좋은 예 (Clock 사용)

```java
class TimeProvider {
    private final Clock clock;

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
```

테스트:

```java
@Test
void fixed_clock_test() {
    Clock fixed = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
    TimeProvider provider = new TimeProvider(fixed);

    assertEquals(LocalDateTime.parse("2024-01-01T00:00:00"), provider.now());
}
```
<br>

# 테스트 가능한 설계 가이드

### 1️⃣ 하드 코딩된 상수는 외부에서 주입하기

#### ❌ 잘못된 코드

```java
class FileUploader {
    public void upload(byte[] file) {
        Path path = Paths.get("/var/data/files"); // 하드코딩
    }
}
```

#### ✔ 개선된 코드

```java
class FileUploader {
    private final Path basePath;

    public FileUploader(Path basePath) {
        this.basePath = basePath;
    }
}
```

### 2️⃣ 시간/랜덤 생성은 Provider로 분리

```java
interface RandomGenerator {
    String generate();
}

class UuidGenerator implements RandomGenerator {
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
```

테스트 Stub:

```java
class StubRandom implements RandomGenerator {
    public String generate() {
        return "fixed-uuid";
    }
}
```

### 3️⃣ 외부 API 추상화 (xxxClient)

```java
public interface PaymentClient {
    PaymentResult requestPayment(PaymentRequest req);
}
```

테스트에서는 Stub/Fake로 대체.

### 4️⃣ 외부 라이브러리는 Adapter 로 감싸기

예: BCrypt

```java
interface PasswordEncoder {
    String encode(String raw);
}

class BCryptPasswordEncoderAdapter implements PasswordEncoder {
    public String encode(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }
}
```

<br>

# 테스트 종류 & 예시 코드

## 1️⃣ 단위 테스트 (Unit Test)

* 클래스/메서드 같은 **작은 단위** 테스트
* 외부 의존성은 모두 Stub/Fake/Mock 대체

```java
class PriceCalculatorTest {

    @Test
    void calculate_discount_price() {
        PriceCalculator calc = new PriceCalculator();

        int result = calc.discount(10000, 10);

        assertEquals(9000, result);
    }
}
```

## 2️⃣ 통합 테스트 (Integration Test)

* 실제 DB, 실제 스프링 컨텍스트 사용
* API 호출 같은 외부 시스템은 Stub 사용

```java
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void save_and_find() {
        User user = new User("kim");
        userRepository.save(user);

        User found = userRepository.findById(user.getId()).get();

        assertEquals("kim", found.getName());
    }
}
```

## 3️⃣ E2E 테스트 (End-to-End Test)

* 전체 사용자 시나리오 흐름을 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserE2ETest {

    @Autowired
    MockMvc mvc;

    @Test
    void user_registration_flow() throws Exception {
        mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"kim\"}"))
            .andExpect(status().isCreated());
    }
}
```

