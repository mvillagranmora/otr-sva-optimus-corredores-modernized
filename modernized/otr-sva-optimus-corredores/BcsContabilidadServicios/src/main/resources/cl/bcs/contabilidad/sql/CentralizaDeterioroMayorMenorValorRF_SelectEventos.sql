WITH v_TabEventosPaso AS(
	    SELECT
	             "IdCartera"                       	AS "IdMovimiento"
	            ,TO_BIGINT(:p_IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	            , CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
					THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
					ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
				END AS "MontoDeterioro"
	            ,"IdMonedaSistema"
	            , CASE WHEN "IdMonedaInstrumento" <> '' THEN "IdMonedaInstrumento" ELSE "IdMonedaSistema" END AS "IdMonedaInstrumento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR  --ORDER BY "FolioCartera" DESC
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   CAR."IdEstado" = 'VIG'
	AND   CAR."IdTipoOperacion" IN ('COMRF','INGTIT')
	AND   CAR."IdTipoProducto" IN( 'INRF','CUST') 
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
	
	UNION ALL 
	
	SELECT
	             "IdCartera"                       	AS "IdMovimiento"
	            ,TO_BIGINT(:p_IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	          	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
					THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
					ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
				END AS "MontoDeterioro"
	            ,"IdMonedaSistema"
	            ,CASE WHEN "IdMonedaInstrumento" <> '' THEN "IdMonedaInstrumento" ELSE "IdMonedaSistema" END AS "IdMonedaInstrumento"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor" 
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   OPE."IdEstado" = 'VIG'
	AND   CAR."IdEstado" = 'VIG'
	AND   CAR."IdTipoOperacion" IN ('COMTER')
	AND   CAR."IdTipoProducto" = 'MERF'
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   CAR."IdTipoInstrumento" != 'PAC'
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
	
	UNION ALL 
	--Pactos
	
	SELECT
	             "IdCartera"                       	AS "IdMovimiento"
	            ,TO_BIGINT(:p_IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	            , CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
					THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
					ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
				END AS "MontoDeterioro"
	            ,"IdMonedaSistema"
	            ,CASE WHEN "IdMonedaInstrumento" <> '' THEN "IdMonedaInstrumento" ELSE "IdMonedaSistema" END AS "IdMonedaInstrumento"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE  
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor"
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   OPE."IdEstado" = 'VIG'
	AND   CAR."IdEstado" IN ('VIG','RPA')
	AND   CAR."IdTipoOperacion" IN ('VENPAC')
	AND   CAR."IdTipoProducto" = 'MERF'
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   CAR."IdTipoInstrumento" != 'PAC'
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
	UNION ALL
	--Garantias

	SELECT
	             GAR."IdGarantia"                       	AS "IdMovimiento"
	            ,TO_BIGINT(:p_IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	            ,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF'
					THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(GAR."INGValorRescate",0) AS DECIMAL(26,8)))
					ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(GAR."INGCantidad",0) AS DECIMAL(26,8)))
				END AS "MontoDeterioro"
	            ,GAR."IdMonedaSistema"
	            ,CASE WHEN GAR."IdMonedaInstrumento" <> '' THEN GAR."IdMonedaInstrumento" ELSE GAR."IdMonedaSistema" END AS "IdMonedaInstrumento"
	FROM  "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"  AS GAR
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = GAR."IdPortafolio" 
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR."IdEstado" = 'VIG'
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = GAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE GAR."IdEmpresa" = :p_IdEmpresa
	AND   GAR."IdEstado"='VIG' 
	AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND DET."FlagDeterioro" = 'S'
	AND DET."PorcentajeDeterioro" > 0
	AND GAR."FechaOperacion" <=:p_FechaContable
	AND CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
)
SELECT "IdMovimiento"
	,"IdTipoCentralizacion",
	'MONTO_DETERIORO' AS "IdEvento"
	,'Monto Deterioro'	AS "Nombre"
	,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(v_TabEventosPaso."MontoDeterioro",MF."MontoDecimales",MF."MontoTrucaRedondea"), v_TabEventosPaso."MontoDeterioro") AS "MontoEvento"
	,"IdMonedaInstrumento" AS "CodigoMoneda"
FROM v_TabEventosPaso
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			= v_TabEventosPaso."IdMonedaInstrumento" 
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= '';
