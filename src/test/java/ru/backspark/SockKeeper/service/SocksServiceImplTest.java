package ru.backspark.SockKeeper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.backspark.SockKeeper.dto.SocksRsDto;
import ru.backspark.SockKeeper.error.exception.FileProcessingException;
import ru.backspark.SockKeeper.error.exception.InsufficientSocksInWarehouseException;
import ru.backspark.SockKeeper.error.exception.InvalidDataFormatException;
import ru.backspark.SockKeeper.error.exception.SocksNotFoundInWarehouse;
import ru.backspark.SockKeeper.model.Socks;
import ru.backspark.SockKeeper.repository.SocksRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SocksServiceImplTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private SocksService socksService;

    @Autowired
    private SocksRepository socksRepository;

    @BeforeEach
    void setUp() {
        // Очищаем таблицу перед каждым тестом
        socksRepository.deleteAll();

    }

    @Test
    @DisplayName("Регистрация новой записи о приходе носков")
    @Transactional
    void registerIncome_shouldCreateNewRecord() {
        socksService.registerIncome("red", 50, 100);

        Socks socks = socksRepository.findByColorAndCottonPart("red", 50).orElseThrow();
        assertThat(socks.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("Увеличение количества для существующей записи")
    @Transactional
    void registerIncome_shouldUpdateExistingRecord() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(50)
                .build();
        socksRepository.save(existingSocks);

        socksService.registerIncome("red", 50, 100);

        Socks updatedSocks = socksRepository.findByColorAndCottonPart("red", 50).orElseThrow();
        assertThat(updatedSocks.getQuantity()).isEqualTo(150);
    }

    @Test
    @DisplayName("Ошибка при отрицательном количестве")
    void registerIncome_shouldThrowExceptionForNegativeQuantity() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerIncome("red", 50, -100)
        );

        assertThat(exception.getMessage()).isEqualTo("Количество должно быть положительным.");
    }

    @Test
    @DisplayName("Ошибка при проценте хлопка вне диапазона (больше 100)")
    void registerIncome_shouldThrowExceptionForCottonPartAbove100() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerIncome("red", 150, 100)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Ошибка при проценте хлопка вне диапазона (меньше 0)")
    void registerIncome_shouldThrowExceptionForCottonPartBelow0() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerIncome("red", -10, 100)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Успешный отпуск носков")
    @Transactional
    void registerOutcome_shouldDecreaseQuantity() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
        socksRepository.save(existingSocks);

        socksService.registerOutcome("red", 50, 30);

        Socks updatedSocks = socksRepository.findByColorAndCottonPart("red", 50).orElseThrow();
        assertThat(updatedSocks.getQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("Ошибка: Нехватка носков на складе")
    @Transactional
    void registerOutcome_shouldThrowExceptionWhenNotEnoughSocks() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(20)
                .build();
        socksRepository.save(existingSocks);

        InsufficientSocksInWarehouseException exception = assertThrows(
                InsufficientSocksInWarehouseException.class,
                () -> socksService.registerOutcome("red", 50, 50)
        );

        assertThat(exception.getMessage()).isEqualTo("Недостаточно носков на складе для выполнения операции.");
    }

    @Test
    @DisplayName("Ошибка: Носки с указанными параметрами не найдены")
    void registerOutcome_shouldThrowExceptionWhenSocksNotFound() {
        SocksNotFoundInWarehouse exception = assertThrows(
                SocksNotFoundInWarehouse.class,
                () -> socksService.registerOutcome("blue", 70, 10)
        );

        assertThat(exception.getMessage()).isEqualTo("Носки с указанными параметрами не найдены.");
    }

    @Test
    @DisplayName("Ошибка: Неверное количество (отрицательное)")
    void registerOutcome_shouldThrowExceptionForNegativeQuantity() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerOutcome("red", 50, -10)
        );

        assertThat(exception.getMessage()).isEqualTo("Количество должно быть положительным.");
    }

    @Test
    @DisplayName("Ошибка: Процент хлопка вне диапазона (больше 100)")
    void registerOutcome_shouldThrowExceptionForCottonPartAbove100() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerOutcome("red", 150, 10)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Ошибка: Процент хлопка вне диапазона (меньше 0)")
    void registerOutcome_shouldThrowExceptionForCottonPartBelow0() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.registerOutcome("red", -10, 10)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Фильтрация по цвету")
    @Transactional
    void getSocks_shouldFilterByColor() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks("red", null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(socks -> socks.getColor().equals("red"));
    }

    @Test
    @DisplayName("Фильтрация по минимальному проценту хлопка")
    @Transactional
    void getSocks_shouldFilterByMinCottonPart() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks(null, 50, null, null);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(socks -> socks.getCottonPart() >= 50);
    }

    @Test
    @DisplayName("Фильтрация по максимальному проценту хлопка")
    @Transactional
    void getSocks_shouldFilterByMaxCottonPart() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks(null, null, 50, null);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(socks -> socks.getCottonPart() <= 50);
    }

    @Test
    @DisplayName("Фильтрация по диапазону процента хлопка")
    @Transactional
    void getSocks_shouldFilterByCottonPartRange() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks(null, 30, 70, null);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(socks -> socks.getCottonPart() >= 30 && socks.getCottonPart() <= 70);
    }

    @Test
    @DisplayName("Сортировка по цвету")
    @Transactional
    void getSocks_shouldSortByColor() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks(null, null, null, "color");

        assertThat(result).hasSize(4);
        assertThat(result).extracting(SocksRsDto::getColor).containsExactly("blue", "green", "red", "red");
    }

    @Test
    @DisplayName("Сортировка по проценту хлопка")
    @Transactional
    void getSocks_shouldSortByCottonPart() {
        socksRepository.saveAll(List.of(
                Socks.builder().color("red").cottonPart(50).quantity(100).build(),
                Socks.builder().color("blue").cottonPart(30).quantity(200).build(),
                Socks.builder().color("green").cottonPart(70).quantity(50).build(),
                Socks.builder().color("red").cottonPart(80).quantity(150).build()
        ));

        List<SocksRsDto> result = socksService.getSocks(null, null, null, "cottonPart");

        assertThat(result).hasSize(4);
        assertThat(result).extracting(SocksRsDto::getCottonPart).containsExactly(30, 50, 70, 80);
    }

    @Test
    @DisplayName("Ошибка при некорректном значении параметра сортировки")
    void getSocks_shouldThrowExceptionForInvalidSortBy() {
        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.getSocks(null, null, null, "invalid")
        );

        assertThat(exception.getMessage()).isEqualTo("Недопустимое значение для параметра sortBy. Доступные значения: color, cottonPart.");
    }

    @Test
    @DisplayName("Успешное обновление носков")
    @Transactional
    void updateSocks_shouldUpdateRecord() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
        Socks savedSocks = socksRepository.save(existingSocks);

        SocksRsDto updatedSocks = socksService.updateSocks(savedSocks.getId(), "blue", 70, 200);

        assertThat(updatedSocks.getId()).isEqualTo(savedSocks.getId());
        assertThat(updatedSocks.getColor()).isEqualTo("blue");
        assertThat(updatedSocks.getCottonPart()).isEqualTo(70);
        assertThat(updatedSocks.getQuantity()).isEqualTo(200);

        Socks socksInDb = socksRepository.findById(savedSocks.getId()).orElseThrow();
        assertThat(socksInDb.getColor()).isEqualTo("blue");
        assertThat(socksInDb.getCottonPart()).isEqualTo(70);
        assertThat(socksInDb.getQuantity()).isEqualTo(200);
    }

    @Test
    @DisplayName("Ошибка: Носки с указанным ID не найдены")
    void updateSocks_shouldThrowExceptionWhenSocksNotFound() {
        SocksNotFoundInWarehouse exception = assertThrows(
                SocksNotFoundInWarehouse.class,
                () -> socksService.updateSocks(999L, "blue", 70, 200)
        );

        assertThat(exception.getMessage()).isEqualTo("Носки с указанным ID не найдены.");
    }

    @Test
    @DisplayName("Ошибка: Неверное количество (отрицательное)")
    void updateSocks_shouldThrowExceptionForNegativeQuantity() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
        Socks savedSocks = socksRepository.save(existingSocks);

        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.updateSocks(savedSocks.getId(), "blue", 70, -10)
        );

        assertThat(exception.getMessage()).isEqualTo("Количество должно быть положительным.");
    }

    @Test
    @DisplayName("Ошибка: Процент хлопка вне диапазона (больше 100)")
    void updateSocks_shouldThrowExceptionForCottonPartAbove100() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
        Socks savedSocks = socksRepository.save(existingSocks);

        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.updateSocks(savedSocks.getId(), "blue", 150, 10)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Ошибка: Процент хлопка вне диапазона (меньше 0)")
    void updateSocks_shouldThrowExceptionForCottonPartBelow0() {
        Socks existingSocks = Socks.builder()
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
        Socks savedSocks = socksRepository.save(existingSocks);

        InvalidDataFormatException exception = assertThrows(
                InvalidDataFormatException.class,
                () -> socksService.updateSocks(savedSocks.getId(), "blue", -10, 10)
        );

        assertThat(exception.getMessage()).isEqualTo("Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Успешная обработка файла партии носков")
    @Transactional
    void processSocksBatch_shouldProcessValidFile() throws Exception {
        String content = "red,50,100\nblue,30,200\ngreen,70,50";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "socks.csv",
                "text/csv",
                content.getBytes()
        );

        socksService.processSocksBatch(file);

        List<Socks> savedSocks = socksRepository.findAll();
        assertThat(savedSocks).hasSize(3);

        assertThat(savedSocks).extracting(Socks::getColor).containsExactlyInAnyOrder("red", "blue", "green");
        assertThat(savedSocks).extracting(Socks::getCottonPart).containsExactlyInAnyOrder(50, 30, 70);
        assertThat(savedSocks).extracting(Socks::getQuantity).containsExactlyInAnyOrder(100, 200, 50);
    }

    @Test
    @DisplayName("Ошибка: Пустой файл")
    void processSocksBatch_shouldThrowExceptionForEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> socksService.processSocksBatch(emptyFile)
        );

        assertThat(exception.getMessage()).isEqualTo("Файл не может быть пустым.");
    }

    @Test
    @DisplayName("Ошибка: Некорректный формат строки в файле")
    void processSocksBatch_shouldThrowExceptionForInvalidRowFormat() {
        String invalidContent = "red,50\nblue,30,200"; // В первой строке не хватает столбца
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidContent.getBytes()
        );

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> socksService.processSocksBatch(file)
        );

        assertThat(exception.getMessage()).isEqualTo("Ошибка при обработке файла: Каждая строка должна содержать три значения: цвет, процент хлопка, количество.");
    }

    @Test
    @DisplayName("Ошибка: Некорректные числовые значения в строке")
    void processSocksBatch_shouldThrowExceptionForInvalidNumbers() {
        String invalidContent = "red,abc,100\nblue,30,xyz"; // Некорректные числа
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid_numbers.csv",
                "text/csv",
                invalidContent.getBytes()
        );

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> socksService.processSocksBatch(file)
        );

        assertThat(exception.getMessage()).isEqualTo("Ошибка при обработке файла: Процент хлопка и количество должны быть числами.");
    }

    @Test
    @DisplayName("Ошибка: Процент хлопка вне диапазона")
    void processSocksBatch_shouldThrowExceptionForInvalidCottonPartRange() {
        String invalidContent = "red,150,100\nblue,-10,200"; // Неверный диапазон процента хлопка
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid_cotton.csv",
                "text/csv",
                invalidContent.getBytes()
        );

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> socksService.processSocksBatch(file)
        );

        assertThat(exception.getMessage()).isEqualTo("Ошибка при обработке файла: Процент хлопка должен быть в диапазоне 0-100.");
    }

    @Test
    @DisplayName("Ошибка: Отрицательное количество")
    void processSocksBatch_shouldThrowExceptionForNegativeQuantity() {
        String invalidContent = "red,50,-100\nblue,30,200"; // Отрицательное количество
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid_quantity.csv",
                "text/csv",
                invalidContent.getBytes()
        );

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> socksService.processSocksBatch(file)
        );

        assertThat(exception.getMessage()).isEqualTo("Ошибка при обработке файла: Количество должно быть положительным.");
    }
}
