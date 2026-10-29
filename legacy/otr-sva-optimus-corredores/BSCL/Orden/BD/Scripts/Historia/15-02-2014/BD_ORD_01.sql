SET SCHEMA "BSCL";

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" where "RutaServicio" like '%BSCL.Plataforma.BD.Servicios::BolsaInstrumentoPreciosActual%';
DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 1443;
DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" where "RutaServicio" like '%BSCL.Orden.BD.Servicios::ReasignacionFidessa%';
DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" where "RutaServicio" like 'BSCL.CanalAgente.BD.Servicio::ConsultarHistorialMovimientosCuentaInversion';




ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdBoveda" NVARCHAR(40) NULL);
ALTER TABLE "BSCL.Plataforma.BD::INS.Corredora" ADD ("NombreCorto" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion" ADD ("EsFueraRueda" NVARCHAR(1) NOT NULL DEFAULT 'N');


CREATE TABLE "BSCL.Orden.BD::ORD.LogCambioTransaccion" (
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

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20008,'PLUGIN_ORD.jar@cl.bcs.ordenes.PrevisualizarDesasignacion',0,'J','','','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20009,'PLUGIN_ORD.jar@cl.bcs.ordenes.EjecutarDesasignacion',0,'J','','','');

INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'PorcentajeMaximoComision','Porcentaje maximo de comision permitido en la corredora',10);

INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'PorcentajeMinimoComision','Porcentaje minima de comision permitido en la corredora',0);

ALTER TABLE "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" ADD("MontoMaximoOperacion" DECIMAL(26,8) NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.Plataforma.BD::MER.Emisor" ADD ("IdEntidadClasificadoraRiesgo"	NVARCHAR(40)	NOT NULL DEFAULT '');

ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("51" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("52" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("53" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("54" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("55" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("56" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("57" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("58" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("59" NVARCHAR(1) DEFAULT NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.Identidad" ADD("60" NVARCHAR(1) DEFAULT NULL);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "51") VALUES(50,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "52") VALUES(51,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "53") VALUES(52,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "54") VALUES(53,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "55") VALUES(54,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "56") VALUES(55,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "57") VALUES(56,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "58") VALUES(57,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "59") VALUES(58,'');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Identidad" ("0", "60") VALUES(59,'');


INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CLIENTEBLOQUEADO', 'CLIENTE BLOQUEADO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('PERSONABLOQUEADA', 'PERSONA BLOQUEADA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('OPERACIONSINFLUJOCUSTODIA', 'OPERACION SIN FLUJO CUSTODIA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('EJECUTIVOSINPRODUCTO', 'EJECUTIVO SIN PRODUCTO HABILITADO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('EJECUTIVOSINRUTEO', 'EJECUTIVO SIN RUTEO HABILITADO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('EJECUTIVOPORTAFOLIOBLOQUEADO', 'EJECUTIVO PORTAFOLIO BLOQUEADO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('MONTOOPERACIONSUPERAPERMITIDO', 'MONTO OPERACION SUPERA EL PERMITIDO PARA EL EJECUTIVO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('COMISIONSUPERAMAXIMOPERMITIDO', 'COMISION SUPERA EL MAXIMO PERMITIDO PARA LA CORREDORA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('COMISIONBAJOMINIMOPERMITIDO', 'COMISION BAJO EL MINIMO PERMITIDO PARA LA CORREDORA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('MONTOSUPERAMAXIMOCANALDISTRIBUCION', 'EL MONTO SUPERA EL MAXIMO PERMITIDO PARA CANAL DE DISTRIBUCION', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('ORDENREQUIEREAUTORIZACION', 'ORDEN REQUIERE AUTORIZACION SIEMPRE', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('DESCUENTOCOMISIONSUPERAPERMITIDO', 'DESCUENTO DE LA COMISION SUPERA EL PERMITIDO EN PLANTILLA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('FOTOCOPIACARNET', 'FOTOCOPIACARNET', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('ANTECEDENTESLEGALES', 'ANTECEDENTES LEGALES', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

-----------------------------------

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(50, 'CORREDORA', 'CLIENTEBLOQUEADO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(51, 'CORREDORA', 'PERSONABLOQUEADA', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(52, 'CORREDORA', 'OPERACIONSINFLUJOCUSTODIA', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(53, 'CORREDORA', 'EJECUTIVOSINPRODUCTO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(54, 'CORREDORA', 'EJECUTIVOSINRUTEO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(55, 'CORREDORA', 'EJECUTIVOPORTAFOLIOBLOQUEADO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(56, 'CORREDORA', 'MONTOOPERACIONSUPERAPERMITIDO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(57, 'CORREDORA', 'COMISIONSUPERAMAXIMOPERMITIDO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(58, 'CORREDORA', 'COMISIONBAJOMINIMOPERMITIDO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(59, 'CORREDORA', 'MONTOSUPERAMAXIMOCANALDISTRIBUCION', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(60, 'CORREDORA', 'ORDENREQUIEREAUTORIZACION', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(61, 'CORREDORA', 'DESCUENTOCOMISIONSUPERAPERMITIDO', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(62, 'CORREDORA', 'FOTOCOPIACARNET', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(63, 'CORREDORA', 'ANTECEDENTESLEGALES', 'INGRESOORDEN', 'RECHAZO', 0, 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1011, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Antecedentes Legales','ANTLEGAL', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1012, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Fotocopia Documento Identidad','FOTOCOPIA', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1014, 'PORTAFOLIO', 1, 0, 'COMBO', 'Libro de Ordenes','ORDLIBRO', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 26, 'N' );

--delete from "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" = 1014

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" (
	"IdInstrumento"						BIGINT NOT NULL,
	"IdEntidadClasificadoraRiesgo"		NVARCHAR(10) NOT NULL,
	"ValorClasificacionRiesgo"			NVARCHAR(20) NOT NULL,
   primary key ("IdInstrumento","IdEntidadClasificadoraRiesgo")
);

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.AnularAsignacionMasivaOptimizada_Entrada_Asignaciones"(
	"IdAsignacion"	 						BIGINT
) ON COMMIT DELETE ROWS;
 
 
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion"(
	"IdInterno"				BIGINT,
	"IdMovimiento"			BIGINT,
	"FechaSimulacion"		TIMESTAMP,
	"FechaIngresoRegistro"	TIMESTAMP,
	"IdTransaccion"			BIGINT,
	"FolioTransaccion"		NVARCHAR(40),
	"FechaTransaccion"		DATE,
	"FolioCartera"			BIGINT,
	"IdPortafolio"			BIGINT,
	"CodigoPortafolio"		NVARCHAR(100),
	"IdCliente"				BIGINT,
	"IdLibroOrdenes"		BIGINT,
	"IdInstrumento"			BIGINT,
	"Instrumento"			NVARCHAR(40),
	"IdTipoInstrumento"		NVARCHAR(3),
	"IdMonedaInstrumento"	NVARCHAR(3),
	"IdOrden"				BIGINT,
	"DocumentoIdentidad"	NVARCHAR(40),
	"NombreCompleto"		NVARCHAR(100),
	"CantidadAsignacion"	DECIMAL(26,8),
	"ValorRescateAsignacion"DECIMAL(26,8),
	"PorcentajeAsignacion"	DECIMAL(26,8),
	"PrecioSimulacion"		DECIMAL(26,8),
	"IdTipoProducto"		NVARCHAR(4),
	"IdTipoOperacion"		NVARCHAR(6),
	"IdTipoCanalIngreso"	NVARCHAR(3),
	"IdBolsa"				NVARCHAR(3),
	"IdFormaCustodia"		NVARCHAR(3),
	"IdBoveda"				NVARCHAR(40),
	"Glosa"					NVARCHAR(500),
	"TasaVentaCorta"		NVARCHAR(500),
	"PlazoVentaCorta"		NVARCHAR(500)
) ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes"(
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
	"IdBoveda"							NVARCHAR(40),
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
	"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
	"TasaVentaCorta"					DECIMAL(26,8),
	"TasaSimultanea"					DECIMAL(26,8),
	"TasaRFIF"							DECIMAL(26,8),
	"PlazoIF"							BIGINT,
	"PlazoSimultanea"					BIGINT,
	"PlazoVentaCorta"					BIGINT,
	"EsVentaCorta"						NVARCHAR(1),
	"EsOD"								NVARCHAR(1),
	"EsFungible"						NVARCHAR(1),
	"IdCorredorCorresponsal"			BIGINT,
	"MontoPlazo"						DECIMAL(26,8),
	"IdPersonaAutorizadoDarOrdenes"		BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3)
) ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Ordenes";
CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Ordenes" (
	"IdInterno"						BIGINT,
	"IdOrden"						BIGINT,
	"FechaRecepcion"				SECONDDATE,
	"FechaIngresoRegistro"			TIMESTAMP,
	"FechaIngresoRegistroCorta"		DATE,
	"FechaSolicitudAnulacion"		SECONDDATE,
	"FechaAnulacion"				SECONDDATE,
	"FechaOperacion"				SECONDDATE,
	"FechaVigencia"					DATE,
	"FechaVencimientoInstrumento"	DATE,
	"FechaEmision"					DATE,
	"FechaLiquidacion"				DATE,
	"FechaAceptacion"				SECONDDATE,
	"FechaAutorizacion"				DATE,
	"IdTipoVigenciaOrden"			NVARCHAR(2),
	"Glosa"							NVARCHAR(520),
	"IdEstadoOrden"					NVARCHAR(3),
	"Cantidad"						DECIMAL(26,8),
	"ValorRescate"					DECIMAL(26,8),
	"IdTipoReajuste"				NVARCHAR(3),
	"PrecioOrden"					DECIMAL(26,8),
	"PrecioMercado"					DECIMAL(26,8),
	"IndicadorFacturacionInmediata"	NVARCHAR(1),
	"IdCondicionDeLiquidacion"		NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazo" NVARCHAR(2),
	"IdTipoOperacion"				NVARCHAR(6),
	"IdPersonaCliente"				BIGINT,
	"IdCliente"						BIGINT,
	"IdPortafolioCliente"			BIGINT,
	"IdTipoCanalIngreso"			NVARCHAR(3),
	"IdLibroOrdenes"				BIGINT,
	"IdEmisor"						NVARCHAR(40),
	"IdEmpresa"						BIGINT,
	"IdBolsa"						NVARCHAR(3),
	"EspecificaBolsa"				NVARCHAR(1),
	"IdInstrumento"					BIGINT,
	"IdInstrumentoCustodia"			BIGINT,
	"IdTipoInstrumento"				NVARCHAR(3),
	"IdClasificacionTipoInstrumento" NVARCHAR(2),
	"IdProducto"					NVARCHAR(4),
	"IdInstrumentoSubyacente"		BIGINT,
	"Nemotecnico"					NVARCHAR(40),
	"IdGrupoCobro"					BIGINT,
	"TasaRFIF"						DECIMAL(26,8),
	"TasaVentaCorta"				DECIMAL(26,8),
	"TasaSimultanea"				DECIMAL(26,8),
	"PlazoIF"						BIGINT,
	"PlazoVentaCorta"				BIGINT,
	"PlazoSimultanea"				BIGINT,
	"EsVentaCorta"					NVARCHAR(1),
	"IdTipoPrestamo"				NVARCHAR(6),
	"IdMonedaOrden"					NVARCHAR(3),
	"IdMonedaLiquidacionOrden"		NVARCHAR(3),
	"IdMonedaLiquidacionCobros"		NVARCHAR(3),
	"TipoCambio"					DECIMAL(26,8),
	"MontoOrden"					DECIMAL(26,8),
	"MontoLiquidacion"				DECIMAL(26,8),
	"MontoLocal"					DECIMAL(26,8),
	"IdTipoPrecio"					NVARCHAR(1),
	"IdPorcentajeVisibilidad"		BIGINT,
	"IdFormaCustodia"				NVARCHAR(3),
	"OrdenVigente"					NVARCHAR(1),
	"IdEmpleadoEjecutivoPortafolio"	BIGINT, 
	"IdPersonaEjecutivoPortafolio"	BIGINT, 
	"IdEmpleadoDigitador"			BIGINT,
	"IdEstadoEmpleadoDigitador"		NVARCHAR(3),
	"IdMedioDePago"					NVARCHAR(3),
	
	"IdTipoLibroOrden"				NVARCHAR(6),
	"EsFungible"					NVARCHAR(2),
	"FechaOperacionCorta"			NVARCHAR(10),
	"IdEstadoInstrumento"			NVARCHAR(3),
	"IdInstrumentoReal"				BIGINT,
	"NombreTipoInstrumento"			NVARCHAR(100),
	"TamanoCorte"					DECIMAL(26,8),
	"IdMonedaInstrumento"			NVARCHAR(3),
	"NombreClasificacionTipoInstrumento"	NVARCHAR(100),
	"Tamano"						BIGINT,
	"IdTipoOperacionReal"			NVARCHAR(6),
	"NombreTipoOperacion"			NVARCHAR(100),
	"AccionOrden"					NVARCHAR(40),
	"IdBolsaReal"					NVARCHAR(3),
	"EsOD"							NVARCHAR(1),
	"MontoPlazo"					DECIMAL(26,8),
	"IdCondicionDeLiquidacionContadoReal"	NVARCHAR(3),
	"NombreCondicionDeLiquidacion" 			NVARCHAR(100),
	"IdCondicionDeLiquidacionPlazoReal" 	NVARCHAR(3),
	"IdTipoVigenciaReal"					NVARCHAR(2),
	"NombreTipoVigencia"					NVARCHAR(100),
	"IdBoveda"								NVARCHAR(40),
	"IdBovedaReal"							NVARCHAR(40),
	"IdFormaCustodiaReal"					NVARCHAR(3),
	"IdPorcentajeVisibilidadReal"			BIGINT,
	"IdTipoPrecioReal"						NVARCHAR(1),
	"NombreTipoPrecio"						NVARCHAR(100),
	"IdTipoCanalIngresoReal"				NVARCHAR(3),
	"DiasLiquidacionContado"				BIGINT,
	"IdTipoReajusteReal"					NVARCHAR(3),
	"InstrumentoSubyacente"					NVARCHAR(40),
	"InstrumentoPermiteVentaCorta"			NVARCHAR(1),
	"InstrumentoPermiteSimultanea"			NVARCHAR(1),
	"IdPerfilCobro"						NVARCHAR(40),
	"IdEstadoCliente"					NVARCHAR(3),
	"IdEstadoPortafolio"				NVARCHAR(3),
	"NombreCompleto"					NVARCHAR(100),
	"TipoDocumentoIdentidad"			NVARCHAR(1),
	"DocumentoIdentidad"				NVARCHAR(40),
	"IdDireccionNotificacion"			NVARCHAR(255),
	"IdCorredorCorresponsal"			BIGINT,
	"IdPersonaAutorizadoDarOrdenes"		BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3),
	"IdTipoCanalDistribucionReal"		NVARCHAR(3),
	"TotalCobros"						DECIMAL(26,8),
	"MontoTotal"						DECIMAL(26,8),
	"RuteoGeneraMovimientoFacturacionAutomatico"	NVARCHAR(1),
	"RuteoGeneraComprobanteFacturacionAutomatico"	NVARCHAR(1),
	"PlantillaEmpleadoMaximoDescuentoComision"		DECIMAL(26,8),
	"MontoMaximoDistribucionOrden"		DECIMAL(26,8),
	"CantidadCobros"					BIGINT NOT NULL DEFAULT 0,
	"PortafolioConFlujoCaja"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"PortafolioConSaldoCustodia"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	
	"ValidaHorario"						NVARCHAR(1),
	"AplazaOrdenPorHorarioNegociacion"	NVARCHAR(1),
	"AplazaOrdenPorModuloCerrado"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	"CumpleHorario"						NVARCHAR(1) NOT NULL DEFAULT 'S',
	"HorarioPreMarketInicio"			TIME,
	"HorarioPreMarketFin"				TIME,
	"HorarioMarketInicio"				TIME,
	"HorarioMarketFin"					TIME,
	
	"IdEstadoModulo"					NVARCHAR(1),
	"NombreModulo"						NVARCHAR(100),
	
	--VALIDACIONES PARAMETRICAS
	"ValidaSaldoCustodia"				NVARCHAR(1),
	"ValidaSaldoCaja"					NVARCHAR(1),
	"OperaLineaSobregiro"				NVARCHAR(1),
	
	--CONTADORES
	"ADVERTENCIA"						BIGINT,
	"AUTORIZACION"						BIGINT,
	"RECHAZO"							BIGINT
) ON COMMIT DELETE ROWS;

ALTER TABLE "BSCL.Plataforma.BD::PLA.SolicitudCambio" ALTER ("ValorAntiguo" NVARCHAR(255) NULL);
ALTER TABLE "BSCL.Plataforma.BD::PLA.SolicitudCambio" ALTER ("ValorNuevo" NVARCHAR(255) NULL);
ALTER TABLE "BSCL.Plataforma.BD::IND.Indicador" ALTER ("ValorIndicador" NVARCHAR(255) NULL);

CREATE UNIQUE INDEX "UK_IND_INDICADOR" ON "BSCL.Plataforma.BD::IND.Indicador"("IdEntidad", "IdTipoEntidad","IdTipoIndicador","ModeloCliente");

COMMIT;
