SET SCHEMA "BSCL";


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ORDER BY "IdServicio" desc

--ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdSistemaBursatil" NVARCHAR(40) NULL);

--ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD CONSTRAINT "fk_Orden_SistemaBursatil" 
--FOREIGN KEY("IdSistemaBursatil","IdEmpresa") REFERENCES "BSCL.Orden.BD::ORD.SistemaBursatil"("IdSistemaBursatil","IdEmpresa") ON DELETE RESTRICT;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20059,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearTransacciones',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
("IdEmpresa","CodigoParametro","Descripcion","Valor","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones")
SELECT 1, 'TasaReferenciaReportante','Tasa de referencia para operaciones de compra de reporte','0.5','Se carga en ingreso de ordenes de reportado','0.5','','S','S','RULLOA',CURRENT_UTCDATE,'','0001-01-01',''FROM DUMMY;

ALTER TABLE "BSCL.Plataforma.BD::INS.Instrumento" ADD ("PresenciaBursatilAjustada" INT NOT NULL DEFAULT 0);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(4201,'BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI',0,'Q','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(4202,'BSCL.Orden.BD.Vistas::ConsultarAsignacionReportesUI',0,'Q','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(4203,'BSCL.Orden.BD.Vistas::ConsultarOrdenPorNegociarReportes',0,'Q','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(4204,'BSCL.Orden.BD.Vistas::ConsultarOrdenEnNegociacionReportes',0,'Q','','','');
GO

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('MECANISMOCAPTACIONHABILITADO', 'MECANISMO CAPTACION HABILITADO PARA PORTAFOLIO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(202, 'CORREDORA', 'MECANISMOCAPTACIONHABILITADO', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(203, 'CORREDORA', 'MECANISMOCAPTACIONHABILITADO', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');


INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
("IdEmpresa","CodigoParametro","Descripcion","Valor","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones")
SELECT 1, 'FechaCargaTransaccionesBEC','Fecha para cargar transacciones de la BEC','0001-01-01','Para realizar pruebas de carga de palos BEC, si va en blanco, el sistema usa la fecha actual. De lo contrario, usa esta fecha','0001-01-01','','S','S','RULLOA',CURRENT_UTCDATE,'','0001-01-01',''FROM DUMMY;

UPDATE "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes" SET "Valor" = '2015-10-26'
WHERE "CodigoParametro" = 'FechaCargaTransaccionesBEC';

--LIBERAR TABLAS SERVICIO JAVA

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD ("IndicadorOrdenDirecta" NVARCHAR(2) NOT NULL DEFAULT '');
GO
UPDATE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" SET "IndicadorOrdenDirecta" = CASE WHEN "EsOD" = 'S' THEN 'OD' ELSE '' END;
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("EsOD");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("EsOE");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("MontoValorizado");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("ValorCaptacion");
GO
--ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD ("MontoLocal" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("MontoLocal");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Asignacion" DROP ("EsOD");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" DROP ("PagaderoEn");
GO
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD ("PagaderoEn" NVARCHAR(3) NOT NULL DEFAULT '');
GO

COMMIT;




