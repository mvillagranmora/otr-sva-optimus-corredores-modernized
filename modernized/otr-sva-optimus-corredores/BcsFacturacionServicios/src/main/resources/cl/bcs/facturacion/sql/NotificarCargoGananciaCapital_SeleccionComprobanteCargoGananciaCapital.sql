
SELECT CPE."Direccion" AS "CORREO_ELECTRONICO",
COM."NombreLargoCliente" AS "CLIENTE",
COM."DocumentoIdentidad" AS "RUT",
CPO."CodigoPropositoPortafolio" AS "CUENTA", --validar con PO
COM."FechaIngresoRegistro" AS "FECHA",
COM."IdMoneda" AS "TIPO_MONEDA",
MON."Nombre" AS "NOMBRE_MONEDA",
CASE when CD."IdMonedaComprobanteDetalle" = 'CLP' THEN round(CD."MontoRetencionGananciaCap",0) ELSE CD."MontoRetencionGananciaCap" end AS "RETENCION_GANANCIA_CAP",
CD."MontoLiquidacion" AS "OPERACION_VENTA",
CASE when CD."IdMonedaComprobanteDetalle" = 'CLP' then round((CD."MontoLiquidacion" - CD."MontoRetencionGananciaCap"),0) else (CD."MontoLiquidacion" - CD."MontoRetencionGananciaCap") end AS "IMPORTE_NETO",
COM."FolioComprobante" AS "NUMERO_COMPROBANTE",
COM."FechaComprobante" AS "FECHA_FACTURACION",
COM."DiasPlazo" AS "RETENCION_DIAS",
CP."IdPersona",
CPE."IdDireccionElectronica",
LM."Pais" AS "PaisMoneda"
FROM  "BSCL.Facturacion.BD::FAC.Comprobante" AS COM  LEFT JOIN
"BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CD on CD."IdComprobante" = COM."IdComprobante" LEFT join
"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"AS CP ON com."DocumentoIdentidad" = CP."DocumentoIdentidad" LEFT JOIN 
"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPE ON CPE."IdPersona" = CP."IdPersona" AND CPE."Predeterminado" = 'UI_SI' LEFT JOIN 
"BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" AS CS ON CS."IdSucursal" = COM."IdSucursalEmpleadoEjecutivoPortafolio" LEFT JOIN 
"BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS CPO ON CPO."IdPortafolio" = COM."IdPortafolio" LEFT JOIN 
"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MON ON MON."IdMoneda" = COM."IdMoneda" LEFT JOIN 
"BSCL.Plataforma.BD::PLA.LocalizacionMoneda" AS LM ON LM."IdMoneda" = MON."IdMoneda"
WHERE com."CargoGananciaCap"='C' and COM."MontoRetencionGananciaCap" > 0 AND COM."IdEstadoDTE" = 'REC' AND
COM."IdEmpresa" = :p_IdEmpresa and COM."FechaIngresoRegistro" between to_date(:p_FechaEjecucion,'YYYY-MM-DD') and add_days(to_date(:p_FechaEjecucion, 'YYYY-MM-DD'), +1)