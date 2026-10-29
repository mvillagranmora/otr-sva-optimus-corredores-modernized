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

set schema bscl;

--
--
-- LIBERACION SEMANA: 29-01-2016
--

--===========================================================
---------- SE ELIMINA UNA COLUMNA QUE NO SE UTILIZABA--

drop table "BSCL.Orden.BD::ORD.AsignacionResultado" cascade;

CREATE ROW TABLE "BSCL.Orden.BD::ORD.AsignacionResultado" (
 	 "IdAsignacionResultado"	BIGINT			NOT NULL ,
	 "IdEstadoFacturacion" 		NVARCHAR(3)		NOT NULL ,
	 "CantidadAsignada" 		BIGINT			NOT NULL , --
	 "FechaAsignacion" 			SECONDDATE	 	NOT NULL ,
	 "IdTransaccionPaso" 		BIGINT 			NOT NULL , 
	 "IdOrdenPaso" 				BIGINT			NOT NULL 
	 
 );

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionResultado" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
 


commit;


