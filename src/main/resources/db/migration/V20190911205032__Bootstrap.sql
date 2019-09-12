CREATE TABLE pagebuilder_page (
    id UUID PRIMARY KEY,
    archived BOOL NOT NULL DEFAULT false,
    label VARCHAR(255) NOT NULL,
    draft_version_id UUID,
    public_version_id UUID,
    publication_id UUID
);

CREATE TABLE pagebuilder_page_version (
    id UUID PRIMARY KEY,
    page_id UUID NOT NULL,
    hash VARCHAR(40) NOT NULL,
    updated_datetime TIMESTAMP NOT NULL,
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

CREATE TABLE pagebuilder_page_publication (
    id UUID PRIMARY KEY,
    domain_name VARCHAR(255) NOT NULL,
    subdomain_name VARCHAR(255),
    path_name VARCHAR(255),
    status VARCHAR(16) NOT NULL,
    deployed_datetime TIMESTAMP NOT NULL,
    version_id UUID NOT NULL
);

CREATE UNIQUE INDEX uk_5d517824b0ae4_uri
    ON pagebuilder_page_publication (
        COALESCE(subdomain_name, ''),
        domain_name,
        COALESCE(path_name, '')
    );

ALTER TABLE pagebuilder_page_publication
    ADD CONSTRAINT fk_5d5177e610ddf_version_id
    FOREIGN KEY (version_id)
    REFERENCES pagebuilder_page_version(id)
    ON DELETE CASCADE;

ALTER TABLE pagebuilder_page
    ADD CONSTRAINT fk_5d527f8c6b379_publication_id
    FOREIGN KEY (publication_id)
    REFERENCES pagebuilder_page_publication(id)
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