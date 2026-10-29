SELECT * FROM 	
(	
SELECT 
		 TRASPASO."IdTraspaso"
	    ,TRASPASO."IdEmpresa"
	    ,TRASPASO."IdModulo"
		,TRASPASO."IdUsuario"
	    ,TRASPASO."IdCliente"
	    ,TRASPASO."IdPortafolio"
	 	,TRASPASO."IdEmisor"
		,TRASPASO."IdMotivoTraspaso"
		,TRASPASO."IdTipoMovimientoTraspaso"
	    ,TRASPASO."IdTipoTraspaso"
	    ,TRASPASO."Glosa"
	    ,TO_NVARCHAR(TRASPASO."FechaHoraSuscripcion",'YYYY-MM-DD HH24:MI:SS') AS "FechaHoraSuscripcion"
	    ,TRASPASO."IdDivisionPais"
	    ,TRASPASO."FolioTraspaso"
	    ,TRASPASO."IdEstado"
	    ,TRASPASO."Monto" 
	    ,TRASPASO."FechaIngreso"
	    ,TRASPASO."FechaAceptacion"
	    ,TRASPASO."FechaAnulacion"
	    ,TRASPASO."FechaModificacion"
	    ,TRASPASO."FechaIngresoRegistro"
	    ,TRASPASO."FechaAceptacionRegistro" 
	    ,TRASPASO."FechaAnulacionRegistro" 
	    ,TRASPASO."FechaModificacionRegistro"
	    ,CONFIG."FechaSistema"
	    ,DAYS_BETWEEN (TO_DATE (TRASPASO."FechaIngreso", 'YYYY-MM-DD'), TO_DATE(CONFIG."FechaSistema", 'YYYY-MM-DD')) "DiasTranscurridos"
	FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
	INNER JOIN (
				SELECT  "Valor" AS "FechaSistema"
			 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
			 WHERE "IdEmpresa"=:IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema'
	) AS CONFIG ON (1=1)
	WHERE TRASPASO."IdEmpresa"=:IdEmpresa  AND TRASPASO."IdEstado"='ING'
) AS TRAS
WHERE "DiasTranscurridos" = :DiasTranscurridos;