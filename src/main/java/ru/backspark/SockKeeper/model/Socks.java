package ru.backspark.SockKeeper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "socks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Socks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String color;

    @Column(name = "cotton_part", nullable = false)
    @Min(0)
    @Max(100)
    private Integer cottonPart;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer quantity;
}
