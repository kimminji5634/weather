package zerobase.weather;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;
import zerobase.weather.repository.JdbcMemoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Transactional /*db test 할 때 test code 때문에 db 정보가 변경되는 걸 막는 어노테이션*/
public class JdbcMemoRepositoryTest {

    @Autowired
    JdbcMemoRepository jdbcMemoRepository;

    /*given, when, then 순서로 테스트 코드를 작성해야 한다*/
    @Test
    void insertMemoTest(){

        //given : 주어진 것
        Memo newMemo = new Memo(2, "insertMemoTest"); // memo 객체 하나 생성

        //when : ~ 했을 때
        jdbcMemoRepository.save(newMemo);

        //then : ~일 것이다. => assert문 들어감
        Optional<Memo> result = jdbcMemoRepository.findById(2);
        assertEquals(result.get().getText(), "insertMemoTest");
    }

    @Test
    void findAllMemoTest(){

        List<Memo> memoList = jdbcMemoRepository.findAll();
        System.out.println(memoList); //[zerobase.weather.domain.Memo@25d9291a] 객체 위치 잘 나옴 됨
        assertNotNull(memoList);
    }
}
