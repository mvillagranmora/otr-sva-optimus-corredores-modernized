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
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("CodigoClaseOrden" 	NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdMedioDePago"     NVARCHAR(3)  NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("CodigoProcedenciaOrden"     NVARCHAR(3)  NOT NULL DEFAULT '');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("FechaOferta"     SECONDDATE  NOT NULL DEFAULT '0001-01-01');


delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearLoteOrdenPostrera';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearTransaccion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearAsignacion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CancelarSimulacionAsignacion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearOrdenSimultanea';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::ObtenerDetalleSimulacionAsignacion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearOrdenPostrera';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CalcularMontoyCantidadOrden';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearOrden';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearLoteAsignacion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::ProgramarSimulacionAsignacion';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::ORD.ActivarLibro';

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
where "RutaServicio" = 'BSCL.Orden.BD.Servicios::ORD.DesactivarLibro';
