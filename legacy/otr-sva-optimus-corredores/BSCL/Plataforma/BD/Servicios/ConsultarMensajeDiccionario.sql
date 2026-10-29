SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario";

CREATE PROCEDURE
-- 
--  Proposito: Traduce desde un codigo a un mensaje de acuerdo al idioma del sistema
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (
	IN  p_Codigo  nvarchar(100),
	IN  p_valores table ("Codigo" nvarchar(100),"Valor" nvarchar(520)),
	OUT p_out_Mensaje nvarchar(520)	 
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS
BEGIN
	declare v_existe integer;
	declare v_cod   nvarchar(520) := '';
	declare v_msg   nvarchar(520) := '';	
	declare v_idioma nvarchar(50)  := 'CL'; --TODO: leer desde tabla configuracion
	declare CURSOR c_c1 FOR SELECT "Codigo","Valor" FROM :p_valores;	
	select  count(1) into v_existe from "BSCL.Plataforma.BD::PLA.Diccionario"
	 where  "CodDiccionario" = :p_Codigo and "CodLenguaje" = v_idioma;	 
	if v_existe=0 then
		 v_msg:=p_Codigo||' sin traducción';
	 	--TODO: agregar un registro de log::error
	else 
		select  "Descripcion" into v_msg from "BSCL.Plataforma.BD::PLA.Diccionario"
		 where  "CodDiccionario" = :p_Codigo and "CodLenguaje" = v_idioma;			 
	end if;
	FOR reg AS c_c1 DO
		v_cod := '::'||reg."Codigo"||'::';
		v_msg := replace (v_msg,:v_cod,reg."Valor");			
	END FOR;
	--fin
	p_out_Mensaje:=:v_msg;	
END;
