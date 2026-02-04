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
}