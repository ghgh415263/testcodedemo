
# 테스트 코드 작성 가이드 (with Examples)

* 각각의 테스트 메서드는 **독립적으로 동작**해야 합니다.

```java
class BadCounterTest {

    static int counter = 0;  // ❌ 모든 테스트가 공유함

    @Test
    void test1() {
        counter++;
        assertEquals(1, counter); // test1 먼저 실행되면 OK
    }

    @Test
    void test2() {
        counter++;
        assertEquals(1, counter); // ❌ 실행 순서 바뀌면 실패
    }
}

@SpringBootTest
class BadUserRepositoryTest {

    @Autowired
    EntityManager em;

    @Test
    void test1_insertUser() {
        em.persist(new User("Alice"));
        em.flush(); // DB에 저장됨

        long count = em.createQuery("select count(u) from User u", Long.class)
                       .getSingleResult();

        assertEquals(1, count); // OK
    }

    @Test
    void test2_findUser() {
        long count = em.createQuery("select count(u) from User u", Long.class)
                       .getSingleResult();

        assertEquals(0, count); // ❌ 실패 (test1이 만든 Alice가 남아 있음)
    }
}
```
* 테스트는 외부 환경(API, DB, 파일 등)에 영향을 받지 않아야 합니다.
```java
class BadApiTest {

    @Test
    void callRealApi() throws Exception {
        URL url = new URL("https://example.com/api");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        int status = conn.getResponseCode(); // ❌ 네트워크와 저api의 가용성에 의존함

        assertEquals(200, status); // ❌ 인터넷 끊거나 api가 불능상태면 실패
    }
}
```
* 실행 순서와 무관하게 항상 **같은 결과**가 나와야 합니다.
```java
class BadRandomTest {

    @Test
    void randomNumber() {
        int n = new Random().nextInt(100);
        assertEquals(50, n); // ❌ 절대 성공할 수 없음
    }
}

@SpringBootTest
class OrderDependentDbTest {  //test2부터 했으면 성공했음음

    @Autowired
    EntityManager em;

    @Test
    void test1_insert() {
        em.persist(new User("Alice"));
        em.flush(); // 실제 DB에 저장됨 (롤백 없음)

        long count =
                em.createQuery("select count(u) from User u", Long.class)
                  .getSingleResult();

        assertEquals(1, count); // ✔ 성공
    }

    @Test
    void test2_expectEmpty() {
        long count =
                em.createQuery("select count(u) from User u", Long.class)
                  .getSingleResult();

        assertEquals(0, count);  
        // ❌ 실패: test1이 먼저 실행되었으면 1이 들어있음
    }
}
```

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
class CouponServiceTest {

    @Test
    void createCoupon_success() {
        // --- Stubs (테스트 전용 고정값 생성기) ---
        RandomGenerator randomStub = () -> "fixed-uuid";
        TimeProvider timeStub = () -> LocalDateTime.of(2025, 1, 1, 12, 0);

        // Fake Repository (DB 없이 메모리에 기록)
        class FakeRepo implements CouponRepository {
            Coupon saved;
            @Override
            public void save(Coupon coupon) {
                this.saved = coupon;
            }
        }
        FakeRepo fakeRepo = new FakeRepo();

        // --- 테스트 대상 서비스 ---
        CouponService service =
                new CouponService(randomStub, timeStub, fakeRepo);

        // --- when ---
        Coupon coupon = service.createCoupon(100L);

        // --- then ---
        assertEquals("fixed-uuid", coupon.getCode());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), coupon.getIssuedAt());
        assertEquals(100L, coupon.getUserId());
        assertEquals(fakeRepo.saved, coupon); // 저장됐는지도 검증
    }
}
```

## 2️⃣ 통합 테스트 (Integration Test)

* 실제 DB나 외부시스템과 잘 연동되는지 확인하는 것

```java
@DataJpaTest
class CouponRepositoryTest {

    @Autowired
    CouponRepository couponRepository;

    @Test
    void save_and_find() {
        // given
        Coupon coupon = new Coupon(
                "ABC-123",
                100L,
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );

        // when
        Coupon saved = couponRepository.save(coupon);

        // then
        Coupon found = couponRepository.findById(saved.getId())
                                       .orElseThrow();

        assertEquals("ABC-123", found.getCode());
        assertEquals(100L, found.getUserId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), found.getIssuedAt());
    }
}
```

## 3️⃣ 컴포넌트 테스트

* 외부서비스는 대역을 사용, 카프카나 db는 도커 컴포즈를 활용하여 테스트

```java
```

