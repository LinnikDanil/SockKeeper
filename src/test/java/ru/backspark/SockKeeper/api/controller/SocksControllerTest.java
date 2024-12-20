package ru.backspark.SockKeeper.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.backspark.SockKeeper.dto.SocksRsDto;
import ru.backspark.SockKeeper.service.SocksService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SocksController.class)
class SocksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocksService socksService;

    private SocksRsDto socksRsDto;

    @BeforeEach
    void setUp() {
        socksRsDto = SocksRsDto.builder()
                .id(1L)
                .color("red")
                .cottonPart(50)
                .quantity(100)
                .build();
    }

    @Test
    void registerIncome_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/socks/income")
                        .param("color", "red")
                        .param("cottonPart", "50")
                        .param("quantity", "100"))
                .andExpect(status().isOk());

        verify(socksService, times(1)).registerIncome("red", 50, 100);
    }

    @Test
    void registerOutcome_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/socks/outcome")
                        .param("color", "red")
                        .param("cottonPart", "50")
                        .param("quantity", "100"))
                .andExpect(status().isOk());

        verify(socksService, times(1)).registerOutcome("red", 50, 100);
    }

    @Test
    void getSocks_shouldReturnList() throws Exception {
        when(socksService.getSocks("red", 30, 70, "color")).thenReturn(List.of(socksRsDto));

        mockMvc.perform(get("/api/socks")
                        .param("color", "red")
                        .param("minCottonPart", "30")
                        .param("maxCottonPart", "70")
                        .param("sortBy", "color"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].color", is("red")))
                .andExpect(jsonPath("$[0].cottonPart", is(50)))
                .andExpect(jsonPath("$[0].quantity", is(100)));

        verify(socksService, times(1)).getSocks("red", 30, 70, "color");
    }

    @Test
    void updateSocks_shouldReturnUpdatedDto() throws Exception {
        when(socksService.updateSocks(1L, "blue", 60, 200)).thenReturn(socksRsDto);

        mockMvc.perform(put("/api/socks/1")
                        .param("color", "blue")
                        .param("cottonPart", "60")
                        .param("quantity", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.color", is("red")))
                .andExpect(jsonPath("$.cottonPart", is(50)))
                .andExpect(jsonPath("$.quantity", is(100)));

        verify(socksService, times(1)).updateSocks(1L, "blue", 60, 200);
    }

    @Test
    void uploadSocksBatch_shouldCallService() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "socks.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "color,cottonPart,quantity\nred,50,100".getBytes()
        );

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(file))
                .andExpect(status().isOk());

        verify(socksService, times(1)).processSocksBatch(file);
    }
}
