package zerobase.weather.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;

import java.time.LocalDate;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer>{
    List<Diary> findAllByDate(LocalDate date);
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    // 수정을 위한 함수
    Diary getFirstByDate(LocalDate date); // 날짜 기준으로 가장 첫 데이터를 가져옴(First)

    @Transactional // 삭제시는 이걸 붙여야 삭제가 됨
    void deleteAllByDate(LocalDate date); // 삭제를 위한 함수
}
