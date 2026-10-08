package ru.zidan.rbpo.repository;

import org.springframework.stereotype.Repository;
import ru.zidan.rbpo.entity.Exhibit;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExhibitRepository {

    private List<Exhibit> exhibits = new ArrayList<>();
    private long nextId = 1;

    public List<Exhibit> findAll() {
        return new ArrayList<>(exhibits);
    }

    public Exhibit findById(Long id) {
        for (Exhibit e : exhibits) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    public Exhibit add(Exhibit exhibit) {
        exhibit.setId(nextId);
        nextId++;
        exhibits.add(exhibit);
        return exhibit;
    }

    public void deleteById(Long id) {
        Exhibit e = findById(id);
        if (e != null) {
            exhibits.remove(e);
        }
    }
}
