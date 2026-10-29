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
	,CAST("CuentaCorriente" as BIGINT) as "CuentaCorriente"
	,"CodigoEstado"	
	--,* 
FROM  "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido"
WHERE "CodigoEstado" IN ('CON','REZ')
AND ("IdRecaudacionCliente" in (:IdRecaudacionClienteGrupo))
;