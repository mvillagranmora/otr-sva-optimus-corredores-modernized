
	SELECT 
		 ORD."IdCliente"
		,ORD."IdPortafolioCliente"
		,ORD."IdEstadoOrden"
		,ORD."Nemotecnico"
		,ORD."IdInstrumento"
		,SUM(ORD."Cantidad") AS "Cantidad"
		,SUM(ORD."ValorRescate") AS "ValorRescate"
		,INS."CorteMinimo" 
		,ORD."IdEmisor"
		,ORD."IdBoveda"
		,ORD."EsFungible"
		,ORD."FechaVencimiento"
		,ORD."IdTipoReajuste"	
	FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = ORD."IdInstrumento" 
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS FAC ON FAC."IdOrden" = ORD."IdOrden" AND FAC."IdEstadoFacturacion" = 'FAC'
	WHERE ORD."IdCliente" IN (:idCliente)
	AND ORD."IdPortafolioCliente" IN (:idPortafolio)
	AND ORD."IdClasificacionTipoInstrumento" IN ('RF')
	AND ORD."IdTipoOperacion"IN ('VENRF')
	AND ORD."IdEstadoOrden" ='OFE'
	AND ORD."IdInstrumento" IN (:idInstrumento)
	AND FAC."IdEstadoFacturacion" IS NULL
	GROUP BY  ORD."IdCliente"
			,ORD."IdPortafolioCliente"
			,ORD."IdEstadoOrden"
			,ORD."Nemotecnico"
			,ORD."IdInstrumento"		
			,INS."CorteMinimo" 
			,ORD."IdEmisor"
			,ORD."IdBoveda"
			,ORD."EsFungible"
			,ORD."FechaVencimiento"
			,ORD."IdTipoReajuste"	
			
UNION ALL
	SELECT 
		 ORD."IdCliente"
		,ORD."IdPortafolioCliente"
		,ORD."IdEstadoOrden"
		,ORD."Nemotecnico"
		,ORD."IdInstrumento"
		,SUM(ORD."Cantidad") AS "Cantidad"
		,SUM(ORD."ValorRescate") AS "ValorRescate"
		,INS."CorteMinimo" 
		,ORD."IdEmisor"
		,ORD."IdBoveda"
		,ORD."EsFungible"
		,ORD."FechaVencimiento"
		,ORD."IdTipoReajuste"	
	FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = ORD."IdInstrumento" 
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS FAC ON FAC."IdOrden" = ORD."IdOrden" AND FAC."IdEstadoFacturacion" = 'FAC'
	WHERE ORD."IdCliente" IN (:idCliente)
	AND ORD."IdPortafolioCliente" IN (:idPortafolio)
	AND ORD."IdClasificacionTipoInstrumento" IN ('IF')
	AND ORD."IdTipoOperacion"IN ('VENIIF')
	AND ORD."IdEstadoOrden" ='OFE'
	AND ORD."IdInstrumento" IN (:idInstrumento)
	AND FAC."IdEstadoFacturacion" IS NULL
	GROUP BY  ORD."IdCliente"
			,ORD."IdPortafolioCliente"
			,ORD."IdEstadoOrden"
			,ORD."Nemotecnico"
			,ORD."IdInstrumento"		
			,INS."CorteMinimo" 
			,ORD."IdEmisor"
			,ORD."IdBoveda"
			,ORD."EsFungible"
			,ORD."FechaVencimiento"
			,ORD."IdTipoReajuste"
