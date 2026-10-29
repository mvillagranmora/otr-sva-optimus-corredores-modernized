SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaCondicionCtb";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaCondicionCtb" 
AS

SELECT 1   AS "IdCondicionCtb"
	 , '>' AS "CodigoCondicionCtb" 
FROM DUMMY
UNION ALL
SELECT 2   AS "IdCondicionCtb"
	 , '<' AS "CodigoCondicionCtb" 
FROM DUMMY
UNION ALL
SELECT 3   AS "IdCondicionCtb"
	 , '=' AS "CodigoCondicionCtb" 	
FROM DUMMY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaCondicionCtb" TO BCS_DESARROLLADOR WITH GRANT OPTION;
