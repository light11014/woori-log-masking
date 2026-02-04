package logmasking;

/**
 * 금융권 표준 마스킹 프리셋
 * 
 * 사용자가 정규식을 몰라도 사용할 수 있도록 자주 사용되는 마스킹 규칙을 미리 정의
 */
public enum MaskingPreset {
	/**
	 * 카드번호: 1234-5678-9012-3456 → 1234-****-****-3456
	 */
	CARD_NUMBER("\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}", MaskingStrategy.PARTIAL, "4-4"),

	/**
	 * 주민등록번호: 901234-1234567 → 901234-****
	 */
	SSN("\\d{6}[- ]?\\d{7}", MaskingStrategy.PARTIAL, "6-0"),

	/**
	 * 계좌번호: 110-12-345678 → 110-****
	 */
	ACCOUNT_NUMBER("\\d{3}[- ]?\\d{2}[- ]?\\d{4,6}", MaskingStrategy.PARTIAL, "3-0"),

	/**
	 * 전화번호: 010-1234-5678 → 010-****-5678
	 */
	PHONE("01[0-9][- ]?\\d{4}[- ]?\\d{4}", MaskingStrategy.PARTIAL, "3-4"),

	/**
	 * 이메일: user@example.com → us**@example.com
	 */
	EMAIL("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", MaskingStrategy.EMAIL, null),

	/**
	 * 비밀번호: password=secret123 → ****
	 */
	PASSWORD("(password|pwd|passwd|pass)[=:\\s]+\\S+", MaskingStrategy.FULL, null);

	private final String regex;
	private final MaskingStrategy strategy;
	private final String param;

	MaskingPreset(String regex, MaskingStrategy strategy, String param) {
		this.regex = regex;
		this.strategy = strategy;
		this.param = param;
	}

	public MaskingPattern createPattern() {
		return new MaskingPattern(regex, strategy, param);
	}

	public String getRegex() {
		return regex;
	}
}