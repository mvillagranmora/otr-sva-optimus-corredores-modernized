SET SCHEMA "BSCL";


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20026,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearAsociaciones',0,'J','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20027,'PLUGIN_ORD.jar@cl.bcs.ordenes.AnularAsociaciones',0,'J','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20029,'PLUGIN_ORD.jar@cl.bcs.ordenes.ConfirmarOrdenes',0,'J','','','');
GO

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20047,'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarFormaDePagoUltimaOrden',0,'J','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20049,'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarPosicion',0,'J','','','');
GO

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_CheckCobroMonedaOperacion" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_IdMonedaCobroOtraMoneda" NVARCHAR(3) NULL);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_MontoFijo_MontoCobro" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_MontoFijo_CheckMontoOperacionMenor" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_MontoFijo_ValorMontoOperacionMenor" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_MontoFijo_CheckSiNo" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_MontoFijo_ValorMontoSiNo" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_ValorCobro" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckSobreMonto" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckSobreCantidad" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckMayorEntreDos" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckMenorEntreDos" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckMontoMinimo" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_ValorMontoMinimo" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_CheckMontoMaximo" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_ValorMontoMaximo" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualFijo_IdMonedaMinimoMaximo" NVARCHAR(3) NULL);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_PorcentualTasaPlazo_ValorCobro" DECIMAL(26,8) NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_Otros_CheckAfectoImpuesto" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_Otros_CheckPermiteAumentarValor" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_Otros_CheckPermiteDisminuirValor" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_Otros_CheckOperacionContado" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("_Otros_CheckOperacionPlazo" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.Bolsa" ADD ("EsExtranjera" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Facturacion.BD::GBL.CrearMovimientoComprobante_IN_MovimientoComprobanteCobro" ALTER ("IdCobro"	NVARCHAR(40));
GO
ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" ALTER ("IdCobro"	NVARCHAR(40) NOT NULL);
GO
ALTER TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteCobro" ALTER ("IdCobro" NVARCHAR(40));
GO

ALTER TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden" ADD ("EsLibroNormal" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden" ADD ("EsLibroOfertaPublica" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden" ADD ("IdTipoOperacionOfertaPublica" NVARCHAR(6) NULL);
GO
--CONFIGURAR TIPOS DE LIBRO, NORMAL O OFERTA PUBLICA

ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ADD CONSTRAINT "fk_TipoOperacionTipoInstrumento_TipoOperacion" 
FOREIGN KEY("IdTipoOperacion") REFERENCES "BSCL.Plataforma.BD::INS.TipoOperacion"("IdTipoOperacion") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ADD CONSTRAINT "fk_TipoOperacionTipoInstrumento_TipoInstrumento" 
FOREIGN KEY("IdTipoInstrumento") REFERENCES "BSCL.Plataforma.BD::INS.TipoInstrumento"("IdTipoInstrumento") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" ADD CONSTRAINT "fk_BolsaTipoInstrumento_IdBolsa" 
FOREIGN KEY("IdBolsa") REFERENCES "BSCL.Plataforma.BD::INS.Bolsa"("IdBolsa") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" ADD CONSTRAINT "fk_BolsaTipoInstrumento_IdTipoInstrumento" 
FOREIGN KEY("IdTipoInstrumento") REFERENCES "BSCL.Plataforma.BD::INS.TipoInstrumento"("IdTipoInstrumento") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.Instrumento" ADD CONSTRAINT "fk_Instrumento_IdTipoInstrumento" 
FOREIGN KEY("IdTipoInstrumento") REFERENCES "BSCL.Plataforma.BD::INS.TipoInstrumento"("IdTipoInstrumento") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" ADD CONSTRAINT "fk_TipoOperacion_IdTipoProducto" 
FOREIGN KEY("IdTipoProducto") REFERENCES "BSCL.Plataforma.BD::INS.TipoProducto"("IdTipoProducto") ON DELETE RESTRICT;
GO
ALTER TABLE "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ADD ("Borrado" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ADD ("IdUsuarioBorra" BIGINT NULL);
GO

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20050,'PLUGIN_ORD.jar@cl.bcs.plataforma.com.QuitarDetallePerfilCobro',0,'J','','','');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion" ADD ("EsOE" NVARCHAR(1) NOT NULL DEFAULT 'N');
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CorredorCorresponsal" ADD ("IdPersona" BIGINT NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CorredorCorresponsal" ADD ("IdSocioDeNegocio" BIGINT NOT NULL DEFAULT 0);
GO
ALTER TABLE "BSCL.Orden.BD::ORD.CorredorCorresponsal" ADD ("IdEmpresa" BIGINT NOT NULL DEFAULT 0);
GO

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("FechaSistemaCreacion" DATE NOT NULL DEFAULT '0001-01-01');
GO
UPDATE "BSCL"."BSCL.Orden.BD::ORD.Orden" SET "FechaSistemaCreacion" = TO_DATE("FechaOperacion");
GO

UPDATE "BSCL.Orden.BD::ORD.CorredorCorresponsal" set "IdEmpresa" = 1

--JS DE CREAR TRANSACCION
--SP DE CREAR TRANSACCION
--SP DE CREAR TRANSACCION MARKET DATA
--VISTA DE TRANSACCIONES

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(4198,'BSCL.Orden.BD.Servicios::ConsultarCustodiaGarantizableReportes',0,'P','','','');
GO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20052,'PLUGIN_PLA.jar@cl.bcs.plataforma.com.ConsultarMatrizOperacional',0,'J','','','');
GO


ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" ADD ("PermiteCobros" NVARCHAR(1) NOT NULL DEFAULT 'N');

commit 

update "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" SET "PermiteCobros" = 'S' 
WHERE "IdTipoOperacion" IN ('COMACN','VENACN','VENRF','COMRF','VENIIF','COMIIF','COMMON',
'VENMON','VENCFI','COMCFI','VENCFX','COMCFX','VENETN','COMETN','VENETX','COMETX','COMOSA',
'VENOSA','VENFUT','COMFUT','COMSIM','VENSIM','COMACX','VENACX','COMMIL','VENMIL'); 
COMMIT;rollback

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("TasaReportanteReferencia" DECIMAL(26,8) NOT NULL DEFAULT 0);

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "TipoServicio"= 'J',"RutaServicio" = 'PLUGIN_ORD.jar@cl.bcs.ordenes.RefacturarMovimiento' WHERE "IdServicio" = 297;
GO
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "TipoServicio"= 'J',"RutaServicio" = 'PLUGIN_ORD.jar@cl.bcs.ordenes.ObtenerDetalleRefacturacion' WHERE "IdServicio" = 296;
GO
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT")
VALUES(20054, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarComprobantesRefacturacion', 0, 'J', '', '', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT")
VALUES(20055, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarSaldoTransaccionesRefacturar', 0, 'J', '', '', '');

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
SET "TipoServicio" = 'J',
"RutaServicio" = 'PLUGIN_ORD.jar@cl.bcs.ordenes.AnularAsignaciones',
"TablasTempIN" = ''
WHERE "IdServicio" = 109;

COMMIT;




