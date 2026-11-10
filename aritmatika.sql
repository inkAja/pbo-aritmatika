Create table aritmatika(
	id_aritmatika INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	bil_1 INT NOT NULL,
	operasi CHAR(1) NOT NULL CHECK (operasi IN('+', '-', 'x', '/')),
	bil_2 INT NOT NULL
)