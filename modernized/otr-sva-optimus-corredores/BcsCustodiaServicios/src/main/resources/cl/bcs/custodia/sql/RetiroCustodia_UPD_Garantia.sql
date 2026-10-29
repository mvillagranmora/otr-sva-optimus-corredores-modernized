/********************************************************** 
Proposito	: Script para la actualizacion de la tabla Garantia
autor		: NRM
fecha		: 22/11/2017
***********************************************************/

UPDATE "BSCL.Custodia.BD::GTA.Garantia" SET 
  "IdEstado" = COALESCE(:IdEstado,"IdEstado")
, "FechaModificacionRegistro" = CURRENT_TIMESTAMP
, "EGRTicketValorizacion" = COALESCE(:EGRTicketValorizacion,"EGRTicketValorizacion")
, "EGRValorizacionConProblemas" = COALESCE(:EGRValorizacionConProblemas,"EGRValorizacionConProblemas")
, "EGRFechaFactorMonedaInstrumento" = COALESCE(:EGRFechaFactorMonedaInstrumento,"EGRFechaFactorMonedaInstrumento")
, "EGRValorFactorMonedaInstrumento" = COALESCE(:EGRValorFactorMonedaInstrumento,"EGRValorFactorMonedaInstrumento")
, "EGRFechaFactorMonedaAdicional" = COALESCE(:EGRFechaFactorMonedaAdicional,"EGRFechaFactorMonedaAdicional")
, "EGRValorFactorMonedaAdicional" = COALESCE(:EGRValorFactorMonedaAdicional,"EGRValorFactorMonedaAdicional")
, "EGRFechaOperacion" = COALESCE(:EGRFechaOperacion,"EGRFechaOperacion")
, "EGRIdTipoOperacion" = COALESCE(:EGRIdTipoOperacion,"EGRIdTipoOperacion")
, "EGRIdOperacion" = COALESCE(:EGRIdOperacion,"EGRIdOperacion")
, "EGRIdMovimientoCarteraEnTransito" = COALESCE(:EGRIdMovimientoCarteraEnTransito,"EGRIdMovimientoCarteraEnTransito")
, "EGRIdTipoCanalIngreso" = COALESCE(:EGRIdTipoCanalIngreso,"EGRIdTipoCanalIngreso")
, "EGRIdCondicionLiquidacion" = COALESCE(:EGRIdCondicionLiquidacion,"EGRIdCondicionLiquidacion")
, "EGRFechaLiquidacion" = COALESCE(:EGRFechaLiquidacion,"EGRFechaLiquidacion")
, "EGRIdTipoComprobante" = COALESCE(:EGRIdTipoComprobante,"EGRIdTipoComprobante")
, "EGRFolioComprobante" = COALESCE(:EGRFolioComprobante,"EGRFolioComprobante")
, "EGRFechaComprobante" = COALESCE(:EGRFechaComprobante,"EGRFechaComprobante")
, "EGRFolioExternoReferencia" = COALESCE(:EGRFolioExternoReferencia,"EGRFolioExternoReferencia")
, "EGRIdModulo" = COALESCE(:EGRIdModulo,"EGRIdModulo")
, "EGRIdConcepto" = COALESCE(:EGRIdConcepto,"EGRIdConcepto")
, "EGRIdUsuario" = COALESCE(:EGRIdUsuario,"EGRIdUsuario")
, "EGRIdSucursal" = COALESCE(:EGRIdSucursal,"EGRIdSucursal")
, "EGRIdAgente" = COALESCE(:EGRIdAgente,"EGRIdAgente")
, "EGRCantidad" = COALESCE(:EGRCantidad,"EGRCantidad")
, "EGRValorRescate" = COALESCE(:EGRValorRescate,"EGRValorRescate")
, "EGRPrecio" = COALESCE(:EGRPrecio,"EGRPrecio")
, "EGRTasa" = COALESCE(:EGRTasa,"EGRTasa")
, "EGRMonto" = COALESCE(:EGRMonto,"EGRMonto")
, "EGRMontoMonedaInstrumento" = COALESCE(:EGRMontoMonedaInstrumento,"EGRMontoMonedaInstrumento")
, "EGRMontoMonedaAdicional" = COALESCE(:EGRMontoMonedaAdicional,"EGRMontoMonedaAdicional")
WHERE "IdGarantia" IN (:IdGarantia);