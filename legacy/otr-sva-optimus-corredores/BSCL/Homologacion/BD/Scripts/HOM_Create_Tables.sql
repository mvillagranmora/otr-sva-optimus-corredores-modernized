/*==============================================================*/
/* Drop Tables: BSCL.Plataforma.BD::AUT.                        */
/*==============================================================*/
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.TipoCarga" cascade ; 
drop table "BSCL"."BSCL.Homologacion.BD::HOM.ModoCarga" cascade;  
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Subyacente" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" cascade; 
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Orden" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Empleado" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Error" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteCobro" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.Reserva" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.OperacionSpot" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.IngresoEgreso" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalleTesoreria" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" cascade;
drop table "BSCL"."BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" cascade;

/*==============================================================*/
/* Table: "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"             */
/*==============================================================*/
create column table "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" (
   "IdHomologacion"  BIGINT			not null,
   "ValorOrigen"     NVARCHAR(40)	not null,
   "TipoValor"       NVARCHAR(40)	not null,
   "Entidad"         NVARCHAR(40)	not null,
   "ValorDestino" 	 NVARCHAR(40)	not null,
   "Descripcion"     NVARCHAR(100)  not null,
   "ValorOrigen2"    NVARCHAR(40)   default '' not null,
   "ValorOrigen3"    NVARCHAR(40)   default '' not null,
   "ValorOrigen4"    NVARCHAR(40)   default '' not null,
   primary key ("IdHomologacion")
);

create column table "BSCL"."BSCL.Homologacion.BD::HOM.Error" (
   "IdError"  		 BIGINT			not null,
   "IdCarga"     	 BIGINT			not null,
   "Entidad"         NVARCHAR(50)   not null,
   "Mensaje"       	 NVARCHAR(100)	not null,
   "NombreCampoGenerico1"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico1" 	 NVARCHAR(100)	not null default '',
   "NombreCampoGenerico2"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico2" 	 NVARCHAR(100)	not null default '',
   "NombreCampoGenerico3"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico3" 	 NVARCHAR(100)	not null default '',
   "NombreCampoGenerico4"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico4" 	 NVARCHAR(100)	not null default '',
   "NombreCampoGenerico5"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico5" 	 NVARCHAR(100)	not null default '',
   "NombreCampoGenerico6"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico6" 	 BIGINT	not null default 0,
   "NombreCampoGenerico7"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico7" 	 BIGINT	not null default 0,
   "NombreCampoGenerico8"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico8" 	 BIGINT	not null default 0,
   "NombreCampoGenerico9"    NVARCHAR(100)	not null default '',
   "ValorCampoGenerico9" 	 SECONDDATE	not null default '0001-01-01 00:00:00',
   "NombreCampoGenerico10"   NVARCHAR(100)	not null default '',
   "ValorCampoGenerico10" 	 SECONDDATE	not null default '0001-01-01 00:00:00',
   primary key ("IdError")
);

create column table "BSCL"."BSCL.Homologacion.BD::HOM.TipoCarga" (
   "IdTipoCarga" 	 BIGINT			not null,
   "Codigo"		     NVARCHAR(40)	not null,
   primary key ("IdTipoCarga")
);

create column table "BSCL"."BSCL.Homologacion.BD::HOM.ModoCarga" (
   "IdModoCarga" 	 BIGINT			not null,
   "Codigo"		     NVARCHAR(40)	not null,
   primary key ("IdModoCarga")
);


create column table "BSCL"."BSCL.Homologacion.BD::HOM.Cartera"(
		"IdCartera" BIGINT
		,"FolioCartera" BIGINT
		,"FolioCarteraRelacionada" BIGINT
		,"FolioGarantiaOrigen" BIGINT
		,"FechaIngresoRegistro" TIMESTAMP
		,"FechaModificacionRegistro" TIMESTAMP
		,"IdEmpresa" BIGINT
		,"FechaOperacion" DATE
		,"FechaVencimientoOperacion" DATE
		,"PlazoEnDiasOperacion" BIGINT
		,"IdCliente" BIGINT
		,"IdPortafolio" BIGINT
		,"IdClienteRelacionado" BIGINT
		,"IdPortafolioRelacionado" BIGINT
		,"IdEstado" NVARCHAR(3)
		,"IdTipoProducto" NVARCHAR(4)
		,"IdTipoCanalIngreso" NVARCHAR(3)
		,"IdMonedaSistema" NVARCHAR(3)
		,"IdMovimientoCarteraEnTransito" BIGINT
		,"IdConcepto" NVARCHAR(6)
		,"IdTipoOperacion" NVARCHAR(6)
		,"IdOperacion" BIGINT
		,"IdTipoComprobante" NVARCHAR(6)
		,"FolioComprobante" BIGINT
		,"FechaComprobante" DATE
		,"FolioExternoReferencia" NVARCHAR(40)
		,"IdMesa" BIGINT
		,"IdBolsa" NVARCHAR(3)
		,"IdSucursal" BIGINT
		,"IdAgente" BIGINT
		,"IdCondicionLiquidacion" NVARCHAR(2)
		,"FechaLiquidacion" DATE
		,"EsIntraday" NVARCHAR(1)
		,"UsaGarantiaEspecifica" NVARCHAR(1)
		,"CobraPrimaAlInicio" NVARCHAR(1)
		,"IdTipoPrestamo" NVARCHAR(6)
		,"Glosa" NVARCHAR(50)
		,"IdUsuario" BIGINT
		,"IdModulo" NVARCHAR(3)
		,"TipoCarteraNormativa" NVARCHAR(6)
		,"FechaCurseInversion" DATE
		,"TipoFactorAjuste" NVARCHAR(3)
		,"ValorFactorAjusteaFechaCurse" DECIMAL(26,8)
		,"CostoInversionEnFactorAjuste" DECIMAL(26,8)
		,"IdClasificacionTipoInstrumento" NVARCHAR(3)
		,"IdTipoInstrumento" NVARCHAR(3)
		,"IdInstrumento" BIGINT
		,"Nemotecnico" NVARCHAR(40)
		,"IdEmisor" NVARCHAR(40)
		,"NemoSVS" NVARCHAR(40)
		,"IdTipoReajuste" NVARCHAR(3)
		,"FechaEmisionInstrumento" DATE
		,"FechaVencimientoInstrumento" DATE
		,"EsFungible" NVARCHAR(1)
		,"ValorizaPor" NVARCHAR(1)
		,"TipoValorizacion" NVARCHAR(10)
		,"CodigoIFRS" NVARCHAR(3)
		,"IdBoveda" NVARCHAR(3)
		,"CuentaBoveda" NVARCHAR(40)
		,"IdMonedaInstrumento" NVARCHAR(3)
		,"Tamano" BIGINT
		,"TIFAfectoBeneficio" NVARCHAR(1)
		,"TIFTasa" DECIMAL(26,8)
		,"Cantidad" DECIMAL(26,8)
		,"CorteMinimo" DECIMAL(26,8)
		,"ValorRescate" DECIMAL(26,8)
		,"PrecioCompra" DECIMAL(26,8)
		,"TasaCompra" DECIMAL(26,8)
		,"MontoCompra" DECIMAL(26,8)
		,"MontoCompraMonedaInstrumento" DECIMAL(26,8)
		,"TasaAgente" DECIMAL(26,8)
		,"PrecioAPlazo" DECIMAL(26,8)
		,"MontoAPlazo" DECIMAL(26,8)
		,"MontoAPlazoMonedaInstrumento" DECIMAL(26,8)
		,"FechaEfectiva" DATE
		,"IdPlaza" BIGINT
		,"IdFixing" BIGINT
		,"FechaFixing" DATE
		,"HoraFixing" TIME
		,"IdModalidadCumplimiento" BIGINT
		,"IdReferenciaMercado" BIGINT
		,"IdMonedaPrincipal" NVARCHAR(3)
		,"IdMonedaSecundaria" NVARCHAR(3)
		,"ValorTipoCambioCierre" DECIMAL(26,8)
		,"ValorParidadCierre" DECIMAL(26,8)
		,"ValorTipoCambioCosto" DECIMAL(26,8)
		,"ValorParidadCosto" DECIMAL(26,8)
		,"MontoMonedaPrincipal" DECIMAL(26,8)
		,"MontoMonedaSecundaria" DECIMAL(26,8)
		,"IdCarga" BIGINT
		,"IdSecuenciaCarteraMigracion" BIGINT
		,"CodigoPeriodo" NVARCHAR(7)
		,"INGGlosa" NVARCHAR(50)
		,"INGTasaAgente" DECIMAL(26,8)
		,"INGFechaOperacion" DATE
		,"INGIdCondicionLiquidacion" NVARCHAR(2)
		,"INGFechaLiquidacion" DATE
		,"INGCantidad" DECIMAL(26,8)
		,"INGValorRescate" DECIMAL(26,8)
		,"INGPrecioPromedioTasaCompra" DECIMAL(26,8)
		,"INGPrecioAPlazo" DECIMAL(26,8)
		,"INGMontoAPlazo" DECIMAL(26,8)
		,"INGMontoAPlazoMonedaInstrumento" DECIMAL(26,8)
		,"INGEsPuntos" NVARCHAR(1)
		,"INGValorTipoCambioSpot" DECIMAL(26,8)
		,"INGValorTipoCambioCotizador" DECIMAL(26,8)
		,"INGTasaPuntosMonedaPrincipal" DECIMAL(26,8)
		,"INGTasaPuntosMonedaSecundaria" DECIMAL(26,8)
		,"INGFechaValorizacionCartera" TIMESTAMP
		,"INGFechaValorizacionAnterior" DATE
		,"INGFechaValorizacionActual" DATE
		,"INGFechaValorizacionProximo" DATE
		,"INGPrecioCompra" DECIMAL(26,8)
		,"INGTasaCompra" DECIMAL(26,8)
		,"INGMontoATasaCompraAnterior" DECIMAL(26,8)
		,"INGMontoInteresCompraAnterior" DECIMAL(26,8)
		,"INGMontoReajusteCompraAnterior" DECIMAL(26,8)
		,"INGMontoCompraAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresCompraAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteCompraAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationCompraAnterior" DECIMAL(26,8)
		,"INGConvexidadCompraAnterior" DECIMAL(26,8)
		,"INGMontoCompraAnteriorPibMas" DECIMAL(26,8)
		,"INGMontoCompraAnteriorPibMasMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoCompraAnteriorPibMenos" DECIMAL(26,8)
		,"INGMontoCompraAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoATasaCompraActual" DECIMAL(26,8)
		,"INGMontoCompraActualMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationCompraActual" DECIMAL(26,8)
		,"INGConvexidadCompraActual" DECIMAL(26,8)
		,"INGMontoCompraActualPibMas" DECIMAL(26,8)
		,"INGMontoCompraActualPibMasMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoCompraActualPibMenos" DECIMAL(26,8)
		,"INGMontoCompraActualPibMenosMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoATasaCompraProximo" DECIMAL(26,8)
		,"INGMontoInteresCompraProximo" DECIMAL(26,8)
		,"INGMontoReajusteCompraProximo" DECIMAL(26,8)
		,"INGMontoCompraProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresCompraProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteCompraProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationCompraProximo" DECIMAL(26,8)
		,"INGConvexidadCompraProximo" DECIMAL(26,8)
		,"INGFechaPrecioTasaMercadoAnterior" DATE
		,"INGFuenteTasaPrecioMercadoAnterior" NVARCHAR(40)
		,"INGTablaFuenteTasaPrecioMercadoAnterior" NVARCHAR(60)
		,"INGPrecioMercadoAnterior" DECIMAL(26,8)
		,"INGTasaMercadoAnterior" DECIMAL(26,8)
		,"INGMontoATasaMercadoAnterior" DECIMAL(26,8)
		,"INGMontoInteresMercadoAnterior" DECIMAL(26,8)
		,"INGMontoReajusteMercadoAnterior" DECIMAL(26,8)
		,"INGMontoMercadoAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresMercadoAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteMercadoAnteriorMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationMercadoAnterior" DECIMAL(26,8)
		,"INGConvexidadMercadoAnterior" DECIMAL(26,8)
		,"INGMontoMercadoAnteriorPibMas" DECIMAL(26,8)
		,"INGMontoMercadoAnteriorPibMasMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoMercadoAnteriorPibMenos" DECIMAL(26,8)
		,"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8)
		,"INGFechaPrecioTasaMercadoActual" DATE
		,"INGFuenteTasaPrecioMercadoActual" NVARCHAR(40)
		,"INGTablaFuenteTasaPrecioMercadoActual" NVARCHAR(60)
		,"INGPrecioMercadoActual" DECIMAL(26,8)
		,"INGTasaMercadoActual" DECIMAL(26,8)
		,"INGMontoATasaMercadoActual" DECIMAL(26,8)
		,"INGMontoMercadoActualMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationMercadoActual" DECIMAL(26,8)
		,"INGConvexidadMercadoActual" DECIMAL(26,8)
		,"INGMontoMercadoActualPibMas" DECIMAL(26,8)
		,"INGMontoMercadoActualPibMasMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoMercadoActualPibMenos" DECIMAL(26,8)
		,"INGMontoMercadoActualPibMenosMonedaInstrumento" DECIMAL(26,8)
		,"INGFechaPrecioTasaMercadoProximo" DATE
		,"INGFuenteTasaPrecioMercadoProximo" NVARCHAR(40)
		,"INGTablaFuenteTasaPrecioMercadoProximo" NVARCHAR(60)
		,"INGPrecioMercadoProximo" DECIMAL(26,8)
		,"INGTasaMercadoProximo" DECIMAL(26,8)
		,"INGMontoATasaMercadoProximo" DECIMAL(26,8)
		,"INGMontoInteresMercadoProximo" DECIMAL(26,8)
		,"INGMontoReajusteMercadoProximo" DECIMAL(26,8)
		,"INGMontoMercadoProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresMercadoProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteMercadoProximoMonedaInstrumento" DECIMAL(26,8)
		,"INGDurationMercadoProximo" DECIMAL(26,8)
		,"INGConvexidadMercadoProximo" DECIMAL(26,8)
		,"INGTasaCorredora" DECIMAL(26,8)
		,"INGFechaTasaCorredora" DATE
		,"INGMontoATasaCorredora" DECIMAL(26,8)
		,"INGMontoATasaCorredoraMonedaInstrumento" DECIMAL(26,8)
		,"INGFechaCorteCupon" DATE
		,"INGNumeroCupon" BIGINT
		,"INGIdMonedaPagoCupon" NVARCHAR(3)
		,"INGMontoCorteCupon" DECIMAL(26,8)
		,"INGFechaPagoCorteCupon" DATE
		,"INGMontoCapitalCorteCupon" DECIMAL(26,8)
		,"INGMontoInteresCorteCupon" DECIMAL(26,8)
		,"INGMontoReajusteCorteCupon" DECIMAL(26,8)
		,"INGMontoCorteCuponMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoCapitalCorteCuponMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresCorteCuponMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteCorteCuponMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoPresenteTIF" DECIMAL(26,8)
		,"INGMontoInteresTIF" DECIMAL(26,8)
		,"INGMontoReajusteTIF" DECIMAL(26,8)
		,"INGMontoInteresAcumTIF" DECIMAL(26,8)
		,"INGMontoReajusteAcumTIF" DECIMAL(26,8)
		,"INGMontoPresenteTIFMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresTIFMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteTIFMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoInteresAcumTIFMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoReajusteAcumTIFMonedaInstrumento" DECIMAL(26,8)
		,"INGMontoMonedaPrincipal" DECIMAL(26,8)
		,"INGMontoMonedaSecundaria" DECIMAL(26,8)
		,"INGMontoDiferido" DECIMAL(26,8)
		,"INGMontoUtilidadDiferida" DECIMAL(26,8)
		,"INGMontoPerdidaDiferida" DECIMAL(26,8)
		,"INGMontoMargen" DECIMAL(26,8)
		,"INGMontoValorRazonableActivo" DECIMAL(26,8)
		,"INGMontoValorRazonablePasivo" DECIMAL(26,8)
		,"INGMontoValorRazonableNeto" DECIMAL(26,8)
		,"INGTasaValorRazonableActivo" DECIMAL(26,8)
		,"INGTasaValorRazonablePasivo" DECIMAL(26,8)
		,"INGMontoDevengoTipoCambio" DECIMAL(26,8)
		,"INGMontoDevengoTipoCambioAnterior" DECIMAL(26,8)
		,"INGMontoDiferidoDiario" DECIMAL(26,8)
		,"INGMontoDiferidoAcumulado" DECIMAL(26,8)
		,"INGMontoMatriz" DECIMAL(26,8)
		,"EGRGlosa" NVARCHAR(50)
		,"EGRFechaOperacion" DATE
		,"EGRIdTipoOperacion" NVARCHAR(6)
		,"EGRIdOperacion" BIGINT
		,"EGRIdMovimientoCarteraEnTransito" BIGINT
		,"EGRIdTipoCanalIngreso" NVARCHAR(3)
		,"EGRIdCondicionLiquidacion" NVARCHAR(2)
		,"EGRFechaLiquidacion" DATE
		,"EGRIdTipoComprobante" NVARCHAR(6)
		,"EGRFolioComprobante" BIGINT
		,"EGRFechaComprobante" DATE
		,"EGRFolioExternoReferencia" NVARCHAR(40)
		,"EGRIdModulo" NVARCHAR(3)
		,"EGRIdConcepto" NVARCHAR(6)
		,"EGRIdUsuario" BIGINT
		,"EGRIdSucursal" BIGINT
		,"EGRIdAgente" BIGINT
		,"EGRFechaCurseRetiro" DATE
		,"EGRCostoRetiroenFactorAjuste" DECIMAL(26,8)
		,"EGRValorFactorAjusteaFechaRetiro" DECIMAL(26,8)
		,"EGRCantidad" DECIMAL(26,8)
		,"EGRValorRescate" DECIMAL(26,8)
		,"EGRPrecio" DECIMAL(26,8)
		,"EGRTasa" DECIMAL(26,8)
		,"EGRMonto" DECIMAL(26,8)
		,"EGRMontoMonedaInstrumento" DECIMAL(26,8)
		,"EGRSpread" DECIMAL(26,8)
		,"EGRSpreadMercado" DECIMAL(26,8)
		,"EGRPrecioAPlazo" DECIMAL(26,8)
		,"EGRMontoAPlazo" DECIMAL(26,8)
		,"EGRMontoAPlazoMonedaInstrumento" DECIMAL(26,8)
		,"EGRMontoPresenteTIF" DECIMAL(26,8)
		,"EGRMontoInteresTIF" DECIMAL(26,8)
		,"EGRMontoReajusteTIF" DECIMAL(26,8)
		,"EGRMontoInteresAcumTIF" DECIMAL(26,8)
		,"EGRMontoReajusteAcumTIF" DECIMAL(26,8)
		,"EGRMontoPresenteTIFMonedaInstrumento" DECIMAL(26,8)
		,"EGRMontoInteresTIFMonedaInstrumento" DECIMAL(26,8)
		,"EGRMontoReajusteTIFMonedaInstrumento" DECIMAL(26,8)
		,"EGRMontoInteresAcumTIFMonedaInstrumento" DECIMAL(26,8)
		,"EGRMontoReajusteAcumTIFMonedaInstrumento" DECIMAL(26,8)
		,"EGRIdSpotAsociado" BIGINT
		,"EGRMontoMonedaPrincipal" DECIMAL(26,8)
		,"EGRMontoMonedaSecundaria" DECIMAL(26,8)
		,"EGRIdMonedaLiquidacion" NVARCHAR(3)
		,"EGRMontoLiquidacion" DECIMAL(26,8)
		,"EGRMontoLiquidacionMonedaPrincipal" DECIMAL(26,8)
		,"EGRValorTipoCambioSpot" DECIMAL(26,8)
		,"EGRValorParidad" DECIMAL(26,8)
		,"EGRValorTipoCambioCotizador" DECIMAL(26,8)
		,"EGREsPuntos" NVARCHAR(1)
		,"EGRTasaPuntosMonedaPrincipal" DECIMAL(26,8)
		,"EGRTasaPuntosMonedaSecundaria" DECIMAL(26,8)
	 --- Datos Sebra en formato sebra 
	 	,"RUT_CLI"	NVARCHAR(11)		
		,"SEC_RUT_CLI" BIGINT			
		,"SEC_CAR_INVER" BIGINT          
		,"COD_PER" NVARCHAR(10)    
		,"FOLIO_COMP_ADJ" BIGINT          
		,"TIPO_COMP_ADJ" NVARCHAR(10)	 
	    ,"LIN_COMP_ADJ" BIGINT          
		,"LIN_ORDEN" BIGINT			
		,"SEC_ORDEN" BIGINT			
		,"TIPO_CARTERA" NVARCHAR(10)	
		,"ID_ERROR" BIGINT          
		,"SEC_MOVTO" BIGINT          
		,"CAN_GARANTIA" DECIMAL(26,8)	
		,"SEC_COMPROM" BIGINT 
		,PRIMARY KEY ("IdCartera")                                                                                 
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Subyacente" (
	"IdSubyacente"							BIGINT        	not null,  
	"FolioSubyacente"						BIGINT			default '0' 			not null,  
	"FolioCarteraRelacionada"			   	BIGINT			default '0' 			not null,  	     
	"FechaIngresoRegistro"					TIMESTAMP		default '0001-01-01' 	not null,
	"FechaModificacionRegistro"				TIMESTAMP       default '0001-01-01' 	not null,
	"IdCarteraRetirada"						BIGINT 			default 0 				not null,
	 
	--OPERACION
	"IdEstado"								NVARCHAR(3) 	default '' 				not null,
	"IdTipoCanalIngreso"					NVARCHAR(3) 	default '' 				not null,
	
	--OPERACION RELACIONADA
	"IdEmpresaOperacionRelacionada"			BIGINT			default '0' 			not null,
	"FechaOperacionRelacionada"				DATE            default '0001-01-01' 	not null,
	"FechaVencimientoOperacionRelacionada"	DATE            default '0001-01-01' 	not null,
	"PlazoEnDiasOperacionRelacionada"		BIGINT 			default '0' 			not null, 	 
	"IdClienteOperacionRelacionada" 		BIGINT			default '0' 			not null,
	"IdPortafolioOperacionRelacionada"		BIGINT			default '0' 			not null,
	"IdConceptoOperacionRelacionada" 		NVARCHAR(6) 	default '' 				not null,
	"IdTipoOperacionOperacionRelacionada" 	NVARCHAR(6) 	default '' 				not null,
	"IdBolsaOperacionRelacionada" 			NVARCHAR(3) 	default '' 				not null,
	"IdMesaOperacionRelacionada" 			BIGINT			default '0' 			not null,
	"IdTipoProductoOperacionRelacionada" 	NVARCHAR(4) 	default ''				not null,
	"IdTipoCanalIngresoOperacionRelacionada" NVARCHAR(3) 	default '' 				not null,
	"IdMonedaSistemaOperacionRelacionada"	NVARCHAR(3) 	default '' 				not null,
	"PrecioCompraOperacionRelacionada"		DECIMAL(26,8) 	default '0' 			not null,
	"TasaCompraOperacionRelacionada"		DECIMAL(26,8) 	default '0' 			not null,
	"PrecioPromedioTasaCompraOperacionRelacionada" DECIMAL(26,8) 	default '0' 	not null,
	"PrecioAPlazoOperacionRelacionada"		DECIMAL(26,8) 	default '0' 			not null,
	"IdCondicionLiquidacionOperacionRelacionada" NVARCHAR(2) 	default '' 			not null,
	"FechaLiquidacionOperacionRelacionada"	DATE            default '0001-01-01' 	not null,
	"IdTipoComprobanteOperacionRelacionada" NVARCHAR(6) 	default '' 				not null,
	"FolioComprobanteOperacionRelacionada" 	BIGINT 			default '0' 			not null,
	"FechaComprobanteOperacionRelacionada" 	DATE          	default '0001-01-01' 	not null,
	"FolioExternoReferenciaOperacionRelacionada" NVARCHAR(40) default '' 			not null,
	--Necesarios para la migracion inicial de movimientos desde sebra
	"IdCargaOperacionRelacionada"			BIGINT 			default '0'				not null,
	"IdSecuenciaCarteraMigracionOperacionRelacionada" BIGINT default '0' 			not null,
	"CodigoPeriodoOperacionRelacionada"		NVARCHAR(7) 	default '' 				not null,
	--INSTRUMENTO RELACIONADO
	"IdClasificacionTipoInstrumentoOperacionRelacionada"	NVARCHAR(3) default '' 	not null,
	"IdTipoInstrumentoOperacionRelacionada"	NVARCHAR(3) 	default '' 				not null,	 
	"IdInstrumentoOperacionRelacionada"		BIGINT			default '0' 			not null,
	"NemotecnicoOperacionRelacionada"		NVARCHAR(40)	default '' 				not null,
	"IdEmisorOperacionRelacionada" 			NVARCHAR(40)	default '' 				not null,
	"NemoSVSOperacionRelacionada"	 		NVARCHAR(40)	default '' 				not null,
	"IdTipoReajusteOperacionRelacionada" 	NVARCHAR(3)		default '' 				not null,
	"IdMonedaInstrumentoOperacionRelacionada" NVARCHAR(3)		default '' 				not null,
	"FechaEmisionInstrumentoOperacionRelacionada" DATE		default '0001-01-01' 	not null,
	"FechaVencimientoInstrumentoOperacionRelacionada" DATE	default '0001-01-01' 	not null,
	"EsFungibleOperacionRelacionada" 		NVARCHAR(1)		default 'N'				not null,
	"ValorizaPorOperacionRelacionada"		NVARCHAR(1) 	default 'T'				not null, --T = Tasa, P= Precio
	"TipoValorizacionOperacionRelacionada"	NVARCHAR(10) 	default ''				not null,
	"CodigoIFRSOperacionRelacionada" 		NVARCHAR(3)		default '' 				not null,
	"IdBovedaOperacionRelacionada" 			NVARCHAR(3)		default '' 				not null,
	"CuentaBovedaOperacionRelacionada"		NVARCHAR(40) 	default '' 				not null,
	"TamanoOperacionRelacionada"			BIGINT			default '0'				not null,
	
	--INSTRUMENTO
	"IdClasificacionTipoInstrumento" 		NVARCHAR(3) 	default '' 				not null,
	"IdTipoInstrumento"						NVARCHAR(3) 	default '' 				not null,	 
	"IdInstrumento" 						BIGINT			default '0' 			not null,
	"Nemotecnico"							NVARCHAR(40)	default '' 				not null,
	"IdEmisor" 								NVARCHAR(40) 	default '' 				not null,
	"NemoSVS"	 							NVARCHAR(40) 	default '' 				not null,
	"IdTipoReajuste" 						NVARCHAR(3) 	default '' 				not null,
	"FechaEmisionInstrumento"				DATE            default '0001-01-01' 	not null,
	"FechaVencimientoInstrumento"			DATE            default '0001-01-01' 	not null,
	"EsFungible" 							NVARCHAR(1) 	default 'N'				not null,
	"ValorizaPor"							NVARCHAR(1) 	default 'T'				not null, --T = Tasa, P= Precio
	"TipoValorizacion"						NVARCHAR(10) 	default ''				not null,
	"IdMonedaInstrumento"					NVARCHAR(3) 	default '' 				not null,
	"Tamano"								BIGINT			default '0'				not null,
	"TIFAfectoBeneficio"					NVARCHAR(1)		default 'N'  			not null,
	"TIFTasa"								DECIMAL(26,8)	default '0'   			not null,
	 
	--CANTIDADES Y MONTOS
	"Cantidad" 								DECIMAL(26,8)	default '0' 			not null,
	"CorteMinimo"							DECIMAL(26,8) 	default '0' 			not null,
	"ValorRescate" 							DECIMAL(26,8) 	default '0' 			not null,
	"PrecioSubyacente"						DECIMAL(26,8) 	default '0' 			not null,
	"TasaSubyacente" 						DECIMAL(26,8) 	default '0' 			not null,
	"MontoSubyacente"		 				DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	"MontoSubyacenteMonedaInstrumento"		DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	
	--INGRESO
	"INGFechaOperacion" 					DATE            default '0001-01-01' 	not null,
	"INGCantidad"							DECIMAL(26,8)	default '0' 			not null,
	"INGValorRescate" 						DECIMAL(26,8) 	default '0' 			not null,
	
	--VALORIZACIONES
	--FECHAS DE VALORIZACION
	"INGFechaValorizacionSubyacente"		TIMESTAMP       default '0001-01-01' 	not null,
	"INGFechaValorizacionAnterior"			DATE            default '0001-01-01' 	not null,
	"INGFechaValorizacionActual"			DATE            default '0001-01-01' 	not null,
	"INGFechaValorizacionProximo"			DATE            default '0001-01-01' 	not null,
	
	"INGPrecioSubyacente"					DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaSubyacente"						DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA DE SUBYACENTE ANTERIOR
	"INGMontoATasaSubyacenteAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresSubyacenteAnterior" 	DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteSubyacenteAnterior"	DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento subyacente anterior
	"INGMontoSubyacenteAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresSubyacenteAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	"INGMontoReajusteSubyacenteAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationSubyacenteAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadSubyacenteAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoSubyacenteAnteriorPibMas"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoSubyacenteAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' 	not null,
	"INGMontoSubyacenteAnteriorPibMenos"	DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'		not null,
	
	--TASA DE SUBYACENTE ACTUAL
	"INGMontoATasaSubyacenteActual" 		DECIMAL(26,8) 	default '0' 			not null,
	---monto a moneda instrumento subyacente actual
	"INGMontoSubyacenteActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	--
	"INGDurationSubyacenteActual"			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadSubyacenteActual" 		DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoSubyacenteActualPibMas"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoSubyacenteActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' 	not null,
	"INGMontoSubyacenteActualPibMenos"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoSubyacenteActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'		not null,
	
	--TASA DE SUBYACENTE PROXIMO
	"INGMontoATasaSubyacenteProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresSubyacenteProximo"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteSubyacenteProximo"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento subyacente proximo
	"INGMontoSubyacenteProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresSubyacenteProximoMonedaInstrumento" DECIMAL(26,8) 	default '0'	not null,
	"INGMontoReajusteSubyacenteProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationSubyacenteProximo"			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadSubyacenteProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA MERCADO ANTERIOR
	"INGFechaPrecioTasaMercadoAnterior"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoAnterior"	NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoAnterior" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoAnterior"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoAnterior" 				DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoAnterior"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresMercadoAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteMercadoAnterior"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado anterior
	"INGMontoMercadoAnteriorMonedaInstrumento"		 DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresMercadoAnteriorMonedaInstrumento"  DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoReajusteMercadoAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	--
	"INGDurationMercadoAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoAnterior"			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoMercadoAnteriorPibMas"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoMercadoAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' 	not null,
	"INGMontoMercadoAnteriorPibMenos"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA MERCADO ACTUAL
	"INGFechaPrecioTasaMercadoActual"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoActual"		NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoActual" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoActual"					DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoActual" 			DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado actual
	"INGMontoMercadoActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	--
	"INGDurationMercadoActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoActual"			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoMercadoActualPibMas"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoMercadoActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' 	not null,
	"INGMontoMercadoActualPibMenos"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoMercadoActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'		not null,
	
	--TASA MERCADO PROXIMO
	"INGFechaPrecioTasaMercadoProximo"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoProximo"		NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoProximo" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoProximo"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoProximo" 				DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresMercadoProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteMercadoProximo"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado proximo
	"INGMontoMercadoProximoMonedaInstrumento" 		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresMercadoProximoMonedaInstrumento"  DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoReajusteMercadoProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	--
	"INGDurationMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA COMPRA OPERACION RELACIONADA
	--TASA DE COMPRA ANTERIOR
	"INGMontoATasaCompraAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresCompraAnterior" 	DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteCompraAnterior"	DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento compra anterior
	"INGMontoCompraAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresCompraAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	"INGMontoReajusteCompraAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationCompraAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoCompraAnteriorPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoCompraAnteriorPibMenos"		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE COMPRA ACTUAL
	"INGMontoATasaCompraActual" 		DECIMAL(26,8) 	default '0' 			not null,
	--monto a moneda instrumento compra actual
	"INGMontoCompraActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	--
	"INGDurationCompraActual"			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraActual" 		DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoCompraActualPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoCompraActualPibMenos"		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE COMPRA PROXIMO
	"INGMontoATasaCompraProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresCompraProximo"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteCompraProximo"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento compra proximo
	"INGMontoCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0'	not null,
	"INGMontoReajusteCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationCompraProximo"			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA CORREDORA 
	"INGTasaCorredora"						DECIMAL(26,8) 	default '0' 		not null,
	"INGFechaTasaCorredora" 				DATE            default '0001-01-01' not null,
	"INGMontoATasaCorredora"				DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoATasaCorredoraMonedaInstrumento"	DECIMAL(26,8) 	default '0' 	not null,
	
	--CORTE DE CUPON FECHA DE VALORIZACION ACTUAL --DUDA
	"INGFechaCorteCupon"					DATE 			default '0001-01-01' not null,
	"INGNumeroCupon"						BIGINT 			default '0'			not null,
	"INGIdMonedaPagoCupon"					NVARCHAR(3) 	default ''			not null,
	"INGMontoCorteCupon"					DECIMAL(26,8) 	default '0'			not null,
	"INGFechaPagoCorteCupon"				DATE 			default '0001-01-01' not null,	
	"INGMontoCapitalCorteCupon"				DECIMAL(26,8) 	default '0'			not null,
	"INGMontoInteresCorteCupon"				DECIMAL(26,8) 	default '0'			not null,
	"INGMontoReajusteCorteCupon"			DECIMAL(26,8) 	default '0'			not null,
	--montos corte cupon a moneda instrumento
	"INGMontoCorteCuponMonedaInstrumento"			DECIMAL(26,8) 	default '0'	not null,
	"INGMontoCapitalCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'	not null,
	"INGMontoInteresCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'	not null,
	"INGMontoReajusteCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'	not null,
	
	--TIF.. QUE GRABAREMOS CON TIF.. SOLO UNA SOLA VALORIZACION??? --DUDA
	"INGMontoPresenteTIF" 					DECIMAL(26,8)	default '0'   		not null,
	"INGMontoInteresTIF"					DECIMAL(26,8)	default '0'   		not null,
	"INGMontoReajusteTIF"					DECIMAL(26,8)	default '0'   		not null,
	"INGMontoInteresAcumTIF"				DECIMAL(26,8)	default '0'   		not null,
	"INGMontoReajusteAcumTIF"				DECIMAL(26,8)	default '0'   		not null,
	--montos tif a moneda instrumento
	"INGMontoPresenteTIFMonedaInstrumento" 	DECIMAL(26,8)	default '0'   		not null,
	"INGMontoInteresTIFMonedaInstrumento"	DECIMAL(26,8)	default '0'   		not null,
	"INGMontoReajusteTIFMonedaInstrumento"	DECIMAL(26,8)	default '0'   		not null,
	"INGMontoInteresAcumTIFMonedaInstrumento" DECIMAL(26,8)	default '0'   		not null,
	"INGMontoReajusteAcumTIFMonedaInstrumento" DECIMAL(26,8)	default '0'   	not null,
	--VALORIZACIONES
	
	--EGRESO
	--OPERACION RELACIONADA
	"EGRFechaOperacion"						DATE            default '0001-01-01' 	not null,
	"EGRIdTipoCanalIngreso" 				NVARCHAR(3) 	default '' 			 	not null,
	"EGRIdCondicionLiquidacion"				NVARCHAR(2)		default '' 			 	not null,
	"EGRFechaLiquidacion" 					DATE          	default '0001-01-01' 	not null,
	"EGRIdTipoComprobante" 					NVARCHAR(6) 	default '' 				not null,
	"EGRFolioComprobante" 					BIGINT 			default '0' 			not null,
	"EGRFechaComprobante" 					DATE          	default '0001-01-01' 	not null,
	"EGRFolioExternoReferencia" 			NVARCHAR(40)    default '0' 		 	not null,
	"EGRIdModulo"							NVARCHAR(3) 	default '' 				not null,
	"EGRIdTipoOperacion" 					NVARCHAR(6) 	default '' 				not null,					
	"EGRIdOperacion"						BIGINT			default '0' 			not null,
	"EGRIdConcepto"							NVARCHAR(6) 	default '' 				not null,
	"EGRIdUsuario"							BIGINT			default '0' 			not null,
	"EGRIdSucursal"							BIGINT			default '0' 			not null,
	"EGRIdAgente"							BIGINT			default '0' 			not null,
	
	"EGRCantidad" 							DECIMAL(26,8)	default '0' 			not null,
	"EGRValorRescate" 						DECIMAL(26,8) 	default '0' 			not null,
	"EGRPrecio" 							DECIMAL(26,8) 	default '0' 			not null,
	"EGRTasa" 								DECIMAL(26,8) 	default '0' 			not null,
	"EGRMonto" 								DECIMAL(26,8)	default '0' 			not null,
	"EGRMontoMonedaInstrumento" 			DECIMAL(26,8)	default '0' 			not null,
	"EGRSpread"								DECIMAL(26,8) 	default '0' 			not null,
	"EGRSpreadMercado"						DECIMAL(26,8) 	default '0' 			not null,
	"EGRPrecioAPlazo" 						DECIMAL(26,8) 	default '0' 			not null,
	"EGRMontoAPlazo" 						DECIMAL(26,8) 	default '0' 			not null,
	"EGRMontoAPlazoMonedaInstrumento"		DECIMAL(26,8) 	default '0' 			not null,
	---TIF
	"EGRMontoPresenteTIF" 					DECIMAL(26,8)	default '0'   			not null,
	"EGRMontoInteresTIF"					DECIMAL(26,8)	default '0'   			not null,
	"EGRMontoReajusteTIF"					DECIMAL(26,8)	default '0'   			not null,
	"EGRMontoInteresAcumTIF"				DECIMAL(26,8)	default '0'   			not null,
	"EGRMontoReajusteAcumTIF"				DECIMAL(26,8)	default '0'   			not null,
	--montos tif a moneda instrumento
	"EGRMontoPresenteTIFMonedaInstrumento" 		DECIMAL(26,8)	default '0'   		not null,
	"EGRMontoInteresTIFMonedaInstrumento"		DECIMAL(26,8)	default '0'   		not null,
	"EGRMontoReajusteTIFMonedaInstrumento"		DECIMAL(26,8)	default '0'   		not null,
	"EGRMontoInteresAcumTIFMonedaInstrumento"	DECIMAL(26,8)	default '0'   		not null,
	"EGRMontoReajusteAcumTIFMonedaInstrumento"	DECIMAL(26,8)	default '0'   		not null,
	 --- Datos Sebra en formato sebra 
	 "RUT_CLI"								NVARCHAR(11)	default '' 				not null,	
	 "SEC_RUT_CLI"							BIGINT			default '0'	 			not null,
	 "SEC_CAR_INVER"                        BIGINT          default '0' 			not null,
	 "COD_PER"								NVARCHAR(10)    default ''	 			not null,
	 "FOLIO_COMP_ADJ"                       BIGINT          default '0' 			not null,
	 "TIPO_COMP_ADJ"						NVARCHAR(10)	default ''	 			not null, 
     "LIN_COMP_ADJ"                         BIGINT          default '0' 			not null,
	 "LIN_ORDEN"							BIGINT			default '0' 			not null,
	 "SEC_ORDEN"							BIGINT			default '0' 			not null,
	 "TIPO_CARTERA"							NVARCHAR(10)	default '0' 			not null,
	 "ID_ERROR"								BIGINT          default '0' 			not null,
	 "SEC_MOVTO"                            BIGINT          default '0' 			not null,
	 "SEC_COMPROM"                          BIGINT          default '0' 			not null,
	 "LIN_COMPROM"	                        BIGINT          default '0' 			not null,
	 "SEC_CAR_CORR"	                        BIGINT          default '0' 			not null,
	 "SEC_CAR_CLI"	                        BIGINT          default '0' 			not null,
	primary key ("IdSubyacente")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Garantia" (
	"IdGarantia"			     			BIGINT			not null,
	"FolioGarantia"	             			BIGINT			not null default '0',  	     
	"FolioCarteraOrigen"	     			BIGINT			not null default '0',
	"FolioCarteraGarantizada" 				BIGINT			not null default '0',
	"IdInstrumentoCarteraGarantizada"	 	BIGINT			not null default '0',
	"EsEspecifica" 							NVARCHAR(1)		not null default 'N',
	"IdContraparteGarantia"					BIGINT			not null default '0',
	"RazonSocialContraparteGarantia"		NVARCHAR(100)	default '' 				not null, 
	"CuentaContraparteGarantia"				NVARCHAR(40)	default '' 				not null, 
	"IdOrigenGarantia"						NVARCHAR(3)		not null default '',  -- Custodia, Cta.Inv, Relacionados, Especies
	"IdEspecieGarantia"						BIGINT			not null default '0',  -- 
	"FechaIngresoRegistro" 					TIMESTAMP		not null default '0001-01-01',
	"FechaModificacionRegistro"				TIMESTAMP		not null default '0001-01-01',
	
	--OPERACION
	"IdEmpresa" 							BIGINT			default '0' 			not null,
	"FechaOperacion"						DATE            default '0001-01-01' 	not null,
	"FechaVencimientoOperacion"				DATE            default '0001-01-01' 	not null,
	"PlazoEnDiasOperacion"					BIGINT 			default '0' 			not null, 
	"IdCliente" 							BIGINT			default '0' 			not null,
	"IdPortafolio" 							BIGINT			default '0' 			not null,
	"IdEstado" 								NVARCHAR(3) 	default '' 				not null,
	"IdTipoCanalIngreso"					NVARCHAR(3) 	default '' 				not null,
	"IdMonedaSistema"						NVARCHAR(3) 	default '' 				not null,
	"IdConcepto" 							NVARCHAR(6) 	default '' 				not null,
	"FolioExternoReferencia"				NVARCHAR(40) 	default ''  			not null,
	"IdSucursal" 							BIGINT			default '0' 			not null,
	"IdAgente" 								BIGINT			default '0' 			not null,
	"IdCondicionLiquidacion" 				NVARCHAR(2) 	default '' 				not null,
	"FechaLiquidacion" 						DATE            default '0001-01-01' 	not null,
	"IdUsuario" 							BIGINT			default '0' 			not null,
	"IdModulo"								NVARCHAR(3)		default '' 				not null,
	"IdMovimientoCarteraEnTransito" 		BIGINT			default '0' 			not null,
	
	--OPERACION ORIGEN
	"IdConceptoOperacionOperacionOrigen"	NVARCHAR(6) 	default '' 				not null,
	"IdTipoOperacionOperacionOrigen"		NVARCHAR(6) 	default '' 				not null,
	"IdBolsaOperacionOrigen" 				NVARCHAR(3) 	default '' 				not null,
	"IdMesaOperacionOrigen" 				BIGINT			default '0' 			not null,
	"IdBovedaOperacionOrigen"				NVARCHAR(3) 	default '' 				not null,
	"CuentaBovedaOperacionOrigen"			NVARCHAR(40) 	default '' 				not null,
	"IdTipoProductoOperacionOrigen" 		NVARCHAR(4) 	default ''				not null,
	"IdTipoComprobanteOperacionOrigen" 		NVARCHAR(6) 	default '' 				not null,
	"FolioComprobanteOperacionOrigen" 		BIGINT 			default '0' 			not null,
	"FechaComprobanteOperacionOrigen" 		DATE          	default '0001-01-01' 	not null,
	"FolioExternoReferenciaOperacionOrigen" NVARCHAR(40) 	default '' 				not null,
	--INSTRUMENTO ORIGEN
	"IdClasificacionTipoInstrumentoOperacionOrigen"	NVARCHAR(3) 	default '' 		not null,
	"IdTipoInstrumentoOperacionOrigen"		NVARCHAR(3) 	default '' 				not null,	 
	"IdInstrumentoOperacionOrigen"			BIGINT			default '0' 			not null,
	"NemotecnicoOperacionOrigen"			NVARCHAR(40)	default '' 				not null,
	"IdEmisorOperacionOrigen" 				NVARCHAR(40) 	default '' 				not null,
	"NemoSVSOperacionOrigen"	 			NVARCHAR(40) 	default '' 				not null,
	"IdTipoReajusteOperacionOrigen"			NVARCHAR(3) 	default '' 				not null,
	"IdMonedaInstrumentoOperacionOrigen"	NVARCHAR(3) 	default '' 				not null,
	"FechaEmisionInstrumentoOperacionOrigen" DATE           default '0001-01-01' 	not null,
	"FechaVencimientoInstrumentoOperacionOrigen" DATE       default '0001-01-01' 	not null,
	"EsFungibleOperacionOrigen" 			NVARCHAR(1) 	default 'N'				not null,
	"ValorizaPorOperacionOrigen"			NVARCHAR(1) 	default 'T'				not null, --T = Tasa, P= Precio
	"TipoValorizacionOperacionOrigen"		NVARCHAR(10) 	default ''				not null,
	"CodigoIFRSOperacionOrigen"				NVARCHAR(3)		default '' 				not null,
	"TamanoOperacionOrigen"					BIGINT			default '0'				not null,
	"PrecioCompraOperacionOrigen"			DECIMAL(26,8)	default '0'				not null,
	"TasaCompraOperacionOrigen"				DECIMAL(26,8)	default '0'				not null,
	"PrecioPromedioTasaCompraOperacionOrigen" DECIMAL(26,8) default '0' 			not null,
	
	--INSTRUMENTO
	"IdClasificacionTipoInstrumento" 		NVARCHAR(3) 	default '' 				not null,
	"IdTipoInstrumento"						NVARCHAR(3) 	default '' 				not null,	 
	"IdInstrumento" 						BIGINT			default '0' 			not null,
	"Nemotecnico"							NVARCHAR(40)	default '' 				not null,
	"IdEmisor" 								NVARCHAR(40) 	default '' 				not null,
	"NemoSVS"	 							NVARCHAR(40) 	default '' 				not null,
	"IdTipoReajuste" 						NVARCHAR(3) 	default '' 				not null,
	"FechaEmisionInstrumento"				DATE            default '0001-01-01' 	not null,
	"FechaVencimientoInstrumento"			DATE            default '0001-01-01' 	not null,
	"EsFungible" 							NVARCHAR(1) 	default 'N'				not null,
	"ValorizaPor"							NVARCHAR(1) 	default 'T'				not null, --T = Tasa, P= Precio
	"TipoValorizacion"						NVARCHAR(10) 	default ''				not null,
	"CodigoIFRS"							NVARCHAR(3)		default '' 				not null,
	"IdMonedaInstrumento"					NVARCHAR(3) 	default '' 				not null,
	"Tamano"								BIGINT			default '0'				not null,
	"TIFAfectoBeneficio"					NVARCHAR(1)		default 'N'  			not null,
	"TIFTasa"								DECIMAL(26,8)	default '0'   			not null,
	
	--CANTIDADES Y MONTOS
	"Cantidad" 								DECIMAL(26,8)	default '0' 			not null,
	"CorteMinimo"							DECIMAL(26,8) 	default '0' 			not null,
	"ValorRescate" 							DECIMAL(26,8) 	default '0' 			not null,
	"PrecioGarantia"						DECIMAL(26,8) 	default '0' 			not null,
	"TasaGarantia" 							DECIMAL(26,8) 	default '0' 			not null,
	"MontoGarantia"		 					DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	"MontoGarantiaMonedaInstrumento"		DECIMAL(26,8) 	default '0' 			not null,
	
	--Necesarios para la migracion inicial de movimientos desde sebra
	"IdCarga"								BIGINT 			default '0'				not null,
	"IdSecuenciaCarteraMigracion"			BIGINT 			default '0' 			not null,
	"CodigoPeriodo"							NVARCHAR(7) 	default '' 				not null,
	
	--INGRESO
	"INGFechaOperacion" 					DATE            default '0001-01-01' 	not null,
	"INGIdCondicionLiquidacion"				NVARCHAR(2)		default '' 				not null,
	"INGFechaLiquidacion"					DATE			default '0001-01-01' 	not null,
	"INGCantidad"							DECIMAL(26,8)	default '0' 			not null,
	"INGValorRescate" 						DECIMAL(26,8) 	default '0' 			not null,
	
	--VALORIZACIONES
	--FECHAS DE VALORIZACION
	"INGFechaValorizacionGarantia"			TIMESTAMP       default '0001-01-01' 	not null,
	"INGFechaValorizacionAnterior"			DATE            default '0001-01-01' 	not null,
	"INGFechaValorizacionActual"			DATE            default '0001-01-01' 	not null,
	"INGFechaValorizacionProximo"			DATE            default '0001-01-01' 	not null,
	
	"INGPrecioGarantia"						DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaGarantia" 						DECIMAL(26,8) 	default '0' 			not null,
	--TASA DE GARANTIA ANTERIOR
	"INGMontoATasaGarantiaAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresGarantiaAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteGarantiaAnterior"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento Garantia anterior
	"INGMontoGarantiaAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresGarantiaAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	"INGMontoReajusteGarantiaAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationGarantiaAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadGarantiaAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoGarantiaAnteriorPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoGarantiaAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoGarantiaAnteriorPibMenos"		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoGarantiaAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE GARANTIA ACTUAL
	"INGMontoATasaGarantiaActual" 			DECIMAL(26,8) 	default '0' 			not null,
	---monto a moneda instrumento Garantia actual
	"INGMontoGarantiaActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	--
	"INGDurationGarantiaActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadGarantiaActual" 			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoGarantiaActualPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoGarantiaActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoGarantiaActualPibMenos"		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoGarantiaActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE GARANTIA PROXIMO
	"INGMontoATasaGarantiaProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresGarantiaProximo"		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteGarantiaProximo"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento Garantia proximo
	"INGMontoGarantiaProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresGarantiaProximoMonedaInstrumento" DECIMAL(26,8) 	default '0'	not null,
	"INGMontoReajusteGarantiaProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationGarantiaProximo"			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadGarantiaProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA MERCADO ANTERIOR
	"INGFechaPrecioTasaMercadoAnterior"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoAnterior"	NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoAnterior" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoAnterior"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoAnterior" 				DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoAnterior"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresMercadoAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteMercadoAnterior"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado anterior
	"INGMontoMercadoAnteriorMonedaInstrumento"		 DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresMercadoAnteriorMonedaInstrumento"  DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoReajusteMercadoAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	--
	"INGDurationMercadoAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoAnterior"			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoMercadoAnteriorPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoMercadoAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoMercadoAnteriorPibMenos"		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoMercadoAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA MERCADO ACTUAL
	"INGFechaPrecioTasaMercadoActual"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoActual"		NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoActual" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoActual"					DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoActual" 			DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado actual
	"INGMontoMercadoActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	--
	"INGDurationMercadoActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoActual"			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoMercadoActualPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoMercadoActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoMercadoActualPibMenos"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoMercadoActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA MERCADO PROXIMO
	"INGFechaPrecioTasaMercadoProximo"		DATE            default '0001-01-01' 	not null,	 
	"INGFuenteTasaPrecioMercadoProximo"		NVARCHAR(40)	default ''				not null,
	"INGTablaFuenteTasaPrecioMercadoProximo" NVARCHAR(60)	default ''				not null,
	"INGPrecioMercadoProximo"				DECIMAL(26,8) 	default '0' 			not null,
	"INGTasaMercadoProximo" 				DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresMercadoProximo" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteMercadoProximo"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento mercado proximo
	"INGMontoMercadoProximoMonedaInstrumento" 		DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresMercadoProximoMonedaInstrumento"  DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoReajusteMercadoProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	--
	"INGDurationMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadMercadoProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA COMPRA OPERACION ORIGEN
	--TASA DE COMPRA ANTERIOR
	"INGMontoATasaCompraAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresCompraAnterior" 		DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteCompraAnterior"		DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento compra anterior
	"INGMontoCompraAnteriorMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoInteresCompraAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	"INGMontoReajusteCompraAnteriorMonedaInstrumento"	DECIMAL(26,8) 	default '0' not null,
	---
	"INGDurationCompraAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraAnterior" 			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoCompraAnteriorPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraAnteriorPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoCompraAnteriorPibMenos"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraAnteriorPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE COMPRA ACTUAL
	"INGMontoATasaCompraActual" 			DECIMAL(26,8) 	default '0' 			not null,
	---monto a moneda instrumento compra actual
	"INGMontoCompraActualMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	--
	"INGDurationCompraActual"				DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraActual" 			DECIMAL(26,8) 	default '0' 			not null,
	
	"INGMontoCompraActualPibMas"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraActualPibMasMonedaInstrumento" DECIMAL(26,8) 	default '0' not null,
	"INGMontoCompraActualPibMenos"			DECIMAL(26,8) 	default '0' 		not null,
	"INGMontoCompraActualPibMenosMonedaInstrumento" DECIMAL(26,8) default '0'	not null,
	
	--TASA DE COMPRA PROXIMO
	"INGMontoATasaCompraProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresCompraProximo"			DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoReajusteCompraProximo"			DECIMAL(26,8) 	default '0' 			not null,
	--montos a moneda instrumento compra proximo
	"INGMontoCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoInteresCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0'			not null,
	"INGMontoReajusteCompraProximoMonedaInstrumento" DECIMAL(26,8) 	default '0' 		not null,
	---
	"INGDurationCompraProximo"				DECIMAL(26,8) 	default '0' 			not null,
	"INGConvexidadCompraProximo" 			DECIMAL(26,8) 	default '0' 			not null,
	
	--TASA CORREDORA 
	"INGTasaCorredora"						DECIMAL(26,8) 	default '0' 			not null,
	"INGFechaTasaCorredora" 				DATE            default '0001-01-01' 	not null,
	"INGMontoATasaCorredora"				DECIMAL(26,8) 	default '0' 			not null,
	"INGMontoATasaCorredoraMonedaInstrumento"	DECIMAL(26,8) 	default '0' 		not null,
	
	--CORTE DE CUPON FECHA DE VALORIZACION ACTUAL --DUDA
	"INGFechaCorteCupon"					DATE 			default '0001-01-01'	not null,
	"INGNumeroCupon"						BIGINT 			default '0'				not null,
	"INGIdMonedaPagoCupon"					NVARCHAR(3) 	default ''				not null,
	"INGMontoCorteCupon"					DECIMAL(26,8) 	default '0'				not null,
	"INGFechaPagoCorteCupon"				DATE 			default '0001-01-01'	not null,	
	"INGMontoCapitalCorteCupon"				DECIMAL(26,8) 	default '0'				not null,
	"INGMontoInteresCorteCupon"				DECIMAL(26,8) 	default '0'				not null,
	"INGMontoReajusteCorteCupon"			DECIMAL(26,8) 	default '0'				not null,
	--montos corte cupon a moneda instrumento
	"INGMontoCorteCuponMonedaInstrumento"			DECIMAL(26,8) 	default '0'		not null,
	"INGMontoCapitalCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'		not null,
	"INGMontoInteresCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'		not null,
	"INGMontoReajusteCorteCuponMonedaInstrumento"	DECIMAL(26,8) 	default '0'		not null,
	
	--VALORIZACIONES
	 
	--EGRESO
	"EGRFechaOperacion"						DATE            default '0001-01-01' 	not null,
	"EGRIdTipoOperacion" 					NVARCHAR(6) 	default '' 				not null,					
	"EGRIdOperacion"						BIGINT			default '0' 			not null,
	"EGRIdMovimientoCarteraEnTransito" 		BIGINT			default '0' 			not null,
	"EGRIdTipoCanalIngreso"					NVARCHAR(3) 	default '' 				not null,
	"EGRIdCondicionLiquidacion" 			NVARCHAR(2) 	default '' 				not null,
	"EGRFechaLiquidacion" 					DATE            default '0001-01-01' 	not null,
	"EGRIdTipoComprobante"					NVARCHAR(6) 	default '' 				not null,
	"EGRFolioComprobante"					BIGINT 			default '0' 			not null,
	"EGRFechaComprobante"					DATE          	default '0001-01-01' 	not null,
	"EGRFolioExternoReferencia" 			NVARCHAR(40) 	default '' 				not null,
	"EGRIdModulo" 							NVARCHAR(3) 	default '' 				not null,
	"EGRIdConcepto" 						NVARCHAR(6) 	default '' 				not null,
	"EGRIdUsuario"							BIGINT			default '0' 			not null,
	"EGRIdSucursal" 						BIGINT			default '0' 			not null,
	"EGRIdAgente" 							BIGINT			default '0' 			not null,
	"EGRCantidad" 							DECIMAL(26,8)	default '0' 			not null,
	"EGRValorRescate" 						DECIMAL(26,8) 	default '0' 			not null,
	"EGRPrecio" 							DECIMAL(26,8) 	default '0' 			not null,
	"EGRTasa" 								DECIMAL(26,8) 	default '0' 			not null,
	"EGRMonto" 								DECIMAL(26,8)	default '0' 			not null,
	"EGRMontoMonedaInstrumento"				DECIMAL(26,8)	default '0' 			not null,
	 --- Datos Sebra en formato sebra 
	 "RUT_CLI"								NVARCHAR(11)	default '' 				not null,	
	 "SEC_RUT_CLI"							BIGINT			default '0'	 			not null,
	 "SEC_CAR_INVER"                        BIGINT          default '0' 			not null,
	 "COD_PER"								NVARCHAR(10)    default ''	 			not null,
	 "FOLIO_COMP_ADJ"                       BIGINT          default '0' 			not null,
	 "TIPO_COMP_ADJ"						NVARCHAR(10)	default ''	 			not null, 
     "LIN_COMP_ADJ"                         BIGINT          default '0' 			not null,
	 "LIN_ORDEN"							BIGINT			default '0' 			not null,
	 "SEC_ORDEN"							BIGINT			default '0' 			not null,
	 "TIPO_CARTERA"							NVARCHAR(10)	default '0' 			not null,
	 "ID_ERROR"								BIGINT          default '0' 			not null,
	 "SEC_MOVTO"                            BIGINT          default '0' 			not null,
	PRIMARY KEY ("IdGarantia")
   );


CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" (
	"IdMovimientoCarteraEnTransito" 		BIGINT 			not null,
	"IdMovimientoCarteraEnTransitoRelacionado" BIGINT		default '0' 			not null,
	"FechaIngresoRegistro" 					TIMESTAMP		default '0001-01-01' 	not null,
	"FechaCertificacionRegistro" 			TIMESTAMP		default '0001-01-01' 	not null,
	"FechaAnulacionRegistro"				TIMESTAMP       default '0001-01-01' 	not null,
	"IdUsuarioIngreso"						BIGINT			default '0' 			not null,
	"IdUsuarioCertificacion"				BIGINT			default '0' 			not null,
	"IdUsuarioAnulacion"					BIGINT			default '0' 			not null,
	"FechaOperacion"						DATE			default '0001-01-01' 	not null,
	"FolioCartera"							BIGINT			default '0' 		 	not null, --INGRESO,RELACIONADA o CARTERA ORIGEN
	"EsRegistroGeneradoPorSistema"			NVARCHAR(1)		not null				default 'N',--S=, porque no tiene saldo, o es nominal.
	--DATOS INFORMATIVOS, DESDE QUE OPERACION SE ORIGINO, RELACIONADO CON EL REGISTRO GENERADO POR SISTEMA
	"OrigenCustodia"						NVARCHAR(3)		not null				default '',--CAR = CARTERA, GAR = GARANTIA
	"IdCustodiaOrigen"						BIGINT			default '0' 			not null,
	--GARANTIAS
	"FolioCarteraGarantizada" 				BIGINT			not null				default '0',
	"IdInstrumentoCarteraGarantizada"	 	BIGINT			not null				default '0',
	"EsEspecifica" 							NVARCHAR(1)		not null				default 'N',
	"IdContraparteGarantia"					BIGINT			not null				default '0',
	"RazonSocialContraparteGarantia"		NVARCHAR(100)	default '' 				not null, 
	"CuentaContraparteGarantia"				NVARCHAR(40)	default '' 				not null, 
	"IdOrigenGarantia"						NVARCHAR(3)		not null				default '',  -- Custodia, Cta.Inv, Relacionados, Especies
	"IdEspecieGarantia"						BIGINT			not null				default '0',  -- 
	--OPERACION
	"IdEmpresa"								BIGINT			default '0' 			not null,
	"IdPais"								BIGINT			default '0' 			not null,
	"EsNominal"								NVARCHAR(1) 	default 'N' 			not null,
	"FolioExternoReferencia"				NVARCHAR(40)    default '' 				not null,
	"IdCliente"								BIGINT			default '0' 			not null,
	"IdPortafolio"							BIGINT			default '0' 			not null,
	"IdEstado"								NVARCHAR(3) 	default '' 				not null,
	"IdTipoCanalIngreso"					NVARCHAR(3) 	default '' 				not null,
	"IdMonedaSistema"						NVARCHAR(3) 	default '' 				not null,
	"CodigoIFRS" 							NVARCHAR(3) 	default '' 				not null,
	"IdConcepto" 							NVARCHAR(6) 	default '' 				not null,
	"IdTipoOperacion" 						NVARCHAR(6) 	default '' 				not null,	 
	"IdBolsa"								NVARCHAR(3) 	default '' 				not null,
	"IdSucursal"							BIGINT			default '0' 			not null,
	"IdAgente"								BIGINT			default '0' 			not null,
	"IdCondicionLiquidacion"				NVARCHAR(2) 	default '' 				not null,
	"FechaLiquidacion"						DATE            default '0001-01-01' 	not null,
	"EsIntraday"							NVARCHAR(1) 	default 'N' 			not null,
	"UsaGarantiaEspecifica"					NVARCHAR(1) 	default 'N' 			not null,
	"Glosa"									NVARCHAR(50) 	default '' 				not null,
	"IdUsuario"								BIGINT			default '0' 			not null,
	"IdModulo"								NVARCHAR(3) 	default '' 				not null,
	--FUERON CREADOS PARA FONDOS MUTUOS
	"TipoCarteraNormativa"					NVARCHAR(6) 	default '' 				not null,
	"FechaCurse"							DATE            default '0001-01-01' 	not null,
	"TipoFactorAjuste"						NVARCHAR(3) 	default '' 				not null, --uf, ipc
	"ValorFactorAjusteaFechaCurse"			DECIMAL(26,8)	default '0'   			not null, --valor de la uf
	"CostoEnFactorAjuste"					DECIMAL(26,8)	default '0'   			not null,
	--INSTRUMENTO
	"IdClasificacionTipoInstrumento"		NVARCHAR(3) 	default '' 				not null,
	"IdTipoInstrumento"						NVARCHAR(3) 	default '' 				not null,	 
	"IdInstrumento" 						BIGINT			default '0' 			not null,
	"Nemotecnico"							NVARCHAR(40)	default '' 				not null,
	"IdEmisor"								NVARCHAR(40) 	default '' 				not null,
	"NemoSVS"	 							NVARCHAR(40) 	default '' 				not null,
	"IdTipoReajuste" 						NVARCHAR(3) 	default '' 				not null,
	"FechaEmisionInstrumento"				DATE            default '0001-01-01' 	not null,
	"FechaVencimientoInstrumento"			DATE            default '0001-01-01' 	not null,
	"EsFungible" 							NVARCHAR(1) 	default 'N'				not null,
	"ValorizaPor"							NVARCHAR(1) 	default 'T'				not null, --T = Tasa, P= Precio
	"TipoValorizacion"						NVARCHAR(10) 	default ''				not null,
	"IdMonedaInstrumento"					NVARCHAR(3) 	default '' 				not null,
	"Tamano"								BIGINT			default '0'				not null,
	"IdBoveda"								NVARCHAR(3) 	default '' 				not null,
	"CuentaBoveda"							NVARCHAR(40) 	default '' 				not null,
	--CANTIDADES Y MONTOS
	"Cantidad"								DECIMAL(26,8)	default '0' 			not null,
	"CorteMinimo"							DECIMAL(26,8) 	default '0' 			not null,
	"ValorRescate"							DECIMAL(26,8) 	default '0' 			not null,
	"PrecioCompra"							DECIMAL(26,8) 	default '0' 			not null,
	"TasaCompra" 							DECIMAL(26,8) 	default '0' 			not null,
	"MontoCompra"		 					DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	"MontoCompraMonedaInstrumento"			DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	--Si el registro fue cargado desde un archivo por ejemplo la carga inicial
	"IdCarga"								BIGINT 			default '0'				not null,
	"IdSecuenciaCarteraMigracion"			BIGINT 			default '0' 			not null,
	"CodigoPeriodo"							NVARCHAR(7) 	default '0001-01'		not null,
	 "RUT_CLI"								NVARCHAR(11)	,	
	 "SEC_RUT_CLI"							BIGINT			,
	 "SEC_CAR_INVER"                        BIGINT          ,
	 "COD_PER"								NVARCHAR(10)    ,
	 "FOLIO_COMP_ADJ"                       BIGINT          ,
	 "TIPO_COMP_ADJ"						NVARCHAR(10)	, 
     "LIN_COMP_ADJ"                         BIGINT          ,
	 "LIN_ORDEN"							BIGINT			,
	 "SEC_ORDEN"							BIGINT			,
	 "TIPO_CARTERA"							NVARCHAR(10)	,
	 "ID_ERROR"								BIGINT          ,
	 "SEC_MOVTO"                            BIGINT          ,
	 "CAN_GARANTIA"							DECIMAL(26,8)	,
	PRIMARY KEY ("IdMovimientoCarteraEnTransito")
);



CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" (
	 "IdCargoAbonoMovimiento"            BIGINT         NOT NULL ,
	 "FechaSistema"                      DATE           NOT NULL ,
	 "IdModulo"                          NVARCHAR(3)    NOT NULL ,
	 "IdTipoMovimiento"                  NVARCHAR(6)    NOT NULL ,
	 "EsIngresoManual"                   NVARCHAR(1)    DEFAULT 'N',
	 "IdCuentaInversion"                 BIGINT         NOT NULL ,
	 "NombreCliente"                     NVARCHAR(300)  DEFAULT '',
	 "IdConcepto"                        NVARCHAR(6)    NOT NULL ,
	 "Descripcion"                       NVARCHAR(520)  DEFAULT '',
	 "IdCondicionDeLiquidacion"          NVARCHAR(2)    NOT NULL ,
	 "FechaEfectivaMovimiento"           DATE           NOT NULL ,
	 "Monto"                             DECIMAL(26,8)  NOT NULL ,
	 "Saldo"                             DECIMAL(26,8)  DEFAULT 0, --Registra saldo por fecha de liquidación
	 "SaldoPorFechaMovimiento"			 DECIMAL(26,8)  DEFAULT 0, --Registra saldo por fecha de movimiento 
	 "IdEvento"                          BIGINT         DEFAULT 0,
	 "EsCertificadoAutomatico"           NVARCHAR(1)    DEFAULT 'N',
	 "IdUsuarioCertificador"             BIGINT         DEFAULT 0,
	 "FechaCertificacion"                TIMESTAMP      DEFAULT '0001-01-01',
	 "IdTipoComprobante"                 NVARCHAR(3)    DEFAULT '',
	 "IdComprobante"                     BIGINT         DEFAULT 0,
	 "FechaComprobante"                  SECONDDATE     DEFAULT '0001-01-01',	 
	 "IdTipoProducto"                    NVARCHAR(4)    DEFAULT '',
	 "IdTipoOperacion"                   NVARCHAR(6)    DEFAULT '',
	 "IdOperacion"                       BIGINT         DEFAULT 0,
	 "FechaOperacion"                    SECONDDATE     DEFAULT '0001-01-01',
	 "FechaLiquidacionOperacion"         DATE           DEFAULT '0001-01-01',
	 "FechaVencimientoOperacion"         DATE           DEFAULT '0001-01-01',
	 "IdMonedaOperacion"                 NVARCHAR(3)    DEFAULT '',
	 "MontoOperacion"                    DECIMAL(26,8)  DEFAULT 0,
	 "TipoCambioOperacion"               DECIMAL(26,8)  DEFAULT 0,
	 "IdTipoInstrumento"                 NVARCHAR(3)    DEFAULT '',
	 "IdInstrumento"                     BIGINT         DEFAULT 0,
	 "Nemo"                              NVARCHAR(40)   DEFAULT '',
	 "EsCargoAbonoPorTraspaso"           NVARCHAR(1)    DEFAULT 'N',
	 "IdCargoAbonoRelacionadoTraspaso"   BIGINT         DEFAULT 0,
	 "EsIndicadorBilateral"              NVARCHAR(1)    DEFAULT 'N',
	 "IdContraparte"                     BIGINT         DEFAULT 0,
	 "IdPersonaPagoCobro"                BIGINT         DEFAULT 0,
	 "FechaIngresoRegistro"              SECONDDATE     NOT NULL ,
	 "IdUsuario"                         BIGINT         DEFAULT 0,
	 "IdEmpresa"                         BIGINT         NOT NULL ,
	 "IdEstado"                          NVARCHAR(3)    NOT NULL ,
	 "IdCarga"                		     BIGINT         DEFAULT 0 NOT NULL,
	 "SecuenciaMovimientoMigracion"      BIGINT         DEFAULT 0 NOT NULL,
	 "FechaMovimientoMigracion"			 DATE			DEFAULT '0001-01-01' NOT NULL, 	       
	 "RUT_CLI"							 NVARCHAR(11)   DEFAULT '' NOT NULL, 			
	 "SEC_RUT_CLI"						 BIGINT			DEFAULT '0' NOT NULL,
	 "FEC_MVTO"							 NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "SEC_MOVTO"						 BIGINT			DEFAULT '0' NOT NULL,
	 "COD_MOVTO"						 NVARCHAR(10)	DEFAULT '' NOT NULL,		 	
	 "FOLIO_COMP_ADJ"					 BIGINT			DEFAULT '0' NOT NULL,	
	 "TIPO_COMP_ADJ"					 NVARCHAR(2)	DEFAULT '' NOT NULL,
	 "LIN_COMP_ADJ"						 BIGINT			DEFAULT '0' NOT NULL,
	 "COD_BOVEDA"						 NVARCHAR(3)	DEFAULT '' NOT NULL,
	 "COD_SUC"							 NVARCHAR(40)	DEFAULT '' NOT NULL,
	 "FEC_TRANS"                         NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "FEC_LIQUID"                        NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "FEC_ING_MVTO"						 NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "IND_TIT_CCTE"                      NVARCHAR(1)	DEFAULT '' NOT NULL, 
	 "COD_PER"                           NVARCHAR(7)    DEFAULT '' NOT NULL,
	 "IND_CCTE_CTDO"                     NVARCHAR(1)    DEFAULT '' NOT NULL,	
     "IND_SALDO_CTDO" 					 NVARCHAR(1)    DEFAULT '' NOT NULL,
     "IND_CCTE_APLAZO" 					 NVARCHAR(1) 	DEFAULT '' NOT NULL,
     "IND_SALDO_APLAZO" 				 NVARCHAR(1)    DEFAULT '' NOT NULL,
     "FEC_VCTO"                          NVARCHAR(10)   DEFAULT '0001-01-01' NOT NULL,
	 PRIMARY KEY ("IdCargoAbonoMovimiento")
 );
 
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" (
 "IdTransaccion" BIGINT
,"Folio" NVARCHAR(40)
,"FechaSistemaCreacion" DATE
,"HoraSistemaCreacion" TIME
,"FechaOperacion" SECONDDATE
,"FechaLiquidacion" DATE
,"FechaEmision" DATE
,"FechaVencimiento" DATE
,"FechaIngresoRegistro" TIMESTAMP
,"IdSistemaBursatil" BIGINT
,"EsOD" NVARCHAR(1)
,"EsCuentaPropia" NVARCHAR(1)
,"EsFungible" NVARCHAR(1)
,"EsAPlazo" NVARCHAR(1)
,"IdEmisor" NVARCHAR(40)
,"Cantidad" DECIMAL(26,8)
,"Monto" DECIMAL(26,8)
,"Precio" DECIMAL(26,8)
,"Tasa" DECIMAL(26,8)
,"Nemotecnico" NVARCHAR(40)
,"NemotecnicoSVS" NVARCHAR(40)
,"ValorRescate" DECIMAL(26,8)
,"ValorCaptacion" DECIMAL(26,8)
,"IdEstado" NVARCHAR(3)
,"IdTransaccionPlazo" BIGINT
,"IdTransaccionOd" BIGINT
,"IdTipoInstrumento" NVARCHAR(3)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdTipoReajuste" NVARCHAR(3)
,"IdEmpleado" BIGINT
,"IdTipoOperacion" NVARCHAR(6)
,"IdCondicionDeLiquidacion" NVARCHAR(2)
,"IdMoneda" NVARCHAR(3)
,"IdEmpresa" BIGINT
,"IdBolsa" NVARCHAR(3)
,"IdInstrumento" BIGINT
,"NemotecnicoSubyacente" NVARCHAR(40)
,"IdInstrumentoSubyacente" BIGINT
,"FolioCartera" BIGINT
,"FechaParidad" DATE
,"ValorParidad" DECIMAL(26,8)
,"FechaTipoCambio" DATE
,"ValorTipoCambio" DECIMAL(26,8)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"IdMonedaSubyacente" NVARCHAR(3)
,"EsVentaCorta" NVARCHAR(1)
,"Plazo" BIGINT
,"FechaIngresoOferta" TIMESTAMP
,"IdCarga" BIGINT
,"CodigoOperadorContraparte" NVARCHAR(40)
,"FechaModificacionOperacion" SECONDDATE
,"MontoValorizado" DECIMAL(26,8)
,"IdLibroOrdenes" BIGINT
,"Reservado" NVARCHAR(1)
,"IdUsuarioReserva" BIGINT
,"MotivoReserva" NVARCHAR(40)
,"Bloqueado" NVARCHAR(1)
,"IdUsuarioBloqueo" BIGINT
,"MotivoBloqueo" NVARCHAR(40)
,"IdComprobanteTesoreria" BIGINT
,"MarcaNegociacion" NVARCHAR(40)
,"IndicadorDCV" NVARCHAR(1)
,"IdCorredorLocal" BIGINT
,"IdProgramaProcesaAutomatico" BIGINT
,"EsBilateral" NVARCHAR(1)
,"IdCamara" NVARCHAR(40)
,"TipoCambioCorresponsal" DECIMAL(26,8)
,"IdCorredorCorresponsal" BIGINT
,"MontoLiquidacionCorresponsal" DECIMAL(26,8)
,"IdMonedaLiquidacionCorresponsal" NVARCHAR(3)
,"IdCorredorContraparte" BIGINT
,"CodigoOperadorCorredor" NVARCHAR(40)
,"MERCADO" NVARCHAR(10)
,"FEC_TRANS" NVARCHAR(30)
,"FOLIO_TRANS" NVARCHAR(30)
,"IND_BOLSA" NVARCHAR(3)
,"TIPO_OPERAC" NVARCHAR(3)
,"FOLIO_SIMUL" NVARCHAR(10)
,"IND_SIMUL" NVARCHAR(3)
,"IND_OD" NVARCHAR(50)
,"RUEDA" NVARCHAR(10)
,"SEC_MOVTO_FAC" BIGINT
, PRIMARY KEY ("IdTransaccion")
);
 
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Orden" (
 "IdOrden" 								BIGINT NOT NULL,
 "Folio"								NVARCHAR(40),
 "FechaRecepcion" 						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaIngresoRegistro"					TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "FechaIngresoRegistroCorta"			DATE NOT NULL DEFAULT '0001-01-01',
 "FechaSolicitudAnulacion"				SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaAnulacion" 						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaOperacion"						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaVigencia" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaVencimiento" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaEmision" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaLiquidacion" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaAceptacion" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaAutorizacion" 					DATE NOT NULL DEFAULT '0001-01-01',
 "Glosa" 								NVARCHAR(520),
 "IdEstadoOrden" 						NVARCHAR(3) NOT NULL ,
 "Cantidad" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "Tasa" 								DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "Plazo" 								BIGINT NOT NULL DEFAULT 0 ,
 "ValorRescate" 						DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdTipoReajuste" 						NVARCHAR(3) NOT NULL,
 "PrecioOrden" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "PrecioMercado" 						DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "TasaAgente" 							DECIMAL(26,8) NOT NULL,
 "IndicadorFacturacionInmediata" 		NVARCHAR(1) NOT NULL,
 "ValorCaptacion" 						DECIMAL(26,8),
 "IdCondicionDeLiquidacion" 			NVARCHAR(2) NOT NULL,
 "IdTipoOperacion" 						NVARCHAR(6) NOT NULL,
 "IdCliente" 							BIGINT NOT NULL,
 "IdPortafolioCliente" 					BIGINT NOT NULL,
 "IdTipoCanalIngreso" 					NVARCHAR(3) NOT NULL ,
 "IdLibroOrdenes" 						BIGINT NOT NULL,
 "IdEmisor" 							NVARCHAR(40) NOT NULL,
 "IdEmpresa" 							BIGINT NOT NULL,
 "IdBolsa" 								NVARCHAR(3) NOT NULL , 
 "IdBolsaDistribucion"					NVARCHAR(3) NOT NULL DEFAULT '',
 "EspecificaBolsa"						NVARCHAR(1) NOT NULL DEFAULT 'S',
 "IdTipoCanalDistribucion"				NVARCHAR(3) NOT NULL DEFAULT '',
 "IdInstrumento" 						BIGINT NOT NULL,
 "IdTipoInstrumento" 					NVARCHAR(3) NOT NULL ,
 "IdClasificacionTipoInstrumento" 		NVARCHAR(3) NOT NULL ,
 "IdProducto" 							NVARCHAR(4) NOT NULL ,
 "Nemotecnico" 							NVARCHAR(40) NOT NULL ,
 "IdGrupoCobro" 						BIGINT NOT NULL,
 "IdCarga"                              BIGINT NOT NULL DEFAULT 0,
 "EsVentaCorta"							NVARCHAR(1) NULL DEFAULT 'N',
 "SecOrdenMigracion"                    BIGINT DEFAULT 0 ,
 "Reservado"	    					NVARCHAR(1) NOT NULL DEFAULT 'N', 
 "IdUsuarioReserva" 					BIGINT NOT NULL DEFAULT 0,
 "MotivoReserva"						NVARCHAR(40) NOT NULL DEFAULT '',
 "Bloqueado"		   					NVARCHAR(1) NOT NULL DEFAULT 'N', 
 "IdUsuarioBloqueo" 					BIGINT NOT NULL DEFAULT 0,
 "MotivoBloqueo"						NVARCHAR(40) NOT NULL DEFAULT '',
 "IdInstrumentoSubyacente"              BIGINT NOT NULL DEFAULT 0,
 "IdEmpleadoPortafolio"					BIGINT NOT NULL DEFAULT 0,
 "IdEmpleadoDigitador" 					BIGINT NOT NULL DEFAULT 0,
 "IdTipoPrecio" 						NVARCHAR(1) NOT NULL DEFAULT '',
 "IdTipoTasa" 							NVARCHAR(1) NOT NULL DEFAULT '',
 "IdPorcentajeVisibilidad" 				BIGINT NOT NULL DEFAULT 0,
 "IdTipoVigenciaOrden" 					NVARCHAR(2) NOT NULL DEFAULT '',
 "IdMonedaOrden"						NVARCHAR(3) NOT NULL DEFAULT '',
 "IdMonedaLiquidacionOrden"				NVARCHAR(3) NOT NULL DEFAULT '',
 "MontoLocal" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "MontoOrden" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "MontoLiquidacion"						DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "TipoCambio"							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "IdTipoCustodia" 						NVARCHAR(3) NOT NULL DEFAULT '',
 "IdFormaCustodia" 						NVARCHAR(3) NOT NULL DEFAULT '',
 "sol_sec"								BIGINT,
 "ord_sec"								BIGINT,
 "ord_folio"							BIGINT,
 "RUT_CLI"								NVARCHAR(11),
 "SEC_RUT_CLI"                          BIGINT, --cuenta es igual a sec_rut_cli
 "TIPO_OPERAC" 							NVARCHAR(1),
 "ope_id"								NVARCHAR(4),
 "MERCADO"								NVARCHAR(4),
 "sol_tipo_gasto" 						NVARCHAR(40), 
 "sol_tipo_comision" 					NVARCHAR(40), 
 "sol_tipo_derecho" 					NVARCHAR(40),
 "sol_val_gastos" 						DECIMAL(26,8), 
 "sol_val_comision" 					DECIMAL(26,8),
 "sol_val_derechos" 					DECIMAL(26,8),                			
 "orv_mto_gastos" 						DECIMAL(26,8),
 "orv_mto_comision" 					DECIMAL(26,8), 
 "orv_mto_derechos"						DECIMAL(26,8),
 "sol_ind_vta_corta"					NVARCHAR(4),
 "sol_ind_simul"						NVARCHAR(4),  	
 PRIMARY KEY ("IdOrden"));
 
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" (
 "IdAsignacion"                         BIGINT NOT NULL,
 "FechaAsignacion"                      TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "FechaIngresoRegistro"                 TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "CantidadAsignada"                     DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoAsignado"                        DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorRescateAsignado"                 DECIMAL(26,8) NOT NULL DEFAULT 0,
 "CantidadAsignadaRef"                	DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorRescateAsignadoRef" 				DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdEstadoAsignacion"                   NVARCHAR(40) NOT NULL,
 "IdEstadoFacturacion"                  NVARCHAR(40) NOT NULL,
 "IdTransaccion"                        BIGINT NOT NULL,
 "IdCarga"                              BIGINT NOT NULL DEFAULT 0,
 "IdOrden"                              BIGINT NOT NULL, 
 "IdLibroOrdenes" 						BIGINT NOT NULL,
 "EsAPlazo" 							NVARCHAR(1) DEFAULT 'N' NOT NULL,
 "IdAsignacionRef"						BIGINT NOT NULL DEFAULT 0,
 "IdCompFacturacionRef"					BIGINT NOT NULL DEFAULT 0,
 "EsOD"									NVARCHAR(1) NOT NULL DEFAULT 'N',
 "SecAsignacionMigracion"               BIGINT DEFAULT 0 NOT NULL,
 "sol_sec"								BIGINT,
 "FEC_TRANS"                            NVARCHAR(30),
 "FOLIO_TRANS"							NVARCHAR(40),
 "MERCADO"								NVARCHAR(4),
 "SEC_ASIGNACION"						BIGINT,
 "TIPO_OPERAC"							NVARCHAR(1),
 "NEMO"									NVARCHAR(50),
 "RUT_CLI"								NVARCHAR(11),
 "SEC_RUT_CLI"							BIGINT,
 "IND_BOLSA"							NVARCHAR(3),
 "IND_SIMUL"							NVARCHAR(4),
 "IND_LIQUID"							NVARCHAR(4),
 "SEC_MOVTO_FAC"						BIGINT,				
 PRIMARY KEY ("IdAsignacion"));

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte"(
 "IdContraparte" 			BIGINT  NOT NULL ,
 "IdPersona" 				BIGINT ,
 "IdEmpresa" 				BIGINT ,
 "CodigoInscripcion"		NVARCHAR(40),
 "Emisor" 					NVARCHAR(1) DEFAULT 'N',
 "Depositos" 				NVARCHAR(1) DEFAULT 'N',
 "Bolsa" 					NVARCHAR(1) DEFAULT 'N',
 "Corredora" 				NVARCHAR(1) DEFAULT 'N',
 "CamaraDeCompensacion" 	NVARCHAR(1) DEFAULT 'N',
 "Banco" 					NVARCHAR(1) DEFAULT 'N', 
 "Boveda"					NVARCHAR(1) DEFAULT 'N', 			
 "IdEstado" 				NVARCHAR(3) DEFAULT 'ACT', 
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "FechaIngreso" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" 			SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" 		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "IdUsuarioCreador"			BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 			NVARCHAR(40),
 "FechaBloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 		NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 		NVARCHAR(40),
 "FechaDesbloqueo" 			SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 	NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdOrigen"                 BIGINT DEFAULT 0 NOT NULL,
 "IdCarga"                  BIGINT DEFAULT 0 NOT NULL,
 PRIMARY KEY ("IdContraparte"));
 
 CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Empleado" (
 "IdEmpleado" BIGINT  NOT NULL ,
 "IdPersona" BIGINT ,
 "CodigoEmpleado" NVARCHAR(40),
 "IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdEmpresa" BIGINT ,
 "CodigoTipoCargoEmpleado"  NVARCHAR(6)    default '' ,
 "DetalleCargo"						NVARCHAR(40) DEFAULT ''  NOT NULL,
 "IdEmpleadoSupervisor" BIGINT ,
 "IdSucursal" BIGINT ,
 "UtilizaPlantillaEmpleado" NVARCHAR(1) DEFAULT 'N',
 "IdPlantillaEmpleado" NVARCHAR(40) DEFAULT '',
 "FechaInicioCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaTerminoCargo" DATE  DEFAULT '0001-01-01' NOT NULL ,
 "FechaIngreso" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaCreacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "FechaModificacion" SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
 "IdCarga" BIGINT DEFAULT 0 NOT NULL,
 "CodigoAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "CodigoSupervisorAgenteMigracion" NVARCHAR(40) DEFAULT '' NOT NULL,
 "EsEnrolado" 				NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "Borrado" 					NVARCHAR(1) DEFAULT 'N',
 "IdUsuarioCreador"   BIGINT  DEFAULT 0,
 "IdTipoBloqueo" 					NVARCHAR(40),
 "FechaBloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioBloqueo" 				NVARCHAR(520) DEFAULT '' NOT NULL,
 "IdTipoDesbloqueo" 				NVARCHAR(40),
 "FechaDesbloqueo" 					SECONDDATE   DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 "ComentarioDesbloqueo" 			NVARCHAR(520) DEFAULT '' NOT NULL,
 "MarcaRepetido"                    BIGINT DEFAULT 0 NOT NULL,
 PRIMARY KEY ("IdEmpleado"))  ;
 
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante"(
"IdMovimientoComprobante" BIGINT
,"IdEmpresa" BIGINT
,"IdConceptoMovimiento" NVARCHAR(6)
,"SecuenciaAgrupacion" NVARCHAR(40)
,"IdCliente" BIGINT
,"IdPortafolio" BIGINT
,"IdAgente" BIGINT
,"IdSucursal" BIGINT
,"IdInstrumento" BIGINT
,"IdInstrumentoSubyacente" BIGINT
,"IdTipoInstrumento" NVARCHAR(3)
,"IdTipoInstrumentoSubyacente" NVARCHAR(3)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdTipoProducto" NVARCHAR(4)
,"IdMercado" NVARCHAR(4)
,"IdCuentaInversion" BIGINT
,"IdMoneda" NVARCHAR(3)
,"IdTipoComprobante" NVARCHAR(3)
,"IdComprobante" BIGINT
,"IdTipoOperacion" NVARCHAR(6)
,"IdMovimientoRef" BIGINT
,"IdBoveda" NVARCHAR(3)
,"IdCorredoraContraparte" BIGINT
,"IdCondicionLiquidacion" NVARCHAR(2)
,"FechaLiquidacion" DATE
,"IdBolsa" NVARCHAR(3)
,"IdTipoReajuste" NVARCHAR(3)
,"IdTipoConvenio" NVARCHAR(3)
,"IdTipoPagoCobroMovimiento" NVARCHAR(3)
,"IdEstadoFacturacion" NVARCHAR(3)
,"IdOrden" BIGINT
,"LineaOrden" INTEGER
,"IdAsignacion" BIGINT
,"IdTipoCanalIngreso" NVARCHAR(3)
,"FechaOrden" TIMESTAMP
,"IdTransaccion" BIGINT
,"FolioTransaccion" NVARCHAR(40)
,"FechaTransaccion" TIMESTAMP
,"FechaVencimiento" TIMESTAMP
,"FechaParidad" TIMESTAMP
,"ValorParidad" DECIMAL(26,8)
,"ValorCaptacionMonedaOrigen" DECIMAL(26,8)
,"FolioComprobante" BIGINT
,"Tasa" DECIMAL(26,8)
,"Cantidad" DECIMAL(26,8)
,"Precio" DECIMAL(26,8)
,"Monto" DECIMAL(26,8)
,"MontoDescuento" DECIMAL(26,8)
,"MontoInteres" DECIMAL(26,8)
,"Glosa" NVARCHAR(260)
,"ValorRescate" DECIMAL(26,8)
,"TasaAgente" DECIMAL(26,8)
,"Spread" DECIMAL(26,8)
,"DiasPlazo" DECIMAL(26,8)
,"CodigoCanalIngreso" NVARCHAR(40)
,"DocumentoIdentidad" NVARCHAR(40)
,"TipoDocumentoIdentidad" NVARCHAR(1)
,"NombreLargoCliente" NVARCHAR(300)
,"DireccionFacturacion" NVARCHAR(100)
,"NombreAgente" NVARCHAR(300)
,"NombreSucursal" NVARCHAR(300)
,"FolioCartera" BIGINT
,"CodigoCuentaInversion" NVARCHAR(40)
,"CodigoTipoProducto" NVARCHAR(40)
,"CodigoTipoOperacion" NVARCHAR(40)
,"CodigoBolsa" NVARCHAR(40)
,"NombreTipoInstrumento" NVARCHAR(100)
,"NombreTipoInstrumentoSubyacente" NVARCHAR(100)
,"NombreClasificacionTipoInstrumento" NVARCHAR(100)
,"NemotecnicoInstrumento" NVARCHAR(40)
,"NemotecnicoSubyacente" NVARCHAR(40)
,"RazonSocialEmisor" NVARCHAR(100)
,"FechaEmision" TIMESTAMP
,"TasaEmision" DECIMAL(26,8)
,"FechaIngresoRegistro" TIMESTAMP
,"FechaSistema" TIMESTAMP
,"EsComprobIndividual" NVARCHAR(1)
,"EsCuentaPropia" NVARCHAR(1)
,"EsOrdenDirecta" NVARCHAR(1)
,"EsEnvio" NVARCHAR(1)
,"EsPrecioPrima" NVARCHAR(1)
,"EsEnRueda" NVARCHAR(1)
,"EsRuteo" NVARCHAR(1)
,"EsIngresoAutomatico" NVARCHAR(1)
,"IdUsuario" BIGINT
,"IdUsuarioBloqueo" BIGINT
,"IdCarga" BIGINT
,"EsAPlazo" NVARCHAR(1)
,"MontoAPlazo" DECIMAL(26,8)
,"PrecioAPlazo" DECIMAL(26,8)
,"IdMovimientoOdRef" BIGINT
,"IdMonedaSubyacente" NVARCHAR(3)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"ValorTipoCambio" DECIMAL(26,8)
,"EsVentaCorta" NVARCHAR(1)
,"IdComprobanteRef" BIGINT
,"SecMovimientoFacturarMigracion" BIGINT
,"ActualizarCustodia" NVARCHAR(1)
,"FechaMovimientoComprobante" SECONDDATE
,"FechaTipoCambio" DATE
,"ActualizarCustodiaRelacionado" NVARCHAR(1)
,"IdMonedaMovimientoComprobante" NVARCHAR(3)
,"MontoMovimientoComprobante" DECIMAL(26,8)
,"MontoLiquidacion" DECIMAL(26,8)
,"IdEmisor" NVARCHAR(40)
,"EsFungible" NVARCHAR(1)
,"IdFormaCustodia" NVARCHAR(3)
,"ActualizarCuentaInversion" NVARCHAR(1)
,"ActualizarTesoreria" NVARCHAR(1)
,"IdInstrumentoRelacionado" BIGINT
,"IdTipoInstrumentoRelacionado" NVARCHAR(3)
,"IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3)
,"NombreTipoInstrumentoRelacionado" NVARCHAR(100)
,"NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100)
,"NemotecnicoRelacionado" NVARCHAR(40)
,"IdAsignacionRef" BIGINT
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT_CLI" BIGINT
,"TIPO_COMP_FAC" NVARCHAR(30)
,"FOLIO_COMP_FAC" BIGINT
, "SEC_MOVTO_FAC" BIGINT
,PRIMARY KEY ("IdMovimientoComprobante")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante"
( "IdComprobante" BIGINT
,"IdEmpresa" BIGINT
,"IdPortafolio" BIGINT
,"IdCliente" BIGINT
,"DocumentoIdentidad" NVARCHAR(40)
,"TipoDocumentoIdentidad" NVARCHAR(1)
,"NombreLargoCliente" NVARCHAR(300)
,"DireccionFacturacion" NVARCHAR(100)
,"FolioComprobante" BIGINT
,"IdTipoComprobante" NVARCHAR(3)
,"IdTipoOperacion" NVARCHAR(6)
,"IdTipoProducto" NVARCHAR(4)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdTipoInstrumento" NVARCHAR(3)
,"Monto" DECIMAL(26,8)
,"MontoCobro" DECIMAL(26,8)
,"MontoFinal" DECIMAL(26,8)
,"MontoPagado" DECIMAL(26,8)
,"IdAgente" BIGINT
,"IdSucursal" BIGINT
,"IdCondicionLiquidacion" NVARCHAR(2)
,"FechaLiquidacion" DATE
,"FechaEmision" TIMESTAMP
,"FechaEnvio" SECONDDATE
,"IdEstadoEnvio" NVARCHAR(3)
,"FechaRespuestaEnvio" SECONDDATE
,"IdEstadoComprobante" NVARCHAR(3)
,"IdTipoComprobanteRef" NVARCHAR(3)
,"IdComprobanteRef" BIGINT
,"Glosa" NVARCHAR(260)
,"EsExento" NVARCHAR(1)
,"FechaIngresoRegistro" TIMESTAMP
,"FechaSistema" DATE
,"IdCarga" BIGINT
,"FolioComprobanteFacturacionMigracion" BIGINT
,"IdMoneda" NVARCHAR(3)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"MontoLiquidacion" DECIMAL(26,8)
,"IdMonedaComprobante" NVARCHAR(3)
,"MontoComprobante" DECIMAL(26,8)
,"MontoCobroComprobante" DECIMAL(26,8)
,"MontoCobroLiquidacion" DECIMAL(26,8)
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT_CLI" BIGINT
,"TIPO_COMP_FAC" NVARCHAR(2)
,"FOLIO_COMP_FAC"  BIGINT
,"MERCADO" NVARCHAR(2)
,"PRODUCTO" NVARCHAR(10)
,"FOLIO_COMP_ASOC" BIGINT
,"FOLIO_COMP_CONT" BIGINT
,"FEC_ING_COMP" NVARCHAR(30) 
,PRIMARY KEY ("IdComprobante")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle"(
"IdComprobanteDetalle" BIGINT
,"IdComprobante" BIGINT
,"NroLinea" INTEGER
,"FechaLiquidacion" DATE
,"IdInstrumento" BIGINT
,"NemotecnicoInstrumento" NVARCHAR(100)
,"IdTipoOperacion" NVARCHAR(6)
,"IdTipoProducto" NVARCHAR(4)
,"Cantidad" DECIMAL(26,8)
,"Precio" DECIMAL(26,8)
,"Monto" DECIMAL(26,8)
,"IdBolsa" NVARCHAR(3)
,"IdAgente" BIGINT
,"IdSucursal" BIGINT
,"IdEstadoComprobanteDetalle" NVARCHAR(3)
,"FechaIngresoRegistro" TIMESTAMP
,"IdCarga" BIGINT
,"FolioComprobanteFacturacionMigracion" BIGINT
,"MontoLiquidacion" DECIMAL(26,8)
,"IdMoneda" NVARCHAR(3)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"MontoComprobanteDetalle" DECIMAL(26,8)
,"IdMonedaComprobanteDetalle" NVARCHAR(3)
,"IdInstrumentoSubyacente" BIGINT
,"IdMonedaSubyacente" NVARCHAR(3)
,"IdTransaccion" BIGINT
,"FechaComprobanteDetalle" SECONDDATE
,"NemotecnicoSubyacente" NVARCHAR(40)
,"IdTipoInstrumento" NVARCHAR(3)
,"IdTipoInstrumentoSubyacente" NVARCHAR(3)
,"NombreTipoInstrumento" NVARCHAR(100)
,"NombreTipoInstrumentoSubyacente" NVARCHAR(100)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3)
,"IdInstrumentoRelacionado" BIGINT
,"IdTipoInstrumentoRelacionado" NVARCHAR(3)
,"NemotecnicoRelacionado" NVARCHAR(40)
,"NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100)
,"NombreTipoInstrumentoRelacionado" NVARCHAR(100)
,"NombreClasificacionTipoInstrumento" NVARCHAR(100)
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT_CLI" BIGINT
,"LINEA_COMP_FAC" BIGINT
,"FOLIO_COMP_FAC" BIGINT
,"TIPO_COMP_FAC" NVARCHAR(2)
,"COD_MOVTO" NVARCHAR(10)
,"MERCADO" NVARCHAR(10)
,"FEC_VCTO" NVARCHAR(10)
,"TIPO_OPERAC" NVARCHAR(1)
,PRIMARY KEY ("IdComprobanteDetalle")
);

create column table "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteCobro"
(
"IdComprobanteCobro" BIGINT
,"IdCobro" NVARCHAR(6)
,"IdComprobante" BIGINT
,"IdMoneda" NVARCHAR(3)
,"Monto" DECIMAL(26,8)
,"Porcentaje" DECIMAL(26,8)
,"FechaSistema" TIMESTAMP
,"FechaIngresoRegistro" TIMESTAMP
,"IdEstado" NVARCHAR(3)
,"IdMonedaComprobanteCobro" NVARCHAR(3)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"MontoComprobanteCobro" DECIMAL(26,8)
,"MontoLiquidacion" DECIMAL(26,8)
,"IdCarga" BIGINT
,"FOLIO_COMP_FAC" BIGINT
,"TIPO_COMP_FAC" NVARCHAR(2)
,PRIMARY KEY ("IdComprobanteCobro")
);


CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"
(
"IdMovimientoComprobanteDetalle" BIGINT
,"IdComprobanteDetalle" BIGINT
,"IdMovimientoComprobante" BIGINT
,"IdTransaccion" BIGINT
,"IdInstrumento" BIGINT
,"NemotecnicoInstrumento" NVARCHAR(100)
,"IdConceptoMovimiento" NVARCHAR(6)
,"Cantidad" DECIMAL(26,8)
,"Precio" DECIMAL(26,8)
,"PrecioPromedio" DECIMAL(26,8)
,"Monto" DECIMAL(26,8)
,"Tasa" DECIMAL(26,8)
,"TasaEmision" DECIMAL(26,8)
,"SpreadTasaEmision" DECIMAL(26,8)
,"ValorRescate" DECIMAL(26,8)
,"ValorParidad" DECIMAL(26,8)
,"ValorCaptacionMonedaOrigen" DECIMAL(26,8)
,"SecuenciaAgrupacion" NVARCHAR(40)
,"IdEstadoMovimientoComprobanteDetalle" NVARCHAR(3)
,"FechaIngresoRegistro" TIMESTAMP
,"MontoLiquidacion" DECIMAL(26,8)
,"IdMonedaLiquidacion" NVARCHAR(3)
,"MontoMovimientoComprobanteDetalle" DECIMAL(26,8)
,"IdMonedaMovimientoComprobanteDetalle" NVARCHAR(3)
,"IdMoneda" NVARCHAR(3)
,"IdInstrumentoSubyacente" BIGINT
,"IdMonedaSubyacente" NVARCHAR(3)
,"IdTipoInstrumento" NVARCHAR(3)
,"IdTipoInstrumentoSubyacente" NVARCHAR(3)
,"NombreTipoInstrumento" NVARCHAR(100)
,"NombreTipoInstrumentoSubyacente" NVARCHAR(100)
,"NemotecnicoSubyacente" NVARCHAR(40)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3)
,"IdInstrumentoRelacionado" BIGINT
,"IdTipoInstrumentoRelacionado" NVARCHAR(3)
,"NemotecnicoRelacionado" NVARCHAR(40)
,"NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100)
,"NombreTipoInstrumentoRelacionado" NVARCHAR(100)
,"NombreClasificacionTipoInstrumento" NVARCHAR(100)
,"IdCarga"  BIGINT
,"FOLIO_COMP_FAC" BIGINT
,"TIPO_COMP_FAC" NVARCHAR(2)
,"LIN_COMP_FAC" BIGINT
,"RUT_CLI" NVARCHAR (11)
,"SEC_RUT_CLI" BIGINT
,"SEC_MOVTO_FAC" BIGINT
,"SecMovimientoFacturarMigracion" BIGINT
,primary key ("IdMovimientoComprobanteDetalle")
);
  
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Operacion"(
 "IdOperacion" BIGINT
,"FechaOperacion" TIMESTAMP
,"FolioOperacion" BIGINT
,"IdTipoProducto" NVARCHAR(4)
,"IdTipoOperacion" NVARCHAR(6)
,"IdOperacionRelacionada" BIGINT
,"IdInstrumento" BIGINT
,"IdMonedaPrincipal" NVARCHAR(3)
,"IdMonedaSecundaria" NVARCHAR(3)
,"IdModalidadLiquidacion" BIGINT
,"IdReferenciaMercado" BIGINT
,"IdPlaza" BIGINT
,"Intermesa" NVARCHAR(1)
,"IdMesaOrigen" BIGINT
,"IdMesaDestino" BIGINT
,"IdCliente" BIGINT
,"IdPortafolio" BIGINT
,"IdClienteCorredor" BIGINT
,"IdPortafolioCorredor" BIGINT
,"IdTipoReajuste" NVARCHAR(3)
,"MontoOperacion" DECIMAL(26,8)
,"MontoOperacionInicial" DECIMAL(26,8)
,"MontoOperacionFinal" DECIMAL(26,8)
,"IdMoneda" NVARCHAR(3)
,"Tasa" DECIMAL(26,8)
,"MontoOperacionMoneda" DECIMAL(26,8)
,"MontoOperacionInicialMoneda" DECIMAL(26,8)
,"MontoOperacionFinalMoneda" DECIMAL(26,8)
,"IdMonedaLiquidacion" DECIMAL(26,8)
,"TCCierre" DECIMAL(26,8)
,"ParidadCierre" DECIMAL(26,8)
,"TCCosto" DECIMAL(26,8)
,"PuntosTasasForward" NVARCHAR(1)
,"PuntosForward" DECIMAL(26,8)
,"TCForward" DECIMAL(26,8)
,"TasasMonedaPrincipal" DECIMAL(26,8)
,"TasasMonedaSecundaria" DECIMAL(26,8)
,"MontoDiferido" DECIMAL(26,8)
,"MontoMargen" DECIMAL(26,8)
,"IdFixing" BIGINT
,"FechaFixing" TIMESTAMP
,"HoraFixing" NVARCHAR(10)
,"IdSpotAsociado" BIGINT
,"IdClasificacionCartera" BIGINT
,"IdCertificado" NVARCHAR(6)
,"PlazoEnDias" BIGINT
,"FechaVencimiento" TIMESTAMP
,"FechaLiquidacion" TIMESTAMP
,"IdCondicionLiquidacion" NVARCHAR(2)
,"FechaCondicionLiquidacion" TIMESTAMP
,"IdAgente" BIGINT
,"TasaAgente" DECIMAL(26,8)
,"IdEstado" NVARCHAR(3)
,"FechaCreacion" TIMESTAMP
,"IdUsuarioCreacion" BIGINT
,"IdTipoCanalIngreso" NVARCHAR(3)
,"Glosa" NVARCHAR(500)
,"SecuenciaCompromisoMigracion"  BIGINT 
,"IdCarga" BIGINT 
,"COD_PER" NVARCHAR(7)
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT" BIGINT
,"RUT_CORR" NVARCHAR(11)
,"SEC_RUT_CORR" BIGINT
,"SEC_COMPROM" BIGINT
,"TIPO_COMPROM" NVARCHAR(2)
,"FOLIOTRANSACCION" BIGINT
,"FOLIOOPERACION" BIGINT
,primary key ("IdOperacion")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.Reserva"
(
 "IdReserva" BIGINT
,"IdOperacion" BIGINT
,"FolioCarteraOrigen" BIGINT
,"FolioOperacion" BIGINT
,"IdEstado" NVARCHAR(3)
,"IdUsuarioCreacion" BIGINT
,"IdTipoProducto" NVARCHAR(4)
,"IdConcepto" NVARCHAR(6)
,"Tasa" DECIMAL(26,8)
,"Precio" DECIMAL(26,8)
,"MontoValorizado" DECIMAL(26,8)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdInstrumento" NVARCHAR(40)
,"IdTipoInstrumento" NVARCHAR(3)
,"IdTipoReajuste" NVARCHAR(3)
,"IdEmisor" NVARCHAR(40)
,"FechaVencimiento" TIMESTAMP
,"EsFungible" NVARCHAR(1)
,"IdBolsa" NVARCHAR(3)
,"IdBoveda" NVARCHAR(3)
,"CodigoIFRS" NVARCHAR(40)
,"CantidadInicial" DECIMAL(26,8)
,"ValorRescateInicial" DECIMAL(26,8)
,"MontoValorizadoReserva" DECIMAL(26,8)
,"FechaVencimientoReserva" TIMESTAMP
,"FechaVencimientoProducto" TIMESTAMP
,"FechaCreacion" TIMESTAMP
,"IngIdUsuario" BIGINT
,"IngCantidad" DECIMAL(26,8)
,"IngValorRescate" DECIMAL(26,8)
,"IngMontoValorizado" DECIMAL(26,8)
,"IngFecha" TIMESTAMP
,"EgrIdUsuario" BIGINT
,"EgrCantidad" DECIMAL(26,8)
,"EgrValorRescate" DECIMAL(26,8)
,"EgrMontoValorizado" DECIMAL(26,8)
,"EgrFecha" TIMESTAMP
,"SecuenciaCompromisoMigracion" BIGINT
,"IdCarga" BIGINT
,"COD_PER" NVARCHAR(7)
,"SEC_COMPROM" BIGINT
,"EMISOR" NVARCHAR(40)
,"NEMO" NVARCHAR(40)
,"TIPO_REAJ" NVARCHAR(2)
,"MERCADO" NVARCHAR(2)
,"SEC_DETALLE" BIGINT
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT_CLI" BIGINT
,"LIN_COMPROM"  BIGINT
,primary key ("IdReserva")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.OperacionSpot"
(
"IdOperacionSpot" BIGINT
,"IdEmpresa" BIGINT
,"IdCliente" BIGINT
,"IdPortafolio" BIGINT
,"IdInstrumento" BIGINT
,"IdTipoOperacion" NVARCHAR(6)
,"FechaOperacion" SECONDDATE
,"FechaLiquidacion" DATE
,"IdMonedaPrincipal" NVARCHAR(3)
,"IdMonedaSecundaria" NVARCHAR(3)
,"IdMonedaLocal" NVARCHAR(3)
,"IdMonedaEquivalente" NVARCHAR(3)
,"MontoMonedaPrincipal" DECIMAL(26,8)
,"MontoMonedaPrincipalEquivalente" DECIMAL(26,8)
,"MontoMonedaSecundaria" DECIMAL(26,8)
,"TipoCambioCierre" DECIMAL(26,8)
,"TipoCambioCierreForward" DECIMAL(26,8)
,"ParidadCierre" DECIMAL(26,8)
,"ParidadCierreForward" DECIMAL(26,8)
,"TipoCambioCosto" DECIMAL(26,8)
,"ParidadCosto" DECIMAL(26,8)
,"MontoEquivalente" DECIMAL(26,8)
,"MontoRentabilidad" DECIMAL(26,8)
,"MontoRentabilidadCanal" DECIMAL(26,8)
,"ResultadoMonedaLocalPuntas" DECIMAL(26,8)
,"ResultadoMonedaLocalBidAsk" DECIMAL(26,8)
,"ResultadoMonedaLocalDO" DECIMAL(26,8)
,"ResultadoMonedaLocalTCSpot" DECIMAL(26,8)
,"ResultadoMonedaLocalTCCierreMesa" DECIMAL(26,8)
,"ResultadoMonedaLocalConsolidado" DECIMAL(26,8)
,"Margen" DECIMAL(26,8)
,"MargenBidAsk" DECIMAL(26,8)
,"Utilidad" DECIMAL(26,8)
,"PuntaCompra" DECIMAL(26,8)
,"PuntaVenta" DECIMAL(26,8)
,"PuntaBid" DECIMAL(26,8)
,"PuntaAsk" DECIMAL(26,8)
,"ParidadBid" DECIMAL(26,8)
,"ParidadAsk" DECIMAL(26,8)
,"ValorDolarObservado" DECIMAL(26,8)
,"ValorTCSpot" DECIMAL(26,8)
,"SolicitudTipoCambio" NVARCHAR(1)
,"IdEvento" BIGINT
,"IdMesa" BIGINT
,"IdModulo" NVARCHAR(3)
,"IdTipoDolar" BIGINT
,"TipoComprobanteFacturacion" NVARCHAR(3)
,"IdComprobanteFacturacion" BIGINT
,"IdComprobanteTesoreriaIngreso" BIGINT
,"IdComprobanteTesoreriaEgreso" BIGINT
,"IdAgente" BIGINT
,"BenOur" NVARCHAR(3)
,"EquivalenteConGasto" DECIMAL(26,8)
,"TipoCambioCierreConGasto" DECIMAL(26,8)
,"Observacion" NVARCHAR(520)
,"CodigoComercio" BIGINT
,"FolioPlanilla"  BIGINT
,"FolioInterfaz" NVARCHAR(50)
,"OperacionFicticia" NVARCHAR(1)
,"IdTipoCanalIngreso" NVARCHAR(3)
,"IdDatatec" NVARCHAR(40)
,"Estado" NVARCHAR(3)
,"IdUsuario" BIGINT
,"IdUsuarioModifica" BIGINT
,"FechaModifica" SECONDDATE
,"FechaReproceso" SECONDDATE
-- campos sebra
,"FolioTransaccion" BIGINT
,"FolioOperacion" BIGINT
,"RutCliente" BIGINT
,"CuentaCliente" BIGINT
,"CodProducto" BIGINT
,"CodSubproducto" BIGINT
,"CodTipoMovimiento" BIGINT
,"CodMonPrincipal" BIGINT
,"CodMonSecu" BIGINT
,"UsuarioCreador" BIGINT
,"IdCarga" BIGINT
,primary key ("IdOperacionSpot")
);

create table "BSCL"."BSCL.Homologacion.BD::HOM.IngresoEgreso"
(
 "IdIngresoEgreso" BIGINT
,"IdEmpresa" BIGINT
,"IdModulo" NVARCHAR(3)
,"IdUsuario" BIGINT
,"IdTipoMovimiento" NVARCHAR(6)
,"IdConcepto" NVARCHAR(6)
,"IdCaja" BIGINT
,"IdMoneda" NVARCHAR(3)
,"Monto" DECIMAL(26)
,"FechaLiquidacion" DATE
,"FechaIngresoRegistro" TIMESTAMP
,"FechaMovimiento" DATE
,"IdTipoPersona" NVARCHAR(3)
,"IdPersona" BIGINT
,"NombrePersona" NVARCHAR(300)
,"IdTipoDocumentoIdentidad" NVARCHAR(1)
,"DocumentoIdentidad" NVARCHAR(40)
,"IdTipoPersonaTitularPago" NVARCHAR(3)
,"IdPersonaTitularPago" BIGINT
,"NombrePersonaTitularPago" NVARCHAR(300)
,"IdPortafolioTitularPago" BIGINT
,"SecuenciaAgrupacion" NVARCHAR(40)
,"Glosa" NVARCHAR(260)
,"IdEmpleadoDigitador" BIGINT
,"IdSucursalEmpleadoDigitador" BIGINT
,"IdTipoComprobanteOrigen" NVARCHAR(3)
,"IdComprobanteOrigen" BIGINT
,"FechaComprobanteOrigen" TIMESTAMP
,"FechaLiquidacionComprobanteOrigen" SECONDDATE
,"IdTipoProducto" NVARCHAR(4)
,"IdTipoOperacion" NVARCHAR(6)
,"IdOperacion" BIGINT
,"FechaOperacion" SECONDDATE
,"IdCondicionLiquidacion" NVARCHAR(2)
,"IdBolsa" NVARCHAR(3)
,"IdMonedaOperacion" NVARCHAR(3)
,"MontoOperacion" DECIMAL(26)
,"FechaParidad" SECONDDATE
,"IdTipoReajuste" NVARCHAR(3)
,"ValorParidad" DECIMAL(26)
,"FolioCartera" BIGINT
,"IdInstrumento" BIGINT
,"IdTipoInstrumento" NVARCHAR(3)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"EsBilateral" NVARCHAR(1)
,"TitularPago" NVARCHAR(1)
,"EsIngresoAutomatico" NVARCHAR(1)
,"AgrupaComprobante" NVARCHAR(1)
,"IdUsuarioBloqueo" BIGINT
,"FechaHoraBloqueo" SECONDDATE
,"IdCarga" BIGINT
,"IdEstado" NVARCHAR(3)
,"MueveCuentaInversion" NVARCHAR(1)
,"SecuenciaRelacionada" BIGINT
,"NeteaComprobante" NVARCHAR(1)
,"IdUsuarioModificacion" BIGINT
,"FechaHoraModificacion" SECONDDATE
,"SEC_MOVTO" BIGINT
,"FEC_MOVTO" NVARCHAR(10)
,"FOLIO_COMP_ADJ" BIGINT
,"TIPO_COMP_ADJ" NVARCHAR(2)
,"SEC_DOCUM" BIGINT
,"FOLIO_CMBTE" BIGINT
,"FEC_CMBTE" NVARCHAR(10)
,"TIPO_DOCUM" NVARCHAR(2)
,"FOLIO_CMBTE_ING" BIGINT
,"FOLIO_CMBTE_EGR" BIGINT
,"SEC_MOVTO_CARGO" BIGINT
,"SEC_MOVTO_ABONO" BIGINT
,"TIPO_CMBTE" NVARCHAR(2)
,primary key ("IdIngresoEgreso")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalleTesoreria"
(
"IdComprobanteDetalle" BIGINT
,"IdComprobante" BIGINT
,"IdConcepto" NVARCHAR(6)
,"NroLinea" INTEGER
,"IdMoneda" NVARCHAR(3)
,"Monto" DECIMAL(26,8)
,"SecuenciaAgrupacion" NVARCHAR(40)
,"IdEmpleadoDigitador" BIGINT
,"IdSucursalEmpleadoDigitador" BIGINT
,"IdTipoComprobanteOrigen" NVARCHAR(3)
,"IdComprobanteOrigen" BIGINT
,"FechaComprobanteOrigen" TIMESTAMP
,"FechaLiquidacionComprobanteOrigen" SECONDDATE
,"IdTipoProducto" NVARCHAR(4)
,"IdTipoOperacion" NVARCHAR(6)
,"IdBolsa" NVARCHAR(3)
,"IdMonedaOperacion" NVARCHAR(3)
,"MontoOperacion" DECIMAL(26,8)
,"FechaParidad" SECONDDATE
,"IdTipoReajuste" NVARCHAR(3)
,"ValorParidad" DECIMAL(26,8)
,"FolioCartera" BIGINT
,"IdInstrumento" BIGINT
,"IdTipoInstrumento" NVARCHAR(3)
,"IdClasificacionTipoInstrumento" NVARCHAR(3)
,"IdCarga" BIGINT
,"IdEstado" NVARCHAR(3)
,"IdTipoMovimiento" NVARCHAR(6)
,"SecuenciaRelacionada" BIGINT
,"SEC_DOCUM" BIGINT
,PRIMARY KEY ("IdComprobanteDetalle")
); 

CREATE TABLE "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" 
(
"IdComprobante" BIGINT
,"IdEmpresa" BIGINT
,"IdUsuario" BIGINT
,"TipoComprobante" NVARCHAR(6)
,"IdTipoMovimiento" NVARCHAR(6)
,"IdConcepto" NVARCHAR(6)
,"IdCaja" BIGINT
,"IdMoneda" NVARCHAR(3)
,"Monto" DECIMAL(26,8)
,"FechaLiquidacion" DATE
,"FechaIngresoRegistro" TIMESTAMP
,"FechaMovimiento" DATE
,"IdTipoPersona" NVARCHAR(3)
,"IdPersona" BIGINT
,"NombrePersona" NVARCHAR(300)
,"IdTipoDocumentoIdentidad" NVARCHAR(1)
,"DocumentoIdentidad" NVARCHAR(40)
,"TitularPago" NVARCHAR(1)
,"IdTipoPersonaTitularPago" NVARCHAR(3)
,"IdPersonaTitularPago" BIGINT
,"NombrePersonaTitularPago" NVARCHAR(300)
,"IdPortafolioTitularPago" BIGINT
,"SecuenciaAgrupacion" NVARCHAR(40)
,"EsBilateral" NVARCHAR(1)
,"IdUsuarioBloqueo" BIGINT
,"FechaHoraBloqueo" TIMESTAMP
,"IdCarga" BIGINT
,"IdEstado" NVARCHAR(3)
,"GeneradoDesdeMovimientos" NVARCHAR(1)
,"IdEvento" BIGINT
,"MueveCuentaInversion" NVARCHAR(1)
,"ComprobanteNeteado" NVARCHAR(1)
,"ComprobanteAgrupado" NVARCHAR(1)
,"SeparadoPorConcepto" NVARCHAR(1)
,"SEC_DOCUM" BIGINT 
,PRIMARY KEY ("IdComprobante")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
(
"IdSolicitudAporteRescate" BIGINT
,"IdCliente" BIGINT
,"IdPortafolio" BIGINT
,"TipoSolicitud" NVARCHAR(3)
,"IdSerieFondo" NVARCHAR(40)
,"IdTipoMovimientoFondoMutuo" NVARCHAR(10)
,"IdDividendo" BIGINT
,"IdSerieFondoDestino" NVARCHAR(40)
,"IdEmisorDestino" NVARCHAR(40)
,"IdClienteDestino" BIGINT
,"IdPortafolioDestino" BIGINT
,"IdSerieFondoOrigen" NVARCHAR(40)
,"IdEmisorOrigen" NVARCHAR(40)
,"MontoOrigen" DECIMAL(26,8)
,"IndicadorMLT" NVARCHAR(1)
,"Overnight" NVARCHAR(1)
,"IdSolicitudAporteRescateRelacionado" BIGINT
,"IdTipoCartera" NVARCHAR(3)
,"CalculoPorMontoCuota" NVARCHAR(1)
,"FechaEmision" DATE
,"FechaAceptacion" DATE
,"FechaCertificacion" DATE
,"FechaLiquidacion" DATE
,"FechaAnulacion" DATE
,"FechaOriginalInversion" DATE
,"FechaIngresoRegistro" TIMESTAMP
,"FechaAnulacionRegistro" TIMESTAMP
,"FechaAceptacionRegistro" TIMESTAMP
,"FechaCertificacionRegistro" TIMESTAMP
,"FechaLiquidacionRegistro" TIMESTAMP
,"Monto" DECIMAL(26,8)
,"NumeroCuotas" DECIMAL(26,8)
,"ValorCuota" DECIMAL(26,8)
,"CostoUF" DECIMAL(26,8)
,"CostoMonedaLocal" DECIMAL(26,8)
,"ValorUFFechaInversion" DECIMAL(26,8)
,"ParidadFechaInversion" DECIMAL(26,8)
,"IdMoneda" NVARCHAR(3)
,"MontoComision" DECIMAL(26,8)
,"MontoPago" DECIMAL(26,8)
,"Estado" NVARCHAR(3)
,"Glosa" NVARCHAR(100)
,"IdEmpresa" BIGINT
,"IdUsuario" BIGINT
,"IdModulo" NVARCHAR(3)
,"EstadoLiquidacion" NVARCHAR(3)
,"MueveCta" NVARCHAR(1)
,"FolioCartaLiquidacion" BIGINT
,"IdMedioDePago" NVARCHAR(3)
,"MontoInversion" DECIMAL(26,8)
,"NumeroCuotasInversion" DECIMAL(26,8)
,"ValorCuotaInversion" DECIMAL(26,8)
,"CostoMonedaLocalInversion" DECIMAL(26,8)
,"ParidadFechaCertificacion" DECIMAL(26,8)
,"MontoComisionLocal" DECIMAL(26,8)
,"MontoPagoLocal" DECIMAL(26,8)
,"FechaModificacion" DATE
,"FechaModificacionRegistro" TIMESTAMP
,"OrigenOperacion" NVARCHAR(1)
,"SolicitudBatch" NVARCHAR(1)
,"ComisionInformada" NVARCHAR(1)
,"FEC_MVTO" NVARCHAR(10)
,"SEC_MOVTO" BIGINT
,"SEC_ID" BIGINT
,"COD_MOVTO" NVARCHAR(8)
,"RUT_CLI" NVARCHAR(11)
,"SEC_RUT_CLI" BIGINT
,"NEMO" NVARCHAR(50)
,"IdCarga" BIGINT
,"IND_BOLSA" NVARCHAR(3)
,"TIPO_COMP_ADJ" NVARCHAR(10)
,PRIMARY KEY ("IdSolicitudAporteRescate")
);



/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/
create index "Homologacion" on "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion"("TipoValor","Entidad","ValorOrigen");



/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
