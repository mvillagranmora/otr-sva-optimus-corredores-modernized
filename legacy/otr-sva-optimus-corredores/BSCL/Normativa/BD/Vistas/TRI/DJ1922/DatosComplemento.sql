

SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.DatosComplemento";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.DatosComplemento" (
		"RutSociedad",
		"Nemotecnico",
		"FechaPago",
		"SecuenciaEvento",
		"RazonSocial"  	
) AS

SELECT DISTINCT 
		DJS."RutAdministradora"												AS "RutSociedad",
		DJS."Fondo"															AS "Nemotecnico",
		DJS."FechaInversion"												AS "FechaPago",
		DJS."SecEvento"														AS "SecuenciaEvento",
		COALESCE(INS."RazonSocial",'Falta Informaci�n de la Sociedad')		AS "RazonSocial"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"						AS DJS 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"	AS INS ON (TRIM(DJS."Fondo") = TRIM(INS."Nemotecnico"))
LEFT JOIN "BSCL.Normativa.BD::TRI.TBSICAVC" 					AS SIC ON (TRIM(DJS."Fondo") = TRIM(SIC."NEMO")
																							 AND DJS."FechaInversion" = SIC."FEC_PAGO" 
																							 AND DJS."SecEvento" = SIC."SEC_EVE") 
WHERE 
	--DJS."Mercado" 	= 'AC'	AND 
   (DJS."MontoNominalAportes"		<> 0	AND DJS."SecEvento"	<> 0)	AND
   (COALESCE(SIC."FACTOR_06",-1) = -1)
ORDER BY "RutSociedad","Nemotecnico", "FechaPago", "SecuenciaEvento"  


WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1922.DatosComplemento" TO bcs_desarrollador WITH GRANT OPTION;
