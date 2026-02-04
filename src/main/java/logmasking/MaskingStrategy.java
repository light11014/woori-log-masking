package logmasking;

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
            if (param == null) return "****";
            String[] params = param.split("-");
            int prefixLen = Integer.parseInt(params[0]);
            int suffixLen = params.length > 1 ? Integer.parseInt(params[1]) : 0;
            String cleaned = value.replaceAll("[^a-zA-Z0-9]", "");
            
            if (cleaned.length() <= prefixLen + suffixLen) return "****";
            
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
    
    EMAIL {
        @Override
        public String mask(String value, String param) {
            int atIndex = value.indexOf('@');
            if (atIndex <= 0) return "****";
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
    };

    public abstract String mask(String value, String param);
}