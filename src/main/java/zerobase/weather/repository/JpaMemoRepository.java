package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;


@Repository
// Jpa는 자바의 표준 ORM 명세이다. JpaRepository에 ORM에 대한 함수들 정의되어 있음
public interface JpaMemoRepository extends JpaRepository<Memo, Integer> {
}
