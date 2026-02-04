# Log Masking Library

금융권 로그에서 민감정보를 자동으로 마스킹하는 Logback Converter 라이브러리입니다.

---

## 빠른 시작

### 1. 의존성 추가
JAR 직접 추가
- log-masking-1.0.0.jar
- logback-classic-1.4.5.jar
- logback-core-1.4.5.jar
- slf4j-api-2.0.9.jar

---

### 2. Logback 설정
logback.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Converter 등록 -->
    <conversionRule conversionWord="mask"
                    converterClass="logmasking.MaskingConverter" />

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- %msg를 그대로 사용 -->
            <pattern>%d{HH:mm:ss} - %mask%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>

</configuration>
```

---

### 3. 사용 예시

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentTest {
    private static final Logger log = LoggerFactory.getLogger(PaymentTest.class);

    public static void main(String[] args) {
        log.info("결제 요청: 카드={}, 금액={}", "1234-5678-9012-3456", 10000);
        log.info("고객 정보: 주민번호={}, 연락처={}", 
                 "901234-1234567", "010-1234-5678");
    }
}
```

출력 결과

```
12:34:56 - 결제 요청: 카드=1234-****-****-3456, 금액=10000
12:34:56 - 고객 정보: 주민번호=901234-****, 연락처=010-****-5678
```

---

## 사용법

### 1. 기본 사용 (자동 마스킹)

```xml
<pattern>%d - %mask%n</pattern>
```

**자동으로 마스킹되는 항목:**
- 카드번호 (CARD_NUMBER): `1234-****-****-3456`
- 주민등록번호 (SSN): `901234-****`
- 계좌번호 (ACCOUNT_NUMBER): `110-****`
- 전화번호 (PHONE): `010-****-5678`
- 이메일 (EMAIL): `us**@example.com`
- 비밀번호 (PASSWORD): `****`

---

### 2. 선택적 마스킹

```xml
<pattern>%d - %mask{CARD_NUMBER, PHONE}%n</pattern>
```

**특정 항목만 마스킹**

---

### 3. 프리셋 재정의 (마스킹 위치 변경)

```xml
<pattern>%d - %mask{PHONE:PARTIAL:7-0}%n</pattern>
```

**파라미터 형식:** `앞자리-뒤자리`

예시

| 설정 | 입력 | 출력 |
|------|------|------|
| `PHONE` (기본) | `010-1234-5678` | `010-****-5678` |
| `PHONE:PARTIAL:7-0` | `010-1234-5678` | `010-1234-****` |
| `PHONE:PARTIAL:3-0` | `010-1234-5678` | `010-****` |
| `PHONE:FULL` | `010-1234-5678` | `****` |

카드번호 예시

| 설정 | 입력 | 출력 |
|------|------|------|
| `CARD_NUMBER` (기본) | `1234-5678-9012-3456` | `1234-****-****-3456` |
| `CARD_NUMBER:PARTIAL:6-4` | `1234-5678-9012-3456` | `1234-56****-3456` |
| `CARD_NUMBER:PARTIAL:8-4` | `1234-5678-9012-3456` | `1234-5678-****-3456` |
| `CARD_NUMBER:FULL` | `1234-5678-9012-3456` | `****` |

---

### 4. 여러 규칙 조합

```xml
<pattern>%d - %msg{
    PHONE:PARTIAL:7-0,
    CARD_NUMBER:PARTIAL:6-4,
    SSN:FULL
}%n</pattern>
```

**쉼표(,)로 여러 규칙 구분**

---

### 5. 커스텀 규칙 추가

형식

```
이름:정규식:전략:파라미터
```

예시

```xml
<pattern>%d - %msg{
    CARD_NUMBER,
    EMPLOYEE_ID:EMP\d{6}:PARTIAL:3-0,
    ORDER_NO:ORD\d{10}:PARTIAL:4-2
}%n</pattern>
```

**결과:**
- `EMP123456` → `EMP123****`
- `ORD1234567890` → `ORD1234****90`

---

## 프리셋 목록

| 프리셋 | 정규식 | 기본 동작 | 예시 |
|--------|--------|----------|------|
| `CARD_NUMBER` | `\d{4}[- ]?\d{4}[- ]?\d{4}[- ]?\d{4}` | 앞4/뒤4 표시 | `1234-****-****-3456` |
| `SSN` | `\d{6}[- ]?\d{7}` | 앞6자리만 | `901234-****` |
| `ACCOUNT_NUMBER` | `\d{3}[- ]?\d{2}[- ]?\d{4,6}` | 은행코드만 | `110-****` |
| `PHONE` | `01[0-9][- ]?\d{4}[- ]?\d{4}` | 앞3/뒤4 표시 | `010-****-5678` |
| `EMAIL` | `[a-zA-Z0-9._%+-]+@[...]` | 아이디 일부 | `us**@example.com` |
| `PASSWORD` | `(password\|pwd\|...)[=:\s]+\S+` | 전체 마스킹 | `****` |

---

## 마스킹 전략

### FULL
전체를 `****`로 마스킹

```xml
<pattern>%msg{SSN:FULL}%n</pattern>
```
- `901234-1234567` → `****`

---

### PARTIAL
앞/뒤 일부만 표시, 중간 마스킹

**파라미터:** `앞자리수-뒤자리수`

```xml
<pattern>%msg{PHONE:PARTIAL:7-0}%n</pattern>
```
- `010-1234-5678` → `010-1234-****`

**계산 방법:**
- 특수문자 제외한 숫자/문자만 카운트
- `010-1234-5678` → `0101234567` (10자리)
- `7-0` → 앞 7자리(`0101234`) + 뒤 0자리 표시
- 결과: `010-1234-****`

---

### EMAIL
이메일 아이디 일부만 마스킹

```xml
<pattern>%msg{EMAIL}%n</pattern>
```
- `user@example.com` → `us**@example.com`

---

## 환경별 설정

### 개발 환경 - 최소 마스킹
```xml
<springProfile name="local">
    <appender name="CONSOLE">
        <encoder>
            <!-- 디버깅 편의를 위해 많이 표시 -->
            <pattern>%d - %msg{CARD_NUMBER:PARTIAL:12-4}%n</pattern>
        </encoder>
    </appender>
</springProfile>
```

### 스테이징 - 선택적 마스킹
```xml
<springProfile name="stage">
    <appender name="CONSOLE">
        <encoder>
            <pattern>%d - %msg{CARD_NUMBER, SSN}%n</pattern>
        </encoder>
    </appender>
</springProfile>
```

### 프로덕션 - 전체 마스킹
```xml
<springProfile name="prod">
    <appender name="FILE">
        <encoder>
            <!-- 기본 프리셋 전체 적용 -->
            <pattern>%d - %msg%n</pattern>
        </encoder>
    </appender>
</springProfile>
```
