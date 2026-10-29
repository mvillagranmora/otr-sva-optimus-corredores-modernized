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
				,PLZ."INGMontoATasaCompraActual" AS "MontoATasaCompra"
				,CAR."IdEmisor"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera" C
					WHERE 
					  C."IdPortafolio" IN (:IdPortafolio)
					  AND C."IdEmpresa" = :IdEmpresa
					  AND C."IdTipoInstrumento" IN ('PRT','EMP')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera"
					WHERE 
					"IdPortafolio" IN (:IdPortafolio)
					AND "IdClasificacionTipoInstrumento" = 'RV'
				  	AND "IdEmpresa" = :IdEmpresa
				  	AND "IdEstado" = 'VIG'
				   	AND "FechaOperacion" <= :FechaHasta
				  	AND "EGRCantidad" = 0
					AND "IdTipoInstrumento" IN ('PRT','EMP')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
		  AND PLZ."IdClasificacionTipoInstrumento" = 'RV'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaAdicional"
			,IFNULL(SUM("CantidadAPlazo"),TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,IFNULL(SUM("CantidadPrestamo"),TO_DECIMAL(0,26,8)) AS "CantidadPrestamo"
			,IFNULL(SUM("MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra","IdEmisor"