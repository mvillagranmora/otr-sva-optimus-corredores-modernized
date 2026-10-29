SELECT
	 COALESCE(MESDEF."IdMesa", MES."IdMesa",0)	AS "IdMesa"
	,COALESCE(CONAGE."Valor",'0')				AS "IdAgente"
	,COALESCE(CONTCF."Valor",'')				AS "TipoComprobanteFacturacion"
	,COALESCE(CONIMP."Valor",'')				AS "IdMedioPago"
	,COALESCE(CONNFP."Valor",'')				AS "NombreFormaDePago"
	,COALESCE(CONHFP."Valor",'')				AS "HoraFinProceso"
	,COALESCE(CONIRP."Valor",'0')				AS "IntentosDeReproceso"
	,COALESCE(CONRSD."Valor",'')				AS "RazonSocialDatatec"
FROM DUMMY
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MESDEF
	ON (MESDEF."IdTipoProducto" = 'MEME' AND
		MESDEF."IdEmpresa" = :IdEmpresa AND
		MESDEF."PorDefecto" = 'S' AND
		MESDEF."IdEstadoRegistro" = 'ACT')
LEFT JOIN (
		SELECT TOP 1 "IdTipoProducto", "IdEmpresa", "IdMesa"
		FROM "BSCL.Mesa.BD::MES.Mesa"
		WHERE "IdTipoProducto" = 'MEME'
		  AND "IdEmpresa" = :IdEmpresa
		  AND "IdEstadoRegistro" = 'ACT'
	) MES
	ON (MES."IdTipoProducto" = 'MEME' AND
		MES."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONAGE
	ON (CONAGE."IdTipoProducto" = 'MEME' AND
		CONAGE."IdEmpresa" = :IdEmpresa AND
		CONAGE."CodigoParametro" = 'AGENTE')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONTCF
	ON (CONTCF."IdTipoProducto" = 'MEME' AND
		CONTCF."IdEmpresa" = :IdEmpresa AND
		CONTCF."CodigoParametro" = 'TIPOCOMPROBANTEFACTURACION')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONIMP
	ON (CONIMP."IdTipoProducto" = 'MEME' AND
		CONIMP."IdEmpresa" = :IdEmpresa AND
		CONIMP."CodigoParametro" = 'IDMEDIOPAGO')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONNFP
	ON (CONNFP."IdTipoProducto" = 'MEME' AND
		CONNFP."IdEmpresa" = :IdEmpresa AND
		CONNFP."CodigoParametro" = 'NOMBREFORMADEPAGOSPOTDATATEC')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONHFP
	ON (CONHFP."IdTipoProducto" = 'MEME' AND
		CONHFP."IdEmpresa" = :IdEmpresa AND
		CONHFP."CodigoParametro" = 'HORAFINPROCESO')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONIRP
	ON (CONIRP."IdTipoProducto" = 'MEME' AND
		CONIRP."IdEmpresa" = :IdEmpresa AND
		CONIRP."CodigoParametro" = 'INTENTOSDEREPROCESO')
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONRSD
	ON (CONRSD."IdTipoProducto" = 'MEME' AND
		CONRSD."IdEmpresa" = :IdEmpresa AND
		CONRSD."CodigoParametro" = 'RAZONSOCIALDATATEC');
