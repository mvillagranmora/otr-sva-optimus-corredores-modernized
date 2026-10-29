SELECT 	 CAR."IdCliente"
		,CAR."IdPortafolio"
		,CAR."IdCartera"
		,CAR."FolioCartera"
		,CAR."IdTipoOperacion"						
		,TOPER."Nombre" AS "NombreOperacion"
		,TOPER."Accion" AS "AccionAlContado"
		,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END AS "AccionAPlazo"
		,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'Reportante' ELSE 'Reportado' END AS "TipoPosicion"
		,CAR."FolioComprobante"
		--------------------------------------------------------------------------------------------
		,MAX(SUB."IdSubyacente") AS "IdSubyacente"
		,MAX(TO_VARCHAR(SUB."INGFechaOperacion",'DD-MM-YYYY')) AS "FechaOperacion"
		,MAX(SUB."INGFechaOperacion") AS "FechaOperacionSimultanea"
		,MAX(SUB."IdTipoInstrumento") AS "IdTipoInstrumento"
		,MAX(SUB."IdInstrumento") AS "IdInstrumento"
		,MAX(SUB."Nemotecnico") AS "Nemotecnico"
		,INS."RazonSocial"
		,CAR."INGCantidad" AS "Cantidad"
		,CAR."PrecioCompra" AS "PrecioContado"
		,CAR."INGTasaCompra" AS "Tasa"
		,MAX(SUB."INGMontoATasaCompraActual") AS "MontoATasaCompraActual"	  
		,MAX(SUB."INGMontoCompraActualMonedaInstrumento") AS "MontoCompraActualMonedaInstrumento"
		,SUM(SUB."INGMontoCompraActualMonedaAdicional") AS "MontoCompraActualMonedaAdicional"			
		,MAX(SUB."INGPrecioMercadoActual") AS "PrecioMercado"				 	  			
		,MAX(SUB."INGTasaMercadoActual") AS "TasaMercado"
		,SUM(SUB."INGMontoATasaMercadoActual") AS "MontoMercadoMonedaSistema"
		,SUM(SUB."INGMontoMercadoActualMonedaInstrumento") AS "MontoMercadoMonedaInstrumento"
		,SUM(SUB."INGMontoMercadoActualMonedaAdicional") AS "MontoMercadoMonedaAdicional"
		--------------------------------------------------------------------------------------------
		,TO_VARCHAR(CAR."FechaVencimientoOperacion",'DD-MM-YYYY') AS "FechaVencimientoOperacion"
		,CAR."PlazoEnDiasOperacion"
		,COALESCE(CAR."PrecioAPlazo", CAST(0 AS DECIMAL(26, 8))) AS "PrecioAPlazo"
		,COALESCE(CAR."INGMontoAPlazo", CAST(0 AS DECIMAL(26, 8))) AS "MontoAPlazoMonedaSistema"
		,CAR."MontoAPlazoMonedaInstrumento" 			 	 			
		,CAR."MontoAPlazoMonedaAdicional"
		--------------------------------------------------------------------------------------------
		,MAX(SUB."IdMonedaInstrumento") AS "IdMonedaInstrumento"
		,CAR."MontoCompra"
		,CAR."MontoCompraMonedaAdicional"
		
FROM  "BSCL.Custodia.BD::HIS.Cartera" CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND CAR."FechaRespaldo" = SUB."FechaRespaldo"
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = SUB."IdInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPER ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
 	MovNormal."IdEmpresa" = CAR."IdEmpresa" and 
 	MovNormal."IdComprobante" = CAR."FolioComprobante" and 
 	MovNormal."NemotecnicoSubyacente" = SUB."Nemotecnico"  and
    MovNormal."Cantidad" = Sub."Cantidad" and 
 	MovNormal."Precio" = Sub."PrecioSubyacente"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
 	MovPlazo."IdEmpresa" = CAR."IdEmpresa" and 
 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
 	MovPlazo."NemotecnicoSubyacente" = SUB."Nemotecnico"
WHERE  CAR."FechaRespaldo" = :FechaHasta
  AND CAR."IdTipoInstrumento" IN ('SIM','RRV','RRF')
  AND CAR."IdEstado" = 'VIG' AND SUB."IdEstado" = 'VIG'
  AND CAR."IdEmpresa" = :IdEmpresa
GROUP BY 
		CAR."IdCliente"
		,CAR."IdPortafolio"
		,CAR."IdCartera"
		,CAR."FolioCartera"
		,CAR."IdTipoOperacion"						
		,TOPER."Nombre"
		,TOPER."Accion"
		,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END
		,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'Reportante' ELSE 'Reportado' END
		,CAR."FolioComprobante"
		,INS."RazonSocial"
		,CAR."INGCantidad"
		,CAR."PrecioCompra"
		,CAR."INGTasaCompra"
		,TO_VARCHAR(CAR."FechaVencimientoOperacion",'DD-MM-YYYY')
		,CAR."PlazoEnDiasOperacion"
		,CAR."PrecioAPlazo"		
		,MovPlazo."Precio"
		,CAR."INGMontoAPlazo"
		,CAR."MontoAPlazoMonedaInstrumento" 			 	 			
		,CAR."MontoAPlazoMonedaAdicional"
		,CAR."MontoCompra"
		,CAR."MontoCompraMonedaAdicional";