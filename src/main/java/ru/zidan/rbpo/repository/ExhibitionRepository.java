package ru.zidan.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zidan.rbpo.entity.Exhibition;

import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    // все выставки, где этот пользователь куратор
    List<Exhibition> findByCuratorId(Long curatorId);

}
