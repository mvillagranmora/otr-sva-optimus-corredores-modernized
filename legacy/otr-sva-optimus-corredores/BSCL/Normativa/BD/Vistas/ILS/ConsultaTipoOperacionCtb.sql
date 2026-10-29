SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoOperacionCtb";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoOperacionCtb" 
AS

SELECT 1   AS "IdOperacionCtb"
	 , '+' AS "CodigoOperacionCtb" 
FROM DUMMY
UNION ALL
SELECT 2   AS "IdOperacionCtb"
	 , '-' AS "CodigoOperacionCtb" 	
FROM DUMMY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoOperacionCtb" TO BCS_DESARROLLADOR WITH GRANT OPTION;
