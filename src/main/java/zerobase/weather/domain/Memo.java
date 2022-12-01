package zerobase.weather.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="memo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 식별키 값인 경우
    private int id;
    private String text;
}
