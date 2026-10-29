SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::Depuracion";

CREATE PROCEDURE 
-- 
--  Proposito: Agregar un log de depuracion utilizando variable ambiente
--     SET 'DEPURACION' = 'S';
--  Autor: CSI - BCS
--  Fecha Creacion: 15/03/2016
--  Fecha Modificacion: 15/03/2016
"BSCL.Plataforma.BD.Servicios::Depuracion" (
	 IdEmpresa			BIGINT			,
	 Fecha 				TIMESTAMP 	    ,	
	 IdUsuario			BIGINT          ,
	 Servicio 			NVARCHAR(255)   ,
	 Bandera 			NVARCHAR(80)   ,
	 Detalle 			NVARCHAR(520) 
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS		
BEGIN
	IF SESSION_CONTEXT('DEPURACION')='S' THEN 
 		insert into "BSCL.Plataforma.BD::PLA.Depuracion" ("IdEmpresa","Fecha","IdUsuario","Bandera","Servicio","Detalle") values (IdEmpresa,Fecha,IdUsuario,Bandera,Servicio,Detalle);		
	END IF;
END;

