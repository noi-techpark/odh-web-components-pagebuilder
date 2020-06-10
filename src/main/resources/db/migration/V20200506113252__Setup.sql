CREATE TABLE pagebuilder_site (
    id UUID PRIMARY KEY,
    subdomain_name VARCHAR(255),
    domain_name VARCHAR(255) NOT NULL,
    path_name VARCHAR(255)
);

CREATE TABLE pagebuilder_page (
    id UUID PRIMARY KEY,
    archived BOOL NOT NULL DEFAULT false,
    label VARCHAR(255) NOT NULL,
    draft_version_id UUID,
    public_version_id UUID,
    site_id UUID
);

ALTER TABLE pagebuilder_page
    ADD CONSTRAINT fk_5ed9c27a497ac_site_id
    FOREIGN KEY (site_id)
    REFERENCES pagebuilder_site(id)
    ON DELETE SET NULL;

CREATE TABLE pagebuilder_page_version (
    id UUID PRIMARY KEY,
    page_id UUID NOT NULL,
    publication_id UUID,
    hash VARCHAR(40) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    title VARCHAR(255),
    description TEXT,
    comment TEXT
);

ALTER TABLE pagebuilder_page_version
    ADD CONSTRAINT fk_5d43be172a6ad_page_id
    FOREIGN KEY (page_id)
    REFERENCES pagebuilder_page(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page_version
    ADD CONSTRAINT uk_5d512f9a843be_hash
    UNIQUE(hash);

ALTER TABLE pagebuilder_page
    ADD CONSTRAINT fk_5d5131f877b52_draft_version_id
    FOREIGN KEY (draft_version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page
    ADD CONSTRAINT fk_5d51320b413d9_public_version_id
    FOREIGN KEY (public_version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;
    
CREATE TABLE pagebuilder_page_content (
    id UUID PRIMARY KEY,
    page_version_id UUID NOT NULL,
    uid VARCHAR(255) NOT NULL,
    content_id UUID NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    assets TEXT NOT NULL,
    markup TEXT NOT NULL,
    position INTEGER NOT NULL DEFAULT 0
);

ALTER TABLE pagebuilder_page_content
    ADD CONSTRAINT fk_5d513199bac61_page_version_id
    FOREIGN KEY (page_version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;

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
    
CREATE TABLE pagebuilder_page_publication (
    id UUID PRIMARY KEY,
    page_id UUID NOT NULL,
    version_id UUID NOT NULL,
    site_id UUID NOT NULL,
    "action" VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    logs JSONB NOT NULL
);

ALTER TABLE pagebuilder_page_version
    ADD CONSTRAINT fk_5ed9c8a36c69f_publication_id
    FOREIGN KEY (publication_id)
    REFERENCES pagebuilder_page_publication(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page_publication
    ADD CONSTRAINT fk_5ed9c3cd24164_page_id
    FOREIGN KEY (page_id)
    REFERENCES pagebuilder_page(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page_publication
    ADD CONSTRAINT fk_5d5177e610ddf_version_id
    FOREIGN KEY (version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page_publication
    ADD CONSTRAINT fk_5ed9c3dad271f_site_id
    FOREIGN KEY (site_id)
    REFERENCES pagebuilder_site(id)
    ON DELETE SET NULL;