package ru.practicum.explore_with_me.ewm.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserDto {
    @NotBlank @Size(min = 2, max = 250)
    private String name;

    @NotBlank @Size(min = 6, max = 254)
    @Email
    private String email;
}
