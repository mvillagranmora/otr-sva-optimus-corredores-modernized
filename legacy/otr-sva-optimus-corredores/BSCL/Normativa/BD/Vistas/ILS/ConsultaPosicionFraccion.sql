SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaPosicionFraccion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaPosicionFraccion" 
AS

SELECT 'N'   AS "IdPosicionFraccion"
	 , 'Numerador' AS "NombrePosicionFraccion" 
FROM DUMMY
UNION ALL
SELECT 'D'   AS "IdPosicionFraccion"
	 , 'Denominador' AS "NombrePosicionFraccion" 	
FROM DUMMY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaPosicionFraccion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
