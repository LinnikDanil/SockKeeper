package ru.backspark.SockKeeper.service;

import org.springframework.web.multipart.MultipartFile;
import ru.backspark.SockKeeper.dto.SocksRsDto;

import java.util.List;

public interface SocksService {

    void registerIncome(String color, Integer cottonPart, Integer quantity);

    void registerOutcome(String color, Integer cottonPart, Integer quantity);

    List<SocksRsDto> getSocks(String color, Integer minCottonPart, Integer maxCottonPart, String sortBy);

    SocksRsDto updateSocks(Long id, String color, Integer cottonPart, Integer quantity);

    void processSocksBatch(MultipartFile file);
}
