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
-- LIBERACION SEMANA: 21-04-2016
--
--##########################################################
--CHERNANDEZ - CORRECCION CUADRATURA.
--##########################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(273,'UI_OFICIALIZAR_ORDENES', 'BTN', 270, 3, 276, 'CuadraturaCierreDiario_OficializarOrdenes', 'S');

--PROJO: REFACTURACION
ALTER TABLE "BSCL.Orden.BD::ORD.Asignacion" ADD ("CantidadAsignadaRef" DECIMAL(26,8) DEFAULT 0, "ValorRescateAsignadoRef" DECIMAL(26,8) DEFAULT 0);
DELETE FROM "BSCL.Orden.BD::ORD.EstadoOrden";
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (1,'ING','TRANSACCION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (2,'ANU','TRANSACCION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (3,'ASP','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (4,'ASC','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (5,'SAS','TRANSACCION.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (6,'ING','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (7,'ACP','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (8,'RZD','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (9,'OFE','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (10,'CAN','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (11,'ANU','ORDEN');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (12,'ASP','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (13,'ASC','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (14,'SAS','ORDEN.ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (15,'NEF','ASIGNACION');	
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (16,'ANU','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (17,'EAF','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (18,'REF','ASIGNACION');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (19,'VCD','ORDEN.VIGENCIA');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (20,'VIG','ORDEN.VIGENCIA');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (21,'ACT','LIBRO.ORDENES');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (22,'INA','LIBRO.ORDENES');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (23,'ACT','GRUPOPORTAFOLIO');
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") values (24,'INA','GRUPOPORTAFOLIO');

update "BSCL"."BSCL.Orden.BD::ORD.Orden" set "IdEstadoOrden"='ACP' where "IdEstadoOrden"='CNF';

--##########################################################
--CHERNANDEZ - Dashboard Ordenes.
--##########################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES (298,'BSCL.Orden.BD.Servicios::DashboardOrdenes',0,'P','Servicio Dashboard Ordenes');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("IdEmpleadoPortafolio");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP ("IdEmpleadoDigitador");
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdEmpleadoPortafolio"					BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdEmpleadoDigitador"					BIGINT NOT NULL DEFAULT 0); 

--PROJO: Bug
ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion" ALTER ("IdOperadorContraparte" NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion" ALTER ("IdOperadorCorredor" 	NVARCHAR(40) NOT NULL DEFAULT '');
