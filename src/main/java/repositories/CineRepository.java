package repositories;

import entities.Cine;
import org.springframework.stereotype.Repository;

@Repository
public interface CineRepository extends BaseRepository<Cine, Long> {
}
