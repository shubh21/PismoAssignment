package com.takeHome.Pismo;

import com.takeHome.Pismo.infrastructure.adapter.out.persistence.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PismoApplicationTests {

	@Test
	void contextLoads() {
	}

}
