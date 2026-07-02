package com.example.financas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "MAIL_HOST=localhost",
        "MAIL_PORT=25",
        "MAIL_USER=mock",
        "MAIL_PASS=mock",
        "DB_URL=jdbc:postgresql://localhost:5432/mock",
        "DB_USER=mock",
        "DB_PASS=mock",
        "acess_jwt_secret=mockSecret123mockSecret123mockSecret123",
        "refresh_jwt_secret=mockSecret123mockSecret123mockSecret123",
        "pre_auth_token=mockSecret123mockSecret123mockSecret123",
        "GEMINI_API_KEY=mock"
})
class FinancasApplicationTests {

	@Test
	void contextLoads() {
	}

}
