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
-- LIBERACION SEMANA: 08-04-2016
--
--nuevo servicio de valorizacion
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
SELECT 266, 'BSCL.Plataforma.BD.Servicios::VAL.ValorizarRFMasivo',0,'P','Servicio de Valorizacion' FROM dummy union all
SELECT 268, 'BSCL.Mercado.BD.Servicios::ConsultarPrecioInstrumentoMasivo',0,'P','Servicio Obtencion de Precio' FROM dummy;

--################################################
--    CH - Correcciones Cuadratura y cierre
--################################################
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::FolioOrdenes";
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::FolioOrdenes" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Orden" ADD ("Folio" NVARCHAR(40) DEFAULT '' NOT NULL);

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" 
VALUES(1, 'DIFERENCIA_MONTO_ASIGNACION', 'Es el monto que puede tener de diferencia el monto asignado a una transaccion, y no ser considerado error de cuadratura', '200');

--Botones pantalla cuadratura y cierre diario
delete from "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" where "IdAcceso" = 270 OR "IdAcceso" = 271 OR "IdAcceso" = 272 OR "IdAcceso" = 273;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(270,'UI_CUADRATURA_CIERRE_DIARIO', 'MENU2', 3, 12, 0, 'CuadraturaCierreDiario', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(271,'UI_CUADRAR_ORDENES', 'BTN', 270, 1, 244, 'CuadraturaCierreDiario_CuadrarOrdenes', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(272,'UI_VENCER_ORDENES', 'BTN', 270, 2, 263, 'CuadraturaCierreDiario_VencerOrdenes', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(273,'UI_OFICIALIZAR_ORDENES', 'BTN', 270, 3, 276, 'CuadraturaCierreDiario_OficializarOrdenes', 'S');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(276,'BSCL.Orden.BD.Se@rvicios::CuadraturaOficializarOrdenes',0,'P','');
