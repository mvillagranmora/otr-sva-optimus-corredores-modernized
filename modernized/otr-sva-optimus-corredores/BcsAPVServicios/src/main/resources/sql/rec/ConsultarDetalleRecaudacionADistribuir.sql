SELECT 

	"FolioRecaudacion"
	,"IdRecaudacionCliente"	
	,"IdRecaudacionEmpleador"		
	,"IdCliente"		
	,"IdPersonaCliente"		
	,"RutEmpleador"
	,"FechaDeposito"			
	,"RecTotal"					
	,"IdBanco"		
	,CAST(0 AS BIGINT) AS "IdContrato"
	,CAST(0 AS BIGINT) AS "IdPortafolio"	
	,"IdTipoDeposito"			
	,"NombreTipoDeposito"
	,"CuentaCorriente"
	,"CodigoEstado"	
	,"Regimen" as "IdRegimen"
	,"RutCliente"
FROM  "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido"

WHERE "CodigoEstado" IN ('CON','REZ')
AND (:IdRecaudacion = 0 OR "FolioRecaudacion" in (:IdsRecaudacion))
;