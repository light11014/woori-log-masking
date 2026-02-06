package logmasking;

public enum MaskingPreset {
	CARD_NUMBER("\\d{4}-\\d{4}-\\d{4}-\\d{4}", MaskingStrategy.PARTIAL, "4-4"),
	SSN("\\d{6}-\\d{7}", MaskingStrategy.PARTIAL, "6-0"),
	ACCOUNT_NUMBER("\\d{3}-\\d{2}-\\d{4,6}", MaskingStrategy.PARTIAL, "3-0"),
	PHONE("01[0-9]-\\d{4}-\\d{4}", MaskingStrategy.PARTIAL, "3-4"),
	EMAIL("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", MaskingStrategy.EMAIL, null),
    
    /**
     * PASSWORD 전략을 WARNING으로 수정
     */
    PASSWORD("(password|pwd|passwd|pass)[=:\\s]+\\S+", MaskingStrategy.WARNING, null),
    /**
     * 사용자 이름: 홍길동 → 홍*동, 홍길 -> 홍*, hong → h**g
     */
    USER_NAME("(user|name|username|userName)[=:\\s]+\\s*[가-힣a-zA-Z]{2,}(?=\\s|,|$)",
              MaskingStrategy.USER_NAME, null);

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