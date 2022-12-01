package zerobase.weather.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.DateWeather;

import java.time.LocalDate;

@Repository
public interface DateWeatherRepository extends JpaRepository<DateWeather, LocalDate> {

    // 날짜에 따라 그 날의 DateWeather 값을 가져오는 함수 생성
    List<DateWeather> findAllByDate(LocalDate localDate); // 프로퍼티 4개이므로 List형!
}
