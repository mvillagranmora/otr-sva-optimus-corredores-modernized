SET SCHEMA "BSCL"; 

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
/*DROP TABLE "BSCL.Orden.BD::ORD.Asignacion" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.CobroOrden" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.CobroRangoOrden" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.EstadoOrden" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.EventoOrden" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.EventoTransaccion" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.Orden" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" CASCADE;   
DROP TABLE "BSCL.Orden.BD::ORD.TipoCustodiaOrden" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.TipoPrecioOrden" CASCADE; 
DROP TABLE "BSCL.Orden.BD::ORD.TipoRuteoOrden" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.TipoVigenciaOrden" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.Transaccion" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.TipoCanalDistribucion" CASCADE;
DROP TABLE "BSCL.Orden.BD::ORD.TipoCanalDistribucionBolsa" CASCADE;

DROP TABLE "BSCL.Orden.BD::ORD.Grupo";
DROP TABLE "BSCL.Orden.BD::ORD.GrupoPortafolio";

DROP TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden";
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenes";
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesOperador";
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesSegmento";
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion";*/

--PARA MANEJAR LAS SALIDAS DE LOS SPS INTERNOS, Y LOS ERRORES

CREATE TYPE "BSCL.Orden.BD.Tipos::SalidaDetalle" AS TABLE (
		"IdInterno" 	BIGINT,
		"IdOperacion" 	BIGINT,
		"Resultado" 	NVARCHAR(50), --RECHAZO, AUTORIZACION, ADVERTENCIA,INFORMACION, OK (OK es dummy)
		"Codigo"		NVARCHAR(100),
		"CodigoDato1" 	NVARCHAR(100),
		"ValorDato1"	NVARCHAR(520),
		"CodigoDato2" 	NVARCHAR(100),
		"ValorDato2" 	NVARCHAR(520)
   
   );
   
   CREATE TYPE "BSCL.Orden.BD.Tipos::SalidaResumen" AS TABLE (
		"IdInterno" 	BIGINT,
		"IdOperacion" 	BIGINT,
		"Resultado" 	NVARCHAR(40), --RECHAZO, AUTORIZACION, ADVERTENCIA,OK
		"OK"			BIGINT,
		"ADVERTENCIA" 	BIGINT,
		"AUTORIZACION"	BIGINT,
		"RECHAZO" 		BIGINT
   
   );

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.ControlMercado" (
	"TipoMensaje"			NVARCHAR(100),
	"IdMensaje1"			NVARCHAR(100),
	"IdMensaje2"			NVARCHAR(100),
	"FechaIngresoRegistro"	TIMESTAMP,
	"FechaProcesamiento"	TIMESTAMP,
	"Procesado"				NVARCHAR(1),
	"Log" 					NVARCHAR(5000) NULL,
	PRIMARY KEY ("TipoMensaje","IdMensaje1","IdMensaje2")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" (
	"IdSistemaBursatil"			NVARCHAR(40),
	"NombreSistemaBursatil"		NVARCHAR(100),
	PRIMARY KEY ("IdSistemaBursatil","NombreSistemaBursatil")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.CorredorCorresponsal" (
	"IdCorredorCorresponsal"		BIGINT 			NOT NULL,
	"IdPais"						BIGINT 			NOT NULL, 
	"CodigoCorredor"				NVARCHAR(100)	NOT NULL,
	"IdContraparte"					BIGINT,
	"IdCobro" 						NVARCHAR(40)	NOT NULL,
	"IdEstado" 						NVARCHAR(3)		NOT NULL,
	"ValorCobro"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"DefaultPais"					NVARCHAR(1)		NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdCorredorCorresponsal")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoAsignacion" (
	"IdTipoAsignacion"         NVARCHAR(40) NOT NULL,
	"Nombre"					NVARCHAR(100) NOT NULL, 
	"IdEstado"					NVARCHAR(3) NOT NULL,
	"SoportaAsignacionAutomatica" NVARCHAR(1),
	PRIMARY KEY ("IdTipoAsignacion")
);


CREATE TABLE BSCL."BSCL.Orden.BD::ORD.LogCambioTransaccion" (
	"TipoLogCambio"				NVARCHAR(1) NOT NULL, --ANULACION(A), MODIFICACION(M), NUEVO(N)
	"FechaLogCambio"			SECONDDATE NOT NULL,
	"FechaIngresoRegistroLog"	SECONDDATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"Motivo"					NVARCHAR(5000) NOT NULL,
	"Observaciones"				NVARCHAR(5000) NOT NULL,
	"IdTransaccion" 			BIGINT NOT NULL,
	"Folio" 					NVARCHAR(40) NULL,
	"FechaSistemaCreacion" 		DATE NOT NULL DEFAULT '0001-01-01',
	"HoraSistemaCreacion" 		TIME NOT NULL DEFAULT '00:00:00',
	"FechaOperacion" 			SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaLiquidacion" 			DATE NOT NULL DEFAULT '0001-01-01',
	"FechaEmision" 				DATE NOT NULL DEFAULT '0001-01-01',
	"FechaVencimiento" 			DATE NOT NULL DEFAULT '0001-01-01',
	"FechaIngresoRegistro"		TIMESTAMP NOT NULL,
	"EsOD" 						NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsCuentaPropia"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsFungible" 				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"EsAPlazo" 					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IdEmisor" 					NVARCHAR(40) NULL,
	"Cantidad" 					DECIMAL(26,8) NULL,
	"Monto" 					DECIMAL(26,8) NULL,
	"Precio" 					DECIMAL(26,8) NULL,
	"Tasa" 						DECIMAL(26,8) NULL,
	"Nemotecnico" 				NVARCHAR(40) NOT NULL,
	"NemotecnicoSVS" 			NVARCHAR(40) NOT NULL,
	"ValorRescate" 				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"ValorCaptacion" 			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdEstado" 					NVARCHAR(3) NULL,
	"IdTransaccionPlazo" 		BIGINT NOT NULL DEFAULT 0,
	"IdTransaccionOd" 			BIGINT NOT NULL DEFAULT 0,
	"IdTipoInstrumento" 		NVARCHAR(3) NOT NULL,
	"IdClasificacionTipoInstrumento" NVARCHAR(3) NOT NULL,
	"IdTipoReajuste" 			NVARCHAR(3) NOT NULL,
	"IdEmpleado" 				BIGINT NOT NULL,
	"IdTipoOperacion" 			NVARCHAR(6) NOT NULL,
	"IdCondicionDeLiquidacion"	NVARCHAR(2) NOT NULL,
	"IdMoneda" 					NVARCHAR(3) NOT NULL,
	"IdEmpresa"					BIGINT NOT NULL,
	"IdBolsa"					NVARCHAR(3) NOT NULL,
	"IdInstrumento"				BIGINT NOT NULL,
	"NemotecnicoSubyacente" 	NVARCHAR(40) NOT NULL,
	"IdInstrumentoSubyacente" 	BIGINT NOT NULL,
	"IdMonedaSubyacente" 			NVARCHAR(3) NOT NULL DEFAULT 0,
	"EsVentaCorta" 					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Plazo" 						BIGINT NOT NULL DEFAULT 0,
	"FechaIngresoOferta" 			TIMESTAMP NOT NULL,
	"CodigoOperadorContraparte" 	NVARCHAR(40) NOT NULL,
	"IdLibroOrdenes" 				BIGINT NOT NULL DEFAULT 0,
	"MarcaNegociacion" 				NVARCHAR(40) NOT NULL,
	"IndicadorDCV"					NVARCHAR(1) NOT NULL DEFAULT 'S',	
	"EsBilateral"					NVARCHAR(1) NOT NULL DEFAULT 'N',	
	"IdCorredorLocal" 				BIGINT NOT NULL DEFAULT 0,
	"TipoCambioCorresponsal" 		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdCorredorCorresponsal" 		BIGINT NOT NULL DEFAULT 0,
	"MontoLiquidacionCorresponsal" 	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdMonedaLiquidacionCorresponsal"	NVARCHAR(3) NOT NULL,
	"IdCorredorContraparte" 			BIGINT NOT NULL DEFAULT 0,
	"CodigoOperadorCorredor" 			NVARCHAR(40) NOT NULL,
	"IdSistemaBursatil" 				NVARCHAR(40) NOT NULL,
	"MejorPrecioVenta" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MejorPrecioCompra" 				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"EsFueraRueda" 						NVARCHAR(1) NOT NULL DEFAULT 'N'
) ;



create column table "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion" (
	"IdTipoCanalDistribucion" 	NVARCHAR(3) NOT NULL ,
	"IdTipoCanalIngreso"	 	NVARCHAR(3) NOT NULL ,
	"IdTipoInstrumento"			NVARCHAR(3) NOT NULL ,
	"IdBolsa" 					NVARCHAR(3) NOT NULL ,
	"IdCondicionDeLiquidacion"	NVARCHAR(2) NOT NULL,
	"ValidaHorario"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"AplazaOrden"				NVARCHAR(1) NOT NULL DEFAULT 'N', --Si esta en S, debe aplazar la fecha operacion de la orden para el siguiente horario de preapertura,Si esta en N, la rechaza
	"HorarioPreMarketInicio"	TIME NOT NULL DEFAULT '00:00:00',
	"HorarioPreMarketFin"		TIME NOT NULL DEFAULT '00:00:00',
	"HorarioMarketInicio"		TIME NOT NULL DEFAULT '00:00:00',
	"HorarioMarketFin"			TIME NOT NULL DEFAULT '00:00:00',
 primary key ("IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","IdCondicionDeLiquidacion")
);

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ValorPorDefectoOrden"

create column table "BSCL.Orden.BD::ORD.ValorPorDefectoOrden" (
	"IdInterno"					BIGINT NOT NULL,
	"IdTipoPrecio"				NVARCHAR(1) NOT NULL DEFAULT 'M',
	"IdPorcentajeVisibilidad"	BIGINT NOT NULL DEFAULT 1,
	"IdFormaCustodia"			NVARCHAR(3) NOT NULL DEFAULT 'CUS',
 primary key ("IdInterno")
);

create column table "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" (
   "IdEmpresa"       BIGINT NOT NULL,
   "CodigoParametro" NVARCHAR(40) NOT NULL,
   "Descripcion"     NVARCHAR(520) NOT NULL,
   "Valor"           NVARCHAR(520) NOT NULL, 
 primary key ("IdEmpresa", "CodigoParametro")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" (
	"IdTipoAsignacionClasificacionTipoInstrumento"	BIGINT,
	"IdTipoAsignacion"         						NVARCHAR(40) NOT NULL,
	"EsContraOrden"									NVARCHAR(1) NOT NULL, --si es N, es contra cliente
	"IdClasificacionTipoInstrumento"				NVARCHAR(2) NOT NULL,
	PRIMARY KEY ("IdTipoAsignacionClasificacionTipoInstrumento")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.ConceptoOrden" (
	"IdConceptoOrden"	BIGINT,
	"IdConcepto"		NVARCHAR(6),
	"IdTipoOperacion"	NVARCHAR(6),
	"Uso"               NVARCHAR(100)   NOT NULL DEFAULT '',
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" (
	 "IdEmpresa"			 	BIGINT NOT NULL ,
	 "IdTipoCanalDistribucion" 	NVARCHAR(3) NOT NULL ,
	 "IdTipoCanalIngreso"		NVARCHAR(3) NOT NULL ,
	 "IdTipoInstrumento"		NVARCHAR(3) NOT NULL ,
	 "IdBolsa" 					NVARCHAR(3) NOT NULL ,
	 "GeneraMovimientoFacturacionAutomatico" NVARCHAR(1) NOT NULL DEFAULT 'N',
	 "GeneraComprobanteFacturacionAutomatico" NVARCHAR(1) NOT NULL DEFAULT 'N',
	 "MontoMaximoDistribucionOrden" DECIMAL(26,8) NOT NULL DEFAULT 0,
	 PRIMARY KEY ("IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","IdEmpresa")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoCanalDistribucion" (
	 "IdTipoCanalDistribucion" 	NVARCHAR(3) NOT NULL ,
	 "Nombre" 					NVARCHAR(100),
	 "IdEstado" 				NVARCHAR(3), 
	 PRIMARY KEY ("IdTipoCanalDistribucion")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.Asignacion" (
 "IdAsignacion"                         BIGINT NOT NULL,
 "IdAsignacionRef"						BIGINT NOT NULL DEFAULT 0,
 "FechaAsignacion"                      TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "FechaIngresoRegistro"                 TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "CantidadAsignada"                     DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoAsignado"                        DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorRescateAsignado"                 DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoAsignadoLocal"                        DECIMAL(26,8) NOT NULL DEFAULT 0,
 "TipoCambio"							     DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoAsignadoLiquidacion"			     DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdEstadoAsignacion"                   NVARCHAR(40) NOT NULL,
 "IdEstadoFacturacion"                  NVARCHAR(40) NOT NULL DEFAULT '',
 "IdTransaccion"                        BIGINT NOT NULL,
 "IdOrden"                              BIGINT NOT NULL, 
 "IdLibroOrdenes" 						BIGINT NOT NULL,
-- "IdSimulacion" 						BIGINT NOT NULL,
 "EsAPlazo" 							NVARCHAR(1) DEFAULT 'N' NOT NULL,
 "EsOD"									NVARCHAR(1) NOT NULL DEFAULT 'N',
 "IdMovimientoComprobante"				BIGINT NOT NULL DEFAULT 0,
 "IdComprobanteRefact"					BIGINT NOT NULL DEFAULT 0,
 "CantidadAsignadaRefact"                	DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorRescateAsignadoRefact" 				DECIMAL(26,8) NOT NULL DEFAULT 0,
 "SecAsignacionMigracion"               BIGINT DEFAULT 0 NOT NULL,
 "IdCarga"                             BIGINT NOT NULL DEFAULT 0,
 "FolioCartera" 						BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY ("IdAsignacion")) ;


CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" (
 "IdCobroAsignacion"					BIGINT NOT NULL,
 "IdAsignacion" 						BIGINT NOT NULL,
 "IdCobro" 								NVARCHAR(6),
 "FechaIngresoCobro"					TIMESTAMP NOT NULL,
 "FechaIngresoRegistro" 				TIMESTAMP NOT NULL,
 "IdPerfilCobro" 						NVARCHAR(40) NOT NULL,
 "FormaCobro" 							NVARCHAR(40),
 "ValorCobro" 							DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorCobroAplicado" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoCobroCalculado"					DECIMAL(26,8) NOT NULL DEFAULT 0, 
 "IdCarga"                              BIGINT DEFAULT 0 NOT NULL
 PRIMARY KEY ("IdCobroAsignacion"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.CobroOrden" (
 "IdCobroOrden" 						BIGINT NOT NULL,
 "IdGrupoCobro" 						BIGINT NOT NULL,
 "IdOrden" 								BIGINT NOT NULL,
 "IdCobro" 								NVARCHAR(6) NOT NULL,
 "ValorCobro" 							DECIMAL(26,8) NOT NULL DEFAULT 0,
 "ValorCobroAplicado" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MontoCobroCalculado" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
 "FormaCobro" 							NVARCHAR(100) NOT NULL,
 "EsComprobante" 						NVARCHAR(3) NOT NULL,
 "IdPerfilCobro" 						NVARCHAR(40) NOT NULL,
 "FechaIngresoCobro" 					TIMESTAMP NOT NULL,
 "IdCarga"                              BIGINT NOT NULL DEFAULT 0,
 "MontoMinimo"							DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdOrigenCobro" NVARCHAR(40) NOT NULL DEFAULT 'PORTAFOLIO',
  PRIMARY KEY ("IdCobroOrden"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.CobroRangoOrden" (
 "IdCobroRangoOrden"                    BIGINT NOT NULL,
 "IdCobroOrden"                         BIGINT NOT NULL,
 "IdOrden"                              BIGINT NOT NULL,
 "IdGrupoCobro"                         BIGINT NOT NULL,
 "IdEmpresa"                            BIGINT NOT NULL,
 "CodigoDefinicionCobro"                NVARCHAR(40) NOT NULL,
 "Hasta"                                DECIMAL(26,8) NOT NULL DEFAULT 0,
 "Valor"                                DECIMAL(26,8) NOT NULL DEFAULT 0,
  PRIMARY KEY ("IdCobroRangoOrden"));  

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.EstadoOrden" (
 "IdEstadoOrden" 							BIGINT NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL,
 "NombreEntidad" 						NVARCHAR(100) NOT NULL, PRIMARY KEY ("IdEstadoOrden"))  ;

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.EventoBitacora" (
 "IdEventoBitacora" 					BIGINT NOT NULL,
 "NombreEntidad"						NVARCHAR(100) NOT NULL,
 "IdOrden"								BIGINT NOT NULL DEFAULT 0,
 "IdAsignacion"							BIGINT NOT NULL DEFAULT 0,
 "IdTransaccion"						BIGINT NOT NULL DEFAULT 0,
 "MensajeEventoBitacora"				NVARCHAR(5000) NOT NULL DEFAULT '',
 "FechaEventoBitacora"					SECONDDATE NOT NULL,
 "FechaIngresoRegistro"					SECONDDATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 "IdTipoEventoBitacora"					NVARCHAR(40) NOT NULL,
 PRIMARY KEY ("IdEventoBitacora")
 );
 
 CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoEventoBitacora" (
 "IdTipoEventoBitacora"					NVARCHAR(40) NOT NULL,
  "DescripcionTipoEventoBitacora"		NVARCHAR(255) NOT NULL,
 PRIMARY KEY ("IdTipoEventoBitacora")
 );

delete from "BSCL.Orden.BD::ORD.TipoEventoBitacora";
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ENVIADA_NEGOCIACION','Enviada a Negociacion' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'EN_NEGOCIACION','En Negociacion' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'PARCIALMENTE_EJECUTADA','Parcialmente Ejecutada' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'EJECUTADA_COMPLETAMENTE','Ejecutada Completamente' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ENVIADA_CANCELAR','Enviada a Cancelar' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ORDEN_CANCELADA','Orden Cancelada' from dummy;
insert into "BSCL.Orden.BD::ORD.TipoEventoBitacora" select 'ORDEN_RECHAZADA','Orden Rechazada' from dummy;
insert into "BSCL.Orden.BD::ORD.EventoBitacora" select 1, 'ORDEN',309386,0,0,'',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ENVIADA_NEGOCIACION' FROM DUMMY;

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.Orden" (
 "IdOrden" 								BIGINT NOT NULL,
 "Folio"								NVARCHAR(40),
 "FechaRecepcion" 						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaIngresoRegistro"					TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
 "FechaIngresoRegistroCorta"			DATE NOT NULL DEFAULT '0001-01-01',
 "FechaSolicitudAnulacion"				SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaAnulacion" 						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaOperacion"						SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaVigencia" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaOferta" 							SECONDDATE NOT NULL DEFAULT '0001-01-01',
 "FechaVencimiento" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaEmision" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaLiquidacion" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaAceptacion" 						SECONDDATE NOT NULL DEFAULT '0001-01-01  00:00:00',
 "FechaAutorizacion" 					DATE NOT NULL DEFAULT '0001-01-01',
 "Glosa" 								NVARCHAR(520),
 "IdEstadoOrden" 						NVARCHAR(3) NOT NULL ,
 "Cantidad" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "Tasa" 								DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "TasaRFIF" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "TasaSimultanea" 						DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "TasaVentaCorta" 						DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "Plazo" 								BIGINT NOT NULL DEFAULT 0 ,
 "PlazoIF" 								BIGINT NOT NULL DEFAULT 0 ,
 "PlazoSimultanea"						BIGINT NOT NULL DEFAULT 0 ,
 "PlazoVentaCorta"						BIGINT NOT NULL DEFAULT 0 ,
 "ValorRescate" 						DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdTipoReajuste" 						NVARCHAR(3) NOT NULL,
 "PrecioOrden" 							DECIMAL(26,8) NOT NULL DEFAULT 0 ,
 "PrecioMercado" 						DECIMAL(26,8) NOT NULL DEFAULT 0 ,
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
 "IdCorredorCorresponsal"				BIGINT NOT NULL DEFAULT 0,
 "MontoPrimaVentaCorta" DECIMAL(26,8) NOT NULL DEFAULT 0
 "IdPersonaAutorizadoDarOrdenes" BIGINT NOT NULL DEFAULT 0,
 "OrdenVigente" NVARCHAR(1) NOT NULL DEFAULT 'N'
  
  PRIMARY KEY ("IdOrden"))  ;
  
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" (
 "IdPorcentajeVisibilidad" 				BIGINT NOT NULL,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , 
 "Porcentaje" 							INT NOT NULL DEFAULT 0
 PRIMARY KEY ("IdPorcentajeVisibilidad"))  ;
 
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoCustodiaOrden" (
 "IdTipoCustodiaOrden" 					NVARCHAR(3) NOT NULL ,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , PRIMARY KEY ("IdTipoCustodiaOrden"))  ;
 

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" (
 "IdTipoFormaCustodiaOrden" 			NVARCHAR(3) NOT NULL ,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , PRIMARY KEY ("IdTipoFormaCustodiaOrden"))  ;
 
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoPrecioOrden" (
 "IdTipoPrecioOrden" 					NVARCHAR(1) NOT NULL ,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , PRIMARY KEY ("IdTipoPrecioOrden"))  ;
 
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoRuteoOrden" (
 "IdTipoRuteoOrden" 					NVARCHAR(2) NOT NULL ,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , PRIMARY KEY ("IdTipoRuteoOrden"))  ;

 CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoVigenciaOrden" (
 "IdTipoVigenciaOrden" 					NVARCHAR(2) NOT NULL ,
 "Codigo" 								NVARCHAR(40) NOT NULL,
 "Nombre" 								NVARCHAR(100) NOT NULL,
 "IdEstado" 							NVARCHAR(3) NOT NULL , PRIMARY KEY ("IdTipoVigenciaOrden"))  ;
  
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.Transaccion" (
 "IdTransaccion" 						BIGINT NOT NULL,
 "Folio" 								NVARCHAR(40),
 "FechaSistemaCreacion" 				DATE NOT NULL DEFAULT '0001-01-01',
 "HoraSistemaCreacion" 					TIME NOT NULL DEFAULT '00:00:00',
 "FechaOperacion" 						SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacionOperacion" 			SECONDDATE  NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaLiquidacion" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaEmision" 						DATE NOT NULL DEFAULT '0001-01-01',
 "FechaVencimiento" 					DATE NOT NULL DEFAULT '0001-01-01',
 "FechaIngresoOferta" 					TIMESTAMP NOT NULL,
 "FechaIngresoRegistro" 				TIMESTAMP NOT NULL,
 "IdSistemaBursatil" 					BIGINT NOT NULL,
 "EsOD" 								NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EsCuentaPropia" 						NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EsFungible" 							NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EsAPlazo" 							NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "EsVentaCorta"							NVARCHAR(1) DEFAULT 'N' NOT NULL ,
 "CodigoOperadorContraparte"			NVARCHAR(40) NOT NULL DEFAULT '',
 "IdEmisor" 							NVARCHAR(40),
 "Cantidad" 							DECIMAL(26,8) NOT NULL DEFAULT 0,
 "Monto" 								DECIMAL(26,8) NOT NULL DEFAULT 0,--se guarda en moneda Instrumento
 "MontoValorizado"						DECIMAL(26,8) NOT NULL DEFAULT 0,--se guarda en moneda Intrumenot
 "Precio" 								DECIMAL(26,8) NOT NULL DEFAULT 0,
 "Tasa" 								DECIMAL(26,8) NOT NULL DEFAULT 0,
 "Plazo" 								BIGINT DEFAULT 0 NOT NULL,
 "Nemotecnico" 							NVARCHAR(40) NOT NULL,
 "NemotecnicoSVS" 						NVARCHAR(40) NOT NULL,
 "ValorRescate" 						DECIMAL(26,8)NOT NULL DEFAULT 0,
 "ValorCaptacion" 						DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdEstado" 							NVARCHAR(3),
 "IdTransaccionPlazo" 					BIGINT NOT NULL DEFAULT 0,
 "IdTransaccionOd"	 					BIGINT NOT NULL DEFAULT 0,
 "IdTipoInstrumento" 					NVARCHAR(3) NOT NULL,
 "IdClasificacionTipoInstrumento" 		NVARCHAR(3) NOT NULL,
 "IdTipoReajuste" 						NVARCHAR(3) NOT NULL,
 "IdEmpleado" 							BIGINT NOT NULL,
 "IdTipoOperacion" 						NVARCHAR(6) NOT NULL ,
 "IdCondicionDeLiquidacion" 			NVARCHAR(2) NOT NULL,
 "IdMoneda" 							NVARCHAR(3) NOT NULL,
 "IdCorredor" 							BIGINT NOT NULL,
 "Reservado"	    					NVARCHAR(1) NOT NULL DEFAULT 'N', 
 "IdUsuarioReserva" 					BIGINT NOT NULL DEFAULT 0,
 "MotivoReserva"						NVARCHAR(40) NOT NULL DEFAULT '',
 "Bloqueado"		   					NVARCHAR(1) NOT NULL DEFAULT 'N', 
 "IdUsuarioBloqueo" 					BIGINT NOT NULL DEFAULT 0,
 "MotivoBloqueo"						NVARCHAR(40) NOT NULL DEFAULT '',
 "IdEmpresa" 							BIGINT NOT NULL,
 "IdBolsa" 								NVARCHAR(3) NOT NULL,
 "IdLibroOrdenes" 						BIGINT NOT NULL,
 "IdInstrumento" 						BIGINT NOT NULL,
 "NemotecnicoSubyacente"				NVARCHAR(40) NOT NULL,
 "IdInstrumentoSubyacente" 				BIGINT NOT NULL,
 "IdMonedaSubyacente"					NVARCHAR(3) NOT NULL,
 "FechaParidad" 						DATE NOT NULL DEFAULT '0001-01-01',
 "ValorParidad" 						DECIMAL(26,8) NOT NULL DEFAULT 0,
 "FechaTipoCambio" 						DATE NOT NULL DEFAULT '0001-01-01',
 "ValorTipoCambio"						DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdCarga"                              BIGINT NOT NULL DEFAULT 0  
 "IdComprobanteTesoreria"		        BIGINT 	NOT NULL DEFAULT 0,  -- No tiene FK ya que si no está liquidado puede estar en cero.  
 "MarcaNegociacion"		  				NVARCHAR(40) NOT NULL DEFAULT '',
 "IndicadorDCV"		  					NVARCHAR(1) NOT NULL DEFAULT 'S',
 "EsBilateral" 							NVARCHAR(1) NOT NULL DEFAULT 'N',
 "IdCamara" 							NVARCHAR(40) NOT NULL DEFAULT '',
 "TipoCambioCorresponsal" 				DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdCorredorCorresponsal"				BIGINT NOT NULL DEFAULT 0,
 "MontoLiquidacionCorresponsal" 		DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdMonedaLiquidacionCorresponsal" 		NVARCHAR(3) NOT NULL DEFAULT '',
 "MejorPrecioCompra" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
 "MejorPrecioVenta" 					DECIMAL(26,8) NOT NULL DEFAULT 0,
 "IdProgramaProcesaAutomatico" 			BIGINT NOT NULL DEFAULT 0,
 "IdCorredorLocal" 						BIGINT NOT NULL DEFAULT 0,
 "CodigoOperadorCorredor" 				NVARCHAR(40) NOT NULL,
 "EsFueraRueda" 						NVARCHAR(1) NOT NULL DEFAULT 'N', 
 "GastosCorresponsal"  					DECIMAL(26,8)  	NOT NULL 	DEFAULT 0,			  -- Para transacciones Extranjeras, MILA
 "Intereses"  							DECIMAL(26,8)  	NOT NULL  	DEFAULT 0,            -- RF Perú
 "Reajustes"  							DECIMAL(26,8)  	NOT NULL  	DEFAULT 0,			  -- RF Perú
 "BloqueoLiquidacion"  					NVARCHAR(1)  	NOT NULL  	DEFAULT 'N',          -- Permite bloquear liquidacion a contraparte(Bilateral/Clearing)
 "FechaPago" 							DATE 			NOT NULL 	DEFAULT '0001-01-01', -- Fecha original de liquidación (ya que en tesaorería se puede editar)  
 "MontoRetencion"  						DECIMAL(26,8)  	NOT NULL  	DEFAULT 0,			  -- Peru (enviado por CAVALI a través de archivo IGK) 
 PRIMARY KEY ("IdTransaccion"));


 
 CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.DebugCrearTransacciones" (
	"IdInterno"	 						BIGINT,
	"FolioTransaccion"					NVARCHAR(40),
	"HoraCreacion"						NVARCHAR(10),
	"FechaEmision"						NVARCHAR(10),
	"Plazo"								BIGINT,
	"EsCuentaPropia"					NVARCHAR(1),
	"EsFungible"						NVARCHAR(1),
	"EsFueraRueda"						NVARCHAR(1),
	"EsVentaCorta"						NVARCHAR(1),
	"IdOperadorContraparte"				NVARCHAR(40),
	"CodigoOperadorContraparte"			NVARCHAR(40),
	"IdOperadorCorredor"				NVARCHAR(40),
	"IdSistemaBursatil"					BIGINT,
	"IdEmisor"							NVARCHAR(40),
	"Cantidad"							DECIMAL(26,8),	
	"ValorCaptacion"					DECIMAL(26,8),	
	"Monto"								DECIMAL(26,8),
	"MontoValorizado"					DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	"Tasa"								DECIMAL(26,8), 
	"MontoPlazo"						DECIMAL(26,8),
	"PrecioPlazo"						DECIMAL(26,8),
	"ValorRescate"						DECIMAL(26,8),
	"IdTipoReajuste"					NVARCHAR(3),
	"IdEmpleado"						BIGINT,
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
	"FechaLiquidacionContado"			NVARCHAR(10),
	"FechaVencimiento"					NVARCHAR(10),
	"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
	"FechaLiquidacionPlazo"				NVARCHAR(10),
	"IdInstrumento"						BIGINT,
	"IdCorredorContraparte"				BIGINT,
	"IdBolsa"							NVARCHAR(3),
	"IdLibroOrdenes"					BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"MarcaNegociacion"		  			NVARCHAR(40),
	"IndicadorDCV"		  				NVARCHAR(1)					
	);
 
 
 
  CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.DebugCrearOrdenesPostreras" (
								 "IdInterno"				BIGINT, 
								"FechaRecepcion" 		NVARCHAR(19),
								"Glosa" 				NVARCHAR(520), 
								"IdTipoCustodia" 		NVARCHAR(3),
								"IdFormaCustodia" 		NVARCHAR(3),
								"EsFacturaInmediata" 	NVARCHAR(1),
								"IdTransaccion" 		BIGINT,
								"FolioTransaccion" 		NVARCHAR(40),
								"IdPortafolio" 			BIGINT,
								"Cantidad" 				DECIMAL(26,8),
								"ValorRescate" 			DECIMAL(26,8)
								);
 
 CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.DebugCrearAsignaciones"(
											"IdInterno" BIGINT, 
											"IdOrden" BIGINT, 
											"IdTransaccion" BIGINT, 
											"Cantidad" DECIMAL(26,8), 
											"ValorRescate" DECIMAL(26,8)
											);

 
 CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.DebugCrearOrdenes" (
										"IdInterno"	 						BIGINT,
										"FechaRecepcion"					NVARCHAR(19),
										"FechaVigencia"						NVARCHAR(10),
										"FechaEmision"						NVARCHAR(10),
										"FechaOperacion"					NVARCHAR(19),
										"IdTipoVigencia"					NVARCHAR(2),
										"Glosa"								NVARCHAR(520),
										"Cantidad"							DECIMAL(26,8),
										"MontoNeto"							DECIMAL(26,8),
										"ValorRescate"						DECIMAL(26,8),
										"IdTipoCustodia"					NVARCHAR(3),
										"IdFormaCustodia"					NVARCHAR(3),
										"IdPorcentajeVisibilidad"			BIGINT,
										"PrecioLimite"						DECIMAL(26,8),
										"PrecioMercado"						DECIMAL(26,8),
										"EsFacturaInmediata"				NVARCHAR(1),
										"IdTipoPrecio"						NVARCHAR(1),
										"IdPortafolio"						BIGINT,
										"IdTipoLibroOrden"					NVARCHAR(40),
										"IdTipoCanalIngreso"				NVARCHAR(3),
										"IdTipoReajuste"					NVARCHAR(3),
										"IdEmisor"							NVARCHAR(40),
										"IdBolsa"							NVARCHAR(3),
										"EspecificaBolsa"					NVARCHAR(1),
										"IdMonedaLiquidacionOrden"			NVARCHAR(3),
										"IdMonedaLiquidacionCobros"			NVARCHAR(3),
										"TipoCambio"						DECIMAL(26,8),
										"IdInstrumento"						BIGINT,
										"IdInstrumentoSubyacente"			BIGINT,
										"IdTipoOperacion"					NVARCHAR(6),
										"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
										"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
										"TasaVentaCorta"					DECIMAL(26,8),
										"TasaSimultanea"					DECIMAL(26,8),
										"TasaRFIF"							DECIMAL(26,8),
										"PlazoSimultanea"					DECIMAL(26,8),
										"PlazoVentaCorta"					BIGINT,
										"EsVentaCorta"						NVARCHAR(1),
										"EsOD"								NVARCHAR(1),
										"EsFungible"						NVARCHAR(1)
										);


CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.Grupo" (
   "IdGrupo"  				BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "Nombre"         		NVARCHAR(50)        not null,
   "IdEstado"       		NVARCHAR(3)         not null,
   "Codigo" 				NVARCHAR(40) 		NOT NULL DEFAULT '',
   PRIMARY KEY("IdGrupo")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.GrupoPortafolio" (
   "IdGrupoPortafolio"		BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "IdGrupo"         		BIGINT        		not null,
   "IdPortafolio"      		BIGINT		    	not null,
   "Porcentaje" DECIMAL(26,8) NOT NULL DEFAULT 0
   PRIMARY KEY("IdGrupoPortafolio")
);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden" (
	"IdTipoLibroOrden"		NVARCHAR(6) NOT NULL,
	"NombreTipoLibroOrden"	NVARCHAR(100) NOT NULL, PRIMARY KEY ("IdTipoLibroOrden"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" (
	"IdLibroOrdenes"				BIGINT			NOT NULL UNIQUE,
	"IdEmpresa"						BIGINT			NOT NULL DEFAULT 0,
	"NombreLibroOrdenes"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdTipoLibroOrden"				NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdEstado"						NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"AsignacionAutomatica"			NVARCHAR(2)		NOT NULL DEFAULT 'NO',
	"IdTipoAsignacion"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"Prioridad"						BIGINT			NOT NULL DEFAULT 0,
	"LibroPorDefecto"				NVARCHAR(2)		NOT NULL DEFAULT 'N',
	"FechaCreacion"					DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdTipoBloqueo"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaBloqueo"					SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioBloqueo"				NVARCHAR(520)	NOT NULL DEFAULT '',
	"IdTipoDesbloqueo"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaDesbloqueo"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioDesbloqueo"			NVARCHAR(520)	NOT NULL DEFAULT '',
	"Borrado"						NVARCHAR(1)		NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdLibroOrdenes"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesOperador" (
	"IdLibroOrdenesOperador"	BIGINT			NOT NULL,
	"IdEmpresa"					BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"			BIGINT			NOT NULL DEFAULT 0,
	"CodigoOperador"			NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdLibroOrdenesOperador"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesSegmento" (
	"IdLibroOrdenesSegmento"	BIGINT			NOT NULL,
	"IdEmpresa"					BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"			BIGINT			NOT NULL DEFAULT 0,
	"IdSegmentoCliente"			BIGINT			NOT NULL DEFAULT 0,
	"IdEstado"					NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdLibroOrdenesSegmento"));

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" (
	"IdMarcaNegociacion"	BIGINT			NOT NULL,
	"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"		BIGINT			NOT NULL DEFAULT 0,
	"MarcaNegociacion"		NVARCHAR(520) 	NOT NULL DEFAULT '',
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"		DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdMarcaNegociacion"));

/*==============================================================*/
/* Constraint:                                                  */
/*==============================================================*/

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
