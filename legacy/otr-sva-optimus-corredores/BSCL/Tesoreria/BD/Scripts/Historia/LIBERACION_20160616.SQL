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
-- LIBERACION SEMANA: 15-06-2016
--



--####################################################################################
-- <JVILLAGRAN> 
--####################################################################################	

-- Inscripción de vistas para sustituir ODATA
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1017, 'BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1018, 'BSCL.Tesoreria.BD.Vistas::ConsultarChequeras', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1019, 'BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionChequeras', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1020, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobante', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1021, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1022, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1023, 'BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciasGiro', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1024, 'BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1025, 'BSCL.Tesoreria.BD.Vistas::ConsultarDeposito', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1026, 'BSCL.Tesoreria.BD.Vistas::ConsultarDepositoDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1027, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajas', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1028, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaMoneda', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1029, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaUsuario', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1030, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaProductoEmpresa', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1031, 'BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso', 0, 'Q', 'Tesoreria: vista');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1032, 'BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1033, 'BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1034, 'BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1035, 'BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1036, 'BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1037, 'BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1038, 'BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1040, 'BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria', 0, 'Q', 'Tesoreria: vista');   



-- Nuevos concepto para liquidación de saldos cuenta inversión
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT MAX("IdConceptoTesoreria")+1,'CALISA','TES.IngresoEgreso','N','CTA','EG' FROM Bscl."BSCL.Tesoreria.BD::TES.ConceptoTesoreria";



-- Acceso Menu 2: Crear Ingreso Egreso
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT MAX("IdAcceso")+1, 'Crear Ingreso Egreso', 'MENU2', 7, 1, 0, 'CrearIngresoEgreso', 'S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Menu 2: Comprobantes de Tesorería
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT MAX("IdAcceso")+1, 'Comprobantes de Tesoreria', 'MENU2', 7, 2, 0, 'ComprobantesTesoreria', 'S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Botón Crear movimiento ingreso egreso
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1, 'Crear Movimiento Tesoreria', 'BTN', (select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'CrearIngresoEgreso') as "Padre", 1, 61, 'frmIngresoEgreso_CrearIngresoEgreso', 'S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso al servicio Crear movimiento ingreso egreso
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT MAX("IdAcceso")+1, 'Servicio Crear Movimiento Tesoreria', 'SERV',  0,  0,  61,  'CREAR_INGRESO_EGRESO',  'N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Crear Comprobante
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Generar Comprobante','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre",1,55,'ConsultarIngreso.GenerarComprobante','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Servicio Crear Comprobante','SERV',0,0,55,'CREAR_COMPROBANTE','N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Liquidar
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Liquidar Comprobante','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre",1,89,'frmConsultarComprobantes_btn_liquidar','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Servicio Liquidar Comprobante','SERV',0,0,89,'LIQUIDAR_COMPROBANTE','N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Crear Partida Ingresos
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Partida Ingresos','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'CrearIngresoEgreso') as "Padre",1,256,'ConsultarIngreso.GenerarPartidaIngresos','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Servicio Requisitos Partida Ingresos','SERV',0,0,256,'REQUISITO_PARTIDA_INGRESOS','N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Anular Mov. Ingreso Egreso
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Crear Ingreso Egreso Tesoreria','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'CrearIngresoEgreso') as "Padre",1,61,'ConsultarIngreso.CrearIngresoEgreso','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Anular Mov. Ingreso Egreso
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Anular Mov. Ingreso Egreso','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'CrearIngresoEgreso') as "Padre",1,313,'ConsultarIngreso.AnularIngresoEgreso','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Servicio Anular Registros','SERV',0,0,313,'ANULAR_REGISTROS','N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Generar comprobante de otros comprobantes
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Generar comprobante de otros comprobantes','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre",1,55,'frmConsultarComprobantes_btn_generarComprobante','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";
			
-- Acceso Anular Mov. Ingreso Egreso
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Girar Egresos','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre",1,226,'frmConsultarComprobantes_btn_girar','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Servicio Girar Egresos','SERV',0,0,226,'GIRAR_EGRESOS','N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";

-- Acceso Anular Comprobantes
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1,'Anular Comprobante','BTN',(select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre",1,313,'frmConsultarComprobantes_btn_anular','S' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";
			
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FECOSA','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";


-- Acceso Botón Girar comprobantes de ingreso
INSERT INTO bscl."BSCL.Plataforma.BD::ACC.Acceso"("IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio","IdElementoHtml","EsVisibleMenu")
SELECT MAX("IdAcceso")+1, 'Girar Egresos', 'BTN', (select "IdAcceso" from bscl."BSCL.Plataforma.BD::ACC.Acceso" where "IdElementoHtml" = 'ComprobantesTesoreria') as "Padre", 1, 226, 'frmGiroEgresos_girarEgresos', 'N' FROM bscl."BSCL.Plataforma.BD::ACC.Acceso";




--####################################################################################
-- <NROJAS> 
--####################################################################################	

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Tipo","IdModulo","Nombre","IdEstado")
SELECT 'LIQING','','TES','LIQUIDACION DE COMPROBANTE DE INGRESO DE TESORERIA','ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Tipo","IdModulo","Nombre","IdEstado")
SELECT 'LIQEGR','','TES','LIQUIDACION DE COMPROBANTE DE EGRESO DE TESORERIA','ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('VVISTA','ST', 'TES', 'EFECTIVO', 'VIG');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'LIQING','TES.IngresoEgreso','N','TES','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'LIQEGR','TES.IngresoEgreso','N','TES','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'VVISTA','TES.DepositoDetalle','N','TES','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";


ALTER TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" ADD ("MueveCuentaInversion" NVARCHAR(1) NOT NULL DEFAULT 'S');
ALTER TABLE "BSCL.Tesoreria.BD::TES.Comprobante" ADD ("MueveCuentaInversion" NVARCHAR(1) NOT NULL DEFAULT 'S');


INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('EDP','ENVIADO A DEPOSITAR');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(50,'MOD','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(51,'EDP','TES.ComprobanteFormaDePago');
DELETE FROM "BSCL.Tesoreria.BD::TES.EstadoTesoreria" WHERE "IdEstado" = 'EMI' AND "NombreEntidad" = 'TES.ComprobanteFormaDePago';
DELETE FROM "BSCL.Tesoreria.BD::TES.EstadoTesoreria" WHERE "IdEstado" = 'DEP' AND "NombreEntidad" = 'TES.ComprobanteFormaDePago';

UPDATE "BSCL.Tesoreria.BD::TES.EstadoTesoreria" SET "IdEstado" = 'DEP' WHERE "NombreEntidad" = 'TES.Deposito' AND "IdEstado" = 'EMI';
UPDATE "BSCL.Tesoreria.BD::TES.EstadoTesoreria" SET "IdEstado" = 'DEP' WHERE "NombreEntidad" = 'TES.DepositoDetalle' AND "IdEstado" = 'EMI';





UPDATE "BSCL"."BSCL.Plataforma.BD::PAG.MedioDePago" SET "Nombre" = UPPER("Nombre");


DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago";
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(1,'CHE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(2,'VVI','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(3,'BDE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(4,'EFE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(5,'TLR','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(6,'TSW','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(7,'TDP','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(8,'TRA','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(9,'VVE','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(10,'EBA','MODCAJ','ACT');

