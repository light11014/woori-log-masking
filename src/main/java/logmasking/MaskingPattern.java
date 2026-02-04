package logmasking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 마스킹 패턴
 * 정규식으로 민감정보를 찾아서 마스킹 전략에 따라 처리
 */
public class MaskingPattern {
    private final Pattern pattern;
    private final MaskingStrategy strategy;
    private final String param;

    public MaskingPattern(String regex, MaskingStrategy strategy, String param) {
        this.pattern = Pattern.compile(regex);
        this.strategy = strategy;
        this.param = param;
    }

    public String mask(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String matched = matcher.group();
            String masked = strategy.mask(matched, param);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}