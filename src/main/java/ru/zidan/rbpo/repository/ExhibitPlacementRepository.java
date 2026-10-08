package ru.zidan.rbpo.repository;

import org.springframework.stereotype.Repository;
import ru.zidan.rbpo.entity.ExhibitPlacement;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExhibitPlacementRepository {

    private List<ExhibitPlacement> placements = new ArrayList<>();
    private long nextId = 1;

    public List<ExhibitPlacement> findAll() {
        return new ArrayList<>(placements);
    }

    public ExhibitPlacement findById(Long id) {
        for (ExhibitPlacement p : placements) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // три метода ниже нужны, чтобы при удалении найти связанные размещения

    public List<ExhibitPlacement> findByExhibitId(Long exhibitId) {
        List<ExhibitPlacement> result = new ArrayList<>();
        for (ExhibitPlacement p : placements) {
            if (p.getExhibitId().equals(exhibitId)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<ExhibitPlacement> findByExhibitionId(Long exhibitionId) {
        List<ExhibitPlacement> result = new ArrayList<>();
        for (ExhibitPlacement p : placements) {
            if (p.getExhibitionId().equals(exhibitionId)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<ExhibitPlacement> findByHallId(Long hallId) {
        List<ExhibitPlacement> result = new ArrayList<>();
        for (ExhibitPlacement p : placements) {
            if (p.getHallId().equals(hallId)) {
                result.add(p);
            }
        }
        return result;
    }

    public ExhibitPlacement add(ExhibitPlacement placement) {
        placement.setId(nextId);
        nextId++;
        placements.add(placement);
        return placement;
    }

    public void deleteById(Long id) {
        ExhibitPlacement p = findById(id);
        if (p != null) {
            placements.remove(p);
        }
    }
}
