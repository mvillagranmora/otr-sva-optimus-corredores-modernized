SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" cascade;
drop table "BSCL.Facturacion.BD::FAC.EstadoFacturacion" cascade;
drop table "BSCL.Facturacion.BD::FAC.PermisoConceptoMovimientoComprobante" cascade;
drop table "BSCL.Facturacion.BD::FAC.TipoComprobante" cascade;
drop table "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento" cascade;
drop table "BSCL.Facturacion.BD::FAC.TipoConvenio" cascade;
drop table "BSCL.Facturacion.BD::FAC.TipoTransaccion" cascade;
drop table "BSCL.Facturacion.BD::FAC.MovimientoComprobante" cascade;
drop table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" cascade;
drop table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" cascade;
drop table "BSCL.Facturacion.BD::FAC.Comprobante" cascade;
drop table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" cascade;
drop table "BSCL.Facturacion.BD::FAC.ComprobanteCobro" cascade;
drop table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cascade;
drop table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" cascade;
drop table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" cascade;
drop table "BSCL.Facturacion.BD::FAC.FolioComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::FAC.NotificacionResumenFacturacionTemporal" CASCADE;
DROP TABLE "BSCL.Facturacion.BD::CAR.TipoCargaMasiva" 		CASCADE;
DROP TABLE "BSCL.Facturacion.BD::CAR.CargaMasiva" 			CASCADE;
DROP TABLE "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" 	CASCADE;
DROP TABLE "BSCL.Facturacion.BD::FAC.TemplateFacturacion"   CASCADE;

DROP TABLE "BSCL.Facturacion.BD::FAC.PeriodoCierre"         CASCADE;  

/*==============================================================*/
/* Create Tables:                                               */
/*==============================================================*/
create column table "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" (
   "IdEmpresa"       BIGINT NOT NULL,
   "CodigoParametro" NVARCHAR(40) NOT NULL,
   "Descripcion"     NVARCHAR(520) NOT NULL,
   "Valor"           NVARCHAR(520) NOT NULL, 
 primary key ("IdEmpresa", "CodigoParametro")
);


create column table "BSCL.Facturacion.BD::FAC.EstadoFacturacion" (
   "IdEstadoFacturacion" BIGINT NOT NULL,
   "IdEstado"            NVARCHAR(3),
   "NombreEntidad"       NVARCHAR(100) NOT NULL, 
 primary key ("IdEstadoFacturacion")
);

create column table "BSCL.Facturacion.BD::FAC.PermisoConceptoMovimientoComprobante" (
   "IdPermisoConceptoMovimientoComprobante" BIGINT NOT NULL,
   "IdConcepto"                             NVARCHAR(6) NOT NULL,
   "EsMovimientoComprobante"                NVARCHAR(1) DEFAULT 'N',
   "EsCobros"                               NVARCHAR(1) DEFAULT 'N',
   "EsFormaDePago"                          NVARCHAR(1) DEFAULT 'N', 
 primary key ("IdPermisoConceptoMovimientoComprobante")
);

create column table "BSCL.Facturacion.BD::FAC.TipoComprobante"  (
	"IdTipoComprobante" 			NVARCHAR(3) NOT NULL,
	"Codigo" 						NVARCHAR(3) NOT NULL,
	"Nombre" 						NVARCHAR(100) NOT NULL,
	"IdEstadoFacturacion" 			NVARCHAR(3) NOT NULL,
	"FechaIngresoRegistro" 			TIMESTAMP NOT NULL,
	"TipoComprobanteFacturacion"	NVARCHAR(3),
	"IdFolioComprobante" 			NVARCHAR(6) NOT NULL,
	"TipoComprobanteDTE" 			BIGINT NOT NULL DEFAULT 0,
	"AfectoImpuesto"				NVARCHAR(1) NOT NULL,
 primary key ("IdTipoComprobante")
);

create column table "BSCL.Facturacion.BD::FAC.TipoPagoCobroMovimiento"  (
   "IdTipoPagoCobroMovimiento" NVARCHAR(3) NOT NULL,
   "Codigo"                    NVARCHAR(3) NOT NULL,
   "Nombre"                    NVARCHAR(100) NOT NULL,
   "IdEstadoFacturacion"       NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL,
 primary key ("IdTipoPagoCobroMovimiento")
);

create column table "BSCL.Facturacion.BD::FAC.TipoConvenio" (
   "IdTipoConvenio"       NVARCHAR(3) NOT NULL,
   "Codigo"               NVARCHAR(3) NOT NULL,
   "Nombre"               NVARCHAR(100) NOT NULL,
   "IdEstadoFacturacion"  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro" TIMESTAMP NOT NULL, 
 primary key ("IdTipoConvenio")
);

create column table "BSCL.Facturacion.BD::FAC.TipoTransaccion" (
   "IdTipoTransaccion"   NVARCHAR(3) NOT NULL,
   "Codigo"              NVARCHAR(3) NOT NULL,
   "Nombre"              NVARCHAR(100) NOT NULL,
   "IdEstadoFacturacion" NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro" TIMESTAMP NOT NULL,
 primary key ("IdTipoTransaccion")
);

create column table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" (
   	"IdConceptoFacturacion"  	BIGINT 			not null,
   	"IdConcepto" 				NVARCHAR(6)		default '' 	not null,
   	"NombreEntidad" 			NVARCHAR(100)	default '' 	not null,
   	"IdTipoProducto"            NVARCHAR(4)		default '',
	"IdTipoOperacion"           NVARCHAR(6)		default '',
	"CodigoPreferencia"         NVARCHAR(40)	default '',
 PRIMARY KEY ("IdConceptoFacturacion")
);


CREATE TABLE BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" (
	"IdMovimientoComprobante" 							BIGINT NOT NULL,
	"IdEmpresa" 										BIGINT NOT NULL,
	"IdConceptoMovimiento" 								NVARCHAR(6) NOT NULL,
	"SecuenciaAgrupacion" 								NVARCHAR(40),
	"IdCliente" 										BIGINT NOT NULL,
	"IdPortafolio" 										BIGINT DEFAULT 0,
	"IdInstrumento" 									BIGINT DEFAULT 0,
	"IdInstrumentoSubyacente" 							BIGINT DEFAULT 0,
	"IdTipoInstrumento" 								NVARCHAR(3) DEFAULT '0',
	"IdTipoInstrumentoSubyacente" 						NVARCHAR(3) DEFAULT '0',
	"IdClasificacionTipoInstrumento" 					NVARCHAR(3) DEFAULT '0',
	"IdTipoProducto" 									NVARCHAR(4),
	"IdCuentaInversion" 								BIGINT DEFAULT 0,
	"IdMoneda" 											NVARCHAR(3) NOT NULL,
	"IdTipoComprobante" 								NVARCHAR(3) DEFAULT '',
	"IdComprobante" 									BIGINT DEFAULT 0,
	"IdTipoOperacion" 									NVARCHAR(6),
	"IdBoveda" 											NVARCHAR(3) DEFAULT '0',
	"IdCorredoraContraparte"							BIGINT DEFAULT 0,
	"IdCondicionLiquidacion" 							NVARCHAR(2) NOT NULL,
	"FechaLiquidacion" 									DATE DEFAULT '0001-01-01',
	"IdBolsa" 											NVARCHAR(3) DEFAULT '0',
	"IdTipoReajuste" 									NVARCHAR(3) NOT NULL,
	"IdTipoConvenio" 									NVARCHAR(3) NOT NULL,
	"IdEstadoFacturacion" 								NVARCHAR(3) NOT NULL,
	"IdOrden" 											BIGINT DEFAULT 0,
	"IdAsignacion" 										BIGINT DEFAULT 0,
	"IdTipoCanalIngreso" 								NVARCHAR(3),
	"FechaOrden" 										TIMESTAMP DEFAULT '0001-01-01',
	"IdTransaccion" 									BIGINT DEFAULT 0,
	"FolioTransaccion" 									NVARCHAR(40),
	"FechaTransaccion" 									TIMESTAMP DEFAULT '0001-01-01',
	"FechaVencimiento" 									TIMESTAMP DEFAULT '0001-01-01',
	"FechaParidad" 										TIMESTAMP DEFAULT '0001-01-01',
	"ValorParidad" 										DECIMAL(26,8) DEFAULT 0,
	"ValorCaptacionMonedaOrigen" 						DECIMAL(26,8) DEFAULT 0,
	"FolioComprobante" 									BIGINT DEFAULT 0,
	"Tasa" 												DECIMAL(26,8) DEFAULT 0.0,
	"Cantidad" 											DECIMAL(26,8) DEFAULT 0,
	"Precio" 											DECIMAL(26,8) DEFAULT 0,
	"Monto" 											DECIMAL(26,8) NOT NULL,
	"MontoDescuento" 									DECIMAL(26,8) DEFAULT 0,
	"MontoInteres" 										DECIMAL(26,8) DEFAULT 0,
	"Glosa" 											NVARCHAR(260),
	"ValorRescate" 										DECIMAL(26,8) DEFAULT 0,
	"TasaAgente" 										DECIMAL(26,8) DEFAULT 0.0,
	"Spread" 											DECIMAL(26,8) DEFAULT 0,
	"DiasPlazo" 										DECIMAL(26,8) DEFAULT 0,
	"CodigoCanalIngreso" 								NVARCHAR(40),
	"DocumentoIdentidad" 								NVARCHAR(40),
	"TipoDocumentoIdentidad" 							NVARCHAR(1),
	"NombreLargoCliente" 								NVARCHAR(300),
	"DireccionFacturacion" 								NVARCHAR(100),
	"FolioCartera" 										BIGINT DEFAULT 0,
	"IdMesa" 											BIGINT DEFAULT 0,
	"CodigoTipoProducto" 								NVARCHAR(40),
	"CodigoTipoOperacion" 								NVARCHAR(40),
	"CodigoBolsa" 										NVARCHAR(40),
	"NombreTipoInstrumento" 							NVARCHAR(100),
	"NombreTipoInstrumentoSubyacente" 					NVARCHAR(100),
	"NombreClasificacionTipoInstrumento" 				NVARCHAR(100),
	"NemotecnicoInstrumento" 							NVARCHAR(40),
	"NemotecnicoSubyacente" 							NVARCHAR(40),
	"RazonSocialEmisor" 								NVARCHAR(100),
	"FechaEmision" 										TIMESTAMP DEFAULT '0001-01-01',
	"TasaEmision" 										DECIMAL(26,8) DEFAULT 0.0,
	"FechaIngresoRegistro" 								TIMESTAMP NOT NULL,
	"FechaSistema" 										TIMESTAMP NOT NULL,
	"EsComprobIndividual" 								NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsCuentaPropia" 									NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IndicadorOrdenDirecta"								NVARCHAR(2) NOT NULL DEFAULT 'N',
	"EsPrecioPrima" 									NVARCHAR(1) DEFAULT 'N',
	"EsEnRueda" 										NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsRuteo" 											NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsIngresoAutomatico" 								NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IdUsuario" 										BIGINT NOT NULL,
	"IdUsuarioBloqueo" 									BIGINT DEFAULT 0,
	"IdCarga" 											BIGINT DEFAULT 0,
	"EsAPlazo" 											NVARCHAR(1) NOT NULL DEFAULT 'N',
	"MontoAPlazo" 										DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PrecioAPlazo" 										DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdMonedaSubyacente" 								NVARCHAR(3),
	"IdMonedaLiquidacion" 								NVARCHAR(3),
	"IdMonedaSpot" 										NVARCHAR(3) DEFAULT ''
	"ValorTipoCambio" 									DECIMAL(26,8) DEFAULT 0,
	"EsVentaCorta" 										NVARCHAR(1) NOT NULL DEFAULT 'N',
	"TasaOperacion" 									DECIMAL(26,8) DEFAULT 0,
	"SecMovimientoFacturarMigracion" 					BIGINT DEFAULT 0,
	"ActualizarCustodia" 								NVARCHAR(1) NOT NULL DEFAULT 'S',
	"FechaMovimientoComprobante" 						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaTipoCambio" 									DATE DEFAULT '0001-01-01',
	"ActualizarCustodiaRelacionado" 					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IdMonedaMovimientoComprobante" 					NVARCHAR(3),
	"MontoMovimientoComprobante" 						DECIMAL(26,8) DEFAULT 0,
	"MontoLiquidacion" 									DECIMAL(26,8) DEFAULT 0,
	"IdEmisor" 											NVARCHAR(40),
	"EsFungible" 										NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IdFormaCustodia" 									NVARCHAR(3),
	"ActualizarCuentaInversion" 						NVARCHAR(1) DEFAULT 'S',
	"ActualizarTesoreria" 								NVARCHAR(1) DEFAULT 'S',
	"IdInstrumentoRelacionado" 							BIGINT DEFAULT 0,
	"IdTipoInstrumentoRelacionado" 						NVARCHAR(3),
	"IdClasificacionTipoInstrumentoRelacionado" 		NVARCHAR(3),
	"NombreTipoInstrumentoRelacionado" 					NVARCHAR(100),
	"NombreClasificacionTipoInstrumentoRelacionado"		NVARCHAR(100),
	"NemotecnicoRelacionado" 							NVARCHAR(40),
	"IdAsignacionRef" 									BIGINT DEFAULT 0,
	"IdEmpleadoDigitador" 								BIGINT DEFAULT 0,
	"IdEmpleadoEjecutivoPortafolio" 					BIGINT DEFAULT 0,
	"IdSucursalEmpleadoEjecutivoPortafolio" 			BIGINT DEFAULT 0,
	"ModPrefFact" 										BIGINT NOT NULL DEFAULT 1,
	"IdSucursalEmpleadoDigitador" 						BIGINT DEFAULT 0,
	"NombreEmpleadoDigitador" 							NVARCHAR(100),
	"NombreSucursalEmpleadoDigitador" 					NVARCHAR(100),
	"NombreEmpleadoEjecutivoPortafolio" 				NVARCHAR(100),
	"NombreSucursalEmpleadoEjecutivoPortafolio" 		NVARCHAR(100),
	"IdComprobanteReferencia" 							BIGINT DEFAULT 0,
	"FolioComprobanteReferencia" 						BIGINT DEFAULT 0,
	"IdMovimientoRelacionado" 							BIGINT DEFAULT 0,
	"IdMovimientoOdRelacionado" 						BIGINT DEFAULT 0,
	"FechaComprobanteReferencia" 						DATE DEFAULT '0001-01-01',
	"CodigoIFRS" 										NVARCHAR(40),
	"IdTipoComprobanteReferencia" 						NVARCHAR(3),
	"IdInstrumentoNuevo" 								BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY ("IdMovimientoComprobante")
);

create column table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" (
   "IdMovimientoComprobanteFormaDePago"   BIGINT NOT NULL, 
   "IdMovimientoComprobante"   BIGINT NOT NULL,
   "IdTipoMovimiento"          NVARCHAR(6) NOT NULL,
   "IdMedioDePago"             NVARCHAR(3) NOT NULL,
   "NombreMedioDePago"         NVARCHAR(100) DEFAULT '',
   "IdCuentaBancaria"          BIGINT DEFAULT 0,
   "NombreCuentaBancaria"      NVARCHAR(100) DEFAULT '',
   "IdTipoCuentaBancaria"      NVARCHAR(4) DEFAULT '',
   "NombreTipoCuentaBancaria"  NVARCHAR(100) DEFAULT '',
   "NumeroCuentaBancaria"      NVARCHAR(100) DEFAULT '',
   "IdTarjetaBancaria"         BIGINT DEFAULT 0,   
   "IdTipoTarjetaBancaria"     NVARCHAR(40) DEFAULT '',
   "NombreTipoTarjetaBancaria" NVARCHAR(100) DEFAULT '',
   "NumeroTarjetaBancaria"     NVARCHAR(100) DEFAULT '',
   "FechaVctoTarjetaBancaria"  DATE DEFAULT '0001-01-01',
   "NumeroSerie"               NVARCHAR(40) DEFAULT '',
   "NumeroDocumento"           BIGINT DEFAULT 0,
   "FechaDocumentoPago"        DATE DEFAULT '0001-01-01',
   "IdModalidadDePago"         NVARCHAR(6) DEFAULT '', 
   "NombreModalidadDePago"     NVARCHAR(100) DEFAULT '', 
   "IdBanco"                   BIGINT DEFAULT 0,
   "CodigoBanco"               NVARCHAR(40) DEFAULT '',
   "NombreBanco"               NVARCHAR(100) DEFAULT '',   
   "IdMoneda"                  NVARCHAR(3) DEFAULT '',
   "MontoLiquidacionFormaDePago" DECIMAL(26,8) NOT NULL,
   "IdEstado"                  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL,
   "AfectarTesoreria"          NVARCHAR(1) DEFAULT 'S'
 primary key ("IdMovimientoComprobanteFormaDePago")
);


create column table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" (
   "IdMovimientoComprobanteCobro" BIGINT NOT NULL,
   "IdCobro"                      NVARCHAR(6) NOT NULL,
   "IdMovimientoComprobante"      BIGINT NOT NULL,
   "IdMoneda"                     NVARCHAR(3) DEFAULT '',
   "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3) NOT NULL,
   "IdMonedaLiquidacion"          NVARCHAR(3) DEFAULT '',
   "Monto"                        DECIMAL(26,8) DEFAULT 0,
   "MontoMovimientoComprobanteCobro" DECIMAL(26,8) NOT NULL,
   "MontoLiquidacion"             DECIMAL(26,8) DEFAULT 0,
   "Porcentaje"                   DECIMAL(26,8) DEFAULT 0.0,
   "FechaSistema"                 DATE NOT NULL,
   "FechaIngresoRegistro"         TIMESTAMP NOT NULL,
   "IdEstado"                     NVARCHAR(3) NOT NULL, 
 primary key ("IdMovimientoComprobanteCobro")
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.Comprobante" (
	"IdComprobante" 							BIGINT NOT NULL,
	"IdEmpresa" 								BIGINT NOT NULL,
	"IdPortafolio" 								BIGINT DEFAULT 0,
	"IdCliente" 								BIGINT NOT NULL,
	"DocumentoIdentidad" 						NVARCHAR(40),
	"TipoDocumentoIdentidad" 					NVARCHAR(1),
	"NombreLargoCliente" 						NVARCHAR(300),
	"DireccionFacturacion" 						NVARCHAR(100),
	"FolioComprobante" 							BIGINT NOT NULL,
	"IdTipoComprobante" 						NVARCHAR(3) NOT NULL,
	"IdTipoOperacion" 							NVARCHAR(6),
	"IdTipoProducto" 							NVARCHAR(4),
	"IdClasificacionTipoInstrumento" 			NVARCHAR(3),
	"IdTipoInstrumento"							NVARCHAR(3),
	"Monto" 									DECIMAL(0,8) DEFAULT 0,
	"MontoCobro" 								DECIMAL(0,8) DEFAULT 0,
	"MontoFinal" 								DECIMAL(0,8) DEFAULT 0,
	"MontoPagado" 								DECIMAL(0,8) DEFAULT 0,
	"IdCondicionLiquidacion" 					NVARCHAR(2) DEFAULT T0,
	"FechaLiquidacion" 							DATE NOT NULL,
	"FechaEmision" 								TIMESTAMP NOT NULL,
	"FechaEnvio" 								SECONDDATE DEFAULT '0001-01-01',
	"FechaRespuestaEnvio" 						SECONDDATE DEFAULT '0001-01-01',
	"IdEstadoComprobante" 						NVARCHAR(3) NOT NULL,
	"Glosa" 									NVARCHAR(260),
	"EsExento" 									NVARCHAR(1) DEFAULT N,
	"FechaIngresoRegistro" 						TIMESTAMP NOT NULL,
	"FechaSistema" 								DATE NOT NULL,
	"IdCarga" 									BIGINT NOT NULL DEFAULT 0,
	"FolioComprobanteFacturacionMigracion"		BIGINT NOT NULL DEFAULT 0,
	"IdMoneda" 									NVARCHAR(3),
	"IdMonedaLiquidacion" 						NVARCHAR(3),
	"MontoLiquidacion" 							DECIMAL(0,8) DEFAULT 0,
	"IdMonedaComprobante" 						NVARCHAR(3),
	"MontoComprobante" 							DECIMAL(0,8) DEFAULT 0,
	"ModPrefFact" 								BIGINT NOT NULL DEFAULT 1,
	"IdConcepto" 								NVARCHAR(6) NOT NULL,
	"IdEmpleadoEjecutivoPortafolio"				BIGINT DEFAULT 0,
	"IdSucursalEmpleadoEjecutivoPortafolio"		BIGINT DEFAULT 0,
	"IdBolsa" 									NVARCHAR(3),
	"EsVentaCorta"								NVARCHAR(1) DEFAULT 'N',
	"IdFormaCustodia" 							NVARCHAR(3),
	"IdComprobanteReferencia" 					BIGINT DEFAULT 0,
	"FolioComprobanteReferencia" 				BIGINT DEFAULT 0,
	"FechaComprobanteReferencia" 				DATE DEFAULT '0001-01-01',
	"IdTipoComprobanteReferencia" 				NVARCHAR(3),
	"Cantidad" 									DECIMAL(0,8) DEFAULT 0,
	"FechaComprobante" 							SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstadoDTE" 								NVARCHAR(3),
	"IdEstadoSII" 								NVARCHAR(3),
	"IdRespuestaDTE" 							BIGINT DEFAULT 0,
	"RespuestaLocationDTE" 						NVARCHAR(40),
	"RespuestaPdfDTE" 							NVARCHAR(250),
	"RespuestaXmlDTE" 							NVARCHAR(250),
	"RespuestaDTEEnvioSII" 						NVARCHAR(5000),
	"NombreEmpleadoEjecutivoPortafolio" 		NVARCHAR(100),
	"NombreSucursalEmpleadoEjecutivoPortafolio" NVARCHAR(100),
	"MontoComprobanteFinal" 					DECIMAL(0,8) DEFAULT 0,
	"MontoLiquidacionFinal" 					DECIMAL(0,8) DEFAULT 0,
	"MontoComprobanteCobro" 					DECIMAL(0,8) DEFAULT 0,
	"MontoLiquidacionCobro" 					DECIMAL(0,8) DEFAULT 0,
	PRIMARY KEY (IdComprobante)
);

create column table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" (
   "IdComprobanteFormaDePago"  BIGINT NOT NULL,
   "IdComprobante"             BIGINT NOT NULL,
   "IdMedioDePago"             NVARCHAR(3) NOT NULL,
   "NombreMedioDePago"         NVARCHAR(100) DEFAULT '',
   "NumeroSerie"               NVARCHAR(40) DEFAULT '',
   "NumeroDocumento"           BIGINT DEFAULT 0,
   "FechaDocumentoPago"        DATE DEFAULT '0001-01-01',
   "IdCuentaBancaria"          BIGINT DEFAULT 0,
   "NombreCuentaBancaria"      NVARCHAR(100) DEFAULT '',
   "IdTipoCuentaBancaria"      NVARCHAR(4) DEFAULT '',
   "NombreTipoCuentaBancaria"  NVARCHAR(100) DEFAULT '',
   "NumeroCuentaBancaria"      NVARCHAR(100) DEFAULT '',
   "IdTarjetaBancaria"         BIGINT DEFAULT 0,
   "IdTipoTarjetaBancaria"     NVARCHAR(40) DEFAULT '',
   "NombreTipoTarjetaBancaria" NVARCHAR(100) DEFAULT '',
   "NumeroTarjetaBancaria"     NVARCHAR(100) DEFAULT '',
   "FechaVctoTarjetaBancaria"  DATE  DEFAULT '0001-01-01',
   "IdModalidadDePago"         NVARCHAR(6) DEFAULT '',
   "NombreModalidadDePago"     NVARCHAR(100) DEFAULT '',
   "IdBanco"                   BIGINT DEFAULT 0,
   "CodigoBanco"               NVARCHAR(40) DEFAULT '',
   "NombreBanco"               NVARCHAR(100) DEFAULT '',
   "IdMoneda"                  NVARCHAR(3) DEFAULT '',
   "MontoLiquidacionFormaDePago" DECIMAL(26,8) NOT NULL,
   "IdEstado"                  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL,
 primary key ("IdComprobanteFormaDePago")
);

create column table "BSCL.Facturacion.BD::FAC.ComprobanteCobro" (
   "IdComprobanteCobro" BIGINT NOT NULL,
   "IdCobro"            NVARCHAR(6) NOT NULL,
   "IdComprobante"      BIGINT NOT NULL,
   "IdMoneda"           NVARCHAR(3) DEFAULT '',
   "IdMonedaComprobanteCobro" NVARCHAR(3) NOT NULL,
   "IdMonedaLiquidacion" NVARCHAR(3) DEFAULT '',   
   "Monto"               DECIMAL(26,8) NOT NULL,
   "MontoComprobanteCobro" DECIMAL(26,8) NOT NULL,
   "MontoLiquidacion"   DECIMAL(26,8) DEFAULT 0,
   "Porcentaje"         DECIMAL(26,8) DEFAULT 0.0,
   "FechaSistema"       DATE NOT NULL,
   "FechaIngresoRegistro" TIMESTAMP NOT NULL,
   "IdEstado"           NVARCHAR(3) NOT NULL,
   "IdCarga"            BIGINT NOT NULL DEFAULT 0,
 primary key ("IdComprobanteCobro")
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" (
	"IdComprobanteDetalle" 								BIGINT NOT NULL,
	"IdComprobante" 									BIGINT NOT NULL,
	"NroLinea" 											INTEGER NOT NULL,
	"FechaLiquidacion" 									DATE DEFAULT '0001-01-01',
	"IdInstrumento" 									BIGINT DEFAULT 0,
	"NemotecnicoInstrumento" 							NVARCHAR(100),
	"IdTipoOperacion" 									NVARCHAR(6),
	"IdTipoProducto" 									NVARCHAR(4),
	"Cantidad" 											DECIMAL(0,8) DEFAULT 0,
	"Precio" 											DECIMAL(0,8) DEFAULT 0,
	"Monto" 											DECIMAL(0,8) DEFAULT 0,
	"IdMesa" 											BIGINT DEFAULT 0,
	"IdBolsa" 											NVARCHAR(3) DEFAULT '0',
	"IdEstadoComprobanteDetalle" 						NVARCHAR(3) NOT NULL,
	"FechaIngresoRegistro" 								TIMESTAMP NOT NULL,
	"IdCarga" 											BIGINT NOT NULL DEFAULT 0,
	"FolioComprobanteFacturacionMigracion" 				BIGINT NOT NULL DEFAULT 0,
	"MontoLiquidacion" 									DECIMAL(0,8) DEFAULT 0,
	"IdMoneda" 											NVARCHAR(3),
	"IdMonedaLiquidacion" 								NVARCHAR(3),
	"MontoComprobanteDetalle" 							DECIMAL(0,8) DEFAULT 0,
	"IdMonedaComprobanteDetalle" 						NVARCHAR(3),
	"IdInstrumentoSubyacente" 							BIGINT DEFAULT 0,
	"IdMonedaSubyacente" 								NVARCHAR(3),
	"IdTransaccion" 									BIGINT DEFAULT 0,
	"FechaComprobanteDetalle" 							DATE NOT NULL DEFAULT '0001-01-01',
	"NemotecnicoSubyacente" 							NVARCHAR(40),
	"IdTipoInstrumento" 								NVARCHAR(3),
	"IdTipoInstrumentoSubyacente" 						NVARCHAR(3),
	"NombreTipoInstrumento" 							NVARCHAR(100),
	"NombreTipoInstrumentoSubyacente" 					NVARCHAR(100),
	"IdClasificacionTipoInstrumento" 					NVARCHAR(3),
	"IdClasificacionTipoInstrumentoRelacionado" 		NVARCHAR(3),
	"IdInstrumentoRelacionado" 							BIGINT DEFAULT 0,
	"IdTipoInstrumentoRelacionado" 						NVARCHAR(3),
	"NemotecnicoRelacionado" 							NVARCHAR(40),
	"NombreClasificacionTipoInstrumentoRelacionado"		NVARCHAR(100),
	"NombreTipoInstrumentoRelacionado" 					NVARCHAR(100),
	"NombreClasificacionTipoInstrumento" 				NVARCHAR(100),
	"IdEmpleadoEjecutivoPortafolio" 					BIGINT DEFAULT 0,
	"IdSucursalEmpleadoEjecutivoPortafolio" 			BIGINT DEFAULT 0,
	"ModPrefFact" 										BIGINT NOT NULL DEFAULT 1,
	"IdConcepto" 										NVARCHAR(6) NOT NULL,
	"IdInstrumentoNuevo" 								BIGINT NOT NULL DEFAULT 0,
	"IndicadorOrdenDirecta"                             NVARCHAR(2) DEFAULT '',
	PRIMARY KEY (IdComprobanteDetalle)
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" (
	"IdMovimientoComprobanteDetalle" 					BIGINT NOT NULL,
	"IdComprobanteDetalle" 								BIGINT NOT NULL,
	"IdMovimientoComprobante" 							BIGINT NOT NULL,
	"IdTransaccion" 									BIGINT DEFAULT 0,
	"IdInstrumento" 									BIGINT DEFAULT 0,
	"NemotecnicoInstrumento" 							NVARCHAR(100),
	"IdConceptoMovimiento" 								NVARCHAR(6),
	"Cantidad" 											DECIMAL(0,8) DEFAULT 0,
	"Precio" 											DECIMAL(0,8) DEFAULT 0,
	"PrecioPromedio" 									DECIMAL(0,8) DEFAULT 0,
	"Monto" 											DECIMAL(0,8) DEFAULT 0,
	"Tasa" 												DECIMAL(0,8) DEFAULT 0,
	"TasaEmision" 										DECIMAL(0,8) DEFAULT 0,
	"ValorRescate" 										DECIMAL(0,8) DEFAULT 0,
	"ValorParidad" 										DECIMAL(0,8) DEFAULT 0,
	"ValorCaptacionMonedaOrigen" 						DECIMAL(0,8) DEFAULT 0,
	"SecuenciaAgrupacion" 								NVARCHAR(40),
	"IdEstadoMovimientoComprobanteDetalle" 				NVARCHAR(3) NOT NULL,
	"FechaIngresoRegistro" 								TIMESTAMP NOT NULL,
	"MontoLiquidacion" 									DECIMAL(0,8) DEFAULT 0,
	"IdMonedaLiquidacion" 								NVARCHAR(3),
	"MontoMovimientoComprobanteDetalle" 				DECIMAL(0,8) DEFAULT 0,
	"IdMonedaMovimientoComprobanteDetalle" 				NVARCHAR(3),
	"IdMoneda" 											NVARCHAR(3),
	"IdMesa" 											BIGINT DEFAULT 0,
	"IdInstrumentoSubyacente" 							BIGINT DEFAULT 0,
	"IdMonedaSubyacente" 								NVARCHAR(3),
	"IdTipoInstrumento" 								NVARCHAR(3),
	"IdTipoInstrumentoSubyacente"						NVARCHAR(3),
	"NombreTipoInstrumento" 							NVARCHAR(100),
	"NombreTipoInstrumentoSubyacente" 					NVARCHAR(100),
	"NemotecnicoSubyacente" 							NVARCHAR(40),
	"IdClasificacionTipoInstrumento" 					NVARCHAR(3),
	"IdClasificacionTipoInstrumentoRelacionado" 		NVARCHAR(3),
	"IdInstrumentoRelacionado" 							BIGINT DEFAULT 0,
	"IdTipoInstrumentoRelacionado" 						NVARCHAR(3),
	"NemotecnicoRelacionado" 							NVARCHAR(40),
	"NombreClasificacionTipoInstrumentoRelacionado"		NVARCHAR(100),
	"NombreTipoInstrumentoRelacionado" 					NVARCHAR(100),
	"NombreClasificacionTipoInstrumento" 				NVARCHAR(100),
	"ModPrefFact" 										BIGINT NOT NULL DEFAULT 1,
	"IdEmpleadoEjecutivoPortafolio" 					BIGINT DEFAULT 0,
	"IdSucursalEmpleadoEjecutivoPortafolio" 			BIGINT DEFAULT 0,
	"CodigoIFRS" 										NVARCHAR(40),
	"FolioTransaccion" 									NVARCHAR(40),
	"EsEnRueda" 										NVARCHAR(1) NOT NULL DEFAULT N,
	"IndicadorOrdenDirecta"								NVARCHAR(2) NOT NULL DEFAULT N,
	"Spread" 											DECIMAL(0,8) DEFAULT 0,
	"IdTipoReajuste" 									NVARCHAR(3) NOT NULL,
	"SecMovimientoFacturarMigracion" 					BIGINT NOT NULL DEFAULT 0,
	"IdCarga" 											BIGINT NOT NULL DEFAULT 0,
	"IdInstrumentoNuevo" 								BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY (IdMovimientoComprobanteDetalle)
);



create column table "BSCL.Facturacion.BD::FAC.ModoFacturacion" (
   "IdModoFacturacion" 				NVARCHAR(40) 	NOT NULL,
   "Nombre"          	 			NVARCHAR(100) 	NOT NULL DEFAULT '',
   primary key ("IdModoFacturacion")
);

create column table "BSCL.Facturacion.BD::FAC.FolioComprobante" (
   "IdFolioComprobante"   NVARCHAR(6)  DEFAULT 1 NOT NULL,
   "Codigo"               NVARCHAR(6),
   "Nombre"               NVARCHAR(40) DEFAULT '' NOT NULL,
   "FolioInicio"          BIGINT       DEFAULT 1 NOT NULL,
   "FolioFin"             BIGINT       DEFAULT 999999999 NOT NULL,
   "FolioActual"          BIGINT       DEFAULT 1 NOT NULL,
   "StockCritico"         BIGINT       DEFAULT 0 NOT NULL,
 primary key ("IdFolioComprobante")
);


CREATE COLUMN TABLE "BSCL"."BSCL.Facturacion.BD::FAC.NotificacionResumenFacturacionTemporal" (
	"IdEmpresa" BIGINT,
	"IdCliente" BIGINT,
	"IdPortafolio" BIGINT,
	"IdPersonaRemitente" BIGINT,
	"IdPersonaDestinatario" BIGINT,
	"CLIENTE" NVARCHAR(1000),
	"RUT" NVARCHAR(100),
	"CUENTA" NVARCHAR(100),
	"AGENTE" NVARCHAR(1000),
	"TELEFONO" NVARCHAR(50),
	"CORREO" NVARCHAR(100),
	"FECHA" NVARCHAR(15),
	"HORA" NVARCHAR(20),
	"CORREDORA" NVARCHAR(1000),
	"TABLA" TEXT,
	"ATENCIONCLIENTE" NVARCHAR(50)
);



CREATE COLUMN TABLE BSCL."BSCL.Facturacion.BD::CAR.TipoCargaMasiva" (
	"IdTipoCargaMasiva"		BIGINT     		NOT NULL,
	"CodigoArchivo"			NVARCHAR(40)    NOT NULL DEFAULT '',
	"Nombre"				NVARCHAR(100)   NOT NULL DEFAULT '',
	"IdEmpresa"  			BIGINT 			NOT NULL DEFAULT 0,
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdTipoCargaMasiva"));



-- Maestro historico de cargas
CREATE COLUMN TABLE BSCL."BSCL.Facturacion.BD::CAR.CargaMasiva" (
	"IdCargaMasiva"			BIGINT     		NOT NULL,
	"IdTipoCargaMasiva"     BIGINT     		NOT NULL DEFAULT 0,
	"IdEmpresa"  			BIGINT 			NOT NULL DEFAULT 0,
	"IdUsuario" 			BIGINT 			NOT NULL DEFAULT 0,
	"CantidadRegistros"     INTEGER         NOT NULL DEFAULT 0,	
	"NombreArchivo"         NVARCHAR(100)   NOT NULL DEFAULT '',
	"FechaCarga" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha de sistema
    "FechaProceso" 			SECONDDATE      NOT NULL DEFAULT '0001-01-01',        -- Fecha en que se proceso la CM
  	"FechaIngresoRegistro"	SECONDDATE      NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Fecha de servidor BD
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdCargaMasiva"));


-- Detalle historico de cargas
CREATE COLUMN TABLE BSCL."BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" (
	"IdCargaMasivaDetalle"				BIGINT     		NOT NULL,
	"IdCargaMasiva"						BIGINT     		NOT NULL DEFAULT 0,
	"IdTipoOperacion" 					NVARCHAR(6) 	NOT NULL DEFAULT '',
	"IdConcepto" 						NVARCHAR(6) 	NOT NULL DEFAULT '',
	"DocumentoIdentidad" 				NVARCHAR(40) 	NOT NULL DEFAULT '',
	"IdPortafolio" 						BIGINT 			NOT NULL DEFAULT 0,
	"CodigoPortafolio" 					NVARCHAR(100) 	NOT NULL DEFAULT '',
	"IdTipoComprobante" 				NVARCHAR(3) 	NOT NULL DEFAULT '',
	"IdCondicionDeLiquidacion" 			NVARCHAR(2) 	NOT NULL DEFAULT '',
	"IdMoneda" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
	"Monto" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
	"Glosa" 							NVARCHAR(260)  	NOT NULL DEFAULT '',
	"IdMedioDePago"             		NVARCHAR(3) 	NOT NULL DEFAULT '',
	"IdModalidadDePago"         		NVARCHAR(6) 	NOT NULL DEFAULT '',
	"ObservacionValidacion"             NVARCHAR(520)   NOT NULL DEFAULT '',
	"IdEstado"							NVARCHAR(3) 	NOT NULL DEFAULT '',	
	"IdMovimientoComprobante"           BIGINT          NOT NULL DEFAULT 0,
PRIMARY KEY ("IdCargaMasivaDetalle"));


create column table "BSCL.Facturacion.BD::FAC.TemplateFacturacion" (
   "IdTemplateFacturacion" BIGINT NOT NULL,
   "CodigoTemplateFacturacion" NVARCHAR(6) NOT NULL DEFAULT '',
   "NombreReporte"         NVARCHAR(100) NOT NULL DEFAULT '',
   "Descripcion"           NVARCHAR(100) NOT NULL DEFAULT '',
   "IdServicio"            BIGINT NOT NULL DEFAULT 0,
   "FormatoSalida"         NVARCHAR(3) NOT NULL DEFAULT '',
   "IdEmpresa"             BIGINT NOT NULL DEFAULT 0,
   "IdTipoComprobante"     NVARCHAR(3) NOT NULL DEFAULT '',
   "IdEstado"              NVARCHAR(3) NOT NULL DEFAULT '',
   "LocalizadorSistema"    NVARCHAR(2) NOT NULL DEFAULT '',
 primary key ("IdTemplateFacturacion")
);

create column table "BSCL.Facturacion.BD::FAC.PeriodoCierre" (
   "IdPeriodoCierre" BIGINT NOT NULL,
   "Periodo"         NVARCHAR(7) NOT NULL DEFAULT '01-0001',
   "IdEstadoPeriodo" NVARCHAR(3) NOT NULL DEFAULT '',
   "IdEstado"        NVARCHAR(3) NOT NULL DEFAULT '',   
   "IdUsuario"  	 BIGINT NOT NULL DEFAULT 0,
   "FechaIngresoRegistro" TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00',
   
 primary key ("IdPeriodoCierre")
);

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */
/*alter table "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
   add constraint "FK_TipoLimite_Limite" foreign key ("IdTipoLimite")
      references "BSCL.Plataforma.BD::LIM.TipoLimite" ("IdTipoLimite");*/
      
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::LIM."("");

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoPagoCobroMovimiento" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoConvenio" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobante" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteFormaDePago" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::Comprobante" cascade;
/*drop sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFactura" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronica" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCredito" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronica" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeDebitoElectronica" cascade;*/
drop sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteFormaDePago" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::Log" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::Tesoreria" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::CuentaInversion" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::FolioComprobante" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::LibroDte" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::LibroDteComprobante" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::ConceptoFacturacion" cascade;
drop sequence "BSCL.Facturacion.BD.Secuencias::PeriodoCierre" cascade;



/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

create sequence "BSCL.Facturacion.BD.Secuencias::TipoPagoCobroMovimiento" 
	start     with 10 
	increment by 1
	minvalue  1;

create sequence "BSCL.Facturacion.BD.Secuencias::TipoConvenio" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobante" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteFormaDePago" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::Comprobante"  
	start     with 10 
	increment by 1
	minvalue  1;
	
/*create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFactura"
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronica" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronicaOtrosConceptos" 
	start     with 10 
	increment by 1
	minvalue  1;	

create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaOtrosConceptos" 
	start     with 10 
	increment by 1
	minvalue  1;	

create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCredito" 
	start     with 10 
	increment by 1
	minvalue  1;

create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoOtrosConceptos"
	start     with 10 
	increment by 1
	minvalue  1;	

create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronica" 
	start     with 10 
	increment by 1
	minvalue  1;


create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronicaOtrosConceptos" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeDebitoElectronica" 
	start     with 10 
	increment by 1
	minvalue  1;


create sequence "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeDebito" 
	start     with 10 
	increment by 1
	minvalue  1;
*/
create sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::ComprobanteFormaDePago" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::Log" 
	start     with 10 
	increment by 1
	minvalue  1;		
	
create sequence "BSCL.Facturacion.BD.Secuencias::Tesoreria" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::CuentaInversion" 
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::FolioComprobante" 
	start     with 10 
	increment by 1
	minvalue  1;			

create sequence "BSCL.Facturacion.BD.Secuencias::LibroDte"
	start     with 10 
	increment by 1
	minvalue  1;


create sequence "BSCL.Facturacion.BD.Secuencias::LibroDteComprobante"
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::ConceptoFacturacion"
	start     with 10 
	increment by 1
	minvalue  1;
	
create sequence "BSCL.Facturacion.BD.Secuencias::PeriodoCierre"
	start     with 10 
	increment by 1
	minvalue  1;	
	

	
	
	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit      

