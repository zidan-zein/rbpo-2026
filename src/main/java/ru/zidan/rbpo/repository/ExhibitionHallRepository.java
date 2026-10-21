package ru.zidan.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zidan.rbpo.entity.ExhibitionHall;

public interface ExhibitionHallRepository extends JpaRepository<ExhibitionHall, Long> {

    ExhibitionHall findByName(String name);

}
