SET SCHEMA "BSCL";

DROP FUNCTION  "BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha";

CREATE FUNCTION 
-- 
--  Proposito: Devuelve el formato de una fecha a String
--  Autor: CST - BCS
--  Fecha Creacion: 01/08/2015
--  Tipos de fechas:
--     d=fecha dd-mm-yyyy
--     g=fecha dd-mm-yyyy HH24:MI
--     G=fecha dd-mm-yyyy HH24:MI:SS
"BSCL.Plataforma.BD.Utilitarios::DarFormatoFecha" ( 
	in  p_timestamp				timestamp,
	in  p_formato				nvarchar(1) default 'd'
) 
	RETURNS resultado nvarchar(20)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	--
	declare v_zona 					nvarchar(5) 	:= 'CL';  --TODO: Por el momento fijo

	-------------------------------------------
	--CHILE
	-------------------------------------------	
	if (v_zona = 'CL') then
		 if p_formato = 'd' then
		 	resultado := TO_VARCHAR (p_timestamp, 'DD-MM-YYYY');		 	 
		 end if;		 
		 if p_formato = 'g' then
		 	resultado := TO_VARCHAR (p_timestamp, 'DD-MM-YYYY HH24:MI');		 	 
		 end if;	
		 if p_formato = 'G' then
		 	resultado := TO_VARCHAR (p_timestamp, 'DD-MM-YYYY HH24:MI:SS');		 	 
		 end if;		 	 
	end if;			
END;
