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
-- LIBERACION SEMANA: 04-07-2016
--

--####################################################################################
/*INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1371,'BSCL.Facturacion.BD.Servicios::EnviarComprobante',0,'P','');*/

/*alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdInstrumentoSubyacente" BIGINT DEFAULT 0);

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdInstrumentoSubyacente" BIGINT DEFAULT 0); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdMonedaSubyacente" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdMonedaSubyacente" NVARCHAR(3) DEFAULT ''); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdTransaccion" BIGINT DEFAULT 0); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("FechaComprobanteDetalle" SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00'); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NemotecnicoSubyacente" NVARCHAR(40) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdTipoInstrumento" NVARCHAR(3) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdTipoInstrumentoSubyacente" NVARCHAR(3) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NombreTipoInstrumento" NVARCHAR(100) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NombreTipoInstrumentoSubyacente" NVARCHAR(100) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NemotecnicoSubyacente" NVARCHAR(40) DEFAULT '');
  
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdTipoInstrumento" NVARCHAR(3) DEFAULT ''); 

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdTipoInstrumentoSubyacente" NVARCHAR(3) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NombreTipoInstrumento" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NombreTipoInstrumentoSubyacente" NVARCHAR(100) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdClasificacionTipoInstrumento" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdInstrumentoRelacionado" BIGINT DEFAULT 0); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("IdTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NemotecnicoRelacionado" NVARCHAR(40) DEFAULT ''); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NombreTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
 add ("NombreClasificacionTipoInstrumento" NVARCHAR(100) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdClasificacionTipoInstrumento" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdClasificacionTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdInstrumentoRelacionado" BIGINT DEFAULT 0); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("IdTipoInstrumentoRelacionado" NVARCHAR(3) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NemotecnicoRelacionado" NVARCHAR(40) DEFAULT ''); 

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NombreClasificacionTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NombreTipoInstrumentoRelacionado" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
 add ("NombreClasificacionTipoInstrumento" NVARCHAR(100) DEFAULT '');  

alter table "BSCL.Facturacion.BD::FAC.TipoComprobante"
 add ("TipoComprobanteFacturacion" NVARCHAR(3) DEFAULT '');      
*/

--SEMANA 18-07-2016
/*insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(50,'FEVNIF','','FAE','INIF','VENIIF','TOTAL');  

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") 
VALUES ('FEVNIF','','FAC','FACTURA ELECTRONICA VENTA IF','VIG');


alter table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
drop ("NombreEntidad");

Delete from "BSCL.Facturacion.BD::FAC.ConceptoFacturacion";


Delete from "BSCL.Plataforma.BD::PLA.Concepto"
where "IdModulo" = 'FAC';

Alter table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"     
drop ("IdTipoComprobante");

alter table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
drop ("IdTipoProducto");

alter table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
add ("Origen" NVarchar(3) Default '' NOT NULL);

alter table "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
drop ("CodigoPreferencia");

insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (1, 'CMOTCN', 'COMOTR', 'FAC');

insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (2, 'VNOTCN', 'VENOTR', 'FAC');

insert into "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre","IdEstado")
Values ('CMOTCN', 'FAC', 'COMPRA OTROS CONCEPTOS', 'VIG');

insert into "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre","IdEstado")
Values ('VNOTCN', 'FAC', 'VENTA OTROS CONCEPTOS', 'VIG');

ANTSIC	ANTICIPO SIMULTANEA COMPRA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (3, 'ANTSIC', 'ANSIMC', 'CUS');
ANTSIV	ANTICIPO SIMULTANEA VENTA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (4, 'ANTSIV', 'ANSIMV', 'CUS');
VCTSIC	VENCIMIENTO SIMULTANEA COMPRA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (5, 'VCTSIC', 'VESIMC', 'CUS');
VCTSIV	VENCIMIENTO SIMULTANEA VENTA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (6, 'VCTSIV', 'VESIMV', 'CUS');
ANTEMP	ANTICIPO EMPRESTITO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (7, 'ANTEMP', 'ANTEMP', 'CUS');
VENEMP	VENCIMIENTO EMPRESTITO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (8, 'VENEMP', 'VENEMP', 'CUS');
ANTPRT	ANTICIPO PRESTAMO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (9, 'ANTPRT', 'ANTPRT', 'CUS');
VENPRT	VENCIMIENTO PRESTAMO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (10, 'VENPRT', 'VENPRT', 'CUS');
INGPRT	INGRESO PRESTAMO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (11, 'INGPRT', 'PRESTA', 'CUS');

--------------------------------------------------------------------
CMACNA	COMPRA ACCIONES NACIONALES
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (12, 'CMACNA', 'COMACN', 'ORD');
CMACEX	COMPRA ACCIONES EXTRANJERAS
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (13, 'CMACEX', 'COMACX', 'ORD');
CMCFIN	COMPRA CFI NACIONAL
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (14, 'CMCFIN', 'COMCFI', 'ORD');
CMCFMN	COMPRA CFM
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (15, 'CMCFMN', 'COMCFM', 'ORD');
CMCFEX	COMPRA CFI EXTRANJERO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (16, 'CMCFEX', 'COMCFX', 'ORD');
CMETFN	COMPRA ETF NACIONAL
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (17, 'CMETFN', 'COMETN', 'ORD');
CMETFX	COMPRA ETF EXTRANJERO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (18, 'CMETFX', 'COMETX', 'ORD');
CMDVFU	COMPRA FUTUROS
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (19, 'CMDVFU', 'COMFUT', 'ORD');
CMINFI	COMPRA INTERMEDIACION FINANCIERA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (20, 'CMINFI', 'COMIIF', 'ORD');
CMACMI	COMPRA ACCIONES MILA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (21, 'CMACMI', 'COMMIL', 'ORD');
CMMONE	COMPRA MONETARIO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (22, 'CMMONE', 'COMMON', 'ORD');
CMOSAC	COMPRA OSA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (23, 'CMOSAC', 'COMOSA', 'ORD');
CMINRF	COMPRA MERCADO RENTA FIJA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (24, 'CMINRF', 'COMRF', 'ORD');
CMSIMU	COMPRA DE SIMULTÁNEA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (25, 'CMSIMU', 'COMSIM', 'ORD');
VNACNA	VENTA ACCIONES NACIONALES
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (26, 'VNACNA', 'VENACN', 'ORD');
VNACEX	VENTA ACCIONES EXTRANJERAS
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (27, 'VNACEX', 'VENACX', 'ORD');
VNCFIN	VENTA CFI NACIONAL
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (28, 'VNCFIN', 'VENCFI', 'ORD');
VNCFMN	VENTA CFM
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (29, 'VNCFMN', 'VENCFM', 'ORD');
VNCFEX	VENTA CFI EXTRANJERO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (30, 'VNCFEX', 'VENCFX', 'ORD');
VNETFN	VENTA ETF NACIONAL
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (31, 'VNETFN', 'VENETN', 'ORD');
VNETFX	VENTA ETF EXTRANJERO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (32, 'VNETFX', 'VENETX', 'ORD');
VNDVFU	VENTA FUTUROS
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (33, 'VNDVFU', 'VENFUT', 'ORD');
VNINFI	VENTA INTERMEDIACION FINANCIERA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (34, 'VNINFI', 'VENIIF', 'ORD');
VNACMI	VENTA ACCIONES MILA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (35, 'VNACMI', 'VENMIL', 'ORD');
VNMONE	VENTA MONETARIO
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (36, 'VNMONE', 'VENMON', 'ORD');
VNOSAC	VENTA OSA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (37, 'VNOSAC', 'VENOSA', 'ORD');
VNINRF	VENTA MERCADO RENTA FIJA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (38, 'VNINRF', 'VENRF', 'ORD');
VNSIMU	VENTA DE SIMULTÁNEA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (39, 'VNSIMU', 'VENSIM', 'ORD');
SUECOS	SUSCRIPCION OSA
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")
Values (40, 'SUECOS', 'SUSOSA', 'CUS');
 
update "BSCL.Plataforma.BD::INS.TipoOperacion"
set "Accion" = 'COMPRA'
Where "IdTipoOperacion" = 'VENPRT';

update "BSCL.Plataforma.BD::INS.TipoOperacion"
set "Accion" = 'COMPRA'
Where "IdTipoOperacion" = 'ANTPRT';

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  
drop ("IdConceptoMovimientoRelacionado");
*/


--SEMANA 25-07-2016
/*Update "BSCL.Plataforma.BD::MAN.Mantenedor"
Set "TablaMantenedor" = '"BSCL.Plataforma.BD::PLA.Moneda"' 
Where "IdMantenedor" = 'MONEDA';

Update "BSCL.Plataforma.BD::MAN.Mantenedor"
Set "TablaMantenedor" = '"BSCL.Plataforma.BD::PLA.Modulo"' 
Where "IdMantenedor" = 'MODULO';

Update "BSCL.Plataforma.BD::MAN.Mantenedor"
Set "TablaMantenedor" = '"BSCL.Plataforma.BD::PLA.Estado"' 
Where "IdMantenedor" = 'ESTADO';

Update "BSCL.Plataforma.BD::MAN.Mantenedor"
Set "TablaMantenedor" = '"BSCL.Plataforma.BD::REG.Pais"' 
Where "IdMantenedor" = 'PAIS';
 
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion","Origen")                    
values(41,'COBPRI','PRESTA','CUS');

insert into "BSCL.Facturacion.BD::FAC.TipoComprobante"
values('FEE','FEE','FACTURA ELECTRONICA EXENTA','ACT',CURRENT_UTCTIMESTAMP,'FAC');

insert into "BSCL.Plataforma.BD::PLA.Concepto"
values('COBPRI','CUS','COBRO PRIMA POR PRESTAMO', 'VIG');

insert into "BSCL.Facturacion.BD::FAC.TipoComprobante"
values('FOX','FOX','FACTURA OTROS CONCEPTOS ELECTRONICA EXENTA','ACT',CURRENT_UTCTIMESTAMP,'FAC');

insert into "BSCL.Facturacion.BD::FAC.TipoComprobante"
values('FOE','FOE','FACTURA OTROS CONCEPTOS ELECTRONICA','ACT',CURRENT_UTCTIMESTAMP,'FAC');*/

--Nota de creditos y anulacion
/*INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "IdTipoOperacionRelacionada", "RecibePagaPortafolio", "EsAnticipo", "EsVencimiento", "EsAnulacion", "EsMovimientoComprobante", "AccionOrden", "IdTipoOperacionAnulacion")
VALUES('ANVSPT', 'ANVSPT', 'ANULACION VENTA SPOT', 'COMPRA', 'MEME', 'S', 'S', 'N', 'N', 'N', 'COMPRA', 'INGRESO', 'VENETN', 'R', 'N', 'N', 'N', 'S', 'COMPRA', 'ANCSPT');
 GO
 
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "IdTipoOperacionRelacionada", "RecibePagaPortafolio", "EsAnticipo", "EsVencimiento", "EsAnulacion", "EsMovimientoComprobante", "AccionOrden", "IdTipoOperacionAnulacion")
VALUES('ANCSPT', 'ANCSPT', 'ANULACION COMPRA SPOT', 'VENTA', 'MEME', 'S', 'S', 'N', 'N', 'N', 'VENTA', 'RETIRO', 'COMETN', 'P', 'N', 'N', 'N', 'S', 'VENTA', 'ANVSPT');
go
UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion"
Set "IdTipoOperacionAnulacion" = 'ANVSPT'
WHERE "IdTipoOperacion"='VENSPT';
go
UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion"
Set "IdTipoOperacionAnulacion" = 'ANCSPT'
WHERE "IdTipoOperacion"='COMSPT';
go
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto", "Nombre", "IdEstado") SELECT 'ANVSPT','ANULACION VENTA SPOT','VIG' FROM DUMMY;
go
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto", "Nombre", "IdEstado") SELECT 'ANCSPT','ANULACION COMPRA SPOT','VIG' FROM DUMMY;
GO
INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(86, 'ANVSPT', 'ANVSPT', 'FAC', 'VIG');
go
INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(87, 'ANCSPT', 'ANCSPT', 'FAC', 'VIG');
go

INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
("IdConcepto", "TipoConcepto", "DesplegarEnCombobox", "DisponibleIngresoManual", "VisiblePorEjecutivo", "NombreEntidad", "Origen")
VALUES('ANVSPT', 'AM', 'N', 'N', 'S', 'CTA.CARGOABONOMOVIMIENTO', 'FAC');
go
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
("IdConcepto", "TipoConcepto", "DesplegarEnCombobox", "DisponibleIngresoManual", "VisiblePorEjecutivo", "NombreEntidad", "Origen")
VALUES('ANCSPT', 'AM', 'N', 'N', 'S', 'CTA.CARGOABONOMOVIMIENTO', 'FAC');
go

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
("IdConcepto", "NombreEntidad", "MultipleConcepto", "Tipo", "DesplegarEnComboBox", "DisponibleIngresoManual", "VisiblePorEjecutivo", "Origen")
VALUES('ANCSPT', 'TES.IngresoEgreso', 'N', 'EG', 'S', 'S', 'N', 'FAC');
go
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
("IdConcepto", "NombreEntidad", "MultipleConcepto", "Tipo", "DesplegarEnComboBox", "DisponibleIngresoManual", "VisiblePorEjecutivo", "Origen")
VALUES('ANVSPT', 'TES.IngresoEgreso', 'N', 'IN', 'S', 'S', 'N', 'FAC');
----------------------------------

ALTER TABLE "BSCL.Facturacion.BD::FAC.Comprobante"
add("EsVentaCorta" NVARCHAR(1) DEFAULT 'N');

ALTER TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
add("EsVentaCorta" NVARCHAR(1) DEFAULT 'N');

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
add("EsVentaCorta" NVARCHAR(1) DEFAULT 'N');


INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
VALUES(1, 'DTE_URL', 'Url Dte descarga de informes', 'http://161.238.90.65:8000', 'N');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
VALUES(1, 'DTE_USER', 'Usuario Dte descarga de informes', 'admin', 'N');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
VALUES(1, 'DTE_PASS', 'Usuario Dte descarga de informes', 'admin', 'N');


ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("NumeroReferenciaNota" Bigint DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("TipoReferenciaNota" Nvarchar(40) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("FolioReferenciaNota" Bigint DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("FechaReferenciaNota" Date DEFAULT '0001-01-01');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("RazonReferenciaNota" Nvarchar(260) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
add("CodigoReferenciaNota" Bigint DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE" 
alter("IdComprobanteDetalleGlosa" Nvarchar(260) DEFAULT '');


ALTER TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
add("ValorTipoCambio" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
add("ValorTipoCambio" DECIMAL(26,8) DEFAULT 0);
 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(20057,'PLUGIN_FAC.jar@cl.bcs.facturacion.ConsultaDocumentoDTE',0,'J','Emite Informes de DTE');


ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
drop("EsOrdenDirecta");

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
add("IndicadorOrdenDirecta" Nvarchar(2) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
drop("EsOrdenDirecta");

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
add("IndicadorOrdenDirecta" Nvarchar(2) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
add("IndicadorOrdenDirecta" Nvarchar(2) DEFAULT '');



update "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
SET --"Uso" = 'Consulta informes generado por DTE',
    --"ModificablePorUsuario" = 'N',
    "ModificablePostProduccion" = 'S', 
    "CreadorParametro" = 'NSILVA'
WHERE "CodigoParametro" IN ('DTE_URL', 'DTE_USER', 'DTE_PASS', 'SEPARAPOROPERACION', 'SEPARAPORPRODUCTO', 'SEPARAPORBOLSA',
                            'SEPARAPORAGENTE', 'SEPARAPORSUCURSAL', 'SEPARAPORFECHALIQ', 'SEPARAPORMONEDA');
                            
                            

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
add("IdFormaCustodia" NVARCHAR(3) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
add("IdFormaCustodia" NVARCHAR(3) DEFAULT '');








UPDATE mvtocmbtedet
SET mvtocmbtedet."IdFormaCustodia" = mvtocmbte."IdFormaCustodia"
from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS mvtocmbtedet
 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS mvtocmbte
    ON mvtocmbte."IdMovimientoComprobante" = mvtocmbtedet."IdMovimientoComprobante";


SELECT mvtocmbte."IdFormaCustodia", *
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS mvtocmbtedet
 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS mvtocmbte
    ON mvtocmbte."IdMovimientoComprobante" = mvtocmbtedet."IdMovimientoComprobante";


UPDATE cmbtedet
SET cmbtedet."IdFormaCustodia" = mvtocmbtedet."IdFormaCustodia"
from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS mvtocmbtedet
 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS mvtocmbte
    ON mvtocmbte."IdMovimientoComprobante" = mvtocmbtedet."IdMovimientoComprobante"
 INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS cmbtedet
    ON cmbtedet."IdComprobanteDetalle" = mvtocmbtedet."IdComprobanteDetalle";

SELECT mvtocmbte."IdFormaCustodia", cmbtedet."IdFormaCustodia", *
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS mvtocmbtedet
 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS mvtocmbte
    ON mvtocmbte."IdMovimientoComprobante" = mvtocmbtedet."IdMovimientoComprobante"
 INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS cmbtedet
    ON cmbtedet."IdComprobanteDetalle" = mvtocmbtedet."IdComprobanteDetalle";                            

    
    
ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
add("NemoSVS" NVARCHAR(40) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
add("NemoSVS" NVARCHAR(40) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
add("NemoSVS" NVARCHAR(40) DEFAULT '');    



UPDATE "BSCL.Facturacion.BD::FAC.TipoComprobante"
SET "TipoComprobanteDTE" = 33
WHERE "IdTipoComprobante" = 'FAC';



ALTER TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
ADD ("AfectarTesoreria" NVARCHAR(1) DEFAULT 'S');

ALTER TABLE "BSCL.Facturacion.BD::GBL.CrearMovimientoComprobante_IN_MovimientoComprobanteFormaDePago"
ADD ("AfectarTesoreria" NVARCHAR(1) DEFAULT 'S');*/

/*
ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("GlosaAjusteComision" NVARCHAR(12) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalComisionCmbte" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("GlosaAjusteImpuesto" NVARCHAR(12) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalImpuestoCmbte" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("GlosaAjusteDerecho" NVARCHAR(12) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalDerechoCmbte" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("GlosaAjusteGasto" NVARCHAR(12) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalGastoCmbte" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("GlosaAjusteRetencion" NVARCHAR(12) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalRetencionCmbte" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("CondicionPago" Nvarchar(2) DEFAULT ''); 

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("DiasPago" Integer DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("CodigoReceptor" NVARCHAR(10) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("ParametroKeyProducto" NVARCHAR(250) DEFAULT '');



ALTER TABLE "BSCL.Facturacion.BD::FAC.Comprobante"
add("IdUsuario" BIGINT NOT NULL DEFAULT 0);


INSERT INTO "BSCL.Homologacion.BD::HOM.Homologacion"
SELECT 908, '', "TipoValor", "Entidad", '', 'Otros Conceptos', "ValorOrigen2", "ValorOrigen3", "ValorOrigen4", "ValorOrigen5"
FROM "BSCL.Homologacion.BD::HOM.Homologacion"
WHERE "IdHomologacion" = 394
-----------------------------------
ALTER TABLE "BSCL.Plataforma.BD::REG.DiaFeriado"
add("IdEstado" Nvarchar(3) NOT NULL DEFAULT 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (4213,'BSCL.Plataforma.BD.Servicios::PLA.EliminarDiaFeriado',0 , 'P','Eliminar dia feriado');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TipoComprobante"
ADD("TipoComprobanteDTEPE" Nvarchar(3) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add ("CodigoSucursal" NVARCHAR(20) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TipoComprobanteDTEPE" NVARCHAR(6) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("AccionTipoOperacionCmbtePE" NVARCHAR(6) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("MonedaTransaccionPE" NVARCHAR(40) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("MonedaLiquidacionPE" NVARCHAR(40) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("FechaLiquidacionPE" DATE DEFAULT '0001-01-01');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("ProductoOperacionPE" NVARCHAR(100) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("NacionalidadReceptorPE" NVARCHAR(100) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("CantidadTotalPE" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("PrecioPromedioPE" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("ImporteTotalPE" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("ImporteLiquidacionPE" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("TotalLiquidacionPE" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("MonedaImportePE" NVARCHAR(3) DEFAULT '');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
add("ImporteAPagarPE" DECIMAL(26,8) DEFAULT 0);

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
values('VENOTC', 'VENOTC', 'VENTA OTROS CONCEPTOS', 'VENTA', 'FACT', 'S', 'N', 'N', 'N', 'N', 'VENTA', '', '', '', 'N', 'N', 'N', 'S', '', '', 'N', 'S', 0)

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
values('COMOTC', 'COMOTC', 'COMPRA OTROS CONCEPTOS', 'COMPRA', 'FACT', 'S', 'N', 'N', 'N', 'N', 'COMPRA', '', '', '', 'N', 'N', 'N', 'S', '', '', 'N', 'S', 0)

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(171, 'S', 'T0', 'COMOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(172, 'S', 'T1', 'COMOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(173, 'S', 'T2', 'COMOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(174, 'S', 'T3', 'COMOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(175, 'S', 'T0', 'VENOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(176, 'S', 'T1', 'VENOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(177, 'S', 'T2', 'VENOTC', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
VALUES(178, 'S', 'T3', 'VENOTC', 'ACT');

INSERT INTO "BSCL.Facturacion.BD::CAR.TipoCargaMasiva"
VALUES (1, 'CARGA_MOVIMIENTOS_OTROS_CONCEPTOS', 'CARGA MOVIMIENTOS OTROS CONCEPTOS', 1, 'ACT');*/


/*INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
VALUES (4217, 'BSCL.Facturacion.BD.Vistas::ConsultarLibroDte', 0, 'Q', 'Vista libro dte', '','')


INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion"
values(43, 'PEN', 'FAC.LibroDte');

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion"
values(44, 'ANU', 'FAC.LibroDte');



create column table "BSCL.Facturacion.BD::FAC.PeriodoCierre" (
   "IdPeriodoCierre" BIGINT NOT NULL,
   "Periodo"         NVARCHAR(7) NOT NULL DEFAULT '01-0001',
   "IdEstadoPeriodo" NVARCHAR(3) NOT NULL DEFAULT '',
   "IdEstado"        NVARCHAR(3) NOT NULL DEFAULT '',   
   "IdUsuario"  	 BIGINT NOT NULL DEFAULT 0,
   "FechaIngresoRegistro" TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00',   
 primary key ("IdPeriodoCierre")
);


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
VALUES (4219, 'BSCL.Facturacion.BD.Vistas::ConsultarPeriodoCierre', 0, 'Q', 'Vista periodo cierre', '','')


INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (1, '01-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (2, '02-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (3, '03-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (4, '04-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (5, '05-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (6, '06-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (7, '07-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (8, '08-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (9, '09-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (10, '10-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (11, '11-2017', 'ABT','VIG', 10, '2017-12-12')
INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
VALUES (12, '12-2017', 'ABT','VIG', 10, '2017-12-12')

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion"
VALUES (47, 'ABT', 'FAC.PeriodoCierre' )

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion"
VALUES (48, 'CER', 'FAC.PeriodoCierre' )*/


 DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante"
 
 CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante"
   ( "IdMovimientoComprobante" BIGINT,
     "IdPortafolio"     BIGINT,
     "IdFormaCustodia"  NVARCHAR(3),
     "IdBoveda"         NVARCHAR(3),
     "IdTipoMovimiento" NVARCHAR(6),
     "EsComprobIndividual" NVARCHAR(1)
   ) ON COMMIT DELETE ROWS;

   
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante";    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante"
    ( "IdMovimientoComprobanteCobro" BIGINT, 
      "IdMovimientoComprobante" BIGINT, 
      "IdCobro" NVARCHAR(40), 
      "NombreCobro" NVARCHAR(100), 
      "IdMonedaCobro" NVARCHAR(3), 
      "MontoCobro" DECIMAL(26,8),
      "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3), 
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8), 
      "IdMonedaLiquidacionCobro" NVARCHAR(3), 
      "MontoLiquidacionCobro" DECIMAL(26,8),
      "ModificarValor" NVARCHAR(1),
      "AumentarValor" NVARCHAR(1),
      "DisminuirValor" NVARCHAR(1)
    ) ON COMMIT DELETE ROWS;
    
    
    
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_Cobro";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_Cobro"
    ( "IdCobro" NVARCHAR(40), 
      "NombreCobro" NVARCHAR(100), 
      "IdMonedaCobro" NVARCHAR(3), 
      "MontoCobro" DECIMAL(26,8),
      "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3), 
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8), 
      "IdMonedaLiquidacionCobro" NVARCHAR(3), 
      "MontoLiquidacionCobro" DECIMAL(26,8),
      "ModificarValor" NVARCHAR(1),
      "AumentarValor" NVARCHAR(1),
      "DisminuirValor" NVARCHAR(1)
    ) ON COMMIT DELETE ROWS;