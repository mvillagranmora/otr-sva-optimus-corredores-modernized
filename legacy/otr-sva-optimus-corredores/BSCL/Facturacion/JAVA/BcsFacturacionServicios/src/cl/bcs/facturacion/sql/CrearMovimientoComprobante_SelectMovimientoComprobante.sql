SELECT MVTO."NumeroMvtoCmbte",MVTO."IdConceptoMovimiento",MVTO."IdTipoComprobante",MVTO."IdCondicionDeLiquidacion",MVTO."IdTipoReajuste",MVTO."IdTipoConvenio",
MVTO."IdEmisor",MVTO."IdMonedaLiquidacion",MVTO."IdInstrumento",MVTO."IdInstrumentoRelacionado",MVTO."IdEmpleadoDigitador",MVTO."IdInstrumentoSubyacente",
MVTO."IdTipoProducto",MVTO."IdMoneda",MVTO."IdMonedaSubyacente",MVTO."IdTipoCanalIngreso",MVTO."IdTipoOperacion",MVTO."IdBoveda",MVTO."IdCorredoraContraparte",
MVTO."IdBolsa",MVTO."IdOrden",

CPTO."IdConcepto" AS "IdConceptoReal",
TCMBTE."IdTipoComprobante" AS "IdTipoComprobanteReal",
CLIQ."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacionReal",
TREAJ."IdTipoReajuste" AS "IdTipoReajusteReal",
TCONV."IdTipoConvenio" AS "IdTipoConvenioReal",
EMI."IdEmisor" AS "IdEmisorReal",
MONLIQ."IdMoneda" AS "IdMonedaLiquidacionReal",
MONLIQ."IdEstado" AS "IdEstadoMonedaLiquidacion",
INS."IdInstrumento" AS "IdInstrumentoReal",
COALESCE(INS."ISIN",'') AS "ISIN",
RELINS."IdInstrumentoRelacionado" AS "IdInstrumentoRelacionadoReal",
SUB."IdInstrumento" AS "IdInstrumentoSubyacenteReal",
COALESCE(SUB."ISIN",'') AS "ISINInstrumentoSubyacente",
EMPDIGITADOR."IdEmpleado" AS "IdEmpleadoDigitadorReal",
TPROD."IdTipoProducto" AS "IdTipoProductoReal",
MONMVTO."IdMoneda" AS "IdMonedaMovimientoComprobanteReal",
MONMVTO."IdEstado" AS "IdEstadoMonedaMovimientoComprobante",
MONSUB."IdMoneda" AS "IdMonedaSubyacenteReal",
MONSUB."IdEstado" AS "IdEstadoMonedaSubyacente",
TCANAL."IdTipoCanalIngreso" AS "IdTipoCanalIngresoReal",
TOPE."IdTipoOperacion" AS "IdTipoOperacionReal",
BOV."IdBoveda" AS "IdBovedaReal",
COR."IdCorredora" AS "IdCorredoraContraparteReal",
BOL."IdBolsa" AS "IdBolsaReal",
ORD."IdOrden" AS "IdOrdenReal",

INSREAJ."CorteMinimo",
TIPINS."IdTipoInstrumento",
TIPINS."Nombre" AS "NombreTipoInstrumento",
TIPINS."IdClasificacionTipoInstrumento",
CLAINS."Nombre" AS "NombreClasificacionTipoInstrumento",

TIPINSREL."IdTipoInstrumento" AS "IdTipoInstrumentoInstrumentoRelacionado",
TIPINSREL."Nombre" AS "NombreTipoInstrumentoRelacionado",
TIPINSREL."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoInstrumentoSubyacente",
CLAREL."Nombre" AS "NombreClasificacionTipoInstrumentoRelacionado",

TIPINSSUB."IdTipoInstrumento" AS "IdTipoInstrumentoInstrumentoSubyacente",
TIPINSSUB."Nombre" AS "NombreTipoInstrumentoSubyacente",
TIPINSSUB."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoInstrumentoSubyacente",

PREF."CodigoValor" AS "ValorModoFacturacion",
EMPPORT."IdEmpleado" AS "IdEmpleadoPortafolio",
COALESCE(EMPPORT."IdSucursal",0) AS "IdSucursalEmpleadoPortafolio",
COALESCE(SUCPORT."NombreSucursal",'') AS "NombreSucursalEmpleadoPortafolio",
COALESCE(EMPPORT."NombreCompleto",'') AS "NombreCompletoEmpleadoPortafolio",

EMPDIG."IdEmpleado" AS "IdEmpleadoDigitador",
COALESCE(EMPDIG."IdSucursal",0) AS "IdSucursalEmpleadoDigitador",
COALESCE(SUCDIG."NombreSucursal",'') AS "NombreSucursalEmpleadoDigitador",
COALESCE(EMPDIG."NombreCompleto",'') AS "NombreCompletoEmpleadoDigitador",

TCANAL."Codigo" AS "CodigoTipoCanalIngreso",

CLI."DocumentoIdentidad",
CLI."TipoDocumentoIdentidad",
CASE WHEN COALESCE(PORT."Alias",'') = '' THEN CLI."NombreCompleto" ELSE PORT."Alias" END AS "NombreCompleto",
COALESCE(TIPOCLI."Nombre",'') AS "NombreTipoCliente",

COALESCE(TPROD."Codigo",'') AS "CodigoTipoProducto",
COALESCE(TOPE."Codigo",'') AS "CodigoTipoOperacion",
COALESCE(BOL."Codigo",'') AS "CodigoBolsa",
COALESCE(EMI."NombreCompleto",'') AS "NombreCompletoEmisor",
"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL AS "IdMovimientoComprobante", 
INS."Nemotecnico" AS "NemotecnicoInstrumento",
REL."Nemotecnico" AS "NemotecnicoInstrumentoRelacionado",
SUB."Nemotecnico" AS "NemotecnicoInstrumentoSubyacente",

COALESCE(CREF."IdComprobante", MVTO."IdComprobanteReferencia", 0)  AS "IdComprobanteReferencia",
CREF."FolioComprobante" AS "FolioComprobanteReferencia",
CREF."FechaComprobante" AS "FechaComprobanteReferencia",
COALESCE(CREF."IdTipoComprobante", '') AS "IdTipoComprobanteReferencia",
CPTO."Contrapartida"

FROM "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso" MVTO
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" CPTO ON MVTO."IdConceptoMovimiento" = CPTO."IdConcepto"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" TCMBTE ON MVTO."IdTipoComprobante" = TCMBTE."IdTipoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CLIQ ON MVTO."IdCondicionDeLiquidacion" = CLIQ."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" TREAJ ON MVTO."IdTipoReajuste" = TREAJ."IdTipoReajuste"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoConvenio" TCONV ON MVTO."IdTipoConvenio" = TCONV."IdTipoConvenio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON MVTO."IdEmisor" = EMI."IdEmisor"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONLIQ ON MVTO."IdMonedaLiquidacion" = MONLIQ."IdMoneda"

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON MVTO."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON INS."IdTipoInstrumento" = TIPINS."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLAINS ON TIPINS."IdClasificacionTipoInstrumento" = CLAINS."IdClasificacionTipoInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::INS.RelacionInstrumento" RELINS ON MVTO."IdInstrumentoRelacionado" = RELINS."IdInstrumentoRelacionado" AND RELINS."IdEstado" = 'VIG'
--LEFT JOIN "BSCL.Plataforma.BD::INS.RelacionInstrumento" RELINS ON MVTO."IdInstrumentoRelacionado" = RELINS."IdInstrumento" AND RELINS."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" REL ON MVTO."IdInstrumentoRelacionado" = REL."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINSREL ON REL."IdTipoInstrumento" = TIPINSREL."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLAREL ON TIPINSREL."IdClasificacionTipoInstrumento" = CLAREL."IdClasificacionTipoInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" SUB ON MVTO."IdInstrumentoSubyacente" = SUB."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINSSUB ON SUB."IdTipoInstrumento" = TIPINSSUB."IdTipoInstrumento"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPDIGITADOR ON MVTO."IdEmpleadoDigitador" = EMPDIGITADOR."IdEmpleado"

LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TPROD ON MVTO."IdTipoProducto" = TPROD."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONMVTO ON MVTO."IdMoneda" = MONMVTO."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSUB ON MVTO."IdMonedaSubyacente" = MONSUB."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TCANAL ON MVTO."IdTipoCanalIngreso" = TCANAL."IdTipoCanalIngreso"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON MVTO."IdTipoOperacion" = TOPE."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON MVTO."IdBoveda" = BOV."IdBoveda"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COR ON MVTO."IdCorredoraContraparte" = COR."IdCorredora"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" BOL ON MVTO."IdBolsa" = BOL."IdBolsa"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON MVTO."IdOrden" = ORD."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" INSREAJ ON MVTO."IdInstrumento" = INSREAJ."IdInstrumento" AND MVTO."IdTipoReajuste" = INSREAJ."IdTipoReajuste"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON PORT."IdPortafolio" = MVTO."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = PORT."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" tipocli ON CLI."IdTipoCliente" = tipocli."IdTipoCliente" AND tipocli."IdEmpresa" = CLI."IdEmpresa"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" PREF ON MVTO."IdPortafolio" = PREF."IdPortafolio" AND PREF."Codigo" = 'FAC_MODFAC'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPPORT ON PORT."IdEmpleadoEjecutivo" = EMPPORT."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPDIG ON MVTO."IdEmpleadoDigitador" = EMPDIG."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUCPORT ON SUCPORT."IdSucursal" = EMPPORT."IdSucursal"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUCDIG ON SUCDIG."IdSucursal" = EMPDIG."IdSucursal"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CREF ON CREF."IdComprobante" = MVTO."IdComprobanteReferencia"


/*SET SCHEMA BSCL

INSERT INTO "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso"
("NumeroMvtoCmbte","IdConcepto","IdTipoComprobante","IdCondicionDeLiquidacion","IdTipoReajuste","IdTipoConvenio",
"IdEmisor","IdMonedaLiquidacion","IdInstrumento","IdInstrumentoRelacionado","IdEmpleadoDigitador","IdInstrumentoSubyacente",
"IdTipoProducto","IdMoneda","IdMonedaSubyacente","IdTipoCanalIngreso","IdTipoOperacion","IdBoveda","IdCorredoraContraparte",
"IdBolsa","IdOrden","IdPortafolio")
VALUES
(3729332,'CMACNA','FAE','T0','','SCV',
'FALABELLA','CLP',916,502,7307,306,
'INAC','CLP','','TRA','COMACN','DCV',2001,
'BCS',315357,343710)


*/

