package vn.edu.bkis.common;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageResolver {
    private final MessageSource messageSource;

    // Khởi tạo bộ resolve message đa ngôn ngữ từ MessageSource của Spring.
    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // Resolve message theo locale hiện tại của request.
    public String get(String code, Object... args) {
        return get(LocaleContextHolder.getLocale(), code, args);
    }

    // Resolve message theo locale chỉ định để dùng trong các handler bảo mật.
    public String get(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
