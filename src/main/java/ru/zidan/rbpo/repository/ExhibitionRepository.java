package ru.zidan.rbpo.repository;

import org.springframework.stereotype.Repository;
import ru.zidan.rbpo.entity.Exhibition;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExhibitionRepository {

    private List<Exhibition> exhibitions = new ArrayList<>();
    private long nextId = 1;

    public List<Exhibition> findAll() {
        return new ArrayList<>(exhibitions);
    }

    public Exhibition findById(Long id) {
        for (Exhibition ex : exhibitions) {
            if (ex.getId().equals(id)) {
                return ex;
            }
        }
        return null;
    }

    // все выставки, где этот пользователь куратор
    public List<Exhibition> findByCuratorId(Long curatorId) {
        List<Exhibition> result = new ArrayList<>();
        for (Exhibition ex : exhibitions) {
            if (curatorId.equals(ex.getCuratorId())) {
                result.add(ex);
            }
        }
        return result;
    }

    public Exhibition add(Exhibition exhibition) {
        exhibition.setId(nextId);
        nextId++;
        exhibitions.add(exhibition);
        return exhibition;
    }

    public void deleteById(Long id) {
        Exhibition ex = findById(id);
        if (ex != null) {
            exhibitions.remove(ex);
        }
    }
}
