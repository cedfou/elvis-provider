
    drop table if exists elvis_usage_mapping cascade;

    drop sequence hibernate_sequence;

    create table elvis_usage_mapping (
        id int4 not null,
        assetPath varchar(255),
        componentIdentifier varchar(255),
        mountPointIdentifier varchar(255),
        pageUrl varchar(255),
        propertyName varchar(255),
        primary key (id)
    );

    create index componentIdentifier on elvis_usage_mapping (componentIdentifier);

    create index propertyName on elvis_usage_mapping (propertyName);

    create sequence hibernate_sequence;
