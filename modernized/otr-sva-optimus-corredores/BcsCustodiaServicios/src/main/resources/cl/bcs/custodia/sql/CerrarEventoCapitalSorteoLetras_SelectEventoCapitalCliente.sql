SELECT "IdEventoCapitalCliente","IdEventoCapital","IdTipoEventoCapital","Secuencia","IdSocioNegocio","TipoSocioNegocio","IdPortafolio","IdTipoDocumentoIdentidad","DocumentoIdentidad"
    ,"NombreCliente","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemo","IdInstrumentoNuevo","NemoNuevo","FechaCierre","FechaPago"
    ,"TipoCartera","MontoCapital","MontoInteres","Total","IdMoneda","SecuenciaCupon","CodigoUsuarioAgente","NombreAgente","CodigoSucursal","FechaHoraIngresoRegistro"
    ,"FechaHoraUltimaActualizacion","IdUsuario","IdEstado","IdBoveda","IdBolsa","IdTipoOperacionGarantia","IdConceptoGarantia","MontoReajuste","PrecioNuevo","Precio"
    ,"NumeroDividendo","Origen","Paridad","IdCartera","FolioCartera","IdTipoReajuste","NumeroCuentaBoveda","PagaClienteCorredor","IdEmpleadoEjecutivoPortafolio","Amortizacion"
    ,"TipoCuentaBoveda","EstadoNotificacion","INGMontoInteresCorteCupon","IdEventoCapitalClienteFormaDePago","IdInstrumentoAnterior","AfectoaClearing","ProcesadoClearing"
    ,"IdCorredorContraparte","CodigoCorredorContraparte","IdComprobanteTesoreria","TipoOperacion","CantidadDerecho","CantidadNueva","IdTipoCuentaCustodio","IdMovimientoComprobanteFac"
    ,"MontoBeneficioArchivo","CantidadDerechoArchivo","AccionesDisponiblesArchivo","DiferenciaCantidadDerecho","DiferenciaAccionesCantidadNueva","MontoInteresArchivo","MontoCapitalArchivo"
    ,"DiferenciaMontoInteres","DiferenciaMontoCapital","TipoCarteraNormativa","IdCuentaCustodioPortafolio","MontoRetencionArchivo","DiferenciaMontoBeneficio","MontoRetencion","DiferenciaMontoRetencion"
    ,soc."IdPersona"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" evc
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc ON (soc."IdSocioDeNegocio" = evc."IdSocioNegocio") 
WHERE "IdTipoEventoCapital"='SL' AND "IdEventoCapital" IN (:p_IdEventoCapital)