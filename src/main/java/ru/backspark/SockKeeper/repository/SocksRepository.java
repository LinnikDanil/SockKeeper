package ru.backspark.SockKeeper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.backspark.SockKeeper.model.Socks;

import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long>, JpaSpecificationExecutor<Socks> {

    Optional<Socks> findByColorAndCottonPart(String color, Integer cottonPart);

}
