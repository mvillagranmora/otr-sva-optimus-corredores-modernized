WITH 
	APlazo AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,PLZ."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,PLZ."IdClasificacionTipoInstrumento"
				,PLZ."IdTipoInstrumento"
				,PLZ."IdInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,PLZ."Cantidad" AS "CantidadAPlazo"
				,PLZ."Cantidad" AS "CantidadPrestamo"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,PLZ."MontoPrecioPromedioCompraOperacionRelacionada" AS "MontoPromedioCompraMonedaSistema"
				,PLZ."MontoPrecioPromedioMonedaAdicionalOperacionRelacionada" AS "MontoPromedioCompraMonedaAdicional"
				,PLZ."INGTasaMercadoActual" AS "TasaMercado"
				,PLZ."INGPrecioMercadoActual" as "PrecioMercado"
				,PLZ."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,PLZ."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				,PLZ."INGMontoATasaCompraActual" AS "MontoATasaCompra"
				,CAR."IdEmisor"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = PLZ."FechaRespaldo"
		INNER JOIN (
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD::HIS.Cartera" C
					WHERE 
					  C."IdPortafolio" IN (:IdPortafolio)
					  AND C."FechaRespaldo" = :FechaHasta
					  AND C."IdEmpresa" = :IdEmpresa
					  AND C."IdTipoInstrumento" IN ('PRT','EMP')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM "BSCL.Custodia.BD::HIS.Cartera"
					WHERE 
					"IdPortafolio" IN (:IdPortafolio)
					AND "FechaRespaldo" = :FechaHasta
					AND "IdClasificacionTipoInstrumento" = 'RV'
				  	AND "IdEmpresa" = :IdEmpresa
				  	AND "IdEstado" = 'VIG'
				   	AND "FechaOperacion" <= :FechaHasta
				  	AND "EGRCantidad" = 0
					AND "IdTipoInstrumento" IN ('PRT','EMP')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
		  AND PLZ."FechaRespaldo" = :FechaHasta
		  AND PLZ."IdClasificacionTipoInstrumento" = 'RV'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("CantidadAPlazo"),TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,IFNULL(SUM("CantidadPrestamo"),TO_DECIMAL(0,26,8)) AS "CantidadPrestamo"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
			,"IdEmisor"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra","TasaMercado","PrecioMercado","IdEmisor"