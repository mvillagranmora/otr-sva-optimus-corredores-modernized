SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.COMUNA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.COMUNA" AS

SELECT
	TO_INT(COALESCE( "ValorOrigen" , DP."IdDivisionPais" )) AS "COM_ID",-- INT  
	TO_INT(DP."IdClasificacion1") AS "REG_ID",-- INT 
	CAST(PAIS."CodigoPais" AS VARCHAR(2)) AS "PAI_ISO",-- CHAR(2)  
	CAST(DP."NombreDivisionPais" AS VARCHAR(30)) AS "COM_NOMBRE",-- VARCHAR(30)  
	CAST(CIUDAD."NombreCiudad" AS VARCHAR(50)) AS "COM_CIUDAD",-- VARCHAR(50)  
	CAST('' AS VARCHAR(5)) AS "COM_POSTAL",-- VARCHAR(5) 
	0 AS "COM_COD_SVS"-- INT  
FROM "BSCL.Plataforma.BD::REG.DivisionPais" DP
JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON CIUDAD."IdPais" = DP."IdPais" AND CIUDAD."IdCiudad" = DP."IdCiudad" 
JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON DP."IdPais" = PAIS."IdPais"
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  ON ("TipoValor" = 'COMUNA_SEBRA' AND "ValorDestino" = DP."IdDivisionPais" ) 
UNION 
SELECT
	TO_INT(COALESCE( "ValorOrigen" ,  DP."IdDivisionPais" )) AS "COM_ID",-- INT  
	TO_INT(DP."IdClasificacion1") AS "REG_ID",-- INT 
	CAST(PAIS."CodigoPais" AS VARCHAR(2)) AS "PAI_ISO",-- CHAR(2)  
	CAST(DP."NombreDivisionPais" AS VARCHAR(30)) AS "COM_NOMBRE",-- VARCHAR(30)  
	CAST(CIUDAD."NombreCiudad" AS VARCHAR(50)) AS "COM_CIUDAD",-- VARCHAR(50)  
	CAST('' AS VARCHAR(5)) AS "COM_POSTAL",-- VARCHAR(5) 
	0 AS "COM_COD_SVS"-- INT  
FROM "BSCL.Plataforma.BD::REG.DivisionPais" DP
JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON CIUDAD."IdPais" = DP."IdPais" AND CIUDAD."IdCiudad" = DP."IdCiudad" 
JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON DP."IdPais" = PAIS."IdPais"
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion"  ON ("TipoValor" = 'COMUNA_SEBRA' AND "ValorDestino" = DP."IdDivisionPais" )
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.COMUNA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
