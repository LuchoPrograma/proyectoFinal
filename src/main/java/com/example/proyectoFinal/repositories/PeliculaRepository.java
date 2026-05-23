package repositories;

import entities.Pelicula;
import org.springframework.stereotype.Repository;

@Repository
public interface PeliculaRepository extends BaseRepository<Pelicula, Long> {
}
