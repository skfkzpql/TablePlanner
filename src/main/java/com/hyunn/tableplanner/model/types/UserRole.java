package com.hyunn.tableplanner.model;

public enum UserRole {
    USER,
    ADMIN,
    PARTNER;

    /**
     * Enum 값을 문자열로 반환하는 메소드.
     *
     * @return Enum 값의 문자열 표현
     */
    public String getRoleName() {
        return this.name();
    }
}