SET SCHEMA "BSCL";
GO

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.InsertarDiccionarioMasivo";
GO

CREATE PROCEDURE  
-- 
--  Proposito: limpia e inserta registros en el diccionario
--  Autor: RULLOA - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::PLA.InsertarDiccionarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos

	IN p_Diccionario		TABLE("CodDiccionario" 	NVARCHAR(100),"CodLenguaje" NVARCHAR(40),"Descripcion" NVARCHAR(520),"Abreviado" NVARCHAR(520)),
	IN p_Lenguaje			NVARCHAR(2),
	
	-- Parametros salida del servicio publico
	OUT p_out_ErroresContador int
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	IF (:p_Lenguaje IS NULL) THEN
		p_out_ErroresContador := 1;
		RETURN;
	END IF;
	
	--DELETE FROM "BSCL.Plataforma.BD::PLA.Diccionario";
	DELETE FROM "BSCL.Plataforma.BD::PLA.Diccionario" where "CodLenguaje" = :p_Lenguaje; --JG:PARA NO BORRAR CONSTANTEMENTE DICCIONARIO PERU
	
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
	SELECT REPLACE(TRIM(SUBSTR("CodDiccionario",0,100)),char(160),''),SUBSTR("CodLenguaje",0,40),SUBSTR("Descripcion",0,520),SUBSTR("Abreviado",0,520)
	FROM :p_Diccionario;
	
	--TEMPORAL -> 
	/*INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
	SELECT "CodDiccionario",:p_Lenguaje,"Descripcion","Abreviado" from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodLenguaje"!='PE' 
		and "CodDiccionario" not in (select "CodDiccionario" from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodLenguaje"='PE');
	*/
	p_out_ErroresContador := 0;
	
END;
GO