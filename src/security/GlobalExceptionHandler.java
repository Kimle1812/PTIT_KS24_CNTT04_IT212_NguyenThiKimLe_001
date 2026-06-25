package security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt riêng lỗi Token hết hạn (ExpiredJwtException)
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtExpiredException(ExpiredJwtException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Token của bạn đã hết hạn sử dụng. Vui lòng đăng nhập lại!");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // 2. Bắt các lỗi Token không hợp lệ khác như sai chữ ký, sai định dạng (JwtException chung)
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Token không hợp lệ hoặc đã bị chỉnh sửa!");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
}