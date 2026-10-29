SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia" As
SELECT  MOV."IdClasificacionTipoInstrumento",
		MOV."IdTipoInstrumento",
		MOV."IdInstrumento",
		MOV."IdEmisor",
		CASE WHEN MOV."IdTipoOperacion" IN ('COMSIM','VENSIM','COMRRV','COMRRF','VENRRV','VENRRF') THEN MOV."NemotecnicoSubyacente"
			 WHEN MOV."IdTipoOperacion" = 'SUSOSA' THEN MOV."NemotecnicoRelacionado"
			 ELSE MOV."NemotecnicoInstrumento"
		 END AS "Nemotecnico",
		'' AS "NemoSVS",
		MOV."IdMoneda",
		MOV."IdTipoReajuste",
		MOV."FechaEmision",
		CASE WHEN MOV."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN MOV."FechaVencimiento" 
			 ELSE '0001-01-01'
		 END AS "FechaVencimiento",
		MOV."CodigoIFRS",
		MOV."EsFungible",
		MOV."IdBoveda", 
		MOV."IdBolsa",
		MOV."IdTipoOperacion",
		TOPE."Accion" As "AccionTipoOperacion",
		MOV."FechaLiquidacion",
		MOV."Cantidad",
		MOV."ValorRescate",
		MOV."Precio",
		MOV."Tasa",
		MOV."Monto",
		MOV."IdCliente",
		MOV."IdPortafolio",
		MOV."IdMovimientoComprobante",
		MOV."IdConceptoMovimiento",
		MOV."IdEmpresa",
		MOV."IdInstrumentoSubyacente"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON TOPE."IdTipoOperacion" = MOV."IdTipoOperacion"
WHERE "ActualizarCustodia" = 'S'
  AND "IdEstadoFacturacion"  = 'ING';
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
