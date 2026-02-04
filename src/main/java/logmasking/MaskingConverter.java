package logmasking;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 로그 메시지 마스킹 Converter
 * 
 * 사용법: 1. 기본 프리셋: <pattern>%msg{CARD_NUMBER}%n</pattern> 
 * 2. 프리셋 재정의: <pattern>%msg{PHONE:PARTIAL:0,4}%n</pattern> 
 * 3. 커스텀 규칙: <pattern>%msg{MY_RULE:\d{8}:PARTIAL:2,2}%n</pattern>
 */

public class MaskingConverter extends MessageConverter {

	private List<MaskingPattern> patterns;

	@Override
	public void start() {
		super.start();

		List<String> options = getOptionList();
		
		// 1. 항상 기본 프리셋 먼저 적용
	    patterns = new ArrayList<>();
	    addDefaultPresets();

	    // 2. 옵션이 있으면 기본값을 override / 추가
	    if (options != null && !options.isEmpty()) {
	        for (String option : options) {
	            addOrOverridePattern(option.trim());
	        }
	    }
	}

	@Override
	public String convert(ILoggingEvent event) {
		String message = event.getFormattedMessage();

		if (patterns.isEmpty()) {
			return message;
		}

		String result = message;
		for (MaskingPattern pattern : patterns) {
			result = pattern.mask(result);
		}

		return result;
	}

	private void addDefaultPresets() {
		patterns.add(MaskingPreset.CARD_NUMBER.createPattern());
		patterns.add(MaskingPreset.SSN.createPattern());
		patterns.add(MaskingPreset.ACCOUNT_NUMBER.createPattern());
		patterns.add(MaskingPreset.PHONE.createPattern());
		patterns.add(MaskingPreset.EMAIL.createPattern());
	
		
		patterns.add(MaskingPreset.PASSWORD.createPattern());
		patterns.add(MaskingPreset.USER_NAME.createPattern());
	}
	
	private void addOrOverridePattern(String option) {
	    MaskingPattern newPattern = parsePattern(option);

	    if (newPattern == null) {
	        return;
	    }

	    // regex 기준 override
	    patterns.removeIf(p -> p.getRegex().equals(newPattern.getRegex()));
	    patterns.add(newPattern);
	}
	
	private MaskingPattern parsePattern(String option) {
	    String[] parts = option.split(":");

	    // 단순 프리셋: "CARD_NUMBER"
	    if (parts.length == 1) {
	        try {
	            MaskingPreset preset = MaskingPreset.valueOf(parts[0].toUpperCase());
	            return preset.createPattern();
	        } catch (IllegalArgumentException e) {
	            addError("알 수 없는 프리셋: " + parts[0]);
	            return null;
	        }
	    }

	    String name = parts[0];

	    // 프리셋 재정의: "PHONE:PARTIAL:0,4"
	    try {
	        MaskingPreset preset = MaskingPreset.valueOf(name.toUpperCase());

	        String regex = preset.getRegex();
	        MaskingStrategy strategy = MaskingStrategy.valueOf(parts[1].toUpperCase());
	        String param = parts.length >= 3 ? parts[2] : null;

	        return new MaskingPattern(regex, strategy, param);
	    } catch (IllegalArgumentException ignored) {
	        // 프리셋 아님 → 커스텀
	    }

	    // 3️⃣ 커스텀 규칙: "MY_RULE:정규식:전략:파라미터"
	    if (parts.length >= 2) {
	        String regex = parts[1];
	        MaskingStrategy strategy = MaskingStrategy.FULL;
	        String param = null;

	        if (parts.length >= 3) {
	            try {
	                strategy = MaskingStrategy.valueOf(parts[2].toUpperCase());
	                param = parts.length >= 4 ? parts[3] : null;
	            } catch (IllegalArgumentException e) {
	                addError("알 수 없는 마스킹 전략: " + parts[2]);
	            }
	        }

	        return new MaskingPattern(regex, strategy, param);
	    }

	    return null;
	}

	
}