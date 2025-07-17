package com.migros.courier.controller.courier.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourierRequest {
    @NotBlank(message = "Kurye adı boş olamaz.")
    String name;
}
