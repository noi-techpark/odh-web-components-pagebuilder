ALTER TABLE pagebuilder_page_publication
    ADD COLUMN "page_id" UUID;
    
DO $$
DECLARE
    rcd RECORD;
BEGIN
    FOR rcd IN SELECT pp.id AS "page_id", ppp.id AS "publication_id" FROM pagebuilder_page_publication ppp JOIN pagebuilder_page pp ON ppp.id = pp.publication_id LOOP
        UPDATE pagebuilder_page_publication SET page_id = rcd.page_id WHERE id = rcd.publication_id;
    END LOOP;
END $$;

ALTER TABLE pagebuilder_page_publication
    ALTER COLUMN "page_id" SET NOT NULL;

ALTER TABLE pagebuilder_page_publication
    ADD CONSTRAINT fk_5e4d826e7abed_page_id
    FOREIGN KEY (page_id)
    REFERENCES pagebuilder_page(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page_publication
    ADD COLUMN "action" VARCHAR(16) NOT NULL DEFAULT 'PUBLISH';

DROP INDEX uk_5d517824b0ae4_uri;