SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaUTC";

CREATE FUNCTION 
-- 
--  Proposito: Obtiene la fecha UTC
--  Autor: CST - BCS
--  Fecha Creacion: 01/08/2015
--  
"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaUTC" ( ) 
	RETURNS resultado timestamp
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	resultado := CURRENT_UTCTIMESTAMP;
END;