package zerobase.weather;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;
import zerobase.weather.repository.JpaMemoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // 테스트 코드에서 쓰게 되면 무조건 커밋을 시키지 않는다는 의미 = DB 상태 변경 안한다
public class JpaMemoRepositoryTest {

    @Autowired // 우리가 작성한 거 불러오기
    JpaMemoRepository jpaMemoRepository;

    @Test
    void insertMemoTest(){
        //given
        Memo newMemo = new Memo(10, "this is jpa memo");
        //when
        jpaMemoRepository.save(newMemo);
        //then
        List<Memo> memoList = (List<Memo>) jpaMemoRepository.findAll();
        assertTrue(memoList.size() > 0);
    }

    @Test
    void findById() {
        //given
        // 여기 11 넣어줘도 mysql 에 auto_increment 된 값이 id 값이 되기 때문에
        // then 에서 findById(11)로 할 경우 no value 가 나온다
        // 여기서 얻을 수 있는 힌트 => when에서 id 값을 아무 값이나 넣어도 되겠다!!!
        Memo newMemo = new Memo(11, "jpa");
        //when
        Memo memo = jpaMemoRepository.save(newMemo);
        System.out.println(memo.getId()); // id값이 6이 나왔다
        //then
        // 따라서 11이 아닌 memo.getId를 기대값으로 넣어주어야 한다
        Optional<Memo> result = jpaMemoRepository.findById(memo.getId());
        assertEquals(result.get().getText(), "jpa");
    }
}
