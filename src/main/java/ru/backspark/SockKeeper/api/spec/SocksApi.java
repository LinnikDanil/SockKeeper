package ru.backspark.SockKeeper.api.spec;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.backspark.SockKeeper.dto.SocksRsDto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Tag(name = "Socks API", description = "API для управления складом носков")
public interface SocksApi {

    @Operation(
            summary = "Регистрация прихода носков",
            description = "Добавляет указанное количество носков на склад. Если носки с такими параметрами уже есть, увеличивает их количество.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Операция успешна"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
            }
    )
    @PostMapping("/api/socks/income")
    void registerIncome(
            @RequestParam @NotBlank @Parameter(description = "Цвет носков", example = "red") String color,
            @RequestParam @Min(0) @Max(100) @Parameter(description = "Процентное содержание хлопка", example = "50") Integer cottonPart,
            @RequestParam @Min(1) @Parameter(description = "Количество носков", example = "100") Integer quantity
    );

    @Operation(
            summary = "Регистрация отпуска носков",
            description = "Уменьшает количество носков на складе. Если недостаточно носков с указанными параметрами, возвращает ошибку.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Операция успешна"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Носки с указанными параметрами не найдены", content = @Content)
            }
    )
    @PostMapping("/api/socks/outcome")
    void registerOutcome(
            @RequestParam @NotBlank @Parameter(description = "Цвет носков", example = "red") String color,
            @RequestParam @Min(0) @Max(100) @Parameter(description = "Процентное содержание хлопка", example = "50") Integer cottonPart,
            @RequestParam @Min(1) @Parameter(description = "Количество носков", example = "50") Integer quantity
    );

    @Operation(
            summary = "Получение общего количества носков",
            description = """
                    Этот метод возвращает список носков, соответствующих указанным фильтрам
                    Вы можете фильтровать носки по цвету, диапазону процентного содержания хлопка и указать поле для сортировки по возрастанию
                                    
                    Пример фильтров:
                    - Цвет: красный
                    - Диапазон содержания хлопка: от 30% до 70%
                    - Сортировка: по цвету или проценту хлопка
                                    
                    Параметры:
                    - `color` (опционально): Фильтр по цвету носков. Пример: `red`.
                    - `minCottonPart` (опционально): Минимальное значение процента содержания хлопка. Пример: 30.
                    - `maxCottonPart` (опционально): Максимальное значение процента содержания хлопка. Пример: 70.
                    - `sortBy` (опционально): Поле для сортировки результата. Доступные значения: color, cottonPart.
                                    
                    Если параметры фильтрации не указаны, метод вернет полный список носков.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Операция успешна", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocksRsDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
            }
    )
    @GetMapping("/api/socks")
    List<SocksRsDto> getSocks(
            @RequestParam(required = false) @Parameter(description = "Цвет носков", example = "red") String color,
            @RequestParam(required = false) @Min(0) @Max(100) @Parameter(description = "Минимальный процент содержания хлопка", example = "30") Integer minCottonPart,
            @RequestParam(required = false) @Min(0) @Max(100) @Parameter(description = "Максимальный процент содержания хлопка", example = "70") Integer maxCottonPart,
            @RequestParam(required = false) @Parameter(description = "Поле для сортировки (color, cottonPart)", example = "color") String sortBy
    );

    @Operation(
            summary = "Обновление данных носков",
            description = "Позволяет обновить параметры носков по их идентификатору.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Операция успешна", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocksRsDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Носки с указанным идентификатором не найдены", content = @Content)
            }
    )
    @PutMapping("/api/socks/{id}")
    SocksRsDto updateSocks(
            @PathVariable("id") @Parameter(description = "Идентификатор носков", example = "14") Long id,
            @RequestParam @NotBlank @Parameter(description = "Цвет носков", example = "blue") String color,
            @RequestParam @Min(0) @Max(100) @Parameter(description = "Процентное содержание хлопка", example = "70") Integer cottonPart,
            @RequestParam @Min(1) @Parameter(description = "Количество носков", example = "30") Integer quantity
    );

    @Operation(
            summary = "Загрузка партий носков из файла",
            description = "Позволяет загрузить партии носков через CSV-файл. В файле должны быть указаны цвет, процент хлопка и количество.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл успешно обработан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка при обработке файла", content = @Content)
            }
    )
    @PostMapping("/api/socks/batch")
    void uploadSocksBatch(
            @RequestParam("file") @Parameter(description = "CSV файл с партией носков", example = "новогодняя партия.csv") MultipartFile file);
}
