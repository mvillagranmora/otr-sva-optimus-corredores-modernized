-- ########################################################################################
--
--  ==========================================================
--  ESTE ARCHIVO CONTIENE LOS SCRIPTS HISTORICOS DE LIBERACION
--  ==========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
--
-- ########################################################################################

set schema "BSCL";
--
--
-- LIBERACION: 21-05-2016
--

--################################################
--       fpino.Actualizacion cambios.
--################################################

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 69;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(69,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarDetalle',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 70;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(70,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.EliminarDetalles',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 94;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(94,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.ObtenerTotales',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 249;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(249,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarDetalle',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 250;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(250,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.EliminarDetalles',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 251;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(251,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.ObtenerTotales',0,'P','');

DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CargaDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CuadraturaDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='GeneracionDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ValidacionDJ';

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CargaDJ', 'Carga de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobCarga', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CuadraturaDJ', 'Cuadratura de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobCuadratura', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('GeneracionDJ', 'Generacion de DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobGeneracion', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ValidacionDJ', 'Validacion de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobValidacion', 'JAVA', '');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

COMMIT;
