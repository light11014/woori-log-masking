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

<br>

### 2. Logback 설정
logback.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Converter 등록 -->
    <conversionRule conversionWord="mask"
                    converterClass="logmasking.MaskingConverter" />

    <!-- 콘솔 출력 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- %mask를 사용 (옵션 없으면 기본 프리셋 전체 적용) -->
            <pattern>%d{HH:mm:ss} - %mask%n</pattern>
        </encoder>
    </appender>

    <!-- 파일 출력 -->
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/application.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level - %mask%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

</configuration>
```

<br>

### 3. 사용 예시

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentTest {
    private static final Logger log = LoggerFactory.getLogger(PaymentTest.class);

    public static void main(String[] args) {
        log.info("결제 요청: 카드={}, 금액={}", "1234-5678-9012-3456", 10000);
        log.info("고객 정보: 주민번호={}, 연락처={}, username=홍길동", 
                 "901234-1234567", "010-1234-5678");
        log.info("이메일: user@example.com, password=secret123");
    }
}
```

출력 결과

```
12:34:56 - 결제 요청: 카드=1234-****-****-3456, 금액=10000
12:34:56 - 고객 정보: 주민번호=901234-****, 연락처=010-****-5678, username=홍*동
12:34:56 - 이메일: us**@example.com, [SECURITY WARNING: SENSITIVE DATA BLOCKED]
```
---

<br>

## 프리셋 목록

라이브러리가 자동으로 마스킹하는 민감정보 항목입니다. ([마스킹 전략](#마스킹-전략) 참고)

| 프리셋 | 설명 | 정규식 | 기본 전략 | 기본 파라미터 | 예시 |
|--------|------|--------|----------|--------------|------|
| `CARD_NUMBER` | 카드번호 | `\d{4}[- ]?\d{4}[- ]?\d{4}[- ]?\d{4}` | PARTIAL | `4-4` | `1234-****-****-3456` |
| `SSN` | 주민등록번호 | `\d{6}[- ]?\d{7}` | PARTIAL | `6-0` | `901234-****` |
| `ACCOUNT_NUMBER` | 계좌번호 | `\d{3}[- ]?\d{2}[- ]?\d{4,6}` | PARTIAL | `3-0` | `110-****` |
| `PHONE` | 전화번호 | `01[0-9][- ]?\d{4}[- ]?\d{4}` | PARTIAL | `3-4` | `010-****-5678` |
| `EMAIL` | 이메일 | `[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}` | EMAIL | null | `us**@example.com` |
| `PASSWORD` | 비밀번호 | `(password\|pwd\|passwd\|pass)[=:\s]+\S+` | WARNING | null | `[SECURITY WARNING: SENSITIVE DATA BLOCKED]` |
| `USER_NAME` | 사용자 이름 | `(user\|name\|username\|userName)[=:\s]+\s*[가-힣a-zA-Z]{2,}(?=\s\|,\|$)` | USER_NAME | null | `username=홍*동` |

<br>

---

## 사용법
이 라이브러리는 **항상 모든 기본 프리셋을 먼저 적용**한 후, 옵션으로 지정된 설정으로 **오버라이드** 또는 **추가**합니다.

```xml
<!-- 옵션 없음 → 7개 기본 프리셋 모두 적용 -->
<pattern>%mask%n</pattern>

<!-- 옵션 있음 → 기본 프리셋 + 옵션의 오버라이드/추가 -->
<pattern>%mask{PHONE:PARTIAL:7-0}%n</pattern>
```
- `%mask` 만 사용하면 **모든 프리셋**이 적용됩니다
- 옵션을 추가하면 해당 프리셋의 설정만 **덮어쓰기**합니다
- **특정 항목만 마스킹하고 싶다면** 필요 없는 항목에 `NONE` 전략을 사용하세요

<br>

### 1. 기본 사용 (모든 프리셋 자동 적용)

```xml
<pattern>%d - %mask%n</pattern>
```

위 [프리셋 목록](#프리셋-목록)의 7개 항목이 모두 자동으로 마스킹됩니다.

<br>

### 2. 선택적 마스킹
- 주민등록번호, 전화번호에만 마스킹을 적용
```xml
<pattern>%d - %mask{
    EMAIL:PARTIAL:NONE 
    CARD_NUMBER:NONE 
    ACCOUNT_NUMBER:NONE
    PASSWORD:NONE
    USER_NAME:NONE
}%n</pattern>
```

**NONE 전략을 사용**하면 해당 프리셋의 마스킹을 비활성화할 수 있습니다.

<br>

### 3. 프리셋 재정의 (마스킹 방식 변경)

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

<br>

### 4. 여러 규칙 조합

```xml
<pattern>%d - %mask{
    PHONE:PARTIAL:7-0,
    CARD_NUMBER:PARTIAL:6-4,
    SSN:FULL
}%n</pattern>
```

- 쉼표로 구분하되 **공백 없이** 작성 권장
- 각 옵션은 해당 프리셋만 오버라이드
- 지정하지 않은 프리셋은 기본값 유지

<br>

### 5. 커스텀 규칙 추가

형식

```
이름:정규식:전략:파라미터
```

예시

```xml
<pattern>%d - %mask{
    CARD_NUMBER,
    EMPLOYEE_ID:EMP\d{6}:PARTIAL:3-0,
    ORDER_NO:ORD\d{10}:PARTIAL:4-2
}%n</pattern>
```

**결과:**
- 기본 7개: CARD_NUMBER, SSN, ACCOUNT_NUMBER, PHONE, EMAIL, PASSWORD, USER_NAME (기본값 적용)
- 추가 2개: `EMP123456` → `EMP123****`, `ORD1234567890` → `ORD1234****90`

---

<br>

## 마스킹 전략

### FULL
전체를 `****`로 마스킹

```xml
<pattern>%msg{SSN:FULL}%n</pattern>
```
- `901234-1234567` → `****`

<br>

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

<br>

### WARNING
민감정보를 완전히 차단하고 경고 문구 출력

```xml
<pattern>%mask{PASSWORD}%n</pattern>
```
- `password=secret123` → `[SECURITY WARNING: SENSITIVE DATA BLOCKED]`
- 원본 데이터를 전혀 노출하지 않음

<br>

### NONE
마스킹하지 않음 (원본 유지)

```xml
<pattern>%mask{EMAIL:NONE}%n</pattern>
```
- `user@example.com` → `user@example.com`
