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
		patterns = new ArrayList<>();

		if (options == null || options.isEmpty()) {
			// 옵션 없음 → 기본 프리셋 전체 적용
			addDefaultPresets();
		} else {
			// 옵션 있음 → 지정된 것만 적용
			for (String option : options) {
				addPattern(option.trim());
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
	}

	private void addPattern(String option) {
		String[] parts = option.split(":");

		if (parts.length == 1) {
			// 단순 프리셋 이름만: "CARD_NUMBER"
			try {
				MaskingPreset preset = MaskingPreset.valueOf(parts[0].toUpperCase());
				patterns.add(preset.createPattern());
				return;
			} catch (IllegalArgumentException e) {
				addError("알 수 없는 프리셋: " + parts[0]);
				return;
			}
		}

		// 프리셋 재정의 또는 커스텀 규칙
		String name = parts[0];

		// 프리셋 재정의 확인: "PHONE:PARTIAL:0,4"
		try {
			MaskingPreset preset = MaskingPreset.valueOf(name.toUpperCase());

			// 프리셋의 정규식 사용 + 새로운 전략
			String regex = preset.getRegex();
			MaskingStrategy strategy = MaskingStrategy.valueOf(parts[1].toUpperCase());
			String param = parts.length >= 3 ? parts[2] : null;

			patterns.add(new MaskingPattern(regex, strategy, param));
			return;
		} catch (IllegalArgumentException e) {
			// 프리셋 아님 → 커스텀 규칙으로 처리
		}

		// 커스텀 규칙: "MY_RULE:정규식:전략:파라미터"
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

			patterns.add(new MaskingPattern(regex, strategy, param));
		}
	}
}