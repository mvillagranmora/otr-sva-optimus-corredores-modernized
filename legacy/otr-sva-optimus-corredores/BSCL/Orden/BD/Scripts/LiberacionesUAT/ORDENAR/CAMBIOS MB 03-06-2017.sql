SET SCHEMA "BSCL";


create column table "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" (
   "IdEmpresa"       BIGINT NOT NULL,
   "CodigoParametro" NVARCHAR(40) NOT NULL,
   "Descripcion"     NVARCHAR(520) NOT NULL,
   "Valor"           NVARCHAR(520) NOT NULL, 
 primary key ("IdEmpresa", "CodigoParametro")
);

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" DROP ("Plazo");
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" DROP ("Tasa");

--LIBERAR TABLAS CREAR ORDENES

DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso"(
	"IdInterno"	 						BIGINT,
	"IdBoveda"							NVARCHAR(40),
	"IdFormaCustodia"					NVARCHAR(3),
	"IdTipoVigencia"					NVARCHAR(2),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCondicionDeLiquidacion"			NVARCHAR(2),
	"IdPorcentajeVisibilidad"			BIGINT,
	"IdTipoPrecio"						NVARCHAR(1),
	"IdPortafolio"						BIGINT,
	"IdTipoCanalIngreso"				NVARCHAR(3),
	"IdTipoReajuste"					NVARCHAR(3),
	"IdBolsa"							NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3)
	
) ON COMMIT DELETE ROWS;


ALTER TABLE "BSCL.Orden.BD::ORD.ControlMercado" DROP PRIMARY KEY;

ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("IdInstrumento" BIGINT NULL);
ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("IdClienteOfertante" BIGINT NULL);
ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("Cantidad" DECIMAL(26,8) NULL);
ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("IdPlantillaCobros" NVARCHAR(40) NULL);
ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("FechaApertura" DATE NOT NULL DEFAULT '0001-01-01');
ALTER TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" ADD ("FechaCierre" DATE NOT NULL DEFAULT '0001-01-01');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3593,'BSCL.Orden.BD.Vistas::ConsultarCompraVenta',0,'Q','','','');

alter table "BSCL.Orden.BD::ORD.Orden" ADD ("OrdenEspecial" NVARCHAR(1) NOT NULL DEFAULT 'N');
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("MotivoAnulacion" NVARCHAR(5000) NULL);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("MontoOrdenPlazo" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("PrecioPlazo" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("MontoLiquidacionPlazo" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("MontoLocalPlazo" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("IdCuentaCustodioPortafolio" BIGINT NULL);
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("EsPorMonto" NVARCHAR(1) NOT NULL DEFAULT 'N');
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("PorcentajeCobros" DECIMAL(26,8) NOT NULL DEFAULT 0);

alter table "BSCL.Orden.BD::ORD.Orden" DROP ("EsConfirmada");
alter table "BSCL.Orden.BD::ORD.Orden" ADD ("EsConfirmada" NVARCHAR(1) NOT NULL DEFAULT 'N');

DROP TABLE "BSCL.Plataforma.BD::COM_2.TipoCobro";
CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.TipoCobro" (
	"IdTipoCobro"					NVARCHAR(40) NOT NULL,
	"NombreTipoCobro"				NVARCHAR(100) NOT NULL,
	"PermiteMontoFijo"				NVARCHAR(1) NOT NULL,
	"PermitePorcentajeFijo"			NVARCHAR(1) NOT NULL,
	"PermitePorcentajeRangos"		NVARCHAR(1) NOT NULL,
	"PermitePorcentajeTasaPlazo"	NVARCHAR(1) NOT NULL,
	PRIMARY KEY ("IdTipoCobro")
);

DELETE FROM "BSCL.Plataforma.BD::COM_2.TipoCobro";
INSERT INTO  "BSCL.Plataforma.BD::COM_2.TipoCobro"("IdTipoCobro","NombreTipoCobro","PermiteMontoFijo","PermitePorcentajeFijo","PermitePorcentajeRangos","PermitePorcentajeTasaPlazo")
SELECT 'MONTOFIJO','MONTO FIJO','S','N','N','N' FROM DUMMY UNION ALL
SELECT 'PORCENTAJEFIJO','PORCENTAJE FIJO','N','S','N','N' FROM DUMMY UNION ALL
SELECT 'PORCENTAJERANGOS','PORCENTAJE POR RANGOS','N','N','S','N' FROM DUMMY UNION ALL
SELECT 'PORCENTAJETASAPLAZO','PORCENTAJE SOBRE TASA/PLAZO','N','N','N','S' FROM DUMMY;


ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("PorcentualTasaPlazo_ValorCobro" DECIMAL(26,8) NOT NULL DEFAULT 0);

--ARREGLO TABLA DE CATEGORIA INVERSIONISTA
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" DROP CONSTRAINT "FK_CategoriaInversionista_Cliente";
ALTER TABLE "BSCL.Plataforma.BD::COM.CategoriaInversionista" DROP PRIMARY KEY;


DELETE FROM "BSCL.Plataforma.BD::COM.CategoriaInversionista";
ALTER TABLE "BSCL.Plataforma.BD::COM.CategoriaInversionista" ADD CONSTRAINT PK_CategoriaInversionista PRIMARY KEY ("IdCategoriaInversionista", "IdEmpresa");
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" ADD CONSTRAINT FK_CategoriaInversionista_Cliente FOREIGN KEY("IdCategoriaInversionista","IdEmpresa") 
	REFERENCES "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","IdEmpresa");


INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('ARRIESGADO','Arriesgado',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('BALANCEADO','Balanceado',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('CONSERVADOR','Conservador',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('CALIFICADO','Calificado',1);


ALTER TABLE "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" DROP PRIMARY KEY;
ALTER TABLE "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" ADD CONSTRAINT PK_UsoTipoRegistroParidadYTipoCambio PRIMARY KEY ("IdEmpresa", "IdUso","IdTipoRegistro");


ALTER TABLE "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio" DROP PRIMARY KEY;
ALTER TABLE "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio" ADD CONSTRAINT PK_UsoParidadYTipoCambio PRIMARY KEY ("IdEmpresa", "IdUso");

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ALTER ("IdCobro" NVARCHAR(40) NOT NULL);

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("TasaPromedioReporte" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("IdTipoCustodia");

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(20031,'PLUGIN_ORD.jar@cl.bcs.ordenes.ModificarOrdenes',0,'J','');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoVentaCortaX" DECIMAL(26,8));
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoVentaCortaX" = "PlazoVentaCorta";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoVentaCorta");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoVentaCorta" BIGINT NOT NULL DEFAULT 0);
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoVentaCorta" = "PlazoVentaCortaX";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoVentaCortaX");

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoReporteX" DECIMAL(26,8));
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoReporteX" = "PlazoReporte";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoReporte");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoReporte" BIGINT NOT NULL DEFAULT 0);
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoReporte" = "PlazoReporteX";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoReporteX");

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoSimultaneaX" DECIMAL(26,8));
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoSimultaneaX" = "PlazoSimultanea";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoSimultanea");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoSimultanea" BIGINT NOT NULL DEFAULT 0);
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoSimultanea" = "PlazoSimultaneaX";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoSimultaneaX");

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoIFX" DECIMAL(26,8));
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoIFX" = "PlazoIF";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoIF");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoIF" BIGINT NOT NULL DEFAULT 0);
UPDATE "BSCL.Orden.BD::ORD.Orden" SET "PlazoIF" = "PlazoIFX";
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("PlazoIFX");

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20032,'PLUGIN_ORD.jar@cl.bcs.ordenes.EnviarAsignacionesFacturar',0,'J','','','');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("OrdenEspecial");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("EsOrdenEspecial" NVARCHAR(1) NOT NULL DEFAULT 'N');

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'MontoMaximoOrden','Monto maximo de orden permitido por la corredora en moneda local',0 from dummy;

UPDATE "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes" SET "Valor" = 0 where "CodigoParametro" = 'MontoMaximoOrden';

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("IdAsociacion" BIGINT NOT NULL DEFAULT 0);

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.FormaDePagoOrden"(
	"IdOrden" 						BIGINT, 
	"IdPreferenciaPagoPortafolio"	BIGINT,
    "IdMedioDePago"					NVARCHAR(3), 
    "IdCuentaBancaria" 				BIGINT, 
    "IdTipoCuentaBancaria" 			NVARCHAR(4),
    "NumeroCuentaBancaria" 			NVARCHAR(100), 
    "IdTarjetaBancaria" 			BIGINT,
    "IdTipoTarjetaBancaria" 		NVARCHAR(40), 
    "NumeroTarjetaBancaria" 		NVARCHAR(100),
    "IdModalidadDePago" 			NVARCHAR(6), 
    "IdBanco" 						BIGINT
);
    
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1672,'BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio',0,'Q','');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("IdMedioDePago");
commit
LIBERAR TABLAS SIMULACION ASIGNACION

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::COM_2.BaseTasaCobro" ALTER ("ValorBaseTasaCobro" BIGINT NOT NULL);
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" DROP ("BaseReporte");
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" ADD ("BaseTasa" BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" ADD ("TipoInteres" NVARCHAR(1) NOT NULL DEFAULT '');
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" ADD ("FechaModificacion" TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00');

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" DROP ("IdMonedaLiquidacionCobros");

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" drop ("DiasPlazo");
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" drop ("IdPerfilCobro");
ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" drop ("DiasPlazo");
ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" drop ("IdPerfilCobro");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdPerfilCobros" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("ValorCobroFinal" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" ADD ("ValorCobroFinal" DECIMAL(26,8) NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("PermiteCambiarValor" NVARCHAR(1) NOT NULL DEFAULT 'N');
CREATE UNIQUE INDEX "idx_unique_TipoOperacionTipoInstrumento" ON "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"("IdTipoOperacion", "IdTipoInstrumento");
	
	
SELECT "IdTipoOperacion","IdTipoInstrumento",count(1) FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" 
group by "IdTipoOperacion","IdTipoInstrumento"
having count(1) > 1

SELECT * FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" WHERE "IdTipoOperacion" = 'COMRF'  and "IdTipoInstrumento" = 'BTP'

IdTipoOperacionTipoInstrumento;IdTipoOperacion;IdTipoInstrumento;IdEstado;IdInstrumento;Nemotecnico
235;COMRF;BTP;ACT;0;
IdTipoOperacionTipoInstrumento;IdTipoOperacion;IdTipoInstrumento;IdEstado;IdInstrumento;Nemotecnico
236;COMRF;BTU;ACT;0;
delete from 	 "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" WHERE "IdTipoOperacionTipoInstrumento" in (235,236);
commit	
	
	
LIBERAR TABLAS DE CREAR ORDENES


insert into "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
select 'PLACEHOLDER_FECHA','PLACEHOLDER_FECHA','DD-MM-YYYY',1,3,'0001-01-01','0001-01-01','ACT' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
select 'PLACEHOLDER_EMAIL','PLACEHOLDER_EMAIL','alguien@dominio.com',1,3,'0001-01-01','0001-01-01','ACT' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
select 'PLACEHOLDER_TELEFONO_MOVIL','PLACEHOLDER_TELEFONO_MOVIL','0051-XXXXXXXXX',1,3,'0001-01-01','0001-01-01','ACT' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
select 'PLACEHOLDER_TELEFONO_MOVIL','PLACEHOLDER_TELEFONO_MOVIL','09-XXXXXXXX',1,3,'0001-01-01','0001-01-01','ACT' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
select 'PLACEHOLDER_TELEFONO','PLACEHOLDER_TELEFONO','0051-1-XXXXXXX',1,3,'0001-01-01','0001-01-01','ACT' from dummy;

--delete from  "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" where "IdValorDefectoSistema" = 'PLACEHOLDER_TELEFONO'

DELETE FROM "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" WHERE "Controlador" = 'ConsultarOrdenesCtrl' and "NombreObjeto" = 'combobox-clasificacion-tipo-instrumento'

ALTER TABLE "BSCL.Orden.BD::ORD.GrupoPortafolio" ADD ("PorcentajeX" DECIMAL(26,8) NOT NULL DEFAULT 0);
UPDATE "BSCL.Orden.BD::ORD.GrupoPortafolio" SET "PorcentajeX" = "Porcentaje";
ALTER TABLE "BSCL.Orden.BD::ORD.GrupoPortafolio" drop ("Porcentaje");
ALTER TABLE "BSCL.Orden.BD::ORD.GrupoPortafolio" ADD ("Porcentaje" DOUBLE NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.GrupoPortafolio" drop ("PorcentajeX");

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoSubyacente"
SELECT 5000,'RRV','BCS','ACN',502 FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoSubyacente"
SELECT 5001,'RRF','BCS','BEM',330184 FROM DUMMY;

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD ("BaseTasa" BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Transaccion" ADD ("TipoInteres" NVARCHAR(1) NOT NULL DEFAULT '');

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
select 1,'MAN','TRA','RRV','BCS','N','N',0 from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
select 1,'MAN','TRA','RRF','BCS','N','N',0 from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
SELECT 5000,'RRV','BCS','ACT','N','',0 FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
SELECT 5001,'RRF','BCS','ACT','N','',0 FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConceptoOrden"
SELECT 29,'VNRERV','VENRRV' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConceptoOrden"
SELECT 30,'VNRERF','VENRRF' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConceptoOrden"
SELECT 31,'CMRERV','COMRRV' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConceptoOrden"
SELECT 32,'CMRERF','COMRRF' FROM DUMMY;


INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
SELECT 59,'VNRERV','VENRRV','ORD','VIG' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
SELECT 60,'VNRERF','VENRRF','ORD','VIG' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
SELECT 61,'CMRERV','COMRRV','ORD','VIG' FROM DUMMY;
INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
SELECT 562,'CMRERF','COMRRF','ORD','VIG' FROM DUMMY;


CREATE UNIQUE INDEX "idx_unique_InstrumentoNemotecnico" ON "BSCL.Plataforma.BD::INS.Instrumento"("Nemotecnico");

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('ORDENPERMANENTEPRECIOMERCADO', 'ORDEN PERMANENTE A PRECIO MERCADO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(200, 'CORREDORA', 'ORDENPERMANENTEPRECIOMERCADO', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(201, 'CORREDORA', 'ORDENPERMANENTEPRECIOMERCADO', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20044,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearSimulacionAsignacion',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20045,'PLUGIN_ORD.jar@cl.bcs.ordenes.CargaAsignacionesPostreras',0,'J','','','');

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.CobroAsignacion" ALTER("IdCobro" NVARCHAR(40) NOT NULL);

SELECT * FROM "BSCL.Plataforma.BD::PLA.Formato"

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 250,'FolioTransaccion','STRING',0,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 251,'FechaTransaccion','STRING',1,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 252,'Nemotecnico','STRING',2,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 253,'CompraVenta','STRING',3,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 254,'TipoDocumentoIdentidad','STRING',4,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 255,'DocumentoIdentidad','STRING',5,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 256,'CodigoPortafolio','STRING',6,0,'S','','','N',0,'N' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
SELECT 257,'Cantidad','NUMBER',7,0,'S','','','S',2,'N' FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera","IdModulo")
SELECT 100,1,'ASIGNACION_CLIENTE','S','\t','N','ORD' FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
SELECT 400,100,250 FROM DUMMY UNION ALL 
SELECT 401,100,251 FROM DUMMY UNION ALL 
SELECT 402,100,252 FROM DUMMY UNION ALL 
SELECT 403,100,253 FROM DUMMY UNION ALL 
SELECT 404,100,254 FROM DUMMY UNION ALL 
SELECT 405,100,255 FROM DUMMY UNION ALL 
SELECT 406,100,256 FROM DUMMY UNION ALL 
SELECT 407,100,257 FROM DUMMY;

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia"
("IdTipoOperacion","IdEmpresa","IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia","PoseeInstrumentoSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva","RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado","UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion","IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno","OrdenEjecucionMovimientos","CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING","CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR","CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING","CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR","CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING","CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR","CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR","CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING","CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR","CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING","CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR","CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING","CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR","CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR","IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro","PermiteRetiroParcialSubyacente","BaseCalculoDeInteres","PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado")
select "IdTipoOperacion",1313 AS "IdEmpresa","IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia","PoseeInstrumentoSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva","RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado","UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion","IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno","OrdenEjecucionMovimientos","CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING","CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR","CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING","CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR","CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING","CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR","CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR","CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING","CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR","CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING","CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR","CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING","CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR","CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR","IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro","PermiteRetiroParcialSubyacente","BaseCalculoDeInteres","PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado"
FROM BSCL."BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia"

alter table BSCL."BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" drop primary key 

ALTER TABLE BSCL."BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" 
ADD CONSTRAINT PK_ConfiguracionMovimientoCustodia PRIMARY KEY ("IdTipoOperacion", "IdEmpresa");

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20046,'PLUGIN_ORD.jar@cl.bcs.ordenes.ReservarLiberarTransacciones',0,'J','','','');

ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion" ALTER ("IdUsuarioReserva" BIGINT NULL);

update "BSCL"."BSCL.Orden.BD::ORD.Transaccion" set "IdUsuarioReserva" = null where "IdUsuarioReserva" = 0


COMMIT;




