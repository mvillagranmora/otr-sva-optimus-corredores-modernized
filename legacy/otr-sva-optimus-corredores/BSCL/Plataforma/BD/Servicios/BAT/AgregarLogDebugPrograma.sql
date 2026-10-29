SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarLogDebugPrograma";

CREATE PROCEDURE
-- 
--  Proposito: Agrega log de depuracion para los jobs  
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogDebugPrograma" (
 	p_IdPrograma 		BIGINT,
 	p_IdTipoPrograma 	NVARCHAR(40),
 	p_Mensaje			NVARCHAR(5000)
								
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

	insert into "BSCL.Plataforma.BD::BAT.LogPrograma" ("FechaLog","IdTipoPrograma","IdPrograma","Mensaje") values (CURRENT_TIMESTAMP,p_IdTipoPrograma,p_IdPrograma,p_Mensaje);
	
END;
