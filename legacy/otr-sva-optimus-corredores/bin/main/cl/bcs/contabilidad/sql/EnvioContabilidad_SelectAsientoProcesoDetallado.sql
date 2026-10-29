SELECT	
CTB_itzEvento."IdEvento",
CTB_itzMovimiento."IdMovimiento",
CTB_itzContabilidadTipo."IdTipoContabilidad",
CTB_itzPreComprobanteAsiento."IdPreComprobante",
CTB_itzCentralizacion."IdTipoCentralizacion",
CTB_itzEvento."CodigoMoneda",
CTB_itzPreComprobanteAsiento."MontoDebe",
CTB_itzPreComprobanteAsiento."MontoHaber",
CTB_itzEvento."Monto",
CTB_itzPreComprobanteAsiento."Glosa",
CTB_itzEventoPreComprobanteAsiento."GlosaDetalle",
CTB_itzHomologaCuenta."CentroCosto",
CTB_itzHomologaCuenta."CodigoContabilidad",
CTB_itzHomologaCuenta."CuentaContabilidad",
CTB_itzMovimiento."DocumentoIdentidad",
CTB_itzMovimiento."Cuenta",
CTB_itzprecomprobante."FechaContable",
CTB_itzPreComprobanteAsiento."IdCuentaContable",
CTB_itzHomologaCuenta."NombreCuenta"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzprecomprobante ON CTB_itzContabilidadTipo."IdTipoContabilidad" = CTB_itzprecomprobante."IdTipoContabilidad"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON CTB_itzprecomprobante."IdPreComprobante" = CTB_itzPreComprobanteAsiento."IdPreComprobante"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" as CTB_itzCentralizacion ON CTB_itzCentralizacion."IdCentralizacion"=CTB_itzMovimiento."IdCentralizacion"
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON CTB_itzPreComprobanteAsiento."IdCuentaContable" = CTB_itzHomologaCuenta."CuentaOptimus"
WHERE 
CTB_itzprecomprobante."Estado" IN ('V', 'D')
AND CTB_itzprecomprobante."IdPreComprobante" IN (:IdPrecomprobante)