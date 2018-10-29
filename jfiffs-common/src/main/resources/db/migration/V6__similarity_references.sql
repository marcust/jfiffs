ALTER TABLE FEED_ENTRY ADD PRIMARY KEY (id);

alter table ENTRY_SIMILARITY add constraint FIRST_KEY_REF foreign key (first_id) references feed_entry(id) on delete cascade;
alter table ENTRY_SIMILARITY add constraint SECOND_KEY_REF foreign key (second_id) references feed_entry(id) on delete cascade;
