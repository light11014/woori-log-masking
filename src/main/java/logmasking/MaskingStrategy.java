package logmasking;

/**
 * 마스킹 전략
 * 
 * logback.xml의 설정값을 실제 마스킹 로직으로 변환하는 역할
 * 
 * 예: logback.xml에서 "PARTIAL:4,4" 설정 
 *     → MaskingStrategy.PARTIAL.mask(value, "4,4") 호출
 *     → "1234-5678-9012-3456"을 "1234-****-****-3456"으로 변환
 */
public enum MaskingStrategy {
    /**
     * 전체 마스킹: 1234-5678-9012-3456 → ****
     */
    FULL {
        @Override
        public String mask(String value, String param) {
            return "****";
        }
    },
    
    /**
     * 부분 마스킹: 앞뒤 일부만 표시
     * param: "앞자리수,뒤자리수" (예: "4,4")
     * 1234-5678-9012-3456 → 1234-****-****-3456
     */
    PARTIAL {
        @Override
        public String mask(String value, String param) {
            if (param == null) {
                return "****";
            }
            
            String[] params = param.split("-");
            int prefixLen = Integer.parseInt(params[0]);
            int suffixLen = params.length > 1 ? Integer.parseInt(params[1]) : 0;
            
            // 특수문자 제외하고 숫자/문자만 카운트
            String cleaned = value.replaceAll("[^a-zA-Z0-9]", "");
            
            if (cleaned.length() <= prefixLen + suffixLen) {
                return "****";
            }
            
            // 원본 형식 유지하면서 마스킹
            int visibleChars = 0;
            StringBuilder result = new StringBuilder();
            boolean inMaskingZone = false;
            
            for (char c : value.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    if (visibleChars < prefixLen || 
                        visibleChars >= cleaned.length() - suffixLen) {
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
    
    NONE  {
        @Override
        public String mask(String value, String param) {
            return value;
        }
    },
    
    /**
     * 이메일 마스킹: 아이디 일부만 표시
     * user@example.com → us**@example.com
     */
    EMAIL {
        @Override
        public String mask(String value, String param) {
            int atIndex = value.indexOf('@');
            if (atIndex <= 0) {
                return "****";
            }
            
            String localPart = value.substring(0, atIndex);
            String domain = value.substring(atIndex);
            
            int visibleLen = Math.min(2, localPart.length() / 2);
            String masked = localPart.substring(0, visibleLen) + "****";
            
            return masked + domain;
        }
    };

    /**
     * 마스킹 수행
     * 
     * @param value 원본 값
     * @param param 마스킹 파라미터 (PARTIAL의 경우 "4,4" 등)
     * @return 마스킹된 값
     */
    public abstract String mask(String value, String param);
}