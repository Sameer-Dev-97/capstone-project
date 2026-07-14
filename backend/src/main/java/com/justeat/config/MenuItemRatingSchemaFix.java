package com.justeat.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
@RequiredArgsConstructor
public class MenuItemRatingSchemaFix {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemRatingSchemaFix.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateConstraintIfNeeded() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();

            if (databaseProductName == null || !databaseProductName.toLowerCase().contains("postgresql")) {
                return;
            }

            jdbcTemplate.execute(
                    """
                            DO $$
                            BEGIN
                                ALTER TABLE menu_item_ratings DROP CONSTRAINT IF EXISTS uk65nc0wkjxbaa5p2wv0660bor3;
                                ALTER TABLE menu_item_ratings DROP CONSTRAINT IF EXISTS menu_item_ratings_customer_id_menu_item_id_key;

                                IF NOT EXISTS (
                                    SELECT 1
                                    FROM pg_constraint
                                    WHERE conname = 'uk_menu_item_ratings_customer_order_menu'
                                ) THEN
                                    ALTER TABLE menu_item_ratings
                                    ADD CONSTRAINT uk_menu_item_ratings_customer_order_menu
                                    UNIQUE (customer_id, order_id, menu_item_id);
                                END IF;
                            END $$;
                            """);

            logger.info("Ensured menu_item_ratings unique constraint is order-scoped");
        } catch (Exception exception) {
            logger.warn("MenuItemRating schema fix skipped: {}", exception.getMessage());
        }
    }
}
