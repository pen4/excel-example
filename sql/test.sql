create Database test;

USE test;

DROP TABLE Dictionary_Geology;

CREATE TABLE Dictionary_Geology(
	id INT AUTO_INCREMENT,
	chinese VARCHAR(100) NOT NULL,
	english VARCHAR(100) NOT NULL,
	content TEXT,
	PRIMARY KEY(id)
);


SELECT * FROM Dictionary_Geology;

