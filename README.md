
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

> **Mock는 최소한으로 사용**하고, 가능한 경우 **Stub → Fake → Spy** 순으로 대체하는 게 유지보수에 더 유리함.

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

# 테스트하기 어려운 코드와 해결 방법

| 문제 상황                   | 해결 방법                              |
| ----------------------- | ---------------------------------- |
| 의존 객체를 클래스 내부에서 new     | → DI(Dependency Injection) 로 외부 주입 |
| 실행 시점에 따라 결과 달라지는 now() | → Clock/Provider 로 분리              |
| 하나의 클래스가 여러 책임을 가짐      | → 단일 책임 원칙(SRP) 적용                 |
| 외부 라이브러리에 직접 의존         | → Adapter/Wrapper 로 감싸기            |

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

interface TimeProvider {
    LocalDateTime now();
}

class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
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

class StubTimeProvider implements TimeProvider {

    private final LocalDateTime fixedTime;

    public StubTimeProvider(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    @Override
    public LocalDateTime now() {
        return fixedTime;
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

### 최종정리
❌ 테스트하기 어려운 코드 (SRP 위반)

문제점:
비즈니스 로직 + 랜덤 코드 생성 + 시간 생성 + 외부 저장 로직이 한 클래스에 다 섞여 있음
랜덤 값, 현재 시간 때문에 출력 예측 못함 → 테스트 불가능
JPA에 대한 의존이 직접적으로 노출되어 있어 추후 타 기술로 바꾸기 힘듬 (타 기술로 바꾸려면 서비스 클래스까지 변경해야함)

- 코드가 랜덤 → 검증값이 매번 다름
- 시간도 매번 바뀜 → 검증 불가
- JPA에 대한 의존
```java
public class CouponService {

    private EntityManager em;

    public Coupon createCoupon(Long userId) {

        // (1) 랜덤 코드 생성, 정적 메서드 사용이라 모킹 불가능.
        String code = UUID.randomUUID().toString();

        // (2) 현재 시간 사용, 정적 메서드 사용이라 모킹 불가능.
        LocalDateTime issuedAt = LocalDateTime.now();

        // (3) 비즈니스 로직
        Coupon coupon = new Coupon(code, userId, issuedAt);

        // (4) 외부 저장소 호출 (DB)
        saveToDatabase(coupon);

        return coupon;
    }

    private void saveToDatabase(Coupon coupon) {
        em.persist(coupon);
        em.flush();
    }
}
```

✔ 테스트하기 좋게 리팩토링
- 랜덤값이나 시간을 fixed하게 설정해서 모킹 가능
- jpa 관련 기술이 서비스로 노출되지 않고 리포지토리 모킹이 쉬움움
```java
public class CouponService {

    private final RandomGenerator randomGenerator;
    private final TimeProvider timeProvider;
    private final CouponRepository couponRepository;

    public Coupon createCoupon(Long userId) {
        String code = randomGenerator.generate();
        LocalDateTime issuedAt = timeProvider.now();

        Coupon coupon = new Coupon(code, userId, issuedAt);

        couponRepository.save(coupon); // 외부에 위임

        return coupon;
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
```

## 3️⃣ E2E 테스트 (End-to-End Test)

* 전체 사용자 시나리오 흐름을 테스트

```java
```

