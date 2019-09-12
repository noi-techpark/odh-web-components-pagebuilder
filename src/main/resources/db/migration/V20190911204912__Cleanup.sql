ALTER TABLE IF EXISTS pagebuilder_page DISABLE TRIGGER ALL;
ALTER TABLE IF EXISTS pagebuilder_page_content DISABLE TRIGGER ALL;
ALTER TABLE IF EXISTS pagebuilder_page_publication DISABLE TRIGGER ALL;
ALTER TABLE IF EXISTS pagebuilder_page_version DISABLE TRIGGER ALL;

DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;
DROP TABLE IF EXISTS pagebuilder_page CASCADE;
DROP TABLE IF EXISTS pagebuilder_page_content CASCADE;
DROP TABLE IF EXISTS pagebuilder_page_publication CASCADE;
DROP TABLE IF EXISTS pagebuilder_page_version CASCADE;