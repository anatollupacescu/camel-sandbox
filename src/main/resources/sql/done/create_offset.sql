DROP TABLE IF EXISTS TUMBLR.OFFSET;

create table if not exists TUMBLR.OFFSET
(
    ID INTEGER NOT NULL auto_increment PRIMARY KEY,
    UPDATED TIMESTAMP DEFAULT NOW(),
    BLOG_NAME VARCHAR(255),
    TOTAL INTEGER NOT NULL,
    DOWNLOADED INTEGER NOT NULL
)