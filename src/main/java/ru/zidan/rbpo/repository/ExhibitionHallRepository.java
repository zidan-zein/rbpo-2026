package ru.zidan.rbpo.repository;

import org.springframework.stereotype.Repository;
import ru.zidan.rbpo.entity.ExhibitionHall;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExhibitionHallRepository {

    private List<ExhibitionHall> halls = new ArrayList<>();
    private long nextId = 1;

    public List<ExhibitionHall> findAll() {
        return new ArrayList<>(halls);
    }

    public ExhibitionHall findById(Long id) {
        for (ExhibitionHall h : halls) {
            if (h.getId().equals(id)) {
                return h;
            }
        }
        return null;
    }

    public ExhibitionHall add(ExhibitionHall hall) {
        hall.setId(nextId);
        nextId++;
        halls.add(hall);
        return hall;
    }

    public void deleteById(Long id) {
        ExhibitionHall h = findById(id);
        if (h != null) {
            halls.remove(h);
        }
    }
}
