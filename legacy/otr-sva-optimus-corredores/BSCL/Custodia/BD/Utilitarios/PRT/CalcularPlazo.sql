SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo";

--  Proposito: retornar la cantidad de dias plazo de la operación
--  Fecha Creacion: 13-10-2022
--  
CREATE FUNCTION "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo" (
		p_fechaDesde	NVARCHAR(10),
		p_fechaHasta	NVARCHAR(10),
		p_DiasHabiles	NVARCHAR(1),
		p_IdCondicionDeLiquidacion NVARCHAR(2),
		p_IdPais		BIGINT
) 
RETURNS p_out_PlazoDias	INTEGER
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
AS

BEGIN
	
	DECLARE v_DiasCondicionDeLiquidacion INT;
	SELECT COALESCE("PlazoEnDias",0) INTO v_DiasCondicionDeLiquidacion from "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" where "IdCondicionDeLiquidacion" = p_IdCondicionDeLiquidacion;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazo" (
		 p_FechaDesde => :p_FechaDesde
		,p_FechaHasta => :p_FechaHasta
		,p_DiasHabiles => :p_DiasHabiles
		,p_DiasCondicionDeLiquidacion => :v_DiasCondicionDeLiquidacion
		,p_IdPais => :p_IdPais
		,p_out_PlazoDias => p_out_PlazoDias
	);
END;

