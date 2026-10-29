SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.DatosComplemento";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.DatosComplemento" (
		"RutSociedad",
		"Nemotecnico",
		"FechaPago",
		"SecuenciaEvento",
		"RazonSocial"  	
) AS

SELECT DISTINCT 
		"RutSociedad"													AS "RutSociedad",
		"Nemo"															AS "Nemotecnico",
		"FecMovto"														AS "FechaPago",
		"SecEvento"														AS "SecuenciaEvento",
		COALESCE("RazonSocial",'Falta Informaci�n de la Sociedad')		AS "RazonSocial"
		
FROM "BSCL.Normativa.BD::TRI.DatosDJ1949"						AS DJS 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"	AS INS ON (TRIM(DJS."Nemo") = TRIM(INS."Nemotecnico"))
LEFT JOIN "BSCL.Normativa.BD::TRI.TBSICAVC" 					AS SIC ON (TRIM(DJS."Nemo") = TRIM(SIC."NEMO") AND DJS."FecMovto" = SIC."FEC_PAGO" AND DJS."SecEvento" = SIC."SEC_EVE") 
WHERE 
	DJS."Mercado" 	= 'AC'	AND 
   (DJS."Monto"		<> 0	AND DJS."SecEvento"	<> 0)	AND
   (COALESCE(SIC."FACTOR_06",-1) = -1)
   


WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1949.DatosComplemento" TO bcs_desarrollador WITH GRANT OPTION;

