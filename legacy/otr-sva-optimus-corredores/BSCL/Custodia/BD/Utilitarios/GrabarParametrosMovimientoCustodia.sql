SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Utilitarios::GrabarParametrosMovimientoCustodia";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Utilitarios::GrabarParametrosMovimientoCustodia"(
-- 
--  Proposito: Grabar los parametros de los servicios de movimiento custodia
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 23/10/2016
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_GenerarMovimientoCustodia_Entrada_Parametros2 "BSCL.Custodia.BD::TYP.GenerarMovimientoCustodia_Entrada_Parametros"
			DEFAULT SELECT
						"IdProcesoMovimientoCustodia","EsDemo","FechaInicioProceso","FechaFinProceso","ProcesoConProblemas",
						--PARAMETROS DE ENTRADA OBLIGATORIOS
						"IdEmpresa","IdUsuario","IdModulo","IdPais","SimularOperacion","UsaReglasNegocio","IdServicioAgrupador",
						"ServicioAgrupador","GrabaLog","FechaSistema","IdMonedaSistema","IdMonedaAdicional"
					FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros";
		
		BEGIN AUTONOMOUS TRANSACTION
			UPDATE p
				SET "ProcesoConProblemas" = 'S',"FechaFinProceso" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaParametrosEntrada" p
			INNER JOIN :v_GenerarMovimientoCustodia_Entrada_Parametros2 AS o ON (o."IdProcesoMovimientoCustodia" = p."IdProcesoMovimientoCustodia");
	
			INSERT INTO "BSCL.Custodia.BD::CUS.GenerarMovimientoCustodiaLog"(
				"IdGenerarMovimientoCustodiaLog","IdProcesoMovimientoCustodia","FechaProcesoMovimientoCustodia","IdInterno",
				--CAMPOS DE ERRORES
				"OrigenError","TipoProblema","CodigoMensaje","CodigoDato1","ValorDato1")
			SELECT
				"BSCL.Custodia.BD.Secuencias::GenerarMovimientoCustodiaLog".NEXTVAL,"IdProcesoMovimientoCustodia",CURRENT_UTCTIMESTAMP,0,
				--CAMPOS DE ERRORES
				::CURRENT_OBJECT_NAME,'E','ERROR_INESPERADO',::SQL_ERROR_CODE,::SQL_ERROR_MESSAGE
			FROM :v_GenerarMovimientoCustodia_Entrada_Parametros2;
			COMMIT;
		END;--AUTONOMOUS TRANSACTION
		RESIGNAL;
	END;
	
	BEGIN
		--VARIABLES DE ENTRADA
		DECLARE v_TicketAtencion,v_IdEmpresa,v_IdUsuario,v_IdProceso,v_IdServicioAgrupador BIGINT :=0;
		DECLARE v_EsDemo NVARCHAR(1) :='N';
		DECLARE v_ServicioAgrupador NVARCHAR(255) := '';
		DECLARE v_GrabaLog NVARCHAR(1) :='S';
		--generar
		DECLARE v_GenerarMovimientoCustodia_Salida_Log "BSCL.Custodia.BD::TYP.GenerarMovimientoCustodia_Salida_Log"
			DEFAULT SELECT
						"IdGenerarMovimientoCustodiaLog","FechaProcesoMovimientoCustodia",
						-----------------------------------------------------------------
						"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
						--CAMPOS DE VALIDACION
						"IdValidacionesMovimientoCustodia","CampoAValidar","ValorFalloValidacion",
						-----------------------------------------------------------------	
						--CAMPOS DE ERRORES
						"OrigenError","TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2",
						"ValorDato2","Descripcion",
						--DATOS COMPLEMENTARIOS PARA COMPRENDER MEJOR EL ERROR
						"CodigoMensajeInterno","GeneraMovimientoEnTransito","TienePasoContingencia","CantidadARetirar","CantidadReservada",
						"CantidadSaldo"
					FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Log";
		DECLARE v_GenerarMovimientoCustodia_Entrada_Parametros "BSCL.Custodia.BD::TYP.GenerarMovimientoCustodia_Entrada_Parametros"
			DEFAULT SELECT
						"IdProcesoMovimientoCustodia","EsDemo","FechaInicioProceso","FechaFinProceso","ProcesoConProblemas",
						--PARAMETROS DE ENTRADA OBLIGATORIOS
						"IdEmpresa","IdUsuario","IdModulo","IdPais","SimularOperacion","UsaReglasNegocio","IdServicioAgrupador","ServicioAgrupador",
						"GrabaLog","FechaSistema","IdMonedaSistema","IdMonedaAdicional"
					FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros";
		DECLARE v_GenerarMovimientoCustodia_Salida_Depuracion "BSCL.Custodia.BD::TYP.GenerarMovimientoCustodia_Salida_Depuracion"
			DEFAULT SELECT
						"IdProceso","NombreProceso","FechaInicioProceso","FechaFinProceso","Diferencia"
					FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion";
		--crear
		DECLARE v_CrearMovimientoCustodia_Entrada_Datos "BSCL.Custodia.BD::TYP.CrearMovimientoCustodia_Entrada_Datos"
			DEFAULT SELECT
						"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
						"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
						"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion","IdAccionOperacionRelacionado",
						--GARANTIAS
						"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
						"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
						--OPERACION
						"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
						"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
						"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
						"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
						"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
						"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo",
						"TipoCarteraCorredor",
						--FUERON CREADOS PARA FONDOS MUTUOS
						"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
						--INSTRUMENTO
						"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
						"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
						"IdBovedaEsIncluyente",
						--CANTIDADES Y MONTOS
						"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
						"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
						"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
					 	--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
						"INGCantidad","INGValorRescate",
					 	--INSTRUMENTO RELACIONADO
						"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
						"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
						"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
						"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
					 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
						"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
						"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado",
						"TasaCompraInstrumentoRelacionado","MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado",
						"MontoCompraMonedaAdicionalRelacionado","PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado",
						"MontoAPlazoMonedaInstrumentoRelacionado","MontoAPlazoMonedaAdicionalRelacionado"	,
						--SE UTILIZA EN OSA, LO INGRESADO EN CARTERA PASA A ESTAR EN GARANTIA
						"MueveGarantiaAutomatica",
						---RF Y FORWARD
						"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
						"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
						--FORWARD
						"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
						"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
						"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
						"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
						"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
						"TipoCambioObservado",
						-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
						"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
					FROM "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Entrada_Datos";
		DECLARE v_CrearMovimientoCustodia_Entrada_Configuracion "BSCL.Custodia.BD::TYP.CrearMovimientoCustodia_Entrada_Configuracion"
			DEFAULT SELECT
						"IdInterno","IdTipoOperacion",
						----------------------------------------------------------
						"IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
						"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
						"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
						"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
						"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
						"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
						"IdTipoOperacionOrigenRelacionada",
						"ExcluirCarteraComprometida",
						-----------------------------------------------------------------------------------------
						"OrdenEjecucionMovimientos",
						--------------------------------Cliente Ingreso-----------------------------------------
						"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
						"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
						-----------------------Cliente Ingreso Carteras Automaticas------------------------------
						"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
						"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
						"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
						"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
						----------------------------------------------------------------------------------------
						--------------------------------Cliente Retiro------------------------------------------
						"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
						"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
						-----------------------Cliente Retiro Carteras Automaticas------------------------------
						"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
						"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
						"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
						"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
						--------------------------------------------------------------
						"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto"
					FROM "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Entrada_Configuracion";
		DECLARE v_CrearMovimientoCustodia_Salida_Configuracion "BSCL.Custodia.BD::TYP.CrearMovimientoCustodia_Salida_Configuracion"
			DEFAULT SELECT
						"IdInterno","IdTipoOperacion",
						----------------------------------------------------------
						"SeEjecutaPasoConfiguracionClienteIngreso","SeEjecutaPasoConfiguracionClienteRetiro","IdAccionOperacion",
						"UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
						"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
						"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
						"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
						"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
						"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
						"IdTipoOperacionOrigenRelacionada",
						"ExcluirCarteraComprometida",
						-----------------------------------------------------------------------------------------
						"OrdenEjecucionMovimientos",
						--------------------------------Cliente Ingreso-----------------------------------------
						"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
						"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
						-----------------------Cliente Ingreso Carteras Automaticas------------------------------
						"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
						"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
						"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
						"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
						----------------------------------------------------------------------------------------
						--------------------------------Cliente Retiro------------------------------------------
						"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
						"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
						-----------------------Cliente Retiro Carteras Automaticas------------------------------
						"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
						"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
						"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
						"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
						--------------------------------------------------------------
						"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto"
					FROM "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Salida_Configuracion";
		DECLARE v_CrearMovimientoCustodia_Salida_ValidacionesCustodia "BSCL.Custodia.BD::TYP.CrearMovimientoCustodia_Salida_ValidacionesCustodia"
			DEFAULT SELECT
						"IdInternoCrear","IdInterno","IdInternoCorregido","CustodiaValida","IdCustodia","FolioCustodia","IdCliente","IdPortafolio",
						"IdCuentaCustodioPortafolio","INGIdCondicionLiquidacion","INGFechaLiquidacion","IdTipoComprobante","FolioComprobante","INGCantidad",
						"INGValorRescate","CantidadDisponible","IdTipoOperacion","TipoCarteraCorredor",
						-----INSTRUMENTO------------------------------
						"MyIdInstrumento","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS",
						"IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","CodigoIFRS","IdBoveda","CuentaBoveda",
						"IdMonedaInstrumento","Tamano","CorteMinimo","TIFAfectoBeneficio","TIFTasa","EsIntraday",
						--DATOS BUSQUEDA
						"IdBovedaEsIncluyente","UtilizaFechaLiquidacionBusqueda","BovedaBusqueda","FechaLiquidacionBusqueda","FolioCustodiaBusqueda",
						"IdTipoComprobanteBusqueda","FolioComprobanteBusqueda","IdInstrumentoBusqueda","IdEmisorBusqueda","FechaVencimientoInstrumentoBusqueda",
						"IdTipoReajusteBusqueda","EsFungibleBusqueda","IdCuentaCustodioPortafolioBusqueda","EsBovedaCorrecta","FechaLiquidacionCorrecta",
						"FolioCustodiaCorrecto","IdTipoComprobanteCorrecto","FolioComprobanteCorrecto","IdInstrumentoCorrecto","IdEmisorCorrecto",
						"FechaVencimientoInstrumentoCorrecta","IdTipoReajusteCorrecto","EsFungibleCorrecto","EsCarteraBuscada",
						"IdCuentaCustodioPortafolioCorrecto","IdTipoOperacionBuscada","TipoCarteraCorredorCorrecto"
					FROM "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Salida_ValidacionesCustodia";
		DECLARE v_CrearMovimientoCustodia_Salida_PlantillaAplicada "BSCL.Custodia.BD::TYP.CrearMovimientoCustodia_Salida_PlantillaAplicada"
				DEFAULT SELECT
							"IdInterno","IdPlantillaMovimientoCustodia","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando",
							"IdMovimientoCarteraEnTransito","TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda",
							"FolioComprobanteBusqueda","FolioOperacionRelacionada",
							--GARANTIAS,
							"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
							"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
							--OPERACION
							"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
							"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
							"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional",
							"FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion",
							"IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente",
							"IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo",
							"Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
							--FUERON CREADOS PARA FONDOS MUTUOS
							"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
							--INSTRUMENTO
							"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
							"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
							"IdBovedaEsIncluyente",
							--CANTIDADES Y MONTOS
							"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
							"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
							"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
							--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
							"INGCantidad","INGValorRescate",
						 	--INSTRUMENTO RELACIONADO
							"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
							"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
							"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
							"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
							--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
							"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
							"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado",
							"TasaCompraInstrumentoRelacionado","MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado",
							"MontoCompraMonedaAdicionalRelacionado","PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado",
							"MontoAPlazoMonedaInstrumentoRelacionado","MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
							--RF Y FORWARD
							"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
							"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
							--FORWARD
							"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
							"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen",
							"MontoValorRazonableActivo","MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo",
							"MontoDevengoTipoCambio","MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot",
							"ValorTipoCambioCotizador","EsPuntos","TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo",
							"TipoCambioAnticipo","MontoARecibirAnticipo","TipoCambioObservado",
							-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
							"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
						FROM "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Salida_PlantillaAplicada";
		--procesar
		DECLARE v_ProcesarMovimientoCustodia_Entrada_CarterasVigentes "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_CarterasVigentes"
			DEFAULT SELECT
						"IdInterno","IdInternoCorregido","IdInternoCrear","IdCartera","FolioCartera",
						--Filtros de busqueda y ordenamiento
						"IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
						"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_CarterasVigentes";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_CarterasVigentesParaUtilizar "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_CarterasVigentesParaUtilizar"
			DEFAULT SELECT
						"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","EsTransitoOrigenCartera",
						--Filtros de busqueda y ordenamiento
						"INGIdCondicionLiquidacion","INGFechaLiquidacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento",
						--CANTIDAD
						"INGCantidad","INGValorRescate","CantidadDisponible",
						--TASA DE COMPRA
						"INGPrecioCompra","INGTasaCompra","IdEstado"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_CarterasVigentesParaUtilizar";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_SubyacentesVigentes "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_SubyacentesVigentes"
			DEFAULT SELECT
						"IdInterno","IdInternoCorregido","IdInternoCrear","IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada",
						"FolioCarteraRetirada","IdMovimientoCarteraEnTransito","EsTransitoOrigenCartera","MyIdInstrumento"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_SubyacentesVigentes";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_SubyacentesVigentesParaUtilizar "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_SubyacentesVigentesParaUtilizar"
			DEFAULT SELECT
						"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada","FolioCarteraRetirada","IdMovimientoCarteraEnTransito",
						"EsTransitoOrigenCartera","MyIdInstrumento",
						--TASA DE COMPRA
						"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
						--TASA SUBYACENTE
						"INGPrecioSubyacente","INGTasaSubyacente",
						--TASA MERCADO ACTUAL
						"INGPrecioMercadoActual","INGTasaMercadoActual",
						--CANTIDAD
						"INGCantidad","INGValorRescate","CantidadDisponible"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_SubyacentesVigentesParaUtilizar";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_GarantiasVigentes "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_GarantiasVigentes"
			DEFAULT SELECT
						"IdInterno","IdInternoCorregido","IdInternoCrear","IdGarantia","FolioGarantia",
						--Filtros de busqueda y ordenamiento
						"MyIdInstrumento","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
						"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_GarantiasVigentes";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_GarantiasVigentesParaUtilizar "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_GarantiasVigentesParaUtilizar"
			DEFAULT SELECT
						"IdGarantia","FolioGarantia","IdCarteraOrigen","FolioCarteraOrigen","MyIdInstrumento",
						------------------------------------------------------
						--TASA DE COMPRA
						"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
						--TASA GARANTIA
						"INGPrecioGarantia","INGTasaGarantia",
						--TASA MERCADO ACTUAL
						"INGPrecioMercadoActual","INGTasaMercadoActual",
						--CANTIDAD
						"INGIdCondicionLiquidacion","INGFechaLiquidacion","INGCantidad","INGValorRescate","CantidadDisponible"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_GarantiasVigentesParaUtilizar";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_CarterasReservadas "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_CarterasReservadas"
			DEFAULT SELECT
						"IdReserva","IdOperacion","IdUsuarioCreacion","FolioCarteraOrigen","IngCantidad","IngValorRescate","CantidadReservada"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_CarterasReservadas";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_Operaciones "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_Operaciones"
			DEFAULT SELECT
						"IdInterno","IdGenerarMovimientoCustodia","IdUsuario","FolioCustodia","IdTipoComprobante","FolioComprobante","FolioOperacionRelacionada",
						"CliIngGeneraMvtoEnTransitoSinCustodia","CliEgrGeneraMvtoEnTransitoSinCustodia","EsNominalIngreso","EsNominalRetiro",
						"IdGrupoOperacionInterno","IdGrupoOperacion","IdOperacion","RetiraCarteraEspecifica","UsuarioPoseeCarteraReservada",
						"CantidadPasosEjecucion","EsIngreso","EstaCertificando","ConCertificadoAutomatico","IdMovimientoCarteraEnTransito"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_Operaciones";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_InstrumentosPorOperacion "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_InstrumentosPorOperacion"
			DEFAULT SELECT
						"IdGenerarMovimientoCustodia","IdInterno","IdInternoCorregido","IdInternoCrear","IdGrupoOperacion","IdGrupoOperacionInterno","TipoRegistro",
						"IdCondicionLiquidacion","FechaLiquidacion","MyIdInstrumento","EsInstrumentoRelacionado","IdBoveda","EsNominalIngreso","EsNominalRetiro",
						--PARA MODIFICACIONES (NUEVO REGISTRO)
						"IdCustodia","FolioCustodia","TablaAfectada",
						--CANTIDAD
						"Cantidad","ValorRescate","CantidadOperacion",
						--TASA DE COMPRA
						"PrecioCompra","TasaCompra","PrecioAPlazo"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_InstrumentosPorOperacion";
		DECLARE v_ProcesarMovimientoCustodia_Entrada_OrdenEjecucion "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Entrada_OrdenEjecucion"
			DEFAULT SELECT
						"IdInterno","IdOrdenEjecucionOperacionesMovimientoCustodia","IdTipoOperacion","IdClientePaso","IdPortafolioPaso",
						"IdCuentaCustodioPortafolioPaso","IdCondicionLiquidacionPaso","FechaLiquidacionPaso",
						----------------------------------------------------
						"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","NombreColumnaPaso","PasoEsIngreso","TablaAfectadaPorPaso","PasoPerteneceClienteRetiro",
						------------------------------------------------------------------------------------------------
						"PasoEjecucion","TablaOrigenDatosDelPaso","OrigenDatosDelPaso","PasoRelacionadoCon","IdEstadoPaso",
						---------------------------------------------------------
						"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
						"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
						"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
						"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
						 "PasoValidaCustodiaOrigenTransito"
					FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Entrada_OrdenEjecucion";
		DECLARE v_ProcesarMovimientoCustodia_Salida_TablaDesarrollo "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Salida_TablaDesarrollo"
				DEFAULT SELECT
							"TablaAfectada","IdEstado",
							--origen de informacion
							"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
							"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion",
							"EGRIdCustodiaOrigenInformacion",
							--FIJA
							"Cantidad","Precio","Tasa","Valoriza",
							---INGRESO------------------------------------------------------------------------------
							"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion",
							"INGIdGrupoOperacionInterno","INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad","INGValoriza",
							"INGEsRegistroNuevo","INGEsSaldo","INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion","INGIdCustodia",
							"INGFolioCustodia","INGIdCondicionLiquidacion","INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa","INGPrecio","INGTasa",
							"INGIdTipoOperacion","INGIdConcepto","INGCodigoIFRS","INGEsIntraday","INGIdCliente","INGIdPortafolio","INGIdCuentaCustodioPortafolio",
							"INGEsNominal","INGMantieneFolio","INGEsRegistroGeneradoPorSistema",
							--INGRESO RELACIONADO			
							"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
							--RETIRO--------------------------------------------------------------------------------
							"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion",
							"EGRIdGrupoOperacionInterno","EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad","EGRPrecio","EGRTasa",
							"EGRValoriza","EGRIdTipoOperacion","EGRIdConcepto","EGRIdCondicionLiquidacion","EGRFechaLiquidacion",
							--RETIRO RELACIONADO			
							"EGRIdTablaDesarrolloRel","EGRTablaRelacionada"
						FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Salida_TablaDesarrollo";
		DECLARE v_ProcesarMovimientoCustodia_Salida_CarterasReservadas "BSCL.Custodia.BD::TYP.ProcesarMovimientoCustodia_Salida_CarterasReservadas"
				DEFAULT SELECT
							"IdReserva","Cantidad"
						FROM "BSCL.Custodia.BD::GBL.ProcesarMovimientoCustodia_Salida_CarterasReservadas";
		--actualizar
		DECLARE v_ActualizarMovimientoCustodia_Entrada_Datos "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_Datos"
			DEFAULT SELECT
						"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
						"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
						"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion",
						--GARANTIAS
						"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
						"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
						--OPERACION
						"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
						"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
						"IdCuentaCustodioPortafolioRetiro",
						--se creo para guardar el id y portafolio de la corredora para compras con pacto
						"IdClienteRelacionado","IdPortafolioRelacionado",
						---------------------------------------------------------
						"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
						"CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
						"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
						"UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
						--FUERON CREADOS PARA FONDOS MUTUOS
						"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
						--INSTRUMENTO
						"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
						"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
						"IdBovedaEsIncluyente",
						--CANTIDADES Y MONTOS
						"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
						"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
						"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
						--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
						"INGCantidad","INGValorRescate",
					 	--INSTRUMENTO RELACIONADO
						"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
						"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
						"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
						"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
					 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
						"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
						"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado",
						"TasaCompraInstrumentoRelacionado","MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado",
						"MontoCompraMonedaAdicionalRelacionado","PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado",
						"MontoAPlazoMonedaInstrumentoRelacionado","MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
						--RF Y FORWARD
						"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
						"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
						--FORWARD
						"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
						"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen",
						"MontoValorRazonableActivo","MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo",
						"MontoDevengoTipoCambio","MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot",
						"ValorTipoCambioCotizador","EsPuntos","TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo",
						"TipoCambioAnticipo","MontoARecibirAnticipo","TipoCambioObservado",
						-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
						"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
					FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_Datos";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_TablaDesarrollo "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_TablaDesarrollo"
			DEFAULT SELECT
						"TablaAfectada","IdEstado",
						--origen de informacion
						"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
						"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion",
						"EGRIdCustodiaOrigenInformacion",
						--FIJA
						"Cantidad","Precio","Tasa","Valoriza",
						---INGRESO------------------------------------------------------------------------------
						"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion",
						"INGIdGrupoOperacionInterno","INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad","INGValoriza",
						"INGEsRegistroNuevo","INGEsSaldo","INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion","INGIdCustodia","INGFolioCustodia",
						"INGIdCondicionLiquidacion","INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa","INGPrecio","INGTasa","INGIdTipoOperacion",
						"INGIdConcepto","INGCodigoIFRS","INGEsIntraday","INGIdCliente","INGIdPortafolio","INGIdCuentaCustodioPortafolio","INGEsNominal",
						"INGMantieneFolio","INGEsRegistroGeneradoPorSistema",
						--INGRESO RELACIONADO			
						"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
						--RETIRO--------------------------------------------------------------------------------
						"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion",
						"EGRIdGrupoOperacionInterno","EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad","EGRPrecio","EGRTasa",
						"EGRValoriza","EGRIdTipoOperacion","EGRIdConcepto","EGRIdCondicionLiquidacion","EGRFechaLiquidacion",
						--RETIRO RELACIONADO			
						"EGRIdTablaDesarrolloRel","EGRTablaRelacionada"
					FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_TablaDesarrollo";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_CarterasReservadas "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_CarterasReservadas"
			DEFAULT SELECT
						"IdReserva","Cantidad"
					FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_CarterasReservadas";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_InstrumentosPorOperacion "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_InstrumentosPorOperacion"
				DEFAULT SELECT
							"IdInterno","IdInternoCrear","EsDatosModificacion",
							--INSTRUMENTO
							"MyIdInstrumento","EsInstrumentoRelacionado","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico",
							"IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible",
							"ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio",
							"TIFTasa","ISIN",
							--CANTIDADES Y MONTOS
							"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra",
							"TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","PrecioAPlazo","MontoAPlazo",
							"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
							--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
							"INGCantidad","INGValorRescate"
						FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_InstrumentosPorOperacion";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_CarterasVigentes "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_CarterasVigentes"
				DEFAULT SELECT
							"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro",
							--OPERACION
							"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio",
							"IdCuentaCustodioPortafolio",
							-----------------------------------------------------------------------------------------
							---se creo para guardar el id y portafolio de la corredora para compras con pacto
							"IdClienteRelacionado","IdPortafolioRelacionado",
							----------------------------------------------------------------------------------------
							"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
							"IdMovimientoCarteraEnTransito","IdConcepto","IdTipoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
							"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
							"UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
							--FUERON CREADOS PARA FONDOS MUTUOS
							"TipoCarteraNormativa","FechaCurseInversion","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoInversionEnFactorAjuste",
							--INSTRUMENTO
							"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
							"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
							"TieneCorteDeCupon","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
							--CANTIDADES Y MONTOS
							"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
							"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
							"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
							--RF Y FORWARD
							"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdModalidadCumplimiento","IdReferenciaMercado","IdMonedaPrincipal",
							"IdMonedaSecundaria","ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","MontoMonedaPrincipal",
							"MontoMonedaSecundaria",
							-- Necesarios para la migracion inicial de movimientos desde sebra
							"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
							---INGRESO
							"INGCantidad","INGValorRescate","CantidadDisponible","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador",
							"INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria"
						FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_CarterasVigentes";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_SubyacentesVigentes "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_SubyacentesVigentes"
				DEFAULT SELECT
							"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","FechaIngresoRegistro","IdCarteraRetirada",
							"IdMovimientoCarteraEnTransito",
							--OPERACION RELACIONADA
							"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
							--OPERACION
							"IdTipoCanalIngreso",
							--INSTRUMENTO
							"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
							"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor",
							"TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
							--CANTIDADES Y MONTOS
							"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioSubyacente",
							"TasaSubyacente","MontoSubyacente","MontoSubyacenteMonedaInstrumento","MontoSubyacenteMonedaAdicional",
							--INGRESO
							"INGCantidad","INGValorRescate","CantidadDisponible"
						FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_SubyacentesVigentes";
		DECLARE v_ActualizarMovimientoCustodia_Entrada_GarantiasVigentes "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Entrada_GarantiasVigentes"
				DEFAULT SELECT
							"IdGarantia","FolioGarantia","FolioCarteraOrigen","IdCarteraOrigen","FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada",
							"EsEspecifica","IdContraparteGarantia",
							------------------------------------------------------
							"RazonSocialContraparteGarantia","CuentaContraparteGarantia",
							------------------------------------------------------
							"IdOrigenGarantia","IdEspecieGarantia","FechaIngresoRegistro",
							--OPERACION ORIGEN
							"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
							--OPERACION
							"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio",
							"IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","IdConcepto",
							"IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdSucursal","IdAgente","IdCondicionLiquidacion",
							"FechaLiquidacion","IdUsuario","IdModulo","IdMovimientoCarteraEnTransito",
							--INSTRUMENTO
							"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
							"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
							"TieneCorteDeCupon","CodigoIFRS","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
							--CANTIDADES Y MONTOS
							"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioGarantia","TasaGarantia",
							"MontoGarantia","MontoGarantiaMonedaInstrumento","MontoGarantiaMonedaAdicional",
							-- Necesarios para la migracion inicial de movimientos desde sebra
							"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
							--INGRESO
							"INGCantidad","INGValorRescate","CantidadDisponible"
						FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Entrada_GarantiasVigentes";
		DECLARE v_ActualizarMovimientoCustodia_Salida_Folios "BSCL.Custodia.BD::TYP.ActualizarMovimientoCustodia_Salida_Folios"
				DEFAULT SELECT
							"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacion","IdGrupoOperacionInterno","IdTipoComprobante",
							"FolioComprobante","FechaComprobante","FolioExternoReferencia","TipoRegistro","EsIngreso","SubModulo","TablaAfectada","IdCustodia",
							"FolioCustodia","IdEstado","EsNominal","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento",
							"IdInstrumento","IdOrigenInstrumento","EsGarantia","Cantidad","ValorRescate","IdUsuario",
						    --FUERON CREADOS PARA FONDOS MUTUOS
							"TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste"
						FROM "BSCL.Custodia.BD::GBL.ActualizarMovimientoCustodia_Salida_Folios";
		DECLARE v_CalcularPrecioPromedio_Entrada_Entrada "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Entrada"
				DEFAULT SELECT
							"IdCliente","IdPortafolio","IdInstrumento"
						FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Entrada";
		DECLARE v_CalcularPrecioPromedio_Salida_Salida "BSCL.Custodia.BD::TYP.CalcularPrecioPromedio_Salida_Salida"
				DEFAULT SELECT
							"FechaParidad","IdMonedaSistema","TablaAfectada","IdCustodia","FolioCustodia","IdCliente","IdPortafolio",
							"IdTipoOperacion","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","INGPrecioCompra",
							"INGCantidad","SumINGPrecioCompra","CantidadRegistros","PrecioPromedioCompra","MontoPrecioPromedioCompra",
							"MontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioMonedaAdicional"
						FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_Salida";
		DECLARE v_CalcularPrecioPromedio_Salida_Configuracion "BSCL.Custodia.BD::TYP.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio"
				DEFAULT SELECT
							"IdConfiguracionPrecioPromedio","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
							"IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento"
						FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio";
		--valorizar
		DECLARE v_ValorizarMovimientoCustodia_Entrada_RegistrosAValorizar "BSCL.Custodia.BD::TYP.ValorizarMovimientoCustodia_Entrada_RegistrosAValorizar"
				DEFAULT SELECT
							"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia",
							"IdGrupoOperacionInterno","IdGrupoOperacion","FolioCustodia","IdCustodia","TablaAfectada","EsIngreso",
							"ValorizaACompra","ValorizaAMercado",
							--INSTRUMENTO
							"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","IdTipoReajuste",
							"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
							"TieneCorteDeCupon","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio",
							--OPERACION
							"FechaVencimientoOperacion","PlazoEnDiasOperacion",
							--MONTOS, PRECIO Y TASA
						    "Cantidad","ValorRescate","PrecioAPlazo","PrecioCompra","TasaCompra","PrecioSubyacente","TasaSubyacente","PrecioGarantia",
						    "TasaGarantia","TIFTasa",
						    --PARA OBTENCION DE PRECIOS O TASAS							
							"TablaOrigen","UsoPrecio","TipoCartera","IdTipoCriterioTasa","IdCliente","IdPortafolio"
						FROM "BSCL.Custodia.BD::GBL.ValorizarMovimientoCustodia_Entrada_RegistrosAValorizar";
		DECLARE v_ValorizarMovimientoCustodia_Salida_RegistrosValorizados "BSCL.Custodia.BD::TYP.ValorizarMovimientoCustodia_Salida_RegistrosValorizados"
			DEFAULT SELECT
							"IdInterno" ,"IdInternoCorregido" ,"IdGenerarMovimientoCustodia" ,"IdGrupoOperacionInterno" ,"IdGrupoOperacion" ,
							"IdValorizacion" ,"FolioCustodia" ,"IdCustodia" ,"TablaAfectada" ,"EsIngreso" ,"IdProcesamientoCompraAnterior" ,
							"IdProcesamientoCompraActual" ,"IdProcesamientoCompraProximo" ,"IdProcesamientoMercadoAnterior" ,"IdProcesamientoMercadoActual" ,
							"IdProcesamientoMercadoProximo" ,"IdProcesamientoSubyacenteAnterior" ,"IdProcesamientoSubyacenteActual" ,
							"IdProcesamientoSubyacenteProximo" ,"IdProcesamientoGarantiaAnterior" ,"IdProcesamientoGarantiaActual" ,
							"IdProcesamientoGarantiaProximo" ,"IdProcesamientoMercadoCarteraAnterior" ,"IdProcesamientoMercadoCarteraActual" ,
							"IdProcesamientoMercadoCarteraProximo" ,"IdProcesamientoCorredora" ,"IdProcesamientoTif" ,"IdProcesamientoRetiroCompra" ,
							"IdProcesamientoRetiroMercado" ,"IdProcesamientoRetiroTif" ,
							-------------------------------------------------------------------------------------------------------------------------------------------------
							"ValCompraConProblemasAnterior" ,"ValCompraConProblemasActual" ,"ValCompraConProblemasProximo" ,
							"ValMercadoConProblemasAnterior" ,"ValMercadoConProblemasActual" ,"ValMercadoConProblemasProximo" ,
							"ValSubyacenteConProblemasAnterior" ,"ValSubyacenteConProblemasActual" ,"ValSubyacenteConProblemasProximo" ,
							"ValGarantiaConProblemasAnterior" ,"ValGarantiaConProblemasActual" ,"ValGarantiaConProblemasProximo" ,
							"ValMercadoCarteraConProblemasAnterior" ,"ValMercadoCarteraConProblemasActual" ,"ValMercadoCarteraConProblemasProximo" ,
							"ValCorredoraConProblemas" ,"ValTifConProblemas" ,"ValRetiroCompraConProblemas" ,"ValRetiroMercadoConProblemas" ,
							"ValRetiroTifConProblemas" ,
							--INGRESO
							"INGCantidad" ,"INGValorRescate" ,"INGPrecioAPlazo" ,"INGMontoAPlazo" ,
							"INGMontoAPlazoMonedaInstrumento" ,"INGMontoAPlazoMonedaAdicional" ,
							--FORWARD
							"INGEsPuntos" ,"INGValorTipoCambioSpot" ,"INGValorTipoCambioCotizador" ,"INGTasaPuntosMonedaPrincipal" ,
							"INGTasaPuntosMonedaSecundaria" ,
							--VALORIZACIONES
							--FECHAS DE VALORIZACION
							"INGTicketValorizacion" ,"INGValorizacionConProblemas" ,"INGFechaValorizacionAnterior" ,"INGFechaValorizacionActual" ,
							"INGFechaValorizacionProximo" ,
							--PARAMETROS FINANCIEROS
							--moneda instrumento
							"INGFechaFactorMonedaInstrumentoAnterior" ,"INGValorFactorMonedaInstrumentoAnterior" ,"INGFechaFactorMonedaInstrumentoActual" ,
							"INGValorFactorMonedaInstrumentoActual" ,"INGFechaFactorMonedaInstrumentoProximo" ,"INGValorFactorMonedaInstrumentoProximo" ,
							--moneda adicional
							"INGFechaFactorMonedaAdicionalAnterior" ,"INGValorFactorMonedaAdicionalAnterior" ,"INGFechaFactorMonedaAdicionalActual" ,
							"INGValorFactorMonedaAdicionalActual" ,"INGFechaFactorMonedaAdicionalProximo" ,"INGValorFactorMonedaAdicionalProximo" ,
							--factores valor dolar, euro, etc
							"INGPrecioCompra" ,"INGTasaCompra" ,
							--
							--TASA DE COMPRA ANTERIOR
							"INGMontoATasaCompraAnterior" ,"INGMontoInteresCompraAnterior" ,"INGMontoReajusteCompraAnterior" ,
							--montos a moneda instrumento compra anterior
							"INGMontoCompraAnteriorMonedaInstrumento" ,"INGMontoInteresCompraAnteriorMonedaInstrumento" ,
							"INGMontoReajusteCompraAnteriorMonedaInstrumento" ,
							---
							"INGMontoCompraAnteriorMonedaAdicional" ,"INGMontoInteresCompraAnteriorMonedaAdicional" ,
							"INGMontoReajusteCompraAnteriorMonedaAdicional" ,
							---
							"INGDurationCompraAnterior" ,"INGConvexidadCompraAnterior" ,"INGMontoCompraAnteriorPibMas" ,
							"INGMontoCompraAnteriorPibMasMonedaInstrumento" ,"INGMontoCompraAnteriorPibMasMonedaAdicional" ,
							"INGMontoCompraAnteriorPibMenos" ,"INGMontoCompraAnteriorPibMenosMonedaInstrumento" ,
							"INGMontoCompraAnteriorPibMenosMonedaAdicional" ,
							--TASA DE COMPRA ACTUAL
							"INGMontoATasaCompraActual" ,
							--monto a moneda instrumento compra actual
							"INGMontoCompraActualMonedaInstrumento" ,
							--
							"INGMontoCompraActualMonedaAdicional" ,
							--
							"INGDurationCompraActual" ,"INGConvexidadCompraActual" ,
							--Intereses y Reajustes Corridos
							"INGMontoInteresCorridoCompraActual" ,"INGMontoReajusteCorridoCompraActual" ,"INGMontoInteresCorridoCompraActualMonedaInstrumento" ,
							"INGMontoReajusteCorridoCompraActualMonedaInstrumento" ,"INGMontoInteresCorridoCompraActualMonedaAdicional" ,
							"INGMontoReajusteCorridoCompraActualMonedaAdicional" ,
							--
							"INGMontoCompraActualPibMas" ,"INGMontoCompraActualPibMasMonedaInstrumento" ,"INGMontoCompraActualPibMasMonedaAdicional" ,
							"INGMontoCompraActualPibMenos" ,"INGMontoCompraActualPibMenosMonedaInstrumento" ,"INGMontoCompraActualPibMenosMonedaAdicional" ,
							--TASA DE COMPRA PROXIMO
							"INGMontoATasaCompraProximo" ,"INGMontoInteresCompraProximo" ,"INGMontoReajusteCompraProximo" ,
							--montos a moneda instrumento compra proximo
							"INGMontoCompraProximoMonedaInstrumento" ,"INGMontoInteresCompraProximoMonedaInstrumento" ,
							"INGMontoReajusteCompraProximoMonedaInstrumento" ,
							---
							"INGMontoCompraProximoMonedaAdicional" ,"INGMontoInteresCompraProximoMonedaAdicional" ,
							"INGMontoReajusteCompraProximoMonedaAdicional" ,
							---
							"INGDurationCompraProximo" ,"INGConvexidadCompraProximo" ,
							--TASA DE SUBYACENTE ANTERIOR
							"INGMontoATasaSubyacenteAnterior" ,"INGMontoInteresSubyacenteAnterior" ,"INGMontoReajusteSubyacenteAnterior" ,
							--montos a moneda instrumento subyacente anterior
							"INGMontoSubyacenteAnteriorMonedaInstrumento" ,"INGMontoInteresSubyacenteAnteriorMonedaInstrumento" ,
							"INGMontoReajusteSubyacenteAnteriorMonedaInstrumento" ,
							---
							"INGMontoSubyacenteAnteriorMonedaAdicional" ,"INGMontoInteresSubyacenteAnteriorMonedaAdicional" ,
							"INGMontoReajusteSubyacenteAnteriorMonedaAdicional" ,
							---
							"INGDurationSubyacenteAnterior" ,"INGConvexidadSubyacenteAnterior" ,"INGMontoSubyacenteAnteriorPibMas" ,
							"INGMontoSubyacenteAnteriorPibMasMonedaInstrumento" ,"INGMontoSubyacenteAnteriorPibMasMonedaAdicional" ,
							"INGMontoSubyacenteAnteriorPibMenos" ,"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento" ,
							"INGMontoSubyacenteAnteriorPibMenosMonedaAdicional" ,
							--TASA DE SUBYACENTE ACTUAL
							"INGMontoATasaSubyacenteActual" ,
							---monto a moneda instrumento subyacente actual
							"INGMontoSubyacenteActualMonedaInstrumento" ,
							--
							"INGMontoSubyacenteActualMonedaAdicional" ,
							--
							"INGDurationSubyacenteActual" ,"INGConvexidadSubyacenteActual" ,
							--Intereses y Reajustes Corridos
							"INGMontoInteresCorridoSubyacenteActual" ,"INGMontoReajusteCorridoSubyacenteActual" ,
							"INGMontoInteresCorridoSubyacenteActualMonedaInstrumento" ,"INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento" ,
							"INGMontoInteresCorridoSubyacenteActualMonedaAdicional" ,"INGMontoReajusteCorridoSubyacenteActualMonedaAdicional" ,
							--
							"INGMontoSubyacenteActualPibMas" ,"INGMontoSubyacenteActualPibMasMonedaInstrumento" ,
							"INGMontoSubyacenteActualPibMasMonedaAdicional" ,"INGMontoSubyacenteActualPibMenos" ,
							"INGMontoSubyacenteActualPibMenosMonedaInstrumento" ,"INGMontoSubyacenteActualPibMenosMonedaAdicional" ,
							--TASA DE SUBYACENTE PROXIMO
							"INGMontoATasaSubyacenteProximo" ,"INGMontoInteresSubyacenteProximo" ,"INGMontoReajusteSubyacenteProximo" ,
							--montos a moneda instrumento subyacente proximo
							"INGMontoSubyacenteProximoMonedaInstrumento" ,"INGMontoInteresSubyacenteProximoMonedaInstrumento" ,
							"INGMontoReajusteSubyacenteProximoMonedaInstrumento" ,
							---
							"INGMontoSubyacenteProximoMonedaAdicional" ,"INGMontoInteresSubyacenteProximoMonedaAdicional" ,
							"INGMontoReajusteSubyacenteProximoMonedaAdicional" ,
							---
							"INGDurationSubyacenteProximo" ,"INGConvexidadSubyacenteProximo" ,
							--TASA DE GARANTIA ANTERIOR
							"INGMontoATasaGarantiaAnterior" ,"INGMontoInteresGarantiaAnterior" ,"INGMontoReajusteGarantiaAnterior" ,
							--montos a moneda instrumento Garantia anterior
							"INGMontoGarantiaAnteriorMonedaInstrumento" ,"INGMontoInteresGarantiaAnteriorMonedaInstrumento" ,
							"INGMontoReajusteGarantiaAnteriorMonedaInstrumento"	,
							---
							"INGMontoGarantiaAnteriorMonedaAdicional" ,"INGMontoInteresGarantiaAnteriorMonedaAdicional" ,
							"INGMontoReajusteGarantiaAnteriorMonedaAdicional" ,
							---
							"INGDurationGarantiaAnterior" ,"INGConvexidadGarantiaAnterior" ,"INGMontoGarantiaAnteriorPibMas" ,
							"INGMontoGarantiaAnteriorPibMasMonedaInstrumento" ,"INGMontoGarantiaAnteriorPibMasMonedaAdicional" ,
							"INGMontoGarantiaAnteriorPibMenos" ,"INGMontoGarantiaAnteriorPibMenosMonedaInstrumento" ,
							"INGMontoGarantiaAnteriorPibMenosMonedaAdicional" ,
							--TASA DE GARANTIA ACTUAL
							"INGMontoATasaGarantiaActual" ,
							---monto a moneda instrumento Garantia actual
							"INGMontoGarantiaActualMonedaInstrumento" ,
							--
							"INGMontoGarantiaActualMonedaAdicional" ,
							--
							"INGDurationGarantiaActual" ,"INGConvexidadGarantiaActual" ,
							--Intereses y Reajustes Corridos
							"INGMontoInteresCorridoGarantiaActual" ,"INGMontoReajusteCorridoGarantiaActual" ,
							"INGMontoInteresCorridoGarantiaActualMonedaInstrumento" ,"INGMontoReajusteCorridoGarantiaActualMonedaInstrumento" ,
							"INGMontoInteresCorridoGarantiaActualMonedaAdicional" ,"INGMontoReajusteCorridoGarantiaActualMonedaAdicional" ,
							-------------------------------
							"INGMontoGarantiaActualPibMas" ,"INGMontoGarantiaActualPibMasMonedaInstrumento" ,"INGMontoGarantiaActualPibMasMonedaAdicional" ,
							"INGMontoGarantiaActualPibMenos" ,"INGMontoGarantiaActualPibMenosMonedaInstrumento" ,"INGMontoGarantiaActualPibMenosMonedaAdicional" ,
							--TASA DE GARANTIA PROXIMO
							"INGMontoATasaGarantiaProximo" ,"INGMontoInteresGarantiaProximo" ,"INGMontoReajusteGarantiaProximo" ,
							--montos a moneda instrumento Garantia proximo
							"INGMontoGarantiaProximoMonedaInstrumento" ,"INGMontoInteresGarantiaProximoMonedaInstrumento" ,
							"INGMontoReajusteGarantiaProximoMonedaInstrumento" ,
							---
							"INGMontoGarantiaProximoMonedaAdicional" ,"INGMontoInteresGarantiaProximoMonedaAdicional" ,
							"INGMontoReajusteGarantiaProximoMonedaAdicional" ,
							---
							"INGDurationGarantiaProximo" ,"INGConvexidadGarantiaProximo" ,
							--TASA MERCADO ANTERIOR
							"INGFechaPrecioTasaMercadoAnterior" ,"INGFuenteTasaPrecioMercadoAnterior" ,"INGPrecioMercadoAnterior" ,
							"INGTasaMercadoAnterior" ,"INGMontoATasaMercadoAnterior" ,"INGMontoInteresMercadoAnterior" ,
							"INGMontoReajusteMercadoAnterior" ,
							--montos a moneda instrumento mercado anterior
							"INGMontoMercadoAnteriorMonedaInstrumento" ,"INGMontoInteresMercadoAnteriorMonedaInstrumento" ,
							"INGMontoReajusteMercadoAnteriorMonedaInstrumento" ,
							--
							"INGMontoMercadoAnteriorMonedaAdicional" ,"INGMontoInteresMercadoAnteriorMonedaAdicional" ,
							"INGMontoReajusteMercadoAnteriorMonedaAdicional" ,
							--
							"INGDurationMercadoAnterior" ,"INGConvexidadMercadoAnterior" ,"INGMontoMercadoAnteriorPibMas" ,
							"INGMontoMercadoAnteriorPibMasMonedaInstrumento" ,"INGMontoMercadoAnteriorPibMasMonedaAdicional" ,
							"INGMontoMercadoAnteriorPibMenos" ,"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" ,
							"INGMontoMercadoAnteriorPibMenosMonedaAdicional" ,
							--TASA MERCADO ACTUAL
							"INGFechaPrecioTasaMercadoActual" ,"INGFuenteTasaPrecioMercadoActual" ,"INGPrecioMercadoActual" ,
							"INGTasaMercadoActual" ,"INGMontoATasaMercadoActual" ,
							--montos a moneda instrumento mercado actual
							"INGMontoMercadoActualMonedaInstrumento" ,
							--
							"INGMontoMercadoActualMonedaAdicional" ,
							--
							"INGDurationMercadoActual" ,"INGConvexidadMercadoActual" ,
							--Intereses y Reajustes Corridos
							"INGMontoInteresCorridoMercadoActual" ,"INGMontoReajusteCorridoMercadoActual" ,
							"INGMontoInteresCorridoMercadoActualMonedaInstrumento" ,"INGMontoReajusteCorridoMercadoActualMonedaInstrumento" ,
							"INGMontoInteresCorridoMercadoActualMonedaAdicional" ,"INGMontoReajusteCorridoMercadoActualMonedaAdicional" ,
							-------------------------------
							"INGMontoMercadoActualPibMas" ,"INGMontoMercadoActualPibMasMonedaInstrumento" ,"INGMontoMercadoActualPibMasMonedaAdicional" ,
							"INGMontoMercadoActualPibMenos" ,"INGMontoMercadoActualPibMenosMonedaInstrumento" ,"INGMontoMercadoActualPibMenosMonedaAdicional" ,
							--TASA MERCADO PROXIMO
							"INGFechaPrecioTasaMercadoProximo" ,"INGFuenteTasaPrecioMercadoProximo" ,"INGPrecioMercadoProximo" ,
							"INGTasaMercadoProximo" ,"INGMontoATasaMercadoProximo" ,"INGMontoInteresMercadoProximo" ,
							"INGMontoReajusteMercadoProximo" ,
							--montos a moneda instrumento mercado proximo
							"INGMontoMercadoProximoMonedaInstrumento" ,"INGMontoInteresMercadoProximoMonedaInstrumento" ,
							"INGMontoReajusteMercadoProximoMonedaInstrumento" ,
							--
							"INGMontoMercadoProximoMonedaAdicional" ,"INGMontoInteresMercadoProximoMonedaAdicional" ,
							"INGMontoReajusteMercadoProximoMonedaAdicional" ,
							--
							"INGDurationMercadoProximo" ,"INGConvexidadMercadoProximo" ,	
							--TASA CORREDORA A FECHA ACTUAL
							"INGTasaCorredora" ,"INGFechaTasaCorredora" ,"INGMontoATasaCorredora" ,"INGMontoATasaCorredoraMonedaInstrumento" ,
							"INGMontoATasaCorredoraMonedaAdicional" ,
							--CORTE DE CUPON FECHA DE VALORIZACION ACTUAL
							"INGFechaCorteCupon" ,"INGNumeroCupon" ,"INGIdMonedaPagoCupon" ,"INGMontoCorteCupon" ,"INGFechaPagoCorteCupon" ,
							"INGMontoCapitalCorteCupon" ,"INGMontoInteresCorteCupon" ,"INGMontoReajusteCorteCupon" ,
							--montos corte cupon a moneda instrumento
							"INGMontoCorteCuponMonedaInstrumento" , "INGMontoCapitalCorteCuponMonedaInstrumento" ,
							"INGMontoInteresCorteCuponMonedaInstrumento" ,"INGMontoReajusteCorteCuponMonedaInstrumento" ,
							--
							"INGMontoCorteCuponMonedaAdicional" ,"INGMontoCapitalCorteCuponMonedaAdicional" ,
							"INGMontoInteresCorteCuponMonedaAdicional" ,"INGMontoReajusteCorteCuponMonedaAdicional" ,
							--TIF.. FECHA DE VALORIZACION ACTUAL
							"INGMontoPresenteTIF" ,"INGMontoInteresTIF" ,"INGMontoReajusteTIF" ,"INGMontoInteresAcumTIF" ,
							"INGMontoReajusteAcumTIF" ,
							--montos tif a moneda instrumento
							"INGMontoPresenteTIFMonedaInstrumento" ,"INGMontoInteresTIFMonedaInstrumento" ,
							"INGMontoReajusteTIFMonedaInstrumento" ,"INGMontoInteresAcumTIFMonedaInstrumento" ,
							"INGMontoReajusteAcumTIFMonedaInstrumento" ,
							--
							"INGMontoPresenteTIFMonedaAdicional" ,"INGMontoInteresTIFMonedaAdicional" ,
							"INGMontoReajusteTIFMonedaAdicional" ,"INGMontoInteresAcumTIFMonedaAdicional" ,
							"INGMontoReajusteAcumTIFMonedaAdicional" ,
							--FORWARD
							"INGMontoMonedaPrincipal" ,"INGMontoMonedaSecundaria" ,"INGMontoDiferido" ,
							"INGMontoUtilidadDiferida" ,"INGMontoPerdidaDiferida" ,"INGMontoMargen" ,
							"INGMontoValorRazonableActivo" ,"INGMontoValorRazonablePasivo" ,"INGMontoValorRazonableNeto" ,
							"INGTasaValorRazonableActivo" ,"INGTasaValorRazonablePasivo" ,"INGMontoDevengoTipoCambio" ,
							"INGMontoDevengoTipoCambioAnterior" ,"INGMontoDiferidoDiario" ,"INGMontoDiferidoAcumulado" ,
							"INGMontoMatriz" ,
							--VALORIZACIONES
							--RETIRO
							"EGRTicketValorizacion" ,"EGRValorizacionConProblemas" ,"EGRFechaFactorMonedaInstrumento" ,
							"EGRValorFactorMonedaInstrumento" ,"EGRFechaFactorMonedaAdicional" ,"EGRValorFactorMonedaAdicional" ,
							"EGRCantidad" ,"EGRValorRescate" ,"EGRDisponible" ,"EGRPrecio" ,"EGRTasa" ,
							"EGRMonto" ,"EGRMontoMonedaInstrumento" ,"EGRMontoMonedaAdicional" ,
							--TASA MERCADO ACTUAL
							"EGRFechaPrecioTasaMercado" ,"EGRFuenteTasaPrecioMercado" ,"EGRPrecioMercado" ,"EGRTasaMercado" ,
							"EGRMontoATasaMercado" ,"EGRMontoMercadoMonedaInstrumento" ,"EGRMontoMercadoMonedaAdicional" ,
							---
							"EGRPrecioAPlazo" ,"EGRMontoAPlazo" ,"EGRMontoAPlazoMonedaInstrumento" ,"EGRMontoAPlazoMonedaAdicional" ,
							--A FECHA DE OPERACION
							"EGRMontoPresenteTIF" ,"EGRMontoPresenteTIFMonedaInstrumento" ,"EGRMontoPresenteTIFMonedaAdicional" 
						FROM "BSCL.Custodia.BD::GBL.ValorizarMovimientoCustodia_Salida_RegistrosValorizados";
		
		SELECT COALESCE(MAX("IdProceso"),0) + 1 INTO v_IdProceso FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ;
		
		SELECT "IdProcesoMovimientoCustodia","EsDemo","IdEmpresa","IdUsuario","IdServicioAgrupador","ServicioAgrupador","GrabaLog"
		INTO v_TicketAtencion,v_EsDemo,v_IdEmpresa,v_IdUsuario,v_IdServicioAgrupador,v_ServicioAgrupador,v_GrabaLog
		FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros";
		
		INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ("IdProceso","NombreProceso","FechaInicioProceso")
		SELECT :v_IdProceso,::CURRENT_OBJECT_NAME,CURRENT_UTCTIMESTAMP FROM DUMMY;
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			:v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
			'Inicio','Inicio Utilitario'
		FROM DUMMY;
		 
		BEGIN AUTONOMOUS TRANSACTION
			INSERT INTO "BSCL.Custodia.BD::CUS.GenerarMovimientoCustodiaLog"(
				"IdGenerarMovimientoCustodiaLog","IdProcesoMovimientoCustodia","FechaProcesoMovimientoCustodia",
				-----------------------------------------------------------------
				"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
				--CAMPOS DE VALIDACION
				"IdValidacionesMovimientoCustodia","CampoAValidar","ValorFalloValidacion",
				-----------------------------------------------------------------	
				--CAMPOS DE ERRORES
				"OrigenError","TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2",
				"ValorDato2","Descripcion",
				--DATOS COMPLEMENTARIOS PARA COMPRENDER MEJOR EL ERROR
				"CodigoMensajeInterno","GeneraMovimientoEnTransito","TienePasoContingencia","CantidadARetirar","CantidadReservada",
				"CantidadSaldo")
			SELECT
				"BSCL.Custodia.BD.Secuencias::GenerarMovimientoCustodiaLog".NEXTVAL,:v_TicketAtencion,"FechaProcesoMovimientoCustodia",
				-----------------------------------------------------------------
				"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
				--CAMPOS DE VALIDACION
				"IdValidacionesMovimientoCustodia","CampoAValidar","ValorFalloValidacion",
				-----------------------------------------------------------------	
				--CAMPOS DE ERRORES
				"OrigenError","TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2",
				"ValorDato2","Descripcion",
				--DATOS COMPLEMENTARIOS PARA COMPRENDER MEJOR EL ERROR
				"CodigoMensajeInterno","GeneraMovimientoEnTransito","TienePasoContingencia","CantidadARetirar","CantidadReservada",
				"CantidadSaldo"
			FROM :v_GenerarMovimientoCustodia_Salida_Log
			ORDER BY "FechaProcesoMovimientoCustodia";
			COMMIT;
		END;--AUTONOMOUS TRANSACTION
	
		IF (:v_EsDemo = 'N') THEN
			--:::::::::::::::::::::::::::::::::::::::::::::::GENERAR::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
			BEGIN AUTONOMOUS TRANSACTION
				UPDATE p
					SET "ProcesoConProblemas" = o."ProcesoConProblemas","FechaFinProceso" = o."FechaFinProceso"
				FROM "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaParametrosEntrada" p
				INNER JOIN :v_GenerarMovimientoCustodia_Entrada_Parametros AS o ON (o."IdProcesoMovimientoCustodia" = p."IdProcesoMovimientoCustodia");
			
			--:::::::::::::::::::::::::::::::::::::::::::::::CREAR::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
				INSERT INTO "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaDatosEntrada"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
					"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
					"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion","IdAccionOperacionRelacionado",
					--GARANTIAS
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
					"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
					"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo",
					"TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
				 	--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
				 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado"	,
					--SE UTILIZA EN OSA, LO INGRESADO EN CARTERA PASA A ESTAR EN GARANTIA
					"MueveGarantiaAutomatica",
					---RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
					"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
					"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
					"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
					"TipoCambioObservado",
					-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
					"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
					"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion","IdAccionOperacionRelacionado",
					--GARANTIAS
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
					"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
					"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo",
					"TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
				 	--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
				 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado"	,
					--SE UTILIZA EN OSA, LO INGRESADO EN CARTERA PASA A ESTAR EN GARANTIA
					"MueveGarantiaAutomatica",
					---RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
					"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
					"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
					"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
					"TipoCambioObservado",
					-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
				FROM :v_CrearMovimientoCustodia_Entrada_Datos;
	
				INSERT INTO "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaConfiguracion"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdTipoOperacion",
					----------------------------------------------------------
					"IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
					"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
					"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
					"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
					"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
					"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
					"IdTipoOperacionOrigenRelacionada","ExcluirCarteraComprometida",
					-----------------------------------------------------------------------------------------
					"OrdenEjecucionMovimientos",
					--------------------------------Cliente Ingreso-----------------------------------------
					"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
					"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
					-----------------------Cliente Ingreso Carteras Automaticas------------------------------
					"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
					"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
					"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
					"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
					----------------------------------------------------------------------------------------
					--------------------------------Cliente Retiro------------------------------------------
					"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
					"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
					-----------------------Cliente Retiro Carteras Automaticas------------------------------
					"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
					"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
					"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
					"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
					--------------------------------------------------------------
					"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto")
				SELECT
					:v_TicketAtencion,
					"IdInterno","IdTipoOperacion",
					----------------------------------------------------------
					"IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
					"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
					"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
					"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
					"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
					"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
					"IdTipoOperacionOrigenRelacionada","ExcluirCarteraComprometida",
					-----------------------------------------------------------------------------------------
					"OrdenEjecucionMovimientos",
					--------------------------------Cliente Ingreso-----------------------------------------
					"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
					"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
					-----------------------Cliente Ingreso Carteras Automaticas------------------------------
					"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
					"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
					"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
					"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
					----------------------------------------------------------------------------------------
					--------------------------------Cliente Retiro------------------------------------------
					"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
					"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
					-----------------------Cliente Retiro Carteras Automaticas------------------------------
					"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
					"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
					"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
					"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
					--------------------------------------------------------------
					"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto"
				FROM :v_CrearMovimientoCustodia_Entrada_Configuracion;
	
				INSERT INTO "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaConfiguracionOUT"(
					"IdProcesoMovimientoCustodia",
					---------------------------------------------------------------------------------------------------------------------------------
					"IdInterno","IdTipoOperacion",
					----------------------------------------------------------
					"SeEjecutaPasoConfiguracionClienteIngreso","SeEjecutaPasoConfiguracionClienteRetiro","IdAccionOperacion",
					"UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
					"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
					"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
					"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
					"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
					"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
					"IdTipoOperacionOrigenRelacionada","ExcluirCarteraComprometida",
					-----------------------------------------------------------------------------------------
					"OrdenEjecucionMovimientos",
					--------------------------------Cliente Ingreso-----------------------------------------
					"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
					"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
					-----------------------Cliente Ingreso Carteras Automaticas------------------------------
					"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
					"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
					"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
					"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
					----------------------------------------------------------------------------------------
					--------------------------------Cliente Retiro------------------------------------------
					"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
					"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
					-----------------------Cliente Retiro Carteras Automaticas------------------------------
					"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
					"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
					"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
					"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
					--------------------------------------------------------------
					"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdTipoOperacion",
					----------------------------------------------------------
					"SeEjecutaPasoConfiguracionClienteIngreso","SeEjecutaPasoConfiguracionClienteRetiro","IdAccionOperacion",
					"UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
					"PoseeInstrumentoSubyacente","PermiteRetiroParcialSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva",
					"RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado",
					"UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
					"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
					"PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado","BaseCalculoDeInteres",
					"IdTipoOperacionOrigenRelacionada","ExcluirCarteraComprometida",
					-----------------------------------------------------------------------------------------
					"OrdenEjecucionMovimientos",
					--------------------------------Cliente Ingreso-----------------------------------------
					"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
					"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
					-----------------------Cliente Ingreso Carteras Automaticas------------------------------
					"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
					"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
					"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
					"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
					----------------------------------------------------------------------------------------
					--------------------------------Cliente Retiro------------------------------------------
					"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING",
					"CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
					-----------------------Cliente Retiro Carteras Automaticas------------------------------
					"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
					"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
					----------------------------------------------------------------------------------------
					--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
					"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
					"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
					"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
					--------------------------------------------------------------
					"ConCertificadoAutomatico","GeneraMovimientoEnTransito","IdConcepto"
				FROM :v_CrearMovimientoCustodia_Salida_Configuracion;
			
				INSERT INTO "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaValidacionesCustodiaOUT"(
					"IdProcesoMovimientoCustodia","IdInternoCrear","IdInterno","IdInternoCorregido","CustodiaValida","IdCustodia","FolioCustodia","IdCliente",
					"IdPortafolio","IdCuentaCustodioPortafolio","INGIdCondicionLiquidacion","INGFechaLiquidacion","IdTipoComprobante","FolioComprobante",
					"INGCantidad","INGValorRescate","CantidadDisponible","IdTipoOperacion","TipoCarteraCorredor",
					-----INSTRUMENTO------------------------------
					"MyIdInstrumento","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano",
					"CorteMinimo","TIFAfectoBeneficio","TIFTasa","EsIntraday",
					--DATOS BUSQUEDA
					"IdBovedaEsIncluyente","UtilizaFechaLiquidacionBusqueda","BovedaBusqueda","FechaLiquidacionBusqueda","FolioCustodiaBusqueda",
					"IdTipoComprobanteBusqueda","FolioComprobanteBusqueda","IdInstrumentoBusqueda","IdEmisorBusqueda","FechaVencimientoInstrumentoBusqueda",
					"IdTipoReajusteBusqueda","EsFungibleBusqueda","IdCuentaCustodioPortafolioBusqueda","EsBovedaCorrecta","FechaLiquidacionCorrecta",
					"IdTipoComprobanteCorrecto","FolioCustodiaCorrecto","FolioComprobanteCorrecto","IdInstrumentoCorrecto","IdEmisorCorrecto",
					"FechaVencimientoInstrumentoCorrecta","IdTipoReajusteCorrecto","EsFungibleCorrecto","EsCarteraBuscada",
					"IdCuentaCustodioPortafolioCorrecto","IdTipoOperacionBuscada","TipoCarteraCorredorCorrecto")
				SELECT
					:v_TicketAtencion,"IdInternoCrear","IdInterno","IdInternoCorregido","CustodiaValida","IdCustodia","FolioCustodia","IdCliente","IdPortafolio",
					"IdCuentaCustodioPortafolio","INGIdCondicionLiquidacion","INGFechaLiquidacion","IdTipoComprobante","FolioComprobante","INGCantidad",
					"INGValorRescate","CantidadDisponible","IdTipoOperacion","TipoCarteraCorredor",
					-----INSTRUMENTO------------------------------
					"MyIdInstrumento","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano",
					"CorteMinimo","TIFAfectoBeneficio","TIFTasa","EsIntraday",
					--DATOS BUSQUEDA
					"IdBovedaEsIncluyente","UtilizaFechaLiquidacionBusqueda","BovedaBusqueda","FechaLiquidacionBusqueda","FolioCustodiaBusqueda",
					"IdTipoComprobanteBusqueda","FolioComprobanteBusqueda","IdInstrumentoBusqueda","IdEmisorBusqueda","FechaVencimientoInstrumentoBusqueda",
					"IdTipoReajusteBusqueda","EsFungibleBusqueda","IdCuentaCustodioPortafolioBusqueda","EsBovedaCorrecta","FechaLiquidacionCorrecta",
					"IdTipoComprobanteCorrecto","FolioCustodiaCorrecto","FolioComprobanteCorrecto","IdInstrumentoCorrecto","IdEmisorCorrecto",
					"FechaVencimientoInstrumentoCorrecta","IdTipoReajusteCorrecto","EsFungibleCorrecto","EsCarteraBuscada",
					"IdCuentaCustodioPortafolioCorrecto","IdTipoOperacionBuscada","TipoCarteraCorredorCorrecto"
				FROM :v_CrearMovimientoCustodia_Salida_ValidacionesCustodia;
	
				INSERT INTO "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaPlantillaAplicadaOUT"(
					"IdProcesoMovimientoCustodia",
					---------------------------------------------
					"IdInterno","IdPlantillaMovimientoCustodia","TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda",
					"FolioComprobanteBusqueda","FolioOperacionRelacionada",
					--GARANTIAS,
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
					"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
					"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo",
					"TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
					"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
					"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
					--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen",
					"MontoValorRazonableActivo","MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo",
					"MontoDevengoTipoCambio","MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot",
					"ValorTipoCambioCotizador","EsPuntos","TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo",
					"TipoCambioAnticipo","MontoARecibirAnticipo","TipoCambioObservado")
				SELECT
					:v_TicketAtencion,
					---------------------------------------------
					"IdInterno","IdPlantillaMovimientoCustodia","TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda",
					"FolioComprobanteBusqueda","FolioOperacionRelacionada",
					--GARANTIAS,
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
					"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
					"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo",
					"TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
					"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
					"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
					--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen",
					"MontoValorRazonableActivo","MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo",
					"MontoDevengoTipoCambio","MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot",
					"ValorTipoCambioCotizador","EsPuntos","TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo",
					"TipoCambioAnticipo","MontoARecibirAnticipo","TipoCambioObservado"
				FROM :v_CrearMovimientoCustodia_Salida_PlantillaAplicada;
				
				--::::::::::::::::::::::::::::::::::::::::::::::::PROCESAR:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasVigentes"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdCartera","FolioCartera",
					--Filtros de busqueda y ordenamiento
					"IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdCartera","FolioCartera",
					--Filtros de busqueda y ordenamiento
					"IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda"
				FROM :v_ProcesarMovimientoCustodia_Entrada_CarterasVigentes;
			
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasVigentesParaUtilizar"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","EsTransitoOrigenCartera",
					--Filtros de busqueda y ordenamiento
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento",
					--CANTIDAD
					"INGCantidad","INGValorRescate","CantidadDisponible",
					--TASA DE COMPRA
					"INGPrecioCompra","INGTasaCompra","IdEstado")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","EsTransitoOrigenCartera",
					--Filtros de busqueda y ordenamiento
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","MyIdInstrumento",
					--CANTIDAD
					"INGCantidad","INGValorRescate","CantidadDisponible",
					--TASA DE COMPRA
					"INGPrecioCompra","INGTasaCompra","IdEstado"
				FROM :v_ProcesarMovimientoCustodia_Entrada_CarterasVigentesParaUtilizar;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaSubyacentesVigentes"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada",
					"FolioCarteraRetirada","IdMovimientoCarteraEnTransito","EsTransitoOrigenCartera","MyIdInstrumento")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada",
					"FolioCarteraRetirada","IdMovimientoCarteraEnTransito","EsTransitoOrigenCartera","MyIdInstrumento"
				FROM :v_ProcesarMovimientoCustodia_Entrada_SubyacentesVigentes;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaSubyacentesVigentesParaUtilizar"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada","FolioCarteraRetirada","IdMovimientoCarteraEnTransito",
					"EsTransitoOrigenCartera","MyIdInstrumento",
					--TASA DE COMPRA
					"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
					--TASA SUBYACENTE
					"INGPrecioSubyacente","INGTasaSubyacente",
					--TASA MERCADO ACTUAL
					"INGPrecioMercadoActual","INGTasaMercadoActual",
					--CANTIDAD
					"INGCantidad","INGValorRescate","CantidadDisponible")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","IdCarteraRetirada","FolioCarteraRetirada","IdMovimientoCarteraEnTransito",
					"EsTransitoOrigenCartera","MyIdInstrumento",
					--TASA DE COMPRA
					"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
					--TASA SUBYACENTE
					"INGPrecioSubyacente","INGTasaSubyacente",
					--TASA MERCADO ACTUAL
					"INGPrecioMercadoActual","INGTasaMercadoActual",
					--CANTIDAD
					"INGCantidad","INGValorRescate","CantidadDisponible"
				FROM :v_ProcesarMovimientoCustodia_Entrada_SubyacentesVigentesParaUtilizar;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaGarantiasVigentes"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdGarantia","FolioGarantia",
					--Filtros de busqueda y ordenamiento
					"MyIdInstrumento","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdInternoCorregido","IdInternoCrear","IdGarantia","FolioGarantia",
					--Filtros de busqueda y ordenamiento
					"MyIdInstrumento","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","IdBoveda","IdBovedaEsIncluyente","EsCarteraBuscada",
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","UtilizaFechaLiquidacionBusqueda","FechaLiquidacionBusqueda"
				FROM :v_ProcesarMovimientoCustodia_Entrada_GarantiasVigentes;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaGarantiasVigentesParaUtilizar"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdGarantia","FolioGarantia","IdCarteraOrigen","FolioCarteraOrigen","MyIdInstrumento",
					------------------------------------------------------
					--TASA DE COMPRA
					"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
					--TASA GARANTIA
					"INGPrecioGarantia","INGTasaGarantia",
					--TASA MERCADO ACTUAL
					"INGPrecioMercadoActual","INGTasaMercadoActual",
					--CANTIDAD
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","INGCantidad","INGValorRescate","CantidadDisponible")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdGarantia","FolioGarantia","IdCarteraOrigen","FolioCarteraOrigen","MyIdInstrumento",
					------------------------------------------------------
					--TASA DE COMPRA
					"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
					--TASA GARANTIA
					"INGPrecioGarantia","INGTasaGarantia",
					--TASA MERCADO ACTUAL
					"INGPrecioMercadoActual","INGTasaMercadoActual",
					--CANTIDAD
					"INGIdCondicionLiquidacion","INGFechaLiquidacion","INGCantidad","INGValorRescate","CantidadDisponible"
				FROM :v_ProcesarMovimientoCustodia_Entrada_GarantiasVigentesParaUtilizar;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasReservadas"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdReserva","IdOperacion","IdUsuarioCreacion","FolioCarteraOrigen","IngCantidad","IngValorRescate",
					"CantidadReservada")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdReserva","IdOperacion","IdUsuarioCreacion","FolioCarteraOrigen","IngCantidad","IngValorRescate",
					"CantidadReservada"
				FROM :v_ProcesarMovimientoCustodia_Entrada_CarterasReservadas;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaOperaciones"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdGenerarMovimientoCustodia","IdUsuario","FolioCustodia","IdTipoComprobante","FolioComprobante",
					"FolioOperacionRelacionada","CliIngGeneraMvtoEnTransitoSinCustodia","CliEgrGeneraMvtoEnTransitoSinCustodia",
					"EsNominalIngreso","EsNominalRetiro","IdGrupoOperacionInterno","IdGrupoOperacion","IdOperacion",
					"RetiraCarteraEspecifica","UsuarioPoseeCarteraReservada","CantidadPasosEjecucion","EsIngreso",
					"EstaCertificando","ConCertificadoAutomatico","IdMovimientoCarteraEnTransito")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdGenerarMovimientoCustodia","IdUsuario","FolioCustodia","IdTipoComprobante","FolioComprobante",
					"FolioOperacionRelacionada","CliIngGeneraMvtoEnTransitoSinCustodia","CliEgrGeneraMvtoEnTransitoSinCustodia",
					"EsNominalIngreso","EsNominalRetiro","IdGrupoOperacionInterno","IdGrupoOperacion","IdOperacion",
					"RetiraCarteraEspecifica","UsuarioPoseeCarteraReservada","CantidadPasosEjecucion","EsIngreso",
					"EstaCertificando","ConCertificadoAutomatico","IdMovimientoCarteraEnTransito"
				FROM :v_ProcesarMovimientoCustodia_Entrada_Operaciones;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaInsPorOperacion"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdGenerarMovimientoCustodia","IdInterno","IdInternoCorregido","IdInternoCrear","IdGrupoOperacion",
					"IdGrupoOperacionInterno","TipoRegistro","IdCondicionLiquidacion","FechaLiquidacion","MyIdInstrumento",
					"EsInstrumentoRelacionado","IdBoveda","EsNominalIngreso","EsNominalRetiro",
					--PARA MODIFICACIONES (NUEVO REGISTRO)
					"IdCustodia","FolioCustodia","TablaAfectada",
					--CANTIDAD
					"Cantidad","ValorRescate","CantidadOperacion",
					--TASA DE COMPRA
					"PrecioCompra","TasaCompra","PrecioAPlazo")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdGenerarMovimientoCustodia","IdInterno","IdInternoCorregido","IdInternoCrear","IdGrupoOperacion",
					"IdGrupoOperacionInterno","TipoRegistro","IdCondicionLiquidacion","FechaLiquidacion","MyIdInstrumento",
					"EsInstrumentoRelacionado","IdBoveda","EsNominalIngreso","EsNominalRetiro",
					--PARA MODIFICACIONES (NUEVO REGISTRO)
					"IdCustodia","FolioCustodia","TablaAfectada",
					--CANTIDAD
					"Cantidad","ValorRescate","CantidadOperacion",
					--TASA DE COMPRA
					"PrecioCompra","TasaCompra","PrecioAPlazo"
				FROM :v_ProcesarMovimientoCustodia_Entrada_InstrumentosPorOperacion;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaOrdenEjecucion"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdOrdenEjecucionOperacionesMovimientoCustodia","IdTipoOperacion","IdClientePaso","IdPortafolioPaso",
					"IdCuentaCustodioPortafolioPaso","IdCondicionLiquidacionPaso","FechaLiquidacionPaso",
					----------------------------------------------------
					"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","NombreColumnaPaso","PasoEsIngreso","TablaAfectadaPorPaso","PasoPerteneceClienteRetiro",
					------------------------------------------------------------------------------------------------
					"PasoEjecucion","TablaOrigenDatosDelPaso","OrigenDatosDelPaso","PasoRelacionadoCon","IdEstadoPaso",
					---------------------------------------------------------
					"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
					"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
					"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
					"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
					"PasoValidaCustodiaOrigenTransito")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdOrdenEjecucionOperacionesMovimientoCustodia","IdTipoOperacion","IdClientePaso","IdPortafolioPaso",
					"IdCuentaCustodioPortafolioPaso","IdCondicionLiquidacionPaso","FechaLiquidacionPaso",
					----------------------------------------------------
					"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","NombreColumnaPaso","PasoEsIngreso","TablaAfectadaPorPaso","PasoPerteneceClienteRetiro",
					------------------------------------------------------------------------------------------------
					"PasoEjecucion","TablaOrigenDatosDelPaso","OrigenDatosDelPaso","PasoRelacionadoCon","IdEstadoPaso",
					---------------------------------------------------------
					"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
					"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
					"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
					"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
					"PasoValidaCustodiaOrigenTransito"
				FROM :v_ProcesarMovimientoCustodia_Entrada_OrdenEjecucion;
			
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaTablaDesarrolloOUT"(
					"IdProcesoMovimientoCustodia",
					----------------------------------------------------------------------------------------
					"TablaAfectada","IdEstado",
					--origen de informacion
					"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
					"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion","EGRIdCustodiaOrigenInformacion",
					--FIJA
					"Cantidad","Precio","Tasa","Valoriza",
					---INGRESO------------------------------------------------------------------------------
					"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion","INGIdGrupoOperacionInterno",
					"INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad","INGValoriza","INGEsRegistroNuevo","INGEsSaldo",
					"INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion","INGIdCustodia","INGFolioCustodia","INGIdCondicionLiquidacion",
					"INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa","INGPrecio","INGTasa","INGIdTipoOperacion","INGIdConcepto","INGCodigoIFRS",
					"INGEsIntraday","INGIdCliente","INGIdPortafolio","INGIdCuentaCustodioPortafolio","INGEsNominal","INGMantieneFolio",
					"INGEsRegistroGeneradoPorSistema",
					--INGRESO RELACIONADO			
					"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
					--RETIRO--------------------------------------------------------------------------------
					"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion","EGRIdGrupoOperacionInterno",
					"EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad","EGRPrecio","EGRTasa","EGRValoriza","EGRIdTipoOperacion",
					"EGRIdConcepto","EGRIdCondicionLiquidacion","EGRFechaLiquidacion",
					--RETIRO RELACIONADO			
					"EGRIdTablaDesarrolloRel","EGRTablaRelacionada")
				SELECT
					:v_TicketAtencion,
					----------------------------------------------------------------------------------------
					"TablaAfectada","IdEstado",
					--origen de informacion
					"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
					"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion","EGRIdCustodiaOrigenInformacion",
					--FIJA
					"Cantidad","Precio","Tasa","Valoriza",
					---INGRESO------------------------------------------------------------------------------
					"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion","INGIdGrupoOperacionInterno",
					"INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad","INGValoriza","INGEsRegistroNuevo","INGEsSaldo",
					"INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion","INGIdCustodia","INGFolioCustodia","INGIdCondicionLiquidacion",
					"INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa","INGPrecio","INGTasa","INGIdTipoOperacion","INGIdConcepto","INGCodigoIFRS",
					"INGEsIntraday","INGIdCliente","INGIdPortafolio","INGIdCuentaCustodioPortafolio","INGEsNominal","INGMantieneFolio",
					"INGEsRegistroGeneradoPorSistema",
					--INGRESO RELACIONADO			
					"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
					--RETIRO--------------------------------------------------------------------------------
					"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion","EGRIdGrupoOperacionInterno",
					"EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad","EGRPrecio","EGRTasa","EGRValoriza","EGRIdTipoOperacion",
					"EGRIdConcepto","EGRIdCondicionLiquidacion","EGRFechaLiquidacion",
					--RETIRO RELACIONADO			
					"EGRIdTablaDesarrolloRel","EGRTablaRelacionada"
				FROM :v_ProcesarMovimientoCustodia_Salida_TablaDesarrollo;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasReservadasOUT"(
					"IdProcesoMovimientoCustodia","IdReserva","Cantidad")
				SELECT
					:v_TicketAtencion,"IdReserva","Cantidad"
				FROM :v_ProcesarMovimientoCustodia_Salida_CarterasReservadas;
				
				
				--:::::::::::::::::::::::::::::::::::::::::::::::ACTUALIZAR::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaDatosEntrada"(
					"IdProcesoMovimientoCustodia",
					--
					"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
					"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
					"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion",
					--GARANTIAS
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro",
					--se creo para guardar el id y portafolio de la corredora para compras con pacto
					"IdClienteRelacionado","IdPortafolioRelacionado",
					---------------------------------------------------------
					"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
					"CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
					"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
					"UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
				 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
					"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
					"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
					"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
					"TipoCambioObservado",
					-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo")
				SELECT
					:v_TicketAtencion,
					--
					"IdInterno","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","EstaCertificando","IdMovimientoCarteraEnTransito",
					"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
					"FolioOperacionRelacionada","EsModificacion","EsDatosModificacion",
					--GARANTIAS
					"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
					"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
					--OPERACION
					"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
					"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
					"IdCuentaCustodioPortafolioRetiro",
					--se creo para guardar el id y portafolio de la corredora para compras con pacto
					"IdClienteRelacionado","IdPortafolioRelacionado",
					---------------------------------------------------------
					"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
					"CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
					"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
					"UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
					"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
					"IdBovedaEsIncluyente",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate",
				 	--INSTRUMENTO RELACIONADO
					"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
					"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
					"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
				 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
					"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
					"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					"MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado",
					"PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado",
					"MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
					"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
					--FORWARD
					"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
					"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
					"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
					"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
					"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
					"TipoCambioObservado",
					-- Necesarios para la carga desde una fuente externa, por ejemplo un archivo
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
				FROM :v_ActualizarMovimientoCustodia_Entrada_Datos;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaTablaDesarrollo"(
					"IdProcesoMovimientoCustodia",
					----------------------------------------------------------------------------------------
					"TablaAfectada","IdEstado",
					--origen de informacion
					"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
					"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion",
					"EGRIdCustodiaOrigenInformacion",
					--FIJA
					"Cantidad","Precio","Tasa","Valoriza",
					---INGRESO------------------------------------------------------------------------------
					"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion",
					"INGIdGrupoOperacionInterno","INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad",
					"INGValoriza","INGEsRegistroNuevo","INGEsSaldo","INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion",
					"INGIdCustodia","INGFolioCustodia","INGIdCondicionLiquidacion","INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa",
					"INGPrecio","INGTasa","INGIdTipoOperacion","INGIdConcepto","INGCodigoIFRS","INGEsIntraday","INGIdCliente","INGIdPortafolio",
					"INGIdCuentaCustodioPortafolio","INGEsNominal","INGMantieneFolio","INGEsRegistroGeneradoPorSistema",
					--INGRESO RELACIONADO			
					"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
					--RETIRO--------------------------------------------------------------------------------
					"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion",
					"EGRIdGrupoOperacionInterno","EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad",
					"EGRPrecio","EGRTasa","EGRValoriza","EGRIdTipoOperacion","EGRIdConcepto","EGRIdCondicionLiquidacion",
					"EGRFechaLiquidacion",
					--RETIRO RELACIONADO			
					"EGRIdTablaDesarrolloRel","EGRTablaRelacionada")
				SELECT
					:v_TicketAtencion,
					----------------------------------------------------------------------------------------
					"TablaAfectada","IdEstado",
					--origen de informacion
					"MyIdInstrumento","INGTablaOrigenDatos","INGIdCustodiaOrigenDatos","INGIdInternoCrearOrigenDatos","INGTablaOrigenInformacion",
					"INGIdCustodiaOrigenInformacion","EGRTablaOrigenDatos","EGRIdCustodiaOrigenDatos","EGRTablaOrigenInformacion",
					"EGRIdCustodiaOrigenInformacion",
					--FIJA
					"Cantidad","Precio","Tasa","Valoriza",
					---INGRESO------------------------------------------------------------------------------
					"INGIdGenerarMovimientoCustodia","INGIdInterno","INGIdInternoCorregido","INGIdInternoCrear","INGIdGrupoOperacion",
					"INGIdGrupoOperacionInterno","INGTipoRegistro","INGIdInternoTablaDesarrollo","INGIdTablaDesarrollo","INGCantidad",
					"INGValoriza","INGEsRegistroNuevo","INGEsSaldo","INGEsCustodiaRetornada","INGMueveCustodia","INGPermiteCertificacion",
					"INGIdCustodia","INGFolioCustodia","INGIdCondicionLiquidacion","INGFechaLiquidacion","INGRetornaCarteraAlPrecioTasa",
					"INGPrecio","INGTasa","INGIdTipoOperacion","INGIdConcepto","INGCodigoIFRS","INGEsIntraday","INGIdCliente","INGIdPortafolio",
					"INGIdCuentaCustodioPortafolio","INGEsNominal","INGMantieneFolio","INGEsRegistroGeneradoPorSistema",
					--INGRESO RELACIONADO			
					"INGIdTablaDesarrolloRel","INGTablaRelacionada","INGIdTablaDesarrolloOperacionRel",
					--RETIRO--------------------------------------------------------------------------------
					"EGRIdGenerarMovimientoCustodia","EGRIdInterno","EGRIdInternoCorregido","EGRIdInternoCrear","EGRIdGrupoOperacion",
					"EGRIdGrupoOperacionInterno","EGRTipoRegistro","EGRIdInternoTablaDesarrollo","EGRIdTablaDesarrollo","EGRCantidad",
					"EGRPrecio","EGRTasa","EGRValoriza","EGRIdTipoOperacion","EGRIdConcepto","EGRIdCondicionLiquidacion",
					"EGRFechaLiquidacion",
					--RETIRO RELACIONADO			
					"EGRIdTablaDesarrolloRel","EGRTablaRelacionada"
				FROM :v_ActualizarMovimientoCustodia_Entrada_TablaDesarrollo;
				
								INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaCarterasReservadas"(
					"IdProcesoMovimientoCustodia",
					--------------------------------------------------------------------------------------------
					"IdReserva","Cantidad")
				SELECT
					:v_TicketAtencion,
					--------------------------------------------------------------------------------------------
					"IdReserva","Cantidad"
				FROM :v_ActualizarMovimientoCustodia_Entrada_CarterasReservadas;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaInsPorOperacion"(
					"IdProcesoMovimientoCustodia",
					-----------------------------------------------------------
					"IdInterno","IdInternoCrear","EsDatosModificacion",
					--INSTRUMENTO
					"MyIdInstrumento","EsInstrumentoRelacionado","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico",
					"IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible",
					"ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano",
					"TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra",
					"TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","PrecioAPlazo","MontoAPlazo",
					"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate")
				SELECT
					:v_TicketAtencion,
					-----------------------------------------------------------
					"IdInterno","IdInternoCrear","EsDatosModificacion",
					--INSTRUMENTO
					"MyIdInstrumento","EsInstrumentoRelacionado","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico",
					"IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible",
					"ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano",
					"TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra",
					"TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","PrecioAPlazo","MontoAPlazo",
					"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
					--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
					"INGCantidad","INGValorRescate"
				FROM :v_ActualizarMovimientoCustodia_Entrada_InstrumentosPorOperacion;
			
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaCarterasVigentes"(
					"IdProcesoMovimientoCustodia",
					--
					"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro",
					--OPERACION
					"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio",
					-----------------------------------------------------------------------------------------
					---se creo para guardar el id y portafolio de la corredora para compras con pacto
					"IdClienteRelacionado","IdPortafolioRelacionado",
					----------------------------------------------------------------------------------------
					"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
					"IdMovimientoCarteraEnTransito","IdConcepto","IdTipoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
					"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
					"UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurseInversion","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoInversionEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
					"TieneCorteDeCupon","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdModalidadCumplimiento","IdReferenciaMercado","IdMonedaPrincipal",
					"IdMonedaSecundaria","ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","MontoMonedaPrincipal",
					"MontoMonedaSecundaria",
					-- Necesarios para la migracion inicial de movimientos desde sebra
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
					---INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador",
					"INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria")
				SELECT
					:v_TicketAtencion,
					--
					"IdCartera","FolioCartera","FolioCarteraRelacionada","FolioGarantiaOrigen","FechaIngresoRegistro",
					--OPERACION
					"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio",
					-----------------------------------------------------------------------------------------
					---se creo para guardar el id y portafolio de la corredora para compras con pacto
					"IdClienteRelacionado","IdPortafolioRelacionado",
					----------------------------------------------------------------------------------------
					"IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional",
					"IdMovimientoCarteraEnTransito","IdConcepto","IdTipoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
					"FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
					"UsaGarantiaEspecifica","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
					--FUERON CREADOS PARA FONDOS MUTUOS
					"TipoCarteraNormativa","FechaCurseInversion","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoInversionEnFactorAjuste",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
					"TieneCorteDeCupon","CodigoIFRS","IdBoveda","CuentaBoveda","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
					"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
					"MontoAPlazoMonedaAdicional",
					--RF Y FORWARD
					"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdModalidadCumplimiento","IdReferenciaMercado","IdMonedaPrincipal",
					"IdMonedaSecundaria","ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","MontoMonedaPrincipal",
					"MontoMonedaSecundaria",
					-- Necesarios para la migracion inicial de movimientos desde sebra
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
					---INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible","INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador",
					"INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria"
				FROM :v_ActualizarMovimientoCustodia_Entrada_CarterasVigentes;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaSubyacentesVigentes"(
					"IdProcesoMovimientoCustodia",
					-----
					"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","FechaIngresoRegistro","IdCarteraRetirada","IdMovimientoCarteraEnTransito",
					--OPERACION RELACIONADA
					"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
					--OPERACION
					"IdTipoCanalIngreso",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor",
					"TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioSubyacente","TasaSubyacente",
					"MontoSubyacente","MontoSubyacenteMonedaInstrumento","MontoSubyacenteMonedaAdicional",
					--INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible")
				SELECT
					:v_TicketAtencion,
					-----
					"IdSubyacente","FolioSubyacente","FolioCarteraRelacionada","FechaIngresoRegistro","IdCarteraRetirada","IdMovimientoCarteraEnTransito",
					--OPERACION RELACIONADA
					"PrecioCompraOperacionRelacionada","TasaCompraOperacionRelacionada",
					--OPERACION
					"IdTipoCanalIngreso",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor",
					"TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioSubyacente","TasaSubyacente",
					"MontoSubyacente","MontoSubyacenteMonedaInstrumento","MontoSubyacenteMonedaAdicional",
					--INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible"
				FROM :v_ActualizarMovimientoCustodia_Entrada_SubyacentesVigentes;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaGarantiasVigentes"(
					"IdProcesoMovimientoCustodia",
					--
					"IdGarantia","FolioGarantia","FolioCarteraOrigen","IdCarteraOrigen","FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada",
					"EsEspecifica","IdContraparteGarantia",
					------------------------------------------------------
					"RazonSocialContraparteGarantia","CuentaContraparteGarantia",
					------------------------------------------------------
					"IdOrigenGarantia","IdEspecieGarantia","FechaIngresoRegistro",
					--OPERACION ORIGEN
					"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
					--OPERACION
					"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio",
					"IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","IdConcepto",
					"IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","IdUsuario","IdModulo","IdMovimientoCarteraEnTransito",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
					"TieneCorteDeCupon","CodigoIFRS","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioGarantia","TasaGarantia",
					"MontoGarantia","MontoGarantiaMonedaInstrumento","MontoGarantiaMonedaAdicional",
					-- Necesarios para la migracion inicial de movimientos desde sebra
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
					--INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible")
				SELECT
					:v_TicketAtencion,
					--
					"IdGarantia","FolioGarantia","FolioCarteraOrigen","IdCarteraOrigen","FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada",
					"EsEspecifica","IdContraparteGarantia",
					------------------------------------------------------
					"RazonSocialContraparteGarantia","CuentaContraparteGarantia",
					------------------------------------------------------
					"IdOrigenGarantia","IdEspecieGarantia","FechaIngresoRegistro",
					--OPERACION ORIGEN
					"PrecioCompraOperacionOrigen","TasaCompraOperacionOrigen",
					--OPERACION
					"IdEmpresa","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio",
					"IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","IdConcepto",
					"IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdSucursal","IdAgente","IdCondicionLiquidacion",
					"FechaLiquidacion","IdUsuario","IdModulo","IdMovimientoCarteraEnTransito",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS",
					"IdTipoReajuste","EsPagaderoEn","FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion",
					"TieneCorteDeCupon","CodigoIFRS","IdMonedaInstrumento","Tamano","TIFAfectoBeneficio","TIFTasa","ISIN",
					--CANTIDADES Y MONTOS
					"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioGarantia","TasaGarantia",
					"MontoGarantia","MontoGarantiaMonedaInstrumento","MontoGarantiaMonedaAdicional",
					-- Necesarios para la migracion inicial de movimientos desde sebra
					"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo",
					--INGRESO
					"INGCantidad","INGValorRescate","CantidadDisponible"
				FROM :v_ActualizarMovimientoCustodia_Entrada_GarantiasVigentes;
				
				--oct, quito foliosout afuera del commit, ya que si falla por una razon ajena al servicio, y despues se realiza nuevamente se graban (dos folios)
				--aunque en realidad se creo solo una vez la cartera
				--y si se intenta anular el registro, se duplican (triplica..) los datos.

				--:::::::::::::::::::::::::::::::::::::::::::::::VALORIZAR::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
				INSERT INTO "BSCL.Custodia.BD::PAR.ValorizarMovimientoCustodiaRegistrosAValorizar"(
					"IdProcesoMovimientoCustodia","IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
					"FolioCustodia","IdCustodia","TablaAfectada","EsIngreso","ValorizaACompra","ValorizaAMercado",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","IdTipoReajuste","FechaEmisionInstrumento",
					"FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano",
					"TIFAfectoBeneficio",
					--OPERACION
					"FechaVencimientoOperacion","PlazoEnDiasOperacion",
					--MONTOS, PRECIO Y TASA
				    "Cantidad","ValorRescate","PrecioAPlazo","PrecioCompra","TasaCompra","PrecioSubyacente","TasaSubyacente","PrecioGarantia","TasaGarantia",
				    "TIFTasa",
				    --PARA OBTENCION DE PRECIOS O TASAS							
					"TablaOrigen","UsoPrecio","TipoCartera","IdTipoCriterioTasa","IdCliente","IdPortafolio")
				SELECT
					:v_TicketAtencion,"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion","FolioCustodia",
					"IdCustodia","TablaAfectada","EsIngreso","ValorizaACompra","ValorizaAMercado",
					--INSTRUMENTO
					"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","IdTipoReajuste","FechaEmisionInstrumento",
					"FechaVencimientoInstrumento","EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano",
					"TIFAfectoBeneficio",
					--OPERACION
					"FechaVencimientoOperacion","PlazoEnDiasOperacion",
					--MONTOS, PRECIO Y TASA
				    "Cantidad","ValorRescate","PrecioAPlazo","PrecioCompra","TasaCompra","PrecioSubyacente","TasaSubyacente","PrecioGarantia","TasaGarantia",
				    "TIFTasa",
				    --PARA OBTENCION DE PRECIOS O TASAS							
					"TablaOrigen","UsoPrecio","TipoCartera","IdTipoCriterioTasa","IdCliente","IdPortafolio"
				FROM :v_ValorizarMovimientoCustodia_Entrada_RegistrosAValorizar;
				
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ValorizarMovimientoCustodiaRegistrosValorizadosOUT"(
					"IdProcesoMovimientoCustodia","IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
					"IdValorizacion","FolioCustodia","IdCustodia","TablaAfectada","EsIngreso","IdProcesamientoCompraAnterior","IdProcesamientoCompraActual",
					"IdProcesamientoCompraProximo","IdProcesamientoMercadoAnterior","IdProcesamientoMercadoActual","IdProcesamientoMercadoProximo",
					"IdProcesamientoSubyacenteAnterior","IdProcesamientoSubyacenteActual","IdProcesamientoSubyacenteProximo","IdProcesamientoGarantiaAnterior",
					"IdProcesamientoGarantiaActual","IdProcesamientoGarantiaProximo","IdProcesamientoCorredora","IdProcesamientoTif","IdProcesamientoRetiroCompra","IdProcesamientoRetiroMercado",
					"IdProcesamientoRetiroTif",
					-------------------------------------------------------------------------------------------------------------------------------------------------
					"ValCompraConProblemasAnterior","ValCompraConProblemasActual","ValCompraConProblemasProximo","ValMercadoConProblemasAnterior",
					"ValMercadoConProblemasActual","ValMercadoConProblemasProximo","ValSubyacenteConProblemasAnterior","ValSubyacenteConProblemasActual",
					"ValSubyacenteConProblemasProximo","ValGarantiaConProblemasAnterior","ValGarantiaConProblemasActual","ValGarantiaConProblemasProximo",
					"ValCorredoraConProblemas","ValTifConProblemas","ValRetiroCompraConProblemas","ValRetiroMercadoConProblemas","ValRetiroTifConProblemas",
					--INGRESO
					"INGCantidad","INGValorRescate","INGPrecioAPlazo","INGMontoAPlazo","INGMontoAPlazoMonedaInstrumento","INGMontoAPlazoMonedaAdicional",
					--FORWARD
					"INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador","INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria",
					--VALORIZACIONES
					--FECHAS DE VALORIZACION
					"INGTicketValorizacion","INGValorizacionConProblemas","INGFechaValorizacionAnterior","INGFechaValorizacionActual",
					"INGFechaValorizacionProximo",
					--PARAMETROS FINANCIEROS
					--moneda instrumento
					"INGFechaFactorMonedaInstrumentoAnterior","INGValorFactorMonedaInstrumentoAnterior","INGFechaFactorMonedaInstrumentoActual",
					"INGValorFactorMonedaInstrumentoActual","INGFechaFactorMonedaInstrumentoProximo","INGValorFactorMonedaInstrumentoProximo",
					--moneda adicional
					"INGFechaFactorMonedaAdicionalAnterior","INGValorFactorMonedaAdicionalAnterior","INGFechaFactorMonedaAdicionalActual",
					"INGValorFactorMonedaAdicionalActual","INGFechaFactorMonedaAdicionalProximo","INGValorFactorMonedaAdicionalProximo",
					--factores valor dolar, euro, etc
					"INGPrecioCompra","INGTasaCompra",
					--
					--TASA DE COMPRA ANTERIOR
					"INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior",
					--montos a moneda instrumento compra anterior
					"INGMontoCompraAnteriorMonedaInstrumento","INGMontoInteresCompraAnteriorMonedaInstrumento","INGMontoReajusteCompraAnteriorMonedaInstrumento",
					---
					"INGMontoCompraAnteriorMonedaAdicional","INGMontoInteresCompraAnteriorMonedaAdicional","INGMontoReajusteCompraAnteriorMonedaAdicional",
					---
					"INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGMontoCompraAnteriorPibMas","INGMontoCompraAnteriorPibMasMonedaInstrumento",
					"INGMontoCompraAnteriorPibMasMonedaAdicional","INGMontoCompraAnteriorPibMenos","INGMontoCompraAnteriorPibMenosMonedaInstrumento",
					"INGMontoCompraAnteriorPibMenosMonedaAdicional",
					--TASA DE COMPRA ACTUAL
					"INGMontoATasaCompraActual",
					--monto a moneda instrumento compra actual
					"INGMontoCompraActualMonedaInstrumento",
					--
					"INGMontoCompraActualMonedaAdicional",
					--
					"INGDurationCompraActual","INGConvexidadCompraActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoCompraActual","INGMontoReajusteCorridoCompraActual","INGMontoInteresCorridoCompraActualMonedaInstrumento",
					"INGMontoReajusteCorridoCompraActualMonedaInstrumento","INGMontoInteresCorridoCompraActualMonedaAdicional",
					"INGMontoReajusteCorridoCompraActualMonedaAdicional",
					--
					"INGMontoCompraActualPibMas","INGMontoCompraActualPibMasMonedaInstrumento","INGMontoCompraActualPibMasMonedaAdicional",
					"INGMontoCompraActualPibMenos","INGMontoCompraActualPibMenosMonedaInstrumento","INGMontoCompraActualPibMenosMonedaAdicional",
					--TASA DE COMPRA PROXIMO
					"INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo",
					--montos a moneda instrumento compra proximo
					"INGMontoCompraProximoMonedaInstrumento","INGMontoInteresCompraProximoMonedaInstrumento","INGMontoReajusteCompraProximoMonedaInstrumento",
					---
					"INGMontoCompraProximoMonedaAdicional","INGMontoInteresCompraProximoMonedaAdicional","INGMontoReajusteCompraProximoMonedaAdicional",
					---
					"INGDurationCompraProximo","INGConvexidadCompraProximo",
					--TASA DE SUBYACENTE ANTERIOR
					"INGMontoATasaSubyacenteAnterior","INGMontoInteresSubyacenteAnterior","INGMontoReajusteSubyacenteAnterior",
					--montos a moneda instrumento subyacente anterior
					"INGMontoSubyacenteAnteriorMonedaInstrumento","INGMontoInteresSubyacenteAnteriorMonedaInstrumento",
					"INGMontoReajusteSubyacenteAnteriorMonedaInstrumento",
					---
					"INGMontoSubyacenteAnteriorMonedaAdicional","INGMontoInteresSubyacenteAnteriorMonedaAdicional",
					"INGMontoReajusteSubyacenteAnteriorMonedaAdicional",
					---
					"INGDurationSubyacenteAnterior","INGConvexidadSubyacenteAnterior","INGMontoSubyacenteAnteriorPibMas",
					"INGMontoSubyacenteAnteriorPibMasMonedaInstrumento","INGMontoSubyacenteAnteriorPibMasMonedaAdicional","INGMontoSubyacenteAnteriorPibMenos",
					"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento","INGMontoSubyacenteAnteriorPibMenosMonedaAdicional",
					--TASA DE SUBYACENTE ACTUAL
					"INGMontoATasaSubyacenteActual",
					---monto a moneda instrumento subyacente actual
					"INGMontoSubyacenteActualMonedaInstrumento",
					--
					"INGMontoSubyacenteActualMonedaAdicional",
					--
					"INGDurationSubyacenteActual","INGConvexidadSubyacenteActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoSubyacenteActual","INGMontoReajusteCorridoSubyacenteActual",
					"INGMontoInteresCorridoSubyacenteActualMonedaInstrumento","INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento",
					"INGMontoInteresCorridoSubyacenteActualMonedaAdicional","INGMontoReajusteCorridoSubyacenteActualMonedaAdicional",
					--
					"INGMontoSubyacenteActualPibMas","INGMontoSubyacenteActualPibMasMonedaInstrumento","INGMontoSubyacenteActualPibMasMonedaAdicional",
					"INGMontoSubyacenteActualPibMenos","INGMontoSubyacenteActualPibMenosMonedaInstrumento","INGMontoSubyacenteActualPibMenosMonedaAdicional",
					--TASA DE SUBYACENTE PROXIMO
					"INGMontoATasaSubyacenteProximo","INGMontoInteresSubyacenteProximo","INGMontoReajusteSubyacenteProximo",
					--montos a moneda instrumento subyacente proximo
					"INGMontoSubyacenteProximoMonedaInstrumento","INGMontoInteresSubyacenteProximoMonedaInstrumento",
					"INGMontoReajusteSubyacenteProximoMonedaInstrumento",
					---
					"INGMontoSubyacenteProximoMonedaAdicional","INGMontoInteresSubyacenteProximoMonedaAdicional",
					"INGMontoReajusteSubyacenteProximoMonedaAdicional",
					---
					"INGDurationSubyacenteProximo","INGConvexidadSubyacenteProximo",
					--TASA DE GARANTIA ANTERIOR
					"INGMontoATasaGarantiaAnterior","INGMontoInteresGarantiaAnterior","INGMontoReajusteGarantiaAnterior",
					--montos a moneda instrumento Garantia anterior
					"INGMontoGarantiaAnteriorMonedaInstrumento","INGMontoInteresGarantiaAnteriorMonedaInstrumento",
					"INGMontoReajusteGarantiaAnteriorMonedaInstrumento",
					---
					"INGMontoGarantiaAnteriorMonedaAdicional","INGMontoInteresGarantiaAnteriorMonedaAdicional",
					"INGMontoReajusteGarantiaAnteriorMonedaAdicional",
					---
					"INGDurationGarantiaAnterior","INGConvexidadGarantiaAnterior","INGMontoGarantiaAnteriorPibMas","INGMontoGarantiaAnteriorPibMasMonedaInstrumento",
					"INGMontoGarantiaAnteriorPibMasMonedaAdicional","INGMontoGarantiaAnteriorPibMenos","INGMontoGarantiaAnteriorPibMenosMonedaInstrumento",
					"INGMontoGarantiaAnteriorPibMenosMonedaAdicional",
					--TASA DE GARANTIA ACTUAL
					"INGMontoATasaGarantiaActual",
					---monto a moneda instrumento Garantia actual
					"INGMontoGarantiaActualMonedaInstrumento",
					--
					"INGMontoGarantiaActualMonedaAdicional",
					--
					"INGDurationGarantiaActual","INGConvexidadGarantiaActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoGarantiaActual","INGMontoReajusteCorridoGarantiaActual","INGMontoInteresCorridoGarantiaActualMonedaInstrumento",
					"INGMontoReajusteCorridoGarantiaActualMonedaInstrumento","INGMontoInteresCorridoGarantiaActualMonedaAdicional",
					"INGMontoReajusteCorridoGarantiaActualMonedaAdicional",
					-------------------------------
					"INGMontoGarantiaActualPibMas","INGMontoGarantiaActualPibMasMonedaInstrumento","INGMontoGarantiaActualPibMasMonedaAdicional",
					"INGMontoGarantiaActualPibMenos","INGMontoGarantiaActualPibMenosMonedaInstrumento","INGMontoGarantiaActualPibMenosMonedaAdicional",
					--TASA DE GARANTIA PROXIMO
					"INGMontoATasaGarantiaProximo","INGMontoInteresGarantiaProximo","INGMontoReajusteGarantiaProximo",
					--montos a moneda instrumento Garantia proximo
					"INGMontoGarantiaProximoMonedaInstrumento","INGMontoInteresGarantiaProximoMonedaInstrumento","INGMontoReajusteGarantiaProximoMonedaInstrumento",
					---
					"INGMontoGarantiaProximoMonedaAdicional","INGMontoInteresGarantiaProximoMonedaAdicional","INGMontoReajusteGarantiaProximoMonedaAdicional",
					---
					"INGDurationGarantiaProximo","INGConvexidadGarantiaProximo",
					--TASA MERCADO ANTERIOR
					"INGFechaPrecioTasaMercadoAnterior","INGFuenteTasaPrecioMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior",
					"INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior",
					--montos a moneda instrumento mercado anterior
					"INGMontoMercadoAnteriorMonedaInstrumento","INGMontoInteresMercadoAnteriorMonedaInstrumento",
					"INGMontoReajusteMercadoAnteriorMonedaInstrumento",
					--
					"INGMontoMercadoAnteriorMonedaAdicional","INGMontoInteresMercadoAnteriorMonedaAdicional","INGMontoReajusteMercadoAnteriorMonedaAdicional",
					--
					"INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGMontoMercadoAnteriorPibMas","INGMontoMercadoAnteriorPibMasMonedaInstrumento",
					"INGMontoMercadoAnteriorPibMasMonedaAdicional","INGMontoMercadoAnteriorPibMenos","INGMontoMercadoAnteriorPibMenosMonedaInstrumento",
					"INGMontoMercadoAnteriorPibMenosMonedaAdicional",
					--TASA MERCADO ACTUAL
					"INGFechaPrecioTasaMercadoActual","INGFuenteTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual",
					"INGMontoATasaMercadoActual",
					--montos a moneda instrumento mercado actual
					"INGMontoMercadoActualMonedaInstrumento",
					--
					"INGMontoMercadoActualMonedaAdicional",
					--
					"INGDurationMercadoActual","INGConvexidadMercadoActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoMercadoActual","INGMontoReajusteCorridoMercadoActual","INGMontoInteresCorridoMercadoActualMonedaInstrumento",
					"INGMontoReajusteCorridoMercadoActualMonedaInstrumento","INGMontoInteresCorridoMercadoActualMonedaAdicional",
					"INGMontoReajusteCorridoMercadoActualMonedaAdicional",
					-------------------------------
					"INGMontoMercadoActualPibMas","INGMontoMercadoActualPibMasMonedaInstrumento","INGMontoMercadoActualPibMasMonedaAdicional",
					"INGMontoMercadoActualPibMenos","INGMontoMercadoActualPibMenosMonedaInstrumento","INGMontoMercadoActualPibMenosMonedaAdicional",
					--TASA MERCADO PROXIMO
					"INGFechaPrecioTasaMercadoProximo","INGFuenteTasaPrecioMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo",
					"INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo",
					--montos a moneda instrumento mercado proximo
					"INGMontoMercadoProximoMonedaInstrumento","INGMontoInteresMercadoProximoMonedaInstrumento","INGMontoReajusteMercadoProximoMonedaInstrumento",
					--
					"INGMontoMercadoProximoMonedaAdicional","INGMontoInteresMercadoProximoMonedaAdicional","INGMontoReajusteMercadoProximoMonedaAdicional",
					--
					"INGDurationMercadoProximo","INGConvexidadMercadoProximo",					
					--TASA CORREDORA A FECHA ACTUAL
					"INGTasaCorredora","INGFechaTasaCorredora","INGMontoATasaCorredora","INGMontoATasaCorredoraMonedaInstrumento",
					"INGMontoATasaCorredoraMonedaAdicional",
					--CORTE DE CUPON FECHA DE VALORIZACION ACTUAL
					"INGFechaCorteCupon","INGNumeroCupon","INGIdMonedaPagoCupon","INGMontoCorteCupon","INGFechaPagoCorteCupon",
					"INGMontoCapitalCorteCupon","INGMontoInteresCorteCupon","INGMontoReajusteCorteCupon",
					--montos corte cupon a moneda instrumento
					"INGMontoCorteCuponMonedaInstrumento","INGMontoCapitalCorteCuponMonedaInstrumento","INGMontoInteresCorteCuponMonedaInstrumento",
					"INGMontoReajusteCorteCuponMonedaInstrumento",
					--
					"INGMontoCorteCuponMonedaAdicional","INGMontoCapitalCorteCuponMonedaAdicional",
					"INGMontoInteresCorteCuponMonedaAdicional","INGMontoReajusteCorteCuponMonedaAdicional",
					--TIF.. FECHA DE VALORIZACION ACTUAL
					"INGMontoPresenteTIF","INGMontoInteresTIF","INGMontoReajusteTIF","INGMontoInteresAcumTIF","INGMontoReajusteAcumTIF",
					--montos tif a moneda instrumento
					"INGMontoPresenteTIFMonedaInstrumento","INGMontoInteresTIFMonedaInstrumento","INGMontoReajusteTIFMonedaInstrumento",
					"INGMontoInteresAcumTIFMonedaInstrumento","INGMontoReajusteAcumTIFMonedaInstrumento",
					--
					"INGMontoPresenteTIFMonedaAdicional","INGMontoInteresTIFMonedaAdicional","INGMontoReajusteTIFMonedaAdicional",
					"INGMontoInteresAcumTIFMonedaAdicional","INGMontoReajusteAcumTIFMonedaAdicional",
					--FORWARD
					"INGMontoMonedaPrincipal","INGMontoMonedaSecundaria","INGMontoDiferido","INGMontoUtilidadDiferida","INGMontoPerdidaDiferida",
					"INGMontoMargen","INGMontoValorRazonableActivo","INGMontoValorRazonablePasivo","INGMontoValorRazonableNeto",
					"INGTasaValorRazonableActivo","INGTasaValorRazonablePasivo","INGMontoDevengoTipoCambio","INGMontoDevengoTipoCambioAnterior",
					"INGMontoDiferidoDiario","INGMontoDiferidoAcumulado","INGMontoMatriz",
					--VALORIZACIONES
					--RETIRO
					"EGRTicketValorizacion","EGRValorizacionConProblemas","EGRFechaFactorMonedaInstrumento","EGRValorFactorMonedaInstrumento",
					"EGRFechaFactorMonedaAdicional","EGRValorFactorMonedaAdicional","EGRCantidad","EGRValorRescate","EGRDisponible","EGRPrecio","EGRTasa",
					"EGRMonto","EGRMontoMonedaInstrumento","EGRMontoMonedaAdicional",
					--TASA MERCADO ACTUAL
					"EGRFechaPrecioTasaMercado","EGRFuenteTasaPrecioMercado","EGRPrecioMercado","EGRTasaMercado","EGRMontoATasaMercado",
					"EGRMontoMercadoMonedaInstrumento","EGRMontoMercadoMonedaAdicional",
					---
					"EGRPrecioAPlazo","EGRMontoAPlazo","EGRMontoAPlazoMonedaInstrumento","EGRMontoAPlazoMonedaAdicional",
					--A FECHA DE OPERACION
					"EGRMontoPresenteTIF","EGRMontoPresenteTIFMonedaInstrumento","EGRMontoPresenteTIFMonedaAdicional")
				SELECT
					:v_TicketAtencion,"IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacionInterno","IdGrupoOperacion",
					"IdValorizacion","FolioCustodia","IdCustodia","TablaAfectada","EsIngreso","IdProcesamientoCompraAnterior","IdProcesamientoCompraActual",
					"IdProcesamientoCompraProximo","IdProcesamientoMercadoAnterior","IdProcesamientoMercadoActual","IdProcesamientoMercadoProximo",
					"IdProcesamientoSubyacenteAnterior","IdProcesamientoSubyacenteActual","IdProcesamientoSubyacenteProximo","IdProcesamientoGarantiaAnterior",
					"IdProcesamientoGarantiaActual","IdProcesamientoGarantiaProximo","IdProcesamientoCorredora","IdProcesamientoTif","IdProcesamientoRetiroCompra",
					"IdProcesamientoRetiroMercado","IdProcesamientoRetiroTif",
					-------------------------------------------------------------------------------------------------------------------------------------------------
					"ValCompraConProblemasAnterior","ValCompraConProblemasActual","ValCompraConProblemasProximo","ValMercadoConProblemasAnterior",
					"ValMercadoConProblemasActual","ValMercadoConProblemasProximo","ValSubyacenteConProblemasAnterior","ValSubyacenteConProblemasActual",
					"ValSubyacenteConProblemasProximo","ValGarantiaConProblemasAnterior","ValGarantiaConProblemasActual","ValGarantiaConProblemasProximo",
					"ValCorredoraConProblemas","ValTifConProblemas","ValRetiroCompraConProblemas","ValRetiroMercadoConProblemas","ValRetiroTifConProblemas",
					--INGRESO
					"INGCantidad","INGValorRescate","INGPrecioAPlazo","INGMontoAPlazo","INGMontoAPlazoMonedaInstrumento","INGMontoAPlazoMonedaAdicional",
					--FORWARD
					"INGEsPuntos","INGValorTipoCambioSpot","INGValorTipoCambioCotizador","INGTasaPuntosMonedaPrincipal","INGTasaPuntosMonedaSecundaria",
					--VALORIZACIONES
					--FECHAS DE VALORIZACION
					"INGTicketValorizacion","INGValorizacionConProblemas","INGFechaValorizacionAnterior","INGFechaValorizacionActual",
					"INGFechaValorizacionProximo",
					--PARAMETROS FINANCIEROS
					--moneda instrumento
					"INGFechaFactorMonedaInstrumentoAnterior","INGValorFactorMonedaInstrumentoAnterior","INGFechaFactorMonedaInstrumentoActual",
					"INGValorFactorMonedaInstrumentoActual","INGFechaFactorMonedaInstrumentoProximo","INGValorFactorMonedaInstrumentoProximo",
					--moneda adicional
					"INGFechaFactorMonedaAdicionalAnterior","INGValorFactorMonedaAdicionalAnterior","INGFechaFactorMonedaAdicionalActual",
					"INGValorFactorMonedaAdicionalActual","INGFechaFactorMonedaAdicionalProximo","INGValorFactorMonedaAdicionalProximo",
					--factores valor dolar, euro, etc
					"INGPrecioCompra","INGTasaCompra",
					--
					--TASA DE COMPRA ANTERIOR
					"INGMontoATasaCompraAnterior","INGMontoInteresCompraAnterior","INGMontoReajusteCompraAnterior",
					--montos a moneda instrumento compra anterior
					"INGMontoCompraAnteriorMonedaInstrumento","INGMontoInteresCompraAnteriorMonedaInstrumento","INGMontoReajusteCompraAnteriorMonedaInstrumento",
					---
					"INGMontoCompraAnteriorMonedaAdicional","INGMontoInteresCompraAnteriorMonedaAdicional","INGMontoReajusteCompraAnteriorMonedaAdicional",
					---
					"INGDurationCompraAnterior","INGConvexidadCompraAnterior","INGMontoCompraAnteriorPibMas","INGMontoCompraAnteriorPibMasMonedaInstrumento",
					"INGMontoCompraAnteriorPibMasMonedaAdicional","INGMontoCompraAnteriorPibMenos","INGMontoCompraAnteriorPibMenosMonedaInstrumento",
					"INGMontoCompraAnteriorPibMenosMonedaAdicional",
					--TASA DE COMPRA ACTUAL
					"INGMontoATasaCompraActual",
					--monto a moneda instrumento compra actual
					"INGMontoCompraActualMonedaInstrumento",
					--
					"INGMontoCompraActualMonedaAdicional",
					--
					"INGDurationCompraActual","INGConvexidadCompraActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoCompraActual","INGMontoReajusteCorridoCompraActual","INGMontoInteresCorridoCompraActualMonedaInstrumento",
					"INGMontoReajusteCorridoCompraActualMonedaInstrumento","INGMontoInteresCorridoCompraActualMonedaAdicional",
					"INGMontoReajusteCorridoCompraActualMonedaAdicional",
					--
					"INGMontoCompraActualPibMas","INGMontoCompraActualPibMasMonedaInstrumento","INGMontoCompraActualPibMasMonedaAdicional",
					"INGMontoCompraActualPibMenos","INGMontoCompraActualPibMenosMonedaInstrumento","INGMontoCompraActualPibMenosMonedaAdicional",
					--TASA DE COMPRA PROXIMO
					"INGMontoATasaCompraProximo","INGMontoInteresCompraProximo","INGMontoReajusteCompraProximo",
					--montos a moneda instrumento compra proximo
					"INGMontoCompraProximoMonedaInstrumento","INGMontoInteresCompraProximoMonedaInstrumento","INGMontoReajusteCompraProximoMonedaInstrumento",
					---
					"INGMontoCompraProximoMonedaAdicional","INGMontoInteresCompraProximoMonedaAdicional","INGMontoReajusteCompraProximoMonedaAdicional",
					---
					"INGDurationCompraProximo","INGConvexidadCompraProximo",
					--TASA DE SUBYACENTE ANTERIOR
					"INGMontoATasaSubyacenteAnterior","INGMontoInteresSubyacenteAnterior","INGMontoReajusteSubyacenteAnterior",
					--montos a moneda instrumento subyacente anterior
					"INGMontoSubyacenteAnteriorMonedaInstrumento","INGMontoInteresSubyacenteAnteriorMonedaInstrumento",
					"INGMontoReajusteSubyacenteAnteriorMonedaInstrumento",
					---
					"INGMontoSubyacenteAnteriorMonedaAdicional","INGMontoInteresSubyacenteAnteriorMonedaAdicional",
					"INGMontoReajusteSubyacenteAnteriorMonedaAdicional",
					---
					"INGDurationSubyacenteAnterior","INGConvexidadSubyacenteAnterior","INGMontoSubyacenteAnteriorPibMas",
					"INGMontoSubyacenteAnteriorPibMasMonedaInstrumento","INGMontoSubyacenteAnteriorPibMasMonedaAdicional","INGMontoSubyacenteAnteriorPibMenos",
					"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento","INGMontoSubyacenteAnteriorPibMenosMonedaAdicional",
					--TASA DE SUBYACENTE ACTUAL
					"INGMontoATasaSubyacenteActual",
					---monto a moneda instrumento subyacente actual
					"INGMontoSubyacenteActualMonedaInstrumento",
					--
					"INGMontoSubyacenteActualMonedaAdicional",
					--
					"INGDurationSubyacenteActual","INGConvexidadSubyacenteActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoSubyacenteActual","INGMontoReajusteCorridoSubyacenteActual",
					"INGMontoInteresCorridoSubyacenteActualMonedaInstrumento","INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento",
					"INGMontoInteresCorridoSubyacenteActualMonedaAdicional","INGMontoReajusteCorridoSubyacenteActualMonedaAdicional",
					--
					"INGMontoSubyacenteActualPibMas","INGMontoSubyacenteActualPibMasMonedaInstrumento","INGMontoSubyacenteActualPibMasMonedaAdicional",
					"INGMontoSubyacenteActualPibMenos","INGMontoSubyacenteActualPibMenosMonedaInstrumento","INGMontoSubyacenteActualPibMenosMonedaAdicional",
					--TASA DE SUBYACENTE PROXIMO
					"INGMontoATasaSubyacenteProximo","INGMontoInteresSubyacenteProximo","INGMontoReajusteSubyacenteProximo",
					--montos a moneda instrumento subyacente proximo
					"INGMontoSubyacenteProximoMonedaInstrumento","INGMontoInteresSubyacenteProximoMonedaInstrumento",
					"INGMontoReajusteSubyacenteProximoMonedaInstrumento",
					---
					"INGMontoSubyacenteProximoMonedaAdicional","INGMontoInteresSubyacenteProximoMonedaAdicional",
					"INGMontoReajusteSubyacenteProximoMonedaAdicional",
					---
					"INGDurationSubyacenteProximo","INGConvexidadSubyacenteProximo",
					--TASA DE GARANTIA ANTERIOR
					"INGMontoATasaGarantiaAnterior","INGMontoInteresGarantiaAnterior","INGMontoReajusteGarantiaAnterior",
					--montos a moneda instrumento Garantia anterior
					"INGMontoGarantiaAnteriorMonedaInstrumento","INGMontoInteresGarantiaAnteriorMonedaInstrumento",
					"INGMontoReajusteGarantiaAnteriorMonedaInstrumento",
					---
					"INGMontoGarantiaAnteriorMonedaAdicional","INGMontoInteresGarantiaAnteriorMonedaAdicional",
					"INGMontoReajusteGarantiaAnteriorMonedaAdicional",
					---
					"INGDurationGarantiaAnterior","INGConvexidadGarantiaAnterior","INGMontoGarantiaAnteriorPibMas","INGMontoGarantiaAnteriorPibMasMonedaInstrumento",
					"INGMontoGarantiaAnteriorPibMasMonedaAdicional","INGMontoGarantiaAnteriorPibMenos","INGMontoGarantiaAnteriorPibMenosMonedaInstrumento",
					"INGMontoGarantiaAnteriorPibMenosMonedaAdicional",
					--TASA DE GARANTIA ACTUAL
					"INGMontoATasaGarantiaActual",
					---monto a moneda instrumento Garantia actual
					"INGMontoGarantiaActualMonedaInstrumento",
					--
					"INGMontoGarantiaActualMonedaAdicional",
					--
					"INGDurationGarantiaActual","INGConvexidadGarantiaActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoGarantiaActual","INGMontoReajusteCorridoGarantiaActual","INGMontoInteresCorridoGarantiaActualMonedaInstrumento",
					"INGMontoReajusteCorridoGarantiaActualMonedaInstrumento","INGMontoInteresCorridoGarantiaActualMonedaAdicional",
					"INGMontoReajusteCorridoGarantiaActualMonedaAdicional",
					-------------------------------
					"INGMontoGarantiaActualPibMas","INGMontoGarantiaActualPibMasMonedaInstrumento","INGMontoGarantiaActualPibMasMonedaAdicional",
					"INGMontoGarantiaActualPibMenos","INGMontoGarantiaActualPibMenosMonedaInstrumento","INGMontoGarantiaActualPibMenosMonedaAdicional",
					--TASA DE GARANTIA PROXIMO
					"INGMontoATasaGarantiaProximo","INGMontoInteresGarantiaProximo","INGMontoReajusteGarantiaProximo",
					--montos a moneda instrumento Garantia proximo
					"INGMontoGarantiaProximoMonedaInstrumento","INGMontoInteresGarantiaProximoMonedaInstrumento","INGMontoReajusteGarantiaProximoMonedaInstrumento",
					---
					"INGMontoGarantiaProximoMonedaAdicional","INGMontoInteresGarantiaProximoMonedaAdicional","INGMontoReajusteGarantiaProximoMonedaAdicional",
					---
					"INGDurationGarantiaProximo","INGConvexidadGarantiaProximo",
					--TASA MERCADO ANTERIOR
					"INGFechaPrecioTasaMercadoAnterior","INGFuenteTasaPrecioMercadoAnterior","INGPrecioMercadoAnterior","INGTasaMercadoAnterior",
					"INGMontoATasaMercadoAnterior","INGMontoInteresMercadoAnterior","INGMontoReajusteMercadoAnterior",
					--montos a moneda instrumento mercado anterior
					"INGMontoMercadoAnteriorMonedaInstrumento","INGMontoInteresMercadoAnteriorMonedaInstrumento",
					"INGMontoReajusteMercadoAnteriorMonedaInstrumento",
					--
					"INGMontoMercadoAnteriorMonedaAdicional","INGMontoInteresMercadoAnteriorMonedaAdicional","INGMontoReajusteMercadoAnteriorMonedaAdicional",
					--
					"INGDurationMercadoAnterior","INGConvexidadMercadoAnterior","INGMontoMercadoAnteriorPibMas","INGMontoMercadoAnteriorPibMasMonedaInstrumento",
					"INGMontoMercadoAnteriorPibMasMonedaAdicional","INGMontoMercadoAnteriorPibMenos","INGMontoMercadoAnteriorPibMenosMonedaInstrumento",
					"INGMontoMercadoAnteriorPibMenosMonedaAdicional",
					--TASA MERCADO ACTUAL
					"INGFechaPrecioTasaMercadoActual","INGFuenteTasaPrecioMercadoActual","INGPrecioMercadoActual","INGTasaMercadoActual",
					"INGMontoATasaMercadoActual",
					--montos a moneda instrumento mercado actual
					"INGMontoMercadoActualMonedaInstrumento",
					--
					"INGMontoMercadoActualMonedaAdicional",
					--
					"INGDurationMercadoActual","INGConvexidadMercadoActual",
					--Intereses y Reajustes Corridos
					"INGMontoInteresCorridoMercadoActual","INGMontoReajusteCorridoMercadoActual","INGMontoInteresCorridoMercadoActualMonedaInstrumento",
					"INGMontoReajusteCorridoMercadoActualMonedaInstrumento","INGMontoInteresCorridoMercadoActualMonedaAdicional",
					"INGMontoReajusteCorridoMercadoActualMonedaAdicional",
					-------------------------------
					"INGMontoMercadoActualPibMas","INGMontoMercadoActualPibMasMonedaInstrumento","INGMontoMercadoActualPibMasMonedaAdicional",
					"INGMontoMercadoActualPibMenos","INGMontoMercadoActualPibMenosMonedaInstrumento","INGMontoMercadoActualPibMenosMonedaAdicional",
					--TASA MERCADO PROXIMO
					"INGFechaPrecioTasaMercadoProximo","INGFuenteTasaPrecioMercadoProximo","INGPrecioMercadoProximo","INGTasaMercadoProximo",
					"INGMontoATasaMercadoProximo","INGMontoInteresMercadoProximo","INGMontoReajusteMercadoProximo",
					--montos a moneda instrumento mercado proximo
					"INGMontoMercadoProximoMonedaInstrumento","INGMontoInteresMercadoProximoMonedaInstrumento","INGMontoReajusteMercadoProximoMonedaInstrumento",
					--
					"INGMontoMercadoProximoMonedaAdicional","INGMontoInteresMercadoProximoMonedaAdicional","INGMontoReajusteMercadoProximoMonedaAdicional",
					--
					"INGDurationMercadoProximo","INGConvexidadMercadoProximo",					
					--TASA CORREDORA A FECHA ACTUAL
					"INGTasaCorredora","INGFechaTasaCorredora","INGMontoATasaCorredora","INGMontoATasaCorredoraMonedaInstrumento",
					"INGMontoATasaCorredoraMonedaAdicional",
					--CORTE DE CUPON FECHA DE VALORIZACION ACTUAL
					"INGFechaCorteCupon","INGNumeroCupon","INGIdMonedaPagoCupon","INGMontoCorteCupon","INGFechaPagoCorteCupon",
					"INGMontoCapitalCorteCupon","INGMontoInteresCorteCupon","INGMontoReajusteCorteCupon",
					--montos corte cupon a moneda instrumento
					"INGMontoCorteCuponMonedaInstrumento","INGMontoCapitalCorteCuponMonedaInstrumento","INGMontoInteresCorteCuponMonedaInstrumento",
					"INGMontoReajusteCorteCuponMonedaInstrumento",
					--
					"INGMontoCorteCuponMonedaAdicional","INGMontoCapitalCorteCuponMonedaAdicional",
					"INGMontoInteresCorteCuponMonedaAdicional","INGMontoReajusteCorteCuponMonedaAdicional",
					--TIF.. FECHA DE VALORIZACION ACTUAL
					"INGMontoPresenteTIF","INGMontoInteresTIF","INGMontoReajusteTIF","INGMontoInteresAcumTIF","INGMontoReajusteAcumTIF",
					--montos tif a moneda instrumento
					"INGMontoPresenteTIFMonedaInstrumento","INGMontoInteresTIFMonedaInstrumento","INGMontoReajusteTIFMonedaInstrumento",
					"INGMontoInteresAcumTIFMonedaInstrumento","INGMontoReajusteAcumTIFMonedaInstrumento",
					--
					"INGMontoPresenteTIFMonedaAdicional","INGMontoInteresTIFMonedaAdicional","INGMontoReajusteTIFMonedaAdicional",
					"INGMontoInteresAcumTIFMonedaAdicional","INGMontoReajusteAcumTIFMonedaAdicional",
					--FORWARD
					"INGMontoMonedaPrincipal","INGMontoMonedaSecundaria","INGMontoDiferido","INGMontoUtilidadDiferida","INGMontoPerdidaDiferida",
					"INGMontoMargen","INGMontoValorRazonableActivo","INGMontoValorRazonablePasivo","INGMontoValorRazonableNeto",
					"INGTasaValorRazonableActivo","INGTasaValorRazonablePasivo","INGMontoDevengoTipoCambio","INGMontoDevengoTipoCambioAnterior",
					"INGMontoDiferidoDiario","INGMontoDiferidoAcumulado","INGMontoMatriz",
					--VALORIZACIONES
					--RETIRO
					"EGRTicketValorizacion","EGRValorizacionConProblemas","EGRFechaFactorMonedaInstrumento","EGRValorFactorMonedaInstrumento",
					"EGRFechaFactorMonedaAdicional","EGRValorFactorMonedaAdicional","EGRCantidad","EGRValorRescate","EGRDisponible","EGRPrecio","EGRTasa",
					"EGRMonto","EGRMontoMonedaInstrumento","EGRMontoMonedaAdicional",
					--TASA MERCADO ACTUAL
					"EGRFechaPrecioTasaMercado","EGRFuenteTasaPrecioMercado","EGRPrecioMercado","EGRTasaMercado","EGRMontoATasaMercado",
					"EGRMontoMercadoMonedaInstrumento","EGRMontoMercadoMonedaAdicional",
					---
					"EGRPrecioAPlazo","EGRMontoAPlazo","EGRMontoAPlazoMonedaInstrumento","EGRMontoAPlazoMonedaAdicional",
					--A FECHA DE OPERACION
					"EGRMontoPresenteTIF","EGRMontoPresenteTIFMonedaInstrumento","EGRMontoPresenteTIFMonedaAdicional"
				FROM :v_ValorizarMovimientoCustodia_Salida_RegistrosValorizados;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaPrecioPromedio"(
					"IdProcesoMovimientoCustodia","IdCliente","IdPortafolio","IdInstrumento")
				SELECT
					:v_TicketAtencion,"IdCliente","IdPortafolio","IdInstrumento"
				FROM :v_CalcularPrecioPromedio_Entrada_Entrada;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaPrecioPromedioOUT"(
					"IdProcesoMovimientoCustodia","FechaParidad","IdMonedaSistema","TablaAfectada","IdCustodia","FolioCustodia","IdCliente","IdPortafolio",
					"IdTipoOperacion","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","INGPrecioCompra","INGCantidad","SumINGPrecioCompra",
					"CantidadRegistros","PrecioPromedioCompra","MontoPrecioPromedioCompra","MontoPrecioPromedioMonedaInstrumento",
					"MontoPrecioPromedioMonedaAdicional")
				SELECT
					:v_TicketAtencion,"FechaParidad","IdMonedaSistema","TablaAfectada","IdCustodia","FolioCustodia","IdCliente","IdPortafolio","IdTipoOperacion",
					"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","INGPrecioCompra","INGCantidad","SumINGPrecioCompra",
					"CantidadRegistros","PrecioPromedioCompra","MontoPrecioPromedioCompra","MontoPrecioPromedioMonedaInstrumento",
					"MontoPrecioPromedioMonedaAdicional"
				FROM :v_CalcularPrecioPromedio_Salida_Salida;
				
				INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaPrecioPromedioConfiguracionOUT"(
					"IdProcesoMovimientoCustodia","IdConfiguracionPrecioPromedio","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
					"IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento")
				SELECT
					:v_TicketAtencion,"IdConfiguracionPrecioPromedio","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
					"IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento"
				FROM :v_CalcularPrecioPromedio_Salida_Configuracion;
				COMMIT;
			END;--AUTONOMOUS TRANSACTION
		END IF; --v_EsDemo ='N'
	    
        --oct, quito foliosout afuera del commit, ya que si falla por una razon ajena al servicio, y despues se realiza nuevamente se graban (dos folios)
        --aunque en realidad se creo solo una vez la cartera
        --y si se intenta anular el registro, se duplican (triplica..) los datos.

		INSERT INTO "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaFoliosOUT"(
            "IdProcesoMovimientoCustodia",
            -----------------------------------------------------------
            "IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacion","IdGrupoOperacionInterno","IdTipoComprobante",
            "FolioComprobante","FechaComprobante","FolioExternoReferencia","TipoRegistro","EsIngreso","SubModulo","TablaAfectada","IdCustodia",
            "FolioCustodia","IdEstado","EsNominal","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento",
            "IdOrigenInstrumento","EsGarantia","Cantidad","ValorRescate","IdUsuario",
            --FUERON CREADOS PARA FONDOS MUTUOS
            "TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste")
        SELECT
            :v_TicketAtencion,
            -----------------------------------------------------------
            "IdInterno","IdInternoCorregido","IdGenerarMovimientoCustodia","IdGrupoOperacion","IdGrupoOperacionInterno","IdTipoComprobante",
            "FolioComprobante","FechaComprobante","FolioExternoReferencia","TipoRegistro","EsIngreso","SubModulo","TablaAfectada","IdCustodia",
            "FolioCustodia","IdEstado","EsNominal","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento",
            "IdOrigenInstrumento","EsGarantia","Cantidad","ValorRescate","IdUsuario",
            --FUERON CREADOS PARA FONDOS MUTUOS
            "TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste"
        FROM :v_ActualizarMovimientoCustodia_Salida_Folios;
                
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			:v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
			'Fin','Fin Utilitario'
		FROM DUMMY;
		
		IF (:v_GrabaLog = 'S') THEN
			CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
		END IF;
		
		UPDATE "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion"  SET "FechaFinProceso" = CURRENT_UTCTIMESTAMP,
			   "Diferencia" = SECONDS_BETWEEN ("FechaInicioProceso",CURRENT_UTCTIMESTAMP)
		WHERE "IdProceso" = :v_IdProceso;
		
		v_GenerarMovimientoCustodia_Salida_Depuracion =
			SELECT
				"IdProceso","NombreProceso","FechaInicioProceso","FechaFinProceso","Diferencia"
			FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion";
		
		BEGIN AUTONOMOUS TRANSACTION	
			INSERT INTO "BSCL.Custodia.BD::CUS.Depuracion"(
				"IdProcesoMovimientoCustodia","IdProceso","NombreProceso","FechaInicioProceso","FechaFinProceso","Diferencia")
			SELECT
				:v_TicketAtencion,"IdProceso","NombreProceso","FechaInicioProceso","FechaFinProceso","Diferencia"
			FROM :v_GenerarMovimientoCustodia_Salida_Depuracion;
			COMMIT;
		END;--AUTONOMOUS TRANSACTION
 	END;
END; 
GO