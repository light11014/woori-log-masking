package logmasking;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import exception.InvalidMaskingParameterException;

class MaskingStrategyTest {
	
	@ParameterizedTest
	@ValueSource(strings = { "a-b", "3-", "-2", "3-2-1" })
	@DisplayName("파라미터가 형식에 맞지 않는 경우, InvalidMaskingParameterException가 발생한다.")
	void invalidParam_shouldThrowException(String param) {
	    String value = "010-1234-5678";

	    // when
		Executable executable = () -> MaskingStrategy.PARTIAL.mask(value, param);
	
		// then
		assertThrows(InvalidMaskingParameterException.class, executable);
	}

}
