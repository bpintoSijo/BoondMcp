package com.sijo.boondmcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"mcp.python.base-url=http://localhost:9999",
		"mcp.python.timeout=5s"
})
class BoondMcpApplicationTests {

	@Test
	void contextLoads() {
	}

}
