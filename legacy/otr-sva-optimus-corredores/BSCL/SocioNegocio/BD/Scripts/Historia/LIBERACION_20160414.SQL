-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 14-04-2016
--

--################################################
--     - Indicadores, Preferencias y Requisitos
--################################################



SET SCHEMA "BSCL";
UPDATE "BSCL.Plataforma.BD::REG.Pais" SET "UsaDivisionPais" = 'N' Where "IdPais" = 166;

ALTER TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" 
ADD("IdTipoPersona" NVARCHAR(1)    DEFAULT 'A'       NOT NULL);



update "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
set "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::IND.AgregarTipoIndicador'
where "IdServicio" = 122;

update "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
set "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::IND.ModificarTipoIndicador'
where "IdServicio" = 123;

update "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
set "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador'
where "IdServicio" = 63;

update "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
set "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador'
where "IdServicio" = 65;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(277,'BSCL.Plataforma.BD.Servicios::IND.ConsultarRequisito',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(278,'BSCL.Plataforma.BD.Servicios::IND.ModificarRequisito',0,'P','');


---carga de indicadores
SET SCHEMA "BSCL";

DELETE FROM "BSCL.Plataforma.BD::IND.TipoIndicador";
DELETE FROM "BSCL.Plataforma.BD::IND.DetalleIndicador";
--select * from "BSCL.Plataforma.BD::IND.TipoIndicador"

/*****************************
* INDICADORES
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1, 'PERSONA', 1, '', 'NVARCHAR', 'Know Your Costumer KYC','KYC', 'Generales', 'IND', 'ACT', 0, 0, 100, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (2, 'PERSONA', 1, 'N', 'CHECKBOX', 'Fatca','FATCA', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (3, 'PERSONA', 1, 'N', 'CHECKBOX', 'Persona Políticamente Expuesta PEP','PEP', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (4, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Inversionista Calificado','INVCALIF', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (5, 'PERSONA', 1, '', 'COMBO', 'Tipo Institución','TIPINST', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (1,5,'Banco');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (2,5,'Compañía Seguro');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (3,5,'AFP');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (4,5,'AGF');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (5,5,'Intermediario Valores');


insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (6, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Cobro de Custodia','COBROCUST', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (7, 'PERSONA', 1, 'N', 'CHECKBOX', 'Declaración de Residencia','DECRESID', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (8, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Administración de Cartera','ADMCART', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (9, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato APV','CNTAPV', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Derechos Privilegiados','DERPRIV', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (11, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Fondo Inversión Privado','FINVPRIV', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (12, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Administración Liquidez','CNTADMLIQ', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (13, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Custodia','CNTCUST', 'Generales', 'IND', 'ACT', 0, 0, 0, 'N' );

/*****************************
* REQUISITOS
******************************/

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (14, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Condiciones Generales Futuro','CCGF', 'Contratos', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (15, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Simultáneas','CS', 'Contratos', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (16, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Venta Corta','CVC', 'Contratos', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (17, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Condiciones de Forward','CCF', 'Contratos', 'REQ', 'ACT', 0, 1, 1, 'N' );

/*****************************
* PREFERENCIAS FACTURACION
******************************/
INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion") 
SELECT 18,'PORTAFOLIO',1,'N','CHECKBOX','FAC_SLFPTR','Separa linea Factura por Transacción','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion") 
SELECT 19,'PORTAFOLIO',1,'N','CHECKBOX','FAC_SFPINS','Separa Factura por Instrumento','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 20,'PORTAFOLIO',1,'N','CHECKBOX','FAC_FLFPPR', 'Fusiona Linea Factura por Precio','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 21,'PORTAFOLIO',1,'NORMAL','COMBO','FAC_MODFAC', 'Modo de Facturación','GRUPOFACTURACION',0,6,8,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 22,'PORTAFOLIO',1,'N','CHECKBOX','FAC_OCCINV','Opera contra Cuenta de Inversión','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 23,'PORTAFOLIO',1,'N','CHECKBOX','FAC_NIETES', 'Netea Ingresos/Egresos Tesorería','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 24,'PORTAFOLIO',1,'N','CHECKBOX','FAC_ENVFEM', 'Envía Factura por Email','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 25,'PORTAFOLIO',1,'','NVARCHAR','FAC_DIRELE', 'Email','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 26,'PORTAFOLIO',1,'','NVARCHAR','FAC_DIRFIS', 'Correo de despacho','GRUPOFACTURACION',0,1,1,'PRE','ACT','N' FROM DUMMY;

/*****************************
* PREFERENCIAS ORDENES
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (27, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Opera Nominal RV','ORDNOMIRV', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (28, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Opera Nominal IF','ORDNOMIIF', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (29, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Valida saldo cuenta Inversión Acciones Nacionales RV','ORDVSCIRV', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (30, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Valida saldo cuenta Inversión Acciones Nacionales IF/RF','ORDVSCIIRF', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (31, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Valida saldo cuenta Inversión OSA','ORDVSCIOSA', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

/*****************************
* PREFERENCIAS FORWARD
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (32, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Ingreso Operación Requiere Autorización','FRWDINGORA', 'GRUPOFORWARD', 'PRE', 'ACT', 0, 0, 0, 'N' );

/*****************************
* PREFERENCIAS NOTIFICACIONES
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (33, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Ingreso de Resumen de Operaciones Diarias','NOT_ENVROP', 'GRUPONOTIFICACIONES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (34, 'PORTAFOLIO', 1, '', 'NVARCHAR', 'Email de Despacho','NOT_EMAIL', 'GRUPONOTIFICACIONES', 'PRE', 'ACT', 0, 0, 100, 'N' );

/*****************************
* PREFERENCIAS CARTOLAS
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (35, 'PORTAFOLIO', 1, 'Mensual', 'COMBO', 'Periodicidad','CAR_PERIOD', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 100, 'N' );

--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (1,35,'Mensual');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (2,35,'Semestral');

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (36, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Envío de Cartola por Email','CAR_ENVCEM', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (37, 'PORTAFOLIO', 1, '', 'NVARCHAR', 'Email','CAR_DIRELE', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 100, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (38, 'PORTAFOLIO', 1, '', 'NVARCHAR', 'Dirección de Despacho','CAR_DIRFIS', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 100, 'N' );

/*****************************
* PREFERENCIAS FONDOS MUTUOS
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (39, 'PORTAFOLIO', 1, '', 'COMBO', 'Money Market Automático','FFMM_MMAUT', 'GRUPOFFMM', 'PRE', 'ACT', 0, 0, 100, 'N' );



UPDATE "BSCL.Plataforma.BD::IND.TipoIndicador" SET "RequiereAutorizacion" = 'N';



--################################################
--    JG - REQUISITOS OPERATIVOS
--################################################
DELETE FROM "BSCL.Plataforma.BD::REQ.CondicionOperativa";

INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PERNATFALLJURDIS','PERSONA NATURAL FALLECIDA/JURÍDICA DISUELTA','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PORTBLOQ','PORTAFOLIO BLOQUEADO','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDGENFUT','CONTRATO CONDICIONES GENERALES FUTURO','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTSIM','CONTRATO DE SIMULTANEAS','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTVENCOR','CONTRATO DE VENTA CORTA','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDFORW','CONTRATO CONDICIONES DE FORWARD','N','N','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(2,'INA');


DELETE FROM "BSCL.Plataforma.BD::REQ.TipoEtapa";

INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDEN','INGRESO ORDEN',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENSIM','INGRESO ORDEN SIMULTANEAS',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENVC','INGRESO ORDEN VENTA CORTA',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOFORWARD','INGRESO FORWARD',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ASIGNARORDEN','ASIGNAR ORDEN',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FACTURAR','FACTURAR',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ADVERTENCIA','ADVERTENCIA',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('RECHAZO','RECHAZO',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('AUTORIZACION','AUTORIZACION',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROCLI','Enrolamiento Cliente',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROPER','Enrolamiento Persona',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('NUEPORTF','Creación Portafolio',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMACN','Compra Acciones RV',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VENACN','Venta Acciones RV',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo"
("IdModeloRequisitoOperativo","Descripcion","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CORREDORA','MODELO DE CORREDORA',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');


ALTER TABLE "BSCL.Plataforma.BD::REQ.RequisitoOperativo" DROP ("IdTipoOperacionUsuario");


--################################################
--    MMACHMAR - Inscripcion Servicios REQUISITOS OPERATIVOS
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(270,'BSCL.Plataforma.BD.Servicios::REQ.CrearRequisitoOperativo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(271,'BSCL.Plataforma.BD.Servicios::REQ.ModificarRequisitoOperativo',0,'P','');


insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGGTA','INGGTA','INGRESO GARANTIA','INGRESO','CUST','N','N','N','N','N','INGRESO','INGRESO','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRGTA','EGRGTA','EGRESO GARANTIA','EGRESO','CUST','N','N','N','N','N','EGRESO','EGRESO','N','');


--################################################
--    JG - MODELO COMERCIAL
--################################################
SET SCHEMA BSCL;
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.RequiereAutorizacionPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobroDetalle";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilCobroDetalle";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobroDetalle"  ;
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearPerfilCobroDetalle";

UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobros'
WHERE "IdServicio" = 87  and "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobro';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(272,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobrosDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(273,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobrosDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(280,'BSCL.Plataforma.BD.Servicios::IND.ModificarPreferencia',0,'P','');

UPDATE "BSCL.Plataforma.BD::IND.TipoIndicador" SET "RequiereAutorizacion" = 'N';

UPDATE "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador" SET "LongitudMaxima" = 100 WHERE "CodigoTipoIndicador" IN ('FAC_DIRELE','FAC_DIRFIS');


create column table "BSCL.Facturacion.BD::FAC.ModoFacturacion" (
   "IdModoFacturacion" 				NVARCHAR(40) 	NOT NULL,
   "Nombre"          	 			NVARCHAR(100) 	NOT NULL DEFAULT '',
   primary key ("IdModoFacturacion")
);

INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ModoFacturacion"("IdModoFacturacion","Nombre")
VALUES('NORMAL','NORMAL');
INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ModoFacturacion"("IdModoFacturacion","Nombre")
VALUES('CONVENIO','CONVENIO');
INSERT INTO "BSCL"."BSCL.Facturacion.BD::FAC.ModoFacturacion"("IdModoFacturacion","Nombre")
VALUES('CONTRATO','CONTRATO');





----PROVEEDOR 14-04-2016
SET SCHEMA "BSCL";

 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PRO.AreaProveedor" (
		 "IdAreaProveedor" 		BIGINT CS_FIXED  NOT NULL ,
		 "NombreArea"	 		NVARCHAR(100)    NOT NULL DEFAULT '',
		 "IdEstado" 			NVARCHAR(3)		 NOT NULL DEFAULT 'ACT',
		 "Borrado" 				NVARCHAR(1) DEFAULT 'N',
		 PRIMARY KEY ("IdAreaProveedor")
 );
 
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(1,'PROVEDORES DE MATERIAS PRIMAS E INSUMOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(2,'PROVEEDORES DE MERCANCÍAS O PRODUCTOS TERMINADOS PARA LA COMERCIALIZACIÓN O DISTRIBUCIÓN','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(3,'PROVEEDORES DE MUEBLES Y ENSERES','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(4,'PROVEEDORES DE BIENES DE CAPITAL','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(5,'PROVEEDORES DE TECNOLOGÍA DURA Y BLANDA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(6,'PROVEEDORES DE SERVICIOS PÚBLICOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(7,'PROVEEDORES DE SUMINISTROS INDUSTRIALES Y DE OFICINA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(8,'PROVEEDORES DE SERVICIOS GENERALES','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(9,'PROVEEDORES DE HIGIENE Y SEGURIDAD INDUSTRIAL','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(10,'PROVEEDORES DE CONSULTORÍA, ASESORÍA Y ASISTENCIA TÉCNICA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(11,'PROVEEDORES DE SERVICIOS DE TRANSPORTE Y ALMACENAMIENTO','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(12,'PROVEEDORES DE SERVICIOS DE IMPORTACIÓN Y EXPORTACIÓN DE PRODUCTOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(13,'PROVEEDORES DE OUTSOURCING, LEASING Y FACTORING','ACT');



--------------------------------------------------
--      FIN  - 
--------------------------------------------------
