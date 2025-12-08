package com.takeHome.Pismo.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Pismo Account & Transaction Service",
                version = "1.0.0",
                description = """
                        A minimal Pismo-like service implementing account creation and financial transactions
                        using Hexagonal Architecture, Spring Boot, and MySQL.
                        
                        Business rules:
                        - Accounts are created with a unique document_number.
                        - Transactions:
                          - operation_type_id:
                            - 1 = CASH_PURCHASE (negative amount)
                            - 2 = INSTALLMENT_PURCHASE (negative amount)
                            - 3 = WITHDRAWAL (negative amount)
                            - 4 = PAYMENT (positive amount)
                          - Client sends amount >= 0, service normalizes sign before persisting.
                        """
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
        },
        tags = {
                @Tag(name = "Accounts", description = "Account management operations"),
                @Tag(name = "Transactions", description = "Transaction management operations")
        }
)
public class OpenAPIConfig {
}
