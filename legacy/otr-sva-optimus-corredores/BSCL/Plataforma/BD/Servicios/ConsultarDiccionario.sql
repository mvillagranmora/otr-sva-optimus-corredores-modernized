SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarDiccionario";

CREATE PROCEDURE
-- 
--  Proposito: Traduce codigo error hana a codigo bcs traducible humano
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarDiccionario" (
		IN  p_lenguaje nvarchar(40),
		OUT p_Diccionario table(
			"Codigo" nvarchar(40),
			"Descripcion" nvarchar(520),
			"Abreviado" nvarchar(15)
		)
	) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA AS
BEGIN
		p_Diccionario = select "CodDiccionario" as "Codigo", rtrim(ltrim("Descripcion")) as "Descripcion" , rtrim(ltrim("Abreviado")) as "Abreviado"
	  	from  "BSCL.Plataforma.BD::PLA.Diccionario"
	  	where "CodLenguaje" = p_lenguaje order by "CodDiccionario";
END;
