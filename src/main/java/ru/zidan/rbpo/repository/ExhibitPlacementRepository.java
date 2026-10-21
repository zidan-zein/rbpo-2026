package ru.zidan.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zidan.rbpo.entity.ExhibitPlacement;

import java.util.List;

public interface ExhibitPlacementRepository extends JpaRepository<ExhibitPlacement, Long> {

    List<ExhibitPlacement> findByExhibitId(Long exhibitId);

    List<ExhibitPlacement> findByExhibitionId(Long exhibitionId);

    List<ExhibitPlacement> findByHallId(Long hallId);

    // размещения этой выставки в этом зале, нужно чтобы посчитать занятую площадь
    List<ExhibitPlacement> findByExhibitionIdAndHallId(Long exhibitionId, Long hallId);

}
