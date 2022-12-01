package zerobase.weather.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class JdbcMemoRepository {
    /*jdbc를 활용할 때는 JdbcTemplate을 활용한다*/
    private final JdbcTemplate jdbcTemplate;

    /*DataSource는 applictaion.properties에 적어놓은 spring.datasource의 정보를 받아옴*/
    @Autowired /*이거를 해줘야 알아서 정보를 application.properties에서 가져옴*/
    public JdbcMemoRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*스프링 부트의 Memo 객체를 db에 저장*/
    public Memo save(Memo memo){
        /*jdbc의 특징 : 쿼리문을 직접 써야 한다*/
        String sql = "insert into memo values(?,?)";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }

    public List<Memo> findAll(){ /*전체 데이터를 가져올 거라서 List형식으로 가져옴*/
        String sql = "select * from memo";
        return jdbcTemplate.query(sql, memoRowMapper()); /*조회는 query 함수를 씀, 데이터 값을 어떻게 반환할 것인지*/
    }

    // 우리는 id가 pk 값이라 해당되는 id 데이터값 하나인 거 아는데 스프링 부트는 모른다. 따라서,
    // .stream().findFirst()를 해줘서 id 하나만 가져올 수 있게 한다!!
    // 반환값을 Optional<Memo>해줘야 하는 건 id가 3인 데이터를 가져오려고 했을 경우 id 3인게 null일 때
    // 처리하기 쉽게 해주도록 Optional 함수를 사용한 것임
    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id = ?";
        return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
    }

    // db에서 데이터를 가져올 때의 형식인 ResultSet을 Memo 형식으로 변환해주는 메서드임
    /*jdbc를 통해서 mysql에서 데이터를 가져올 때의 데이터 형식은 ResultSet 형식임*/
    /*ResultSet을 Memo 형식으로 매핑해주기 위해 RowMapper를 씀*/
    /*rs는 ResultSet의 줄임말*/
    private RowMapper<Memo> memoRowMapper(){
        // ResultSet => 반환 형식
        // {id = 1, text = 'this is memo~'}
        return (rs, rowNum) -> new Memo( /*rs, rowNum 가지고 Memo 객체를 반환해줌*/
                rs.getInt("id"),
                rs.getString("text")
        );
    }
}
