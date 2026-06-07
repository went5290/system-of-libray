package com.humber.library.reader;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReaderCreateRequest(
        @NotBlank(message = "借书证号不能为空")
        @Size(max = 30, message = "借书证号不能超过 30 个字符")
        String readerNo,

        @Size(max = 30, message = "手机号不能超过 30 个字符")
        String phone,

        @Email(message = "邮箱格式不正确")
        @Size(max = 120, message = "邮箱不能超过 120 个字符")
        String email,

        @Min(value = 1, message = "最大借阅数不能小于 1")
        @Max(value = 99, message = "最大借阅数不能超过 99")
        Integer maxBorrowCount) {
}

