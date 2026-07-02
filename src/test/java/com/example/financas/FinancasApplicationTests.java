package com.example.financas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        "MAIL_HOST=localhost",
        "MAIL_PORT=25",
        "MAIL_USER=mock",
        "MAIL_PASS=mock",

        "acess_jwt_secret=ChaveSecretaSuperSeguraComMaisDe32CaracteresTexto!",
        "refresh_jwt_secret=ChaveSecretaSuperSeguraComMaisDe32CaracteresTextoRefresh!",
        "pre_auth_token=ChaveSecretaSuperSeguraComMaisDe32CaracteresTextoPreAuth!",
        "GEMINI_API_KEY=mock"
})
class FinancasApplicationTests {

	@Test
	void contextLoads() {
	}

}
