package ru.backspark.SockKeeper.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.backspark.SockKeeper.api.spec.SocksApi;
import ru.backspark.SockKeeper.dto.SocksRsDto;
import ru.backspark.SockKeeper.service.SocksService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SocksController implements SocksApi {

    private final SocksService socksService;

    @Override
    public void registerIncome(String color, Integer cottonPart, Integer quantity) {
        socksService.registerIncome(color, cottonPart, quantity);
    }

    @Override
    public void registerOutcome(String color, Integer cottonPart, Integer quantity) {
        socksService.registerOutcome(color, cottonPart, quantity);
    }

    @Override
    public List<SocksRsDto> getSocks(String color, Integer minCottonPart, Integer maxCottonPart, String sortBy) {
        return socksService.getSocks(color, minCottonPart, maxCottonPart, sortBy);
    }

    @Override
    public SocksRsDto updateSocks(Long id, String color, Integer cottonPart, Integer quantity) {
        return socksService.updateSocks(id, color, cottonPart, quantity);
    }

    @Override
    public void uploadSocksBatch(MultipartFile file) {
        socksService.processSocksBatch(file);
    }
}
