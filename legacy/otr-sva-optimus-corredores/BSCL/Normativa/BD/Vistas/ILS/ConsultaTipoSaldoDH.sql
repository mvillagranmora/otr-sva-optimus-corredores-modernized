SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoSaldoDH";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoSaldoDH" 
AS

SELECT 1   AS "IdTipoSaldoDH"
	 , 'D' AS "CodigoTipoSaldoDH" 
FROM DUMMY
UNION ALL
SELECT 2   AS "IdTipoSaldoDH"
	 , 'H' AS "CodigoTipoSaldoDH" 	
FROM DUMMY
UNION ALL
SELECT 3   AS "IdTipoSaldoDH"
	 , 'S' AS "CodigoTipoSaldoDH" 	
FROM DUMMY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaTipoSaldoDH" TO BCS_DESARROLLADOR WITH GRANT OPTION;
