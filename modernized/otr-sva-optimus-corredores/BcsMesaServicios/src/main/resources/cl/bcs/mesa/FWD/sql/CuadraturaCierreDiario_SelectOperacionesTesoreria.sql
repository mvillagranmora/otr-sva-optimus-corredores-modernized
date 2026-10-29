SELECT 
CAR."IdOperacion"
,CAR."FolioCartera"
,CLI."NombreCompleto" AS "RazonSocial"
,CAR."MontoMonedaPrincipal"
,CAR."MontoMonedaSecundaria"
,CAR."IdMonedaPrincipal"
,CAR."IdMonedaSecundaria"
,CAR."IdTipoProducto" AS "IdMesa"
,CASE WHEN COALESCE(CT."IdEstado",'PEN') != 'CRT' AND COALESCE(COM."IdEstado",'PEN') != 'LIQ' THEN 'PEN' ELSE 'CRT' END AS "IdEstado"
,CAR."INGMontoValorRazonableActivo" AS "RazonableActivo"
,CAR."INGMontoValorRazonablePasivo" AS "RazonablePasivo"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" cPag
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON (cPag."IdOperacion"= car."IdCartera")
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE ON (OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM ON (PARAM."IdParametro" = OPE."IdModalidadLiquidacion" AND PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND PARAM."IdEmpresa" = OPE."IdEmpresa")
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CT ON  CT."IdTipoProducto" = 'MEFW' AND CT."IdComprobante" = CAR."FolioCartera"
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COM ON COM."IdComprobanteOrigen" = CAR."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON  CAR."IdCliente" = CLI."IdCliente"
WHERE car."IdTipoProducto" = 'MEFW' AND CASE
		WHEN CAR."IdEstado"='ANT' OR (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRFechaOperacion"
		WHEN CAR."IdEstado"='ANU' AND CAR."EGRIdConcepto" IN ('ANTFWD','REVFWD') THEN CAR."EGRFechaOperacion"
		ELSE CAR."FechaVencimientoOperacion" END = :Fecha AND OPE."IdEstado" = 'LIQ' AND cPag."AfectarTesoreria" = 'S' AND PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdEstado" != 'MOD'
