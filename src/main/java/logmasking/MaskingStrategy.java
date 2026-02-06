package logmasking;

import exception.InvalidMaskingParameterException;

public enum MaskingStrategy {
	FULL {
		@Override
		public String mask(String value, String param) {
			return "****";
		}
	},

	PARTIAL {
		@Override
		public String mask(String value, String param) {
			int[] parsed = parseAndValidateParam(param);
		    int prefixLen = parsed[0];
		    int suffixLen = parsed[1];
		    
			String cleaned = value.replaceAll("[^a-zA-Z0-9]", "");

			if (cleaned.length() <= prefixLen + suffixLen)
				return "****";

			int visibleChars = 0;
			StringBuilder result = new StringBuilder();
			boolean inMaskingZone = false;

			for (char c : value.toCharArray()) {
				if (Character.isLetterOrDigit(c)) {
					if (visibleChars < prefixLen || visibleChars >= cleaned.length() - suffixLen) {
						result.append(c);
						inMaskingZone = false;
					} else {
						if (!inMaskingZone) {
							result.append("****");
							inMaskingZone = true;
						}
					}
					visibleChars++;
				} else {
					result.append(c);
					inMaskingZone = false;
				}
			}
			return result.toString();
		}
	},

	NONE {
		@Override
		public String mask(String value, String param) {
			return value;
		}
	},

	/**
	 * 이메일 마스킹: 아이디 일부만 표시 user@example.com → us**@example.com
	 */
	EMAIL {
		@Override
		public String mask(String value, String param) {
			int atIndex = value.indexOf('@');
			if (atIndex <= 0)
				return "****";
			String localPart = value.substring(0, atIndex);
			String domain = value.substring(atIndex);
			int visibleLen = Math.min(2, localPart.length() / 2);
			return localPart.substring(0, visibleLen) + "****" + domain;
		}
	},

	/**
	 * 추가된 전략: 원본 데이터 노출을 완전히 차단하고 경고 문구 출력
	 */
	WARNING {
		@Override
		public String mask(String value, String param) {
			System.out.println("[SYSTEM] WARNING 전략이 실제로 호출되었습니다!");
			return "[SECURITY WARNING: SENSITIVE DATA BLOCKED]";
		}
	},

	/**
	 * 사용자 이름 마스킹
	 */
	USER_NAME {
		@Override
		public String mask(String value, String param) {
			// key=value 구조 분리
			int idx = value.indexOf('=');
			if (idx == -1 || idx == value.length() - 1) {
				return value; // 예상하지 못한 형식은 그대로 반환
			}

			String key = value.substring(0, idx + 1); // username=
			String name = value.substring(idx + 1).trim(); // 실제 이름

			int len = name.length();

			if (len == 2) {
				return key + name.charAt(0) + "*";
			}

			if (len == 3) {
				return key + name.charAt(0) + "*" + name.charAt(2);
			}

			if (len >= 4) {
				StringBuilder sb = new StringBuilder();
				sb.append(name.charAt(0));
				for (int i = 1; i < len - 1; i++) {
					sb.append("*");
				}
				sb.append(name.charAt(len - 1));
				return key + sb;
			}

			return key + "*";
		}
	};

	public abstract String mask(String value, String param);

	/**
	 * 마스킹 파라미터를 파싱하고 유효성을 검사한다.
	 * 
	 * @param param - 마스킹 파라미터 문자열
	 * @return int[2] - prefix와 suffix 길이 (index 0: prefix, index 1: suffix)
	 * @throws InvalidMaskingParameterException 파라미터가 null이거나 형식에 맞지 않는 경우
	 */
	private static int[] parseAndValidateParam(String param) {
		// 파라미터가 null 인경우
		if (param == null) {
			throw new InvalidMaskingParameterException("null");
		}

		String[] parts = param.split("-");
		
		// 구분자 기준 분리 결과가 2개가 아닌 경우
		if (parts.length != 2) {
			throw new InvalidMaskingParameterException(param);
		}

		try {
			// prefix / suffix가 숫자가 아닌 경우
			int prefix = Integer.parseInt(parts[0]);
			int suffix = Integer.parseInt(parts[1]);
			
			// prefix 또는 suffix가 음수인 경우
			if (prefix < 0 || suffix < 0) {
				throw new InvalidMaskingParameterException(param);
			}

			return new int[] { prefix, suffix };

		} catch (NumberFormatException e) {
			throw new InvalidMaskingParameterException(param);
		}
	}
}