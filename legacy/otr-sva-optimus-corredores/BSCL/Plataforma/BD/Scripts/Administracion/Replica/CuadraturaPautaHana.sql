
SELECT 'APV_APV',COUNT(1) FROM "BSCL"."BSCL.APV.BD.Vistas::ConsultarAPVAnalitica" UNION
SELECT 'APV_Contratos',COUNT(1) FROM "BSCL"."BSCL.APV.BD.Vistas::ConsultarContratosAnalitica" UNION
SELECT 'CuentaInversion_MovimientosCuentaInversion',COUNT(1) FROM "BSCL"."BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" UNION
SELECT 'CuentaInversion_PatrimonioCajaFechaLiquidacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19' GROUP BY "FechaPatrimonio" UNION
SELECT 'CuentaInversion_PatrimonioCajaFechaOperacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaOperacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19' GROUP BY "FechaPatrimonio";
SELECT 'CuentaInversion_SaldoFechaEfectiva',COUNT(1) FROM "BSCL"."BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica";
SELECT 'CuentaInversion_SaldoFechaMovimiento"',COUNT(1) FROM "BSCL"."BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaMovimientoAnalitica";
SELECT 'Custodia_CarteraFM',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CAR.CarteraFMAnalitica" UNION
SELECT 'Custodia_CarteraFMHis',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica" WHERE "FechaRespaldo" = '2022-04-19';
SELECT 'CarteraOtrosProductos',COUNT(1) FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraOtrosProductos" UNION
SELECT 'CarteraOtrosProductosHis',COUNT(1) FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraOtrosProductosHis" UNION
SELECT 'CarteraRFIF',COUNT(1) FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraRFIF" UNION
SELECT 'CarteraRFIFHis',COUNT(1) FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraRFIFHis" UNION
SELECT 'CarteraRV',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRVAnalitica" UNION
SELECT 'CarteraRVHis',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRVHisAnalitica" WHERE "FechaRespaldo" = '2022-04-19' UNION
SELECT 'CustodiaFechaLiquidacion',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaFechaLiquidacionAnalitica" UNION
SELECT 'CustodiaFechaOperacion',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaFechaOperacionAnalitica" UNION
SELECT 'EventoCapital',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalAnalitica" UNION
SELECT 'EventoCapitalCliente',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteAnalitica" UNION
SELECT 'Garantia',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica";
SELECT 'GarantiaHis',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaHisAnalitica" UNION
SELECT 'MovimientoCarteraEgresosAnalitica',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" UNION
SELECT 'MovimientoCarteraIngresosAnalitica',COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" UNION
SELECT 'PatrimonioConsolidadoFechaLiquidacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19' GROUP BY "FechaPatrimonio";
SELECT 'PatrimonioConsolidadoFechaOperacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19'  GROUP BY "FechaPatrimonio" UNION
SELECT 'PatrimonioCustodiaFechaLiquidacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaLiquidacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19'  GROUP BY "FechaPatrimonio" UNION
SELECT 'PatrimonioCustodiaFechaOperacion',"FechaPatrimonio",COUNT(1) FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaOperacionAnalitica" WHERE "FechaPatrimonio" >= '2022-04-19' GROUP BY "FechaPatrimonio" UNION
SELECT 'Comprobante',COUNT(1) FROM "BSCL"."BSCL.Facturacion.BD.Vistas::ConsultarComprobanteAnalitica" UNION
SELECT 'ComprobanteCobro',COUNT(1) FROM "BSCL"."BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosAnalitica" UNION
SELECT 'ComprobanteDetalle',COUNT(1) FROM "BSCL"."BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleAnalitica" UNION
SELECT 'ComprobanteFacturacionFormaPago',COUNT(1) FROM "BSCL"."BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaPagoAnalitica";
SELECT 'ConsultarAporteReinversionAgrupado',COUNT(1) FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupadoAnalitica" UNION
SELECT 'ContratoSuscripcionCuotas"',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasAnalitica" UNION
SELECT 'ContratoSuscripcionCuotasDetalle"',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalleAnalitica" UNION
SELECT 'Fondo',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAnalitica" UNION
SELECT 'NOAPV',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarNOAPVAnalitica" UNION
SELECT 'RegimenSerieFondo',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieFondoAnalitica" UNION
SELECT 'Reinversion',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionAnalitica" UNION
SELECT 'ReinversionSerieFondoDestino',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionSerieFondoDestinoAnalitica" UNION
SELECT 'SerieDeFondo',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica" UNION
SELECT 'SolicitudAporteRescate',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica" UNION
SELECT 'TipoMovimientoFondoMutuo',COUNT(1) FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/TipoMovimientoFondoMutuo" UNION
SELECT 'ConsultarTraspasosAnalitica',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica" UNION
SELECT 'ValorCuotaHistorico',COUNT(1) FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica";
SELECT '"_SYS_BIC"."BSCL.Mesa.BD.Analytics/MovimientosFWD"',COUNT(1) FROM "_SYS_BIC"."BSCL.Mesa.BD.Analytics/MovimientosFWD" UNION
SELECT '"_SYS_BIC"."BSCL.Mesa.BD.Analytics/MovimientosSpot"',COUNT(1) FROM "_SYS_BIC"."BSCL.Mesa.BD.Analytics/MovimientosSpot" UNION
SELECT '"_SYS_BIC"."BSCL.Mesa.BD.Analytics/Pactos"',COUNT(1) FROM "_SYS_BIC"."BSCL.Mesa.BD.Analytics/Pactos" UNION
SELECT '"_SYS_BIC"."BSCL.Mesa.BD.Analytics/PactosDetalle"',COUNT(1) FROM "_SYS_BIC"."BSCL.Mesa.BD.Analytics/PactosDetalle" UNION
SELECT '"_SYS_BIC"."BSCL.Orden.BD.Analytics/Asignacion"',COUNT(1) FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/Asignacion" UNION
SELECT '"_SYS_BIC"."BSCL.Orden.BD.Analytics/CobroAsignacion"',COUNT(1) FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/CobroAsignacion" UNION
SELECT '"_SYS_BIC"."BSCL.Orden.BD.Analytics/CobroOrden"',COUNT(1) FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/CobroOrden" UNION
SELECT '"_SYS_BIC"."BSCL.Orden.BD.Analytics/Orden"',COUNT(1) FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/Orden" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Banco"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Banco" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Ciudad"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Ciudad" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ClasificacionDivisionPais"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ClasificacionDivisionPais" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ClasificacionTipoInstrumento"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ClasificacionTipoInstrumento" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Concepto"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Concepto" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/DivisionPais"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/DivisionPais" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Empresa"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Empresa" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Estado"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Estado" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Giro"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Giro" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Instrumento"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Instrumento" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/MatrizComercialCorredora"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/MatrizComercialCorredora" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/MatrizComercialPortafolios"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/MatrizComercialPortafolios" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ModeloComercial"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ModeloComercial" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Moneda"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Moneda" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Pais"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Pais" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ParametroFinanciero"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ParametroFinanciero" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilCobro"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilCobro" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilProducto"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilProducto" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Profesion"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Profesion" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoIndicador"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoIndicador" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoInstrumento"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoInstrumento" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoOperacion"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoOperacion" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoProducto"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/TipoProducto" UNION
SELECT '"_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ValorCobro"',COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ValorCobro" UNION
SELECT '"_SYS_BIC"."BSCL.Portafolio.BD.Analytics/Portafolio"',COUNT(1) FROM "_SYS_BIC"."BSCL.Portafolio.BD.Analytics/Portafolio" UNION
SELECT 'Cliente',COUNT(1) FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica" UNION
SELECT 'DetalleIndicador',COUNT(1) FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDetalleIndicadorAnalitica" UNION
SELECT 'Ejecutivo',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Ejecutivo" UNION
SELECT 'Empleado',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Empleado" UNION
SELECT 'IndicadoresCliente',COUNT(1) FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica" UNION
SELECT 'IndicadoresPersona',COUNT(1) FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica" UNION
SELECT '"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"',COUNT(1) FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" UNION
SELECT '"_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Persona"',COUNT(1) FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaAnalitica" UNION
SELECT '"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica"',COUNT(1) FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentaBancariaAnalitica" UNION
SELECT '"_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaDireccionElectronica"',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaDireccionElectronica" UNION
SELECT 'PersonaDireccionFisica',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaDireccionFisica" UNION
SELECT '"_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaNombreFantasia"',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaNombreFantasia" UNION
SELECT 'PersonaRelacion"',COUNT(1) FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaRelacionAnalitica" UNION
SELECT 'PersonaTelefono"',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaTelefono" UNION
SELECT 'Supervisor',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Supervisor" UNION
SELECT 'TipoRelacion',COUNT(1) FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/TipoRelacion" UNION
SELECT '"BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica"',COUNT(1) FROM "BSCL"."BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica" UNION
SELECT '"_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/ComprobanteDetalle"',COUNT(1) FROM "_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/ComprobanteDetalle" UNION
SELECT '"_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/ComprobanteFormaDePago"',COUNT(1) FROM "_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/ComprobanteFormaDePago" UNION
SELECT '"BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica"',COUNT(1) FROM "BSCL"."BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica" UNION
SELECT '"_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/MandatoPAC"',COUNT(1) FROM "_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/MandatoPAC";

