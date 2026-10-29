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
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,5,'BANCO','Banco');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,5,'CIASGURO','Compañía Seguro');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,5,'AFP','AFP');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,5,'AGF','AGF');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,5,'INTERVALORES','Intermediario Valores');


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

--06/07/2016 Solicitado por Gabriel.
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (44, 'CLIENTE', 1, 'N', 'CHECKBOX', 'ES HABITUAL','ESHABITUAL', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N' );


 insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (64, 'PORTAFOLIO', 1, '1', 'COMBO', 'Envío carta Circularización','CIRCENVIO', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N' );
 
 --DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,64,'INTERNET','Internet');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,64,'MANUAL','Manual');

/*****************************
* REQUISITOS
******************************/

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (14, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Condiciones Generales Futuro','CCGF', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (15, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Simultáneas','CS', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (16, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Venta Corta','CVC', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (17, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Condiciones de Forward','CCF', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );


insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (46, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato Mercantil','CM', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (47, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Custodia','CC', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (48, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Condiciones Generales','CM', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (49, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Condiciones Generales Pacto','CCGP', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (50, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Canal Internet','CCI', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (51, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Administración de Liquidez','CM', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (52, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Contrato de Emprestitos','CM', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1011, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Antecedentes Legales','ANTLEGAL', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1012, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Fotocopia Documento Identidad','FOTOCOPIA', 'CONTRATOS', 'REQ', 'ACT', 0, 1, 1, 'N' );


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
SELECT 21,'PORTAFOLIO',1,'1','COMBO','FAC_MODFAC', 'Modo de Facturación','GRUPOFACTURACION',0,6,8,'PRE','ACT','N' FROM DUMMY;

	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,21,'NORMAL','NORMAL');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,21,'CONTRATO','CONTRATO Seguro');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,21,'CONVENIO','CONVENIO');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","CodigoTipoIndicador",
"NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion")
SELECT 22,'PORTAFOLIO',1,'N','CHECKBOX','GEN_OCCINV','Opera contra Cuenta de Inversión','GRUPOGENERAL',0,1,1,'PRE','ACT','N' FROM DUMMY;

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
VALUES (27, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Opera Custodia RV','ORDOCUSTRV', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (28, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Opera Custodia RF/IF','ORDOCUSTRF', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (29, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Permite vender nominal RV','ORDVNNOMRV', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (30, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Permite vender nominal RF/IF','ORDVNNOMRF', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (45, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Linea Sobregiro Cuenta Inversion','ORDLNSGCTA', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );



insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (43, 'PORTAFOLIO', 1, 'N', 'NVARCHAR', 'Email de Notificación','ORDMAILNTF', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );




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
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,35,'MENSUAL','Mensual');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,35,'SEMESTRAL','Semestral');

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



insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (40, 'CLIENTE', 1, '', 'COMBO', 'Clase de Orden','CLASEORDEN', 'Generales', 'IND', 'ACT', 0, 0, 100, 'N' );
	--DETALLE
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,40,'OE','Orden Escrita');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,40,'OV','Orden Verbal');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,40,'OD','Operador Directo');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,40,'OR','Sistema de Ruteo');
	/*INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,40,'ADMCARTERA','Administración Cartera');*/
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,40,'OM','Orden por otro medio');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (6,40,'TM','Cualquier medio');


insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (41, 'CLIENTE', 1, '', 'COMBO', 'Procedencia','PROCEDENC', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N' );

	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,41,'01','Transacción por cuenta propia');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,41,'02','Transacción por cuenta de terceros no relacionados');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,41,'03','Transacción de persona relacionada, en virtud de las definiciones contenidas en el artículo 100 de la Ley N° 18.045');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,41,'04','Transacción de persona que en virtud de la letra c) del artículo 166 de la ley N° 18.045, se presume tiene acceso a información privilegiada');
	
/*****************************
* PREFERENCIAS MESA
******************************/
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (53, 'PORTAFOLIO', 1, '', 'COMBO', 'Código Sector Económico','MES_SECECO', 'GRUPOMESA', 'PRE', 'ACT', 0, 0, 0, 'N' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (1,53,'11','Agricultura y Caza (Cultivo,pecuario)');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (2,53,'13','Pesca y Explotacion de Criadero o Vivero de Peces');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (3,53,'12','Sivicultura y Extracción de Madera');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (4,53,'61','Comercio al Por Mayor');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (5,53,'62','Comercio al Por Menor');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (6,53,'63','Restaurante y Hoteles');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (7,53,'41','Construcción');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (8,53,'51','Electricidad,Gas y Vapor');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (9,53,'52','Obras Hidráulicas y Suministro de Agua');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (10,53,'37','Fabricación de Productos Metálicos,Maquinaria y Equipos');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (11,53,'34','Fabricación de Sustancias Químicas y de Productos Farmacéuticos y Medicamentos');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (12,53,'33','Industrias de la Madera,Celulosa y del Papel,Imprenta y Editoriales');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (13,53,'36','Industrias Metálicas Basicas');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (14,53,'31','Productos Alimenticios,Bebidas y Tabaco');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (15,53,'35','Productos de Fabricación de Loza Vidrio,Cemento y Yeso');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (16,53,'32','Textiles,Prendas de Vestir e Industrias del Cuero');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (17,53,'21','Explotación de Minas de Carbón');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (18,53,'23','Explotación de Minerales Metálicos');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (19,53,'24','Extracción de Otros Minerales');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (20,53,'22','Producción de Pretóleo Crudo y Gas Natural');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (21,53,'83','Bienes Inmuebles y Servicios prestados a Empresas');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (22,53,'81','Establecimientos financieros,Servicios Financieros,Seguros,Fondos de Pensiones');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (23,53,'82','Servicios Comunales,Sociales y Personales');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (24,53,'74','Comunicaciones');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (25,53,'73','Transporte Aereo');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (26,53,'72','Transporte por Agua');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
		VALUES (27,53,'71','Transporte Terrestre,por Tuberías y Almacenamiento');
		

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (54, 'PORTAFOLIO', 1, '', 'NVARCHAR', 'Código OTC','MES_CODOTC', 'GRUPOMESA', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (55, 'PORTAFOLIO', 1, '', 'NVARCHAR', 'Nombre Datatec','MES_DATTEC', 'GRUPOMESA', 'PRE', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")
values (1001,'CLIENTE','1','N','CHECKBOX','Convenio de Pago Derechos Bolsa','GENERALES',0,0,0,'IND','ACT','N','DRCHCNV','A','N',0,0);





