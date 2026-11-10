package com.pbo.aritmatika;

import com.pbo.aritmatika.Aritmatika;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AritmatikaRepository extends JpaRepository<Aritmatika, Integer> {
}
