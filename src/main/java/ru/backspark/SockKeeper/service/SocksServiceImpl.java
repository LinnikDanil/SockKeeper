package ru.backspark.SockKeeper.service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.backspark.SockKeeper.dto.SocksRsDto;
import ru.backspark.SockKeeper.error.exception.FileProcessingException;
import ru.backspark.SockKeeper.error.exception.InsufficientSocksInWarehouseException;
import ru.backspark.SockKeeper.error.exception.InvalidDataFormatException;
import ru.backspark.SockKeeper.error.exception.SocksNotFoundInWarehouse;
import ru.backspark.SockKeeper.model.Socks;
import ru.backspark.SockKeeper.repository.SocksRepository;

import javax.persistence.criteria.Predicate;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocksServiceImpl implements SocksService {

    private final SocksRepository socksRepository;

    @Override
    @Transactional
    public void registerIncome(String color, Integer cottonPart, Integer quantity) {
        log.debug("Регистрация прихода носков: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);

        validateQuantity(quantity);
        validateCottonPart(cottonPart);

        // Ищем существующую запись с таким цветом и процентом хлопка
        Socks existingSocks = socksRepository.findByColorAndCottonPart(color, cottonPart)
                .orElse(null);

        if (existingSocks != null) {
            // Если запись найдена, увеличиваем количество
            log.debug("Обновление существующей записи: color={}, cottonPart={}, текущий quantity={}",
                    color, cottonPart, existingSocks.getQuantity());
            existingSocks.setQuantity(existingSocks.getQuantity() + quantity);
            socksRepository.save(existingSocks);
            log.info("Количество обновлено. Новый quantity={}", existingSocks.getQuantity());
        } else {
            // Если записи нет, создаем новую
            Socks newSocks = Socks.builder()
                    .color(color)
                    .cottonPart(cottonPart)
                    .quantity(quantity)
                    .build();
            socksRepository.save(newSocks);
            log.debug("Создана новая запись: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);
        }
    }

    @Override
    @Transactional
    public void registerOutcome(String color, Integer cottonPart, Integer quantity) {
        log.debug("Регистрация отпуска носков: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);

        validateQuantity(quantity);
        validateCottonPart(cottonPart);

        // Ищем существующую запись с таким цветом и процентом хлопка
        Socks existingSocks = socksRepository.findByColorAndCottonPart(color, cottonPart)
                .orElseThrow(() -> {
                    log.error("Носки с параметрами color={} и cottonPart={} не найдены", color, cottonPart);
                    return new SocksNotFoundInWarehouse("Носки с указанными параметрами не найдены.");
                });

        // Проверяем, хватает ли количества для отпуска
        if (existingSocks.getQuantity() < quantity) {
            log.error("Недостаточно носков на складе. Запрашиваемое количество={}, доступное количество={}",
                    quantity, existingSocks.getQuantity());
            throw new InsufficientSocksInWarehouseException("Недостаточно носков на складе для выполнения операции.");
        }

        // Уменьшаем количество носков
        existingSocks.setQuantity(existingSocks.getQuantity() - quantity);
        socksRepository.save(existingSocks);

        log.debug("Успешно выполнен отпуск носков. color={}, cottonPart={}, остаток={}",
                color, cottonPart, existingSocks.getQuantity());
    }


    @Override
    @Transactional(readOnly = true)
    public List<SocksRsDto> getSocks(String color, Integer minCottonPart, Integer maxCottonPart, String sortBy) {
        log.debug("Получение списка носков с фильтрами: color={}, minCottonPart={}, maxCottonPart={}, sortBy={}",
                color, minCottonPart, maxCottonPart, sortBy);

        // Построение запроса с фильтрацией
        List<Socks> socksList = socksRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (color != null && !color.isBlank()) {
                predicates.add(cb.equal(root.get("color"), color));
            }
            if (minCottonPart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cottonPart"), minCottonPart));
            }
            if (maxCottonPart != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("cottonPart"), maxCottonPart));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

        // Сортировка
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "color" -> socksList.sort(Comparator.comparing(Socks::getColor));
                case "cottonPart" -> socksList.sort(Comparator.comparing(Socks::getCottonPart));
                default -> {
                    log.warn("Недопустимое значение для sortBy: {}", sortBy);
                    throw new InvalidDataFormatException("Недопустимое значение для параметра sortBy. Доступные значения: color, cottonPart.");
                }
            }
        }

        List<SocksRsDto> result = socksList.stream()
                .map(socks -> SocksRsDto.builder()
                        .id(socks.getId())
                        .color(socks.getColor())
                        .cottonPart(socks.getCottonPart())
                        .quantity(socks.getQuantity())
                        .build())
                .toList();

        log.debug("Найдено записей: {}", result.size());
        return result;
    }


    @Override
    @Transactional
    public SocksRsDto updateSocks(Long id, String color, Integer cottonPart, Integer quantity) {
        log.debug("Обновление носков: id={}, color={}, cottonPart={}, quantity={}", id, color, cottonPart, quantity);

        validateCottonPart(cottonPart);
        validateQuantity(quantity);

        // Поиск записи в базе данных
        Socks existingSocks = socksRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Носки с id={} не найдены", id);
                    return new SocksNotFoundInWarehouse("Носки с указанным ID не найдены.");
                });

        // Обновление полей
        log.debug("Старые данные носков: {}", existingSocks);
        existingSocks.setColor(color);
        existingSocks.setCottonPart(cottonPart);
        existingSocks.setQuantity(quantity);
        socksRepository.save(existingSocks);

        log.debug("Носки обновлены: {}", existingSocks);

        return SocksRsDto.builder()
                .id(existingSocks.getId())
                .color(existingSocks.getColor())
                .cottonPart(existingSocks.getCottonPart())
                .quantity(existingSocks.getQuantity())
                .build();
    }


    @Override
    @Transactional
    public void processSocksBatch(MultipartFile file) {
        log.debug("Обработка файла партии носков: имя файла={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.error("Файл пустой");
            throw new FileProcessingException("Файл не может быть пустым.");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<Socks> socksBatch = new ArrayList<>();
            String[] line;

            while ((line = reader.readNext()) != null) {
                if (line.length != 3) {
                    log.error("Некорректный формат строки: {}", (Object) line);
                    throw new FileProcessingException("Каждая строка должна содержать три значения: цвет, процент хлопка, количество.");
                }

                String color = line[0];
                int cottonPart;
                int quantity;

                try {
                    cottonPart = Integer.parseInt(line[1]);
                    quantity = Integer.parseInt(line[2]);
                } catch (NumberFormatException e) {
                    log.error("Некорректные числовые значения в строке: {}", (Object) line);
                    throw new FileProcessingException("Процент хлопка и количество должны быть числами.");
                }

                validateCottonPart(cottonPart);
                validateQuantity(quantity);

                Socks socks = Socks.builder()
                        .color(color)
                        .cottonPart(cottonPart)
                        .quantity(quantity)
                        .build();

                socksBatch.add(socks);
            }

            socksRepository.saveAll(socksBatch);
            log.debug("Успешно обработано записей: {}", socksBatch.size());

        } catch (Exception e) {
            log.error("Ошибка при обработке файла: {}", e.getMessage(), e);
            throw new FileProcessingException("Ошибка при обработке файла: " + e.getMessage(), e);
        }
    }


    private void validateQuantity(Integer quantity) {
        if (quantity <= 0) {
            log.error("Количество должно быть положительным. Переданное значение: {}", quantity);
            throw new InvalidDataFormatException("Количество должно быть положительным.");
        }
    }

    private void validateCottonPart(Integer cottonPart) {
        if (cottonPart < 0 || cottonPart > 100) {
            log.error("Процент хлопка должен быть в диапазоне 0-100. Переданное значение: {}", cottonPart);
            throw new InvalidDataFormatException("Процент хлопка должен быть в диапазоне 0-100.");
        }
    }
}
