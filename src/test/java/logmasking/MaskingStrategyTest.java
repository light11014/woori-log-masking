package logmasking;

// 1. JUnit 5용 Assertions로 통일 (가장 중요)
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import exception.InvalidMaskingParameterException;

class MaskingStrategyTest {
    
    @ParameterizedTest
    @ValueSource(strings = { "a-b", "3-", "-2", "3-2-1", ""})
    @NullSource
    @DisplayName("예외 시나리오: 파라미터가 형식에 맞지 않는 경우, InvalidMaskingParameterException가 발생한다.")
    void invalidParam_shouldThrowException(String param) {
        String value = "010-1234-5678";

        // when
        Executable executable = () -> MaskingStrategy.PARTIAL.mask(value, param);
    
        // then
        assertThrows(InvalidMaskingParameterException.class, executable);
    }
    
    @Test
    @DisplayName("시나리오 1: 실제 카드 번호 포맷에서 하이픈 위치를 유지하며 분절 마스킹한다")
    void testScenario1_CardNumber() {
        // Given
        String value = "1234-5678-9012-3456";
        String param = "6-4";
        String expected = "1234-56****-****-3456";

        // When
        String actual = MaskingStrategy.PARTIAL.mask(value, param);

        // Then (JUnit 5 순서: expected, actual, message)
        assertEquals(expected, actual, "하이픈을 만날 때마다 마스킹이 새로 시작되어 분절된 형태로 출력되어야 합니다.");
    }

    @ParameterizedTest(name = "value={0}, param={1}")
    @CsvSource({
            "'1234', '2-2'",
            "'123456', '5-3'",
            "'123-456', '4-2'",
            "'123-456', '3-3'",
    })
    @DisplayName("시나리오 2:유효 문자의 총 개수가 노출 설정값의 합(prefix + suffix)보다 작거나 같으면, "
            + "InvalidMaskingParameterException 발생")
    void mask_invalidPrefixAndSuffix_throwsException(String value, String param) {
        assertThrows(
                InvalidMaskingParameterException.class,
                () -> MaskingStrategy.PARTIAL.mask(value, param)
        );
    }

    @Test
    @DisplayName("시나리오 3: 특수문자가 없는 연속된 데이터는 별표(****)를 단 한 번만 출력한다")
    void testScenario3_SingleMasking() {
        // Given
        String value = "1234567890";
        String param = "2-2";
        String expected = "12****90";

        // When
        String actual = MaskingStrategy.PARTIAL.mask(value, param);

        // Then (JUnit 5 순서: expected, actual, message)
        assertEquals(expected, actual, "마스킹 영역 중간에 특수문자가 없으면 별표 덩어리는 하나만 존재해야 합니다.");
    }
}