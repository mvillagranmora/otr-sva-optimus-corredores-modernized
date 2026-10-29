
SELECT 
	 DENSE_RANK() over (ORDER BY "Id", "Entidad") as "IdInterno"
	,:TipoEjecucion as "TipoEjecucion"
	,PRO."Id"
	,PRO."Entidad"
	,PRO."IdConcepto"
	,PRO."Descripcion"
	,DEP."IdDeposito"
	
	,COALESCE(DEP."IdEstado",'')	AS "IdEstadoDeposito"
	,DDET."IdDepositoDetalle"	 		
	,DDET."IdEstado" 	AS "IdEstadoDepositoDetalle"	
	,CAST(:IdUsuario AS BIGINT)	AS "IdUsuario"
	,:FechaMovimiento 	AS "FechaMovimiento"



FROM "BSCL.Tesoreria.BD::GBL.GestionDeDepositos_Proceso" AS PRO
LEFT JOIN "BSCL.Tesoreria.BD::TES.Deposito" AS DEP ON (DEP."IdDeposito" = CASE WHEN (PRO."Entidad" = 'TES.Deposito') THEN PRO."Id" ELSE 0 END)
LEFT JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DDET ON (DDET."IdDepositoDetalle" = CASE WHEN (PRO."Entidad" = 'TES.DepositoDetalle') THEN PRO."Id" ELSE 0 END);
   
