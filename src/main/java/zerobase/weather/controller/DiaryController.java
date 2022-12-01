package zerobase.weather.controller;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;

@RestController // 기본 Controller 기능에 + 상태 코드를 뱉어줌
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장", notes = "이것은 노트")
    //저장해야 하므로 post, 조회하는데 get이 쓰임!
    @PostMapping("/create/diary") // 이 주소로 요청을 보냈을 때
    // RequestParam을 사용하면 create/diary?date = 20221203 이런 식으로 url 뒤에 파라미터로 보낼 수 있음
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
                        , @RequestBody String text){ // RequestBody는 body 값으로 보내달라는 뜻
        // 파라미터로 받은 값을 service로 전달해줘야 함
        diaryService.createDiary(date, text);
    }

    // 일기 조회 => 일기의 날짜값만 input으로 주면 됨 => @RequestParam을 통해 날짜값 받음
    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diary")
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryService.readDiary(date);
    }

    @ApiOperation("선택한 기간중의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diaries") // 날짜 기준 언제 ~ 언제까지 쓴 일기를 조회 => input 2개 받기
    List<Diary> readDiaries(@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회할 기간의 첫번째날", example = "2020-02-02") LocalDate startDate
                , @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회할 기간의 마지막날", example = "2020-02-02") LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }

    // 일기 수정 => PutMapping
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
               , @RequestBody String text) { // RequestBody : 수정할 데이터를 받는다
        diaryService.updateDiary(date, text);
    }

    // 일기 삭제
    @DeleteMapping("/delete/diary") // 해당 날짜의 일기를 다 지움
    void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
