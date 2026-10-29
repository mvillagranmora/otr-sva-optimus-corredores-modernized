-- obtiene y guarda en tabla global datos de proceso
INSERT INTO "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso"
(
	"NumeroReferencia","IdIngresoEgresoInformado","TipoComprobante",
	"IdIngresoEgreso","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto",
	"FechaLiquidacion","FechaIngresoRegistro","FechaMovimiento","IdTipoPersona","IdPersona",
	"IdTipoDocumentoIdentidad","IdTipoPersonaTitularPago","IdPersonaTitularPago",
	"IdPortafolioTitularPago","SecuenciaAgrupacion","SecuenciaRelacionada",
	"Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen",		
	"IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen","IdTipoProducto",
	"IdTipoOperacion","IdBolsa","IdMonedaOperacion","MontoOperacion","FechaParidad","IdTipoReajuste",		
	"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento",
	"EsBilateral","TitularPago","EsIngresoAutomatico","NeteaComprobante","AgrupaComprobante",
	"MueveCuentaInversion","IdEstado","IdEmpresa","IdCajaComprobante","CantidadSecuenciaMovimiento","CantidadSecuenciaComprobante",	
	"NombrePersona","DocumentoIdentidad", "NombrePersonaTitularPago","IdCondicionDeLiquidacion"	
)
SELECT
	IngEgrAGenerar."NumeroReferencia", IngEgrAGenerar."IdIngresoEgreso" as "IdIngresoEgresoInformado", IngEgrAGenerar."TipoComprobante", 
	COALESCE(IngrEgr."IdIngresoEgreso",0) AS "IdIngresoEgreso",
	IngrEgr."IdTipoMovimiento",IngrEgr."IdConcepto",IngrEgr."IdCaja",IngrEgr."IdMoneda",IngrEgr."Monto",
	IngrEgr."FechaLiquidacion",IngrEgr."FechaIngresoRegistro",IngrEgr."FechaMovimiento",IngrEgr."IdTipoPersona",IngrEgr."IdPersona",
	IngrEgr."IdTipoDocumentoIdentidad",IngrEgr."IdTipoPersonaTitularPago",IngrEgr."IdPersonaTitularPago",
	IngrEgr."IdPortafolioTitularPago",IngrEgr."SecuenciaAgrupacion",IngrEgr."SecuenciaRelacionada",
	IngrEgr."Glosa",IngrEgr."IdEmpleadoDigitador",IngrEgr."IdSucursalEmpleadoDigitador",IngrEgr."IdTipoComprobanteOrigen",			
	IngrEgr."IdComprobanteOrigen",IngrEgr."FechaComprobanteOrigen",IngrEgr."FechaLiquidacionComprobanteOrigen",IngrEgr."IdTipoProducto",
	IngrEgr."IdTipoOperacion",IngrEgr."IdBolsa",IngrEgr."IdMonedaOperacion",IngrEgr."MontoOperacion",IngrEgr."FechaParidad",IngrEgr."IdTipoReajuste",
	IngrEgr."ValorParidad",IngrEgr."FolioCartera",IngrEgr."IdInstrumento",IngrEgr."IdTipoInstrumento",IngrEgr."IdClasificacionTipoInstrumento",
	IngrEgr."EsBilateral",IngrEgr."TitularPago",IngrEgr."EsIngresoAutomatico",IngrEgr."NeteaComprobante",IngrEgr."AgrupaComprobante",
	IngrEgr."MueveCuentaInversion",IngrEgr."IdEstado",:IdEmpresa, :IdCajaComprobante,
	COALESCE(SIE."CantidadSecuenciaMovimiento",0) as "CantidadSecuenciaMovimiento",    -- Para validar si falta incluir movimientos segun la secuencia agrupacion comprobante
    COALESCE(SECC."CantidadSecuenciaComprobante",0) as "CantidadSecuenciaComprobante", -- Para validar que la secuencia de comprobante no esté utilizada
    Per."NombreCompleto" as "NombrePersona",
	Per."DocumentoIdentidad",
	PerTP."NombreCompleto" as "NombrePersonaTitularPago",
	COALESCE(FAC."IdCondicionLiquidacion",IngrEgr."IdCondicionLiquidacion",'') as "IdCondicionDeLiquidacion"

FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar" As IngEgrAGenerar
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" As IngrEgr ON (IngEgrAGenerar."IdIngresoEgreso" = IngrEgr."IdIngresoEgreso")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Per   ON Per."IdPersona"   = IngrEgr."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PerTP ON PerTP."IdPersona" = IngrEgr."IdPersonaTitularPago"
LEFT JOIN (SELECT "SecuenciaAgrupacion", COUNT(DISTINCT "SecuenciaAgrupacion") as "CantidadSecuenciaMovimiento"
 		   FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" as E
 		  INNER JOIN ( SELECT "IdIngresoEgreso" 
 		  			   FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" WHERE "IdEstado" = 'ING'
 		  			   EXCEPT 
 		  			   SELECT "IdIngresoEgreso" 
 		  			   FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"
 		  			 ) AS I ON I."IdIngresoEgreso" = E."IdIngresoEgreso"
          GROUP BY "SecuenciaAgrupacion"
		) AS SIE ON SIE."SecuenciaAgrupacion" = IngrEgr."SecuenciaAgrupacion" AND IngrEgr."SecuenciaAgrupacion" <> ''
		  			
		  			
LEFT JOIN (SELECT "SecuenciaAgrupacion", COUNT(DISTINCT "SecuenciaAgrupacion") as "CantidadSecuenciaComprobante"
 		   FROM "BSCL.Tesoreria.BD::TES.Comprobante"
          GROUP BY "SecuenciaAgrupacion"
		) AS SECC ON SECC."SecuenciaAgrupacion" = IngrEgr."SecuenciaAgrupacion" AND IngrEgr."SecuenciaAgrupacion" <>''
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS FAC ON FAC."IdComprobante" = IngrEgr."IdOperacion" AND FAC."IdTipoOperacion" = IngrEgr."IdTipoOperacion"

			 
WHERE IngrEgr."IdEmpresa" = :IdEmpresa;
--FOR UPDATE OF IngrEgr."IdEstado";


