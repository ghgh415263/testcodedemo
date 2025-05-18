package com.example.testcodedemo.example.ui;

/**
 * API 응답을 표현하는 제네릭 레코드 클래스입니다.
 *
 * @param <T> 응답 데이터 타입
 *
 * @param success 요청 성공 여부 (true: 성공, false: 실패)
 * @param code 응답 코드 (예: "SUCCESS", 에러 코드 등)
 * @param message 응답 메시지 (성공 또는 실패에 대한 설명)
 * @param data 성공 시 반환되는 데이터, 실패 시 null
 *
 * <p>정적 팩토리 메서드를 제공하여 성공 및 실패 응답을 간편하게 생성할 수 있습니다.</p>
 *
 */
public record ApiResponse<T>(boolean success, String code, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

}
