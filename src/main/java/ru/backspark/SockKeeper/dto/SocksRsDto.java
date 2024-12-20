package ru.backspark.SockKeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocksRsDto {
    private Long id;
    private String color;
    private Integer cottonPart;
    private Integer quantity;
}
