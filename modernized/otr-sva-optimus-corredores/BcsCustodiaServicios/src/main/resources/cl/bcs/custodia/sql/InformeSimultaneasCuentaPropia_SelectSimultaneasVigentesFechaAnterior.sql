SELECT 
	--GENERALES
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion",
	CAR."IdCliente",	
    CAR."IdPortafolio",
    CAR."IdCartera",
    CAR."FolioCartera",
    --OPERACION
    CAR."IdTipoOperacion",
    TO_NVARCHAR(CAR."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
    TO_NVARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoOperacion",
    CAR."IdOperacion",
    CAST(DAYS_BETWEEN( CAR."FechaOperacion", CAR."FechaVencimientoOperacion" ) AS DECIMAL ) AS "PlazoEnDiasOperacion",
    CAR."IdCondicionLiquidacion",
    TO_NVARCHAR(CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
    'SIM' AS "Diferenciador",
    --INSTRUMENTO
    CAR."IdClasificacionTipoInstrumento",
    CAR."IdTipoInstrumento",
    Sub."IdInstrumento" AS "IdInstrumento",
    Sub."IdEmisor" AS "Emisor",
    Sub."Nemotecnico" AS "Nemotecnico",
    Sub."NemoSVS" AS "NemoSVS",
    CAR."IdTipoReajuste",
    CAR."IdMonedaInstrumento",
    CAR."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
    '' AS "IdTipoReajusteInstrumento",
    '' AS "FechaEmision",
    '' AS "FechaVencimientoInstrumento",
    '' AS "CodigoIFRS",
    '' AS "EsFungible",
    --VALOR ACTUAL
    Sub."INGCantidad" AS "Cantidad",
    Sub."INGValorRescate" AS "ValorRescate",
    CAR."INGPrecioCompra" AS "PrecioCompra",
    CAR."INGTasaCompra" AS "TasaCompra",
    CAR."INGCantidad"  * CAR."PrecioCompra" AS "MontoCompra",
    CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraMonedaInstrumento", 
    CAR."INGMontoCompraActualMonedaAdicional" AS "MontoCompraMonedaAdicional",
    CSDN."NombreCompleto" AS "NombreCliente",
    CAR."PrecioAPlazo",
    CAR."INGMontoAPlazo" AS "MontoAPlazo",
    car."MontoAPlazoMonedaAdicional" AS "MontoAPlazoMonedaAdicional",
    car."INGMontoAPlazoMonedaInstrumento" AS "MontoAPlazoMonedaInstrumento",
    --DATOS INFORME
    '' AS "IdTipoReajusteCabecera",
    'CARTERA' AS "TablaOrigen",
    '' AS "IdUso",
    NULL AS "TipoCartera",
    'CARTERA.SIMULTANEA' AS "OrigenProcesamiento",
    CAR."IdConcepto" AS "IdConcepto",
    CAR."IdBoveda",
    CAR."IdAgente",
    0.0 AS "MontoPactado",
    MovNormal."IdCorredoraContraparte",
    TO_NVARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
    DAYS_BETWEEN(CAR."FechaVencimientoOperacion",:FechaConsulta) AS "PlazoActual",
    CAR."FolioComprobante",
    Car."IdBolsa",
    MovNormal."FolioTransaccion" AS "FolioTransaccionNormal",
	MovPlazo."FolioTransaccion" AS "FolioTransaccionPlazo" ,
	CAR."IdTipoProducto"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS Car
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" AS ultSub ON 
    ultSub."FolioCarteraRelacionada" = Car."FolioCartera" 
    AND ultSub."FechaRespaldo" = Car."FechaRespaldo"
    AND ultSub."FechaRespaldo" = :FechaConsulta
INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS Sub ON 
    Sub."IdSubyacente" = ultSub."IdSubyacente" 
    AND ultSub."FechaRespaldo" = Sub."FechaRespaldo"
    AND Sub."FechaRespaldo" = :FechaConsulta
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CSDN ON 
CSDN."IdCliente" = Car."IdCliente"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovNormal ON 
    MovNormal."IdEmpresa" = Car."IdEmpresa" 
    AND MovNormal."IdComprobante" = Car."FolioComprobante" 
    AND MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico"
    AND MovNormal."Cantidad" = Car."Cantidad"
    AND TRIM(MovNormal."FolioTransaccion") = Car."FolioExternoReferencia"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" as MovPlazo on 
	 	MovPlazo."IdEmpresa" = Car."IdEmpresa" and 
	 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
	 	MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico"
WHERE Car."IdEstado" = 'VIG'
    AND Car."IdTipoInstrumento" = 'SIM'
    AND Car."IdCliente" = :IdCliente
    AND Car."FechaVencimientoOperacion" > TO_DATE(:FechaConsulta)
    AND Car."INGFechaOperacion" <= TO_DATE(:FechaConsulta)
    AND Car."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN Car."IdPortafolio" ELSE :IdPortafolio END
    AND Car."FechaRespaldo" = :FechaConsulta;