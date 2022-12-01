package zerobase.weather.service;

import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiaryService {

    //스프링 부트의 openweathermap.key라는 변수 값을 가져와서 apiKey라는 객체에 넣어주겠다
    @Value("${openweathermap.key}") // application.properties에 정의
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    // 새벽 1시에 날씨 데이터 저장
    // 임시로 (cron = "0/5 * * * * *") 5초마다 동작하게 해서 함수 잘 되는지 확인해 보곘음
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate(){
        dateWeatherRepository.save(getWeatherFromApi());
    }

    // 날씨를 매일 한 번만 가져오기
    private DateWeather getWeatherFromApi() {
        // open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();
        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        // 파싱된 날씨를 DataWeather 도메인에 넣어주기
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now()); // 매일 시간이 정해졌으므로
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));
        return dateWeather;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");
        // 전에는 일기 생성할 때마다 날씨 데이터 가져오고 파싱함 => 날씨 데이터는 하루에 한 번만 가져옴
        // 날씨 데이터 가져오기(api 에서 가져오기 or db 에서 가져오기)
        DateWeather dateWeather = getDateWeather(date);

        // DateWeather 테이블은 캐싱(이전에 요청한 정보에 대한 응답값을 저장했다가 또 다시 요청한 경우 전달)

        // 파싱된 데이터 + 일기 값 우리 db에 넣기
        // Diary entity에 NoArgsConstructor 넣어줘서 생성자 없이 생성 가능!
        Diary nowDiary = new Diary(); // Diary는 날씨 + 일기 테이블
        nowDiary.setDateWeather(dateWeather); // Diary에 메서드 생성 => DateWeather 내용 받음
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }

    private DateWeather getDateWeather(LocalDate date) {
        // 사용자가 가져오고 싶은 날씨 정보가 db에 있는지 확인
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.size() == 0) {
            // db에 날씨 정보가 없는 경우 => api 에서 날씨 정보를 가져와야 한다
            return getWeatherFromApi();
        } else {
            // 정책상(5일 이전의 날씨는 유료임..) 현재 날씨를 가져오도록 하자
            return dateWeatherListFromDB.get(0);
        }
    }

    @Transactional(readOnly = true) // 조회하는 메소드에 붙여준다 = db 상태를 변경시키지 않는 곳에다가
    public List<Diary> readDiary(LocalDate date) {
        /*if (date.isAfter(LocalDate.ofYearDay(3020, 1))) {
            throw new InvalidDate();
        }*/
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        // 일기를 하루에 여러개 쓸 수 있다. 날짜를 기준으로 수정을 하면 해당 날짜에 쓴 일기 전부가 수정된다
        // 동일한 날짜에 쓴 일기 중 가장 먼저 쓰여진 일기만 수정된다고 가정하자
        Diary nowDiary = diaryRepository.getFirstByDate(date); // 해당 날짜중 첫번째 일기를 가져옴
        nowDiary.setText(text); // text 일기 값만 새로 수정
        diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString(){
        // url에 치면 나오는 주소 복 붙
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;
        // url 불러올 때 그 과정에서 오류 발생할 수 있으므로 무조건 try~catch 해줘야 함
        try {
            URL url = new URL(apiUrl); // Spring => url 형식으로
            // apiUrl을 http 형식으로 연결
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET"); // get 요청 보냄
            int responseCode = connection.getResponseCode(); // 응답 코드를 받음
            BufferedReader br;
            if (responseCode == 200) { // 정상인 경우 응답객체를 받아옴
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else { // 오류인 경우 오류 메시지 받아옴
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine); // response에 br에서 한줄 씩 읽어온것 저장
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser(); // import를 우리가 추가해준 라이브러리로 가져와야 함
        JSONObject jsonObject;

        // 파싱이 정상적이지 않은 경우 작업
        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString); // 파싱 해줌
        } catch (ParseException e) {
            throw new RuntimeException(e); // 예외 발생 띄우기
        }
        // 쓰고자 했던 데이터 가져오기 - weather.main, weahter.icon, main.temp
        Map<String, Object> resultMap = new HashMap<>(); // 여기에 위의 3가지 정보 담을 것임

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0); //list안에 객체 하나이므로 0번쨰꺼!
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        return resultMap;
    }



}
