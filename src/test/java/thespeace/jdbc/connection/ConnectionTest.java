package thespeace.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static thespeace.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    /**
     * <h2>DriverManager 를 통해서 커넥션을 획득</h2>
     */
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    /**
     * <h2>스프링이 제공하는 DataSource 가 적용된 DriverManager 인 DriverManagerDataSource 를 사용</h2>
     */
    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    /**
     * <h2>`DriverManager` VS `DriverManagerDataSource`</h2>
     * DriverManager 는 커넥션을 획득할 때 마다 URL , USERNAME , PASSWORD 같은 파라미터를 계속 전달해야
     * 한다. 반면에 DataSource 를 사용하는 방식은 처음 객체를 생성할 때만 필요한 파리미터를 넘겨두고, 커넥션을
     * 획득할 때는 단순히 dataSource.getConnection() 만 호출하면 된다.<p>
     *
     * <ul><h3>설정과 사용의 분리</h3>
     *     <li>설정: DataSource 를 만들고 필요한 속성들을 사용해서 URL , USERNAME , PASSWORD 같은 부분을 입력하는
     *         것을 말한다. 이렇게 설정과 관련된 속성들은 한 곳에 있는 것이 향후 변경에 더 유연하게 대처할 수 있다.</li>
     *     <li>사용: 설정은 신경쓰지 않고, DataSource 의 getConnection() 만 호출해서 사용하면 된다.</li>
     * </ul>
     *
     * <ul><h3>설정과 사용의 분리 설명</h3>
     *     <li>이 부분이 작아보이지만 큰 차이를 만들어내는데, 필요한 데이터를 DataSource 가 만들어지는 시점에 미리 다
     *         넣어두게 되면, DataSource 를 사용하는 곳에서는 dataSource.getConnection() 만 호출하면 되므로,
     *         URL , USERNAME , PASSWORD 같은 속성들에 의존하지 않아도 된다. 그냥 DataSource 만 주입받아서
     *         getConnection() 만 호출하면 된다</li>
     *     <li>쉽게 이야기해서 리포지토리(Repository)는 DataSource 만 의존하고, 이런 속성을 몰라도 된다.</li>
     *     <li>애플리케이션을 개발해보면 보통 설정은 한 곳에서 하지만, 사용은 수 많은 곳에서 하게 된다.</li>
     *     <li>덕분에 객체를 설정하는 부분과, 사용하는 부분을 좀 더 명확하게 분리할 수 있다.</li>
     * </ul>
     *
     */
    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
