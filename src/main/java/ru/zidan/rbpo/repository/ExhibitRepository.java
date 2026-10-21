package ru.zidan.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zidan.rbpo.entity.Exhibit;

public interface ExhibitRepository extends JpaRepository<Exhibit, Long> {
}
