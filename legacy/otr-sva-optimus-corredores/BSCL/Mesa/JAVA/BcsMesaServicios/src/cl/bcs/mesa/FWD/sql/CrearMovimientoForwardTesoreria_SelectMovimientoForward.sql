SELECT 
INGEGR."FolioCartera"
,FWD."IdPortafolio"
,INGEGR."IdMonedaLiquidacion"
,FWD."IngresoEgreso"
,FWD."TipoVencimiento"
,INGEGR."MontoOperacion"
,FWD."IdAgente"
,FWD."IdSucursal"
,FWD."IdTipoOperacion"
,FWD."FechaOperacion"
,FWD."FechaVencimiento"
,INGEGR."MontoOperacion"
,FWD."IdCliente"
,FWD."IdInstrumento"
,FWD."IdTipoInstrumento"
,FWD."Nemotecnico"
,TIPINS."IdClasificacionTipoInstrumento"
,FWD."IdCartera"
,FWD."FolioOperacion"
,CLI."IdPersona"
,CASE WHEN FWD."Nemotecnico" = 'ARBITR'	THEN FWD."ParidadCierre"
 ELSE FWD."TipoCambioCierre" END AS "TipoCambioOperacion"
,INGEGR."IdOperacion"
,FWD."IdTipoCanalIngreso"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" FWD
INNER JOIN "BSCL.Mesa.BD::GBL.CrearMovimientoForwardTesoreria_Proceso_Paso" INGEGR ON (INGEGR."IdCartera" = FWD."IdCartera" AND INGEGR."FolioCartera" = FWD."IdOperacion" AND INGEGR."IdEstado" = FWD."IdEstado")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (FWD."IdCliente" = CLI."IdCliente")
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" AS TIPINS ON (TIPINS."IdTipoInstrumento" = FWD."IdTipoInstrumento")
WHERE FWD."ModalidadLiquidacionValor" = 'COMPENSADA'