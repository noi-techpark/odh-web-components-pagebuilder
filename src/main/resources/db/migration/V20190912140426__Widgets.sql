CREATE TABLE pagebuilder_page_widget (
    id UUID PRIMARY KEY,
    page_version_id UUID NOT NULL,
    uid VARCHAR(255) NOT NULL,
    widget_id UUID NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    assets TEXT NOT NULL,
    markup TEXT NOT NULL,
    position INTEGER NOT NULL DEFAULT 0
);

ALTER TABLE pagebuilder_page_widget
    ADD CONSTRAINT fk_5d7b139b2db3d_page_version_id
    FOREIGN KEY (page_version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;