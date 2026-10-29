SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::ObtenerValores";

-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ObtenerValores" (
	IN p_IndFuente			NVARCHAR(10),           -- EJ: si es = local o bcs, entonces toma los valores directo de la RUEBAS
	IN p_Fecha				DATE,
									
	--Salida
   	OUT P_OUT_ERROR			NVARCHAR(500),
   	OUT p_out_Indicadores	TABLE (	"IdIndicador"	NVARCHAR(10),
   									"Fecha"			DATE,
   									"TipoCambio"	DECIMAL(26,8),
   									"Paridad"		DECIMAL(26,8),
   									"FechaValor"	DATE,
   									"Fuente"		NVARCHAR(10))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

	--- Controlado
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
	END;	
				
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		P_OUT_ERROR := SUBSTRING(::SQL_ERROR_MESSAGE,1,500);
	END;
	
    
	p_out_Indicadores =
		SELECT
			 MON."IdMoneda"							AS "IdIndicador"
			,:p_Fecha								AS "Fecha"
			,COALESCE(PAR."TipoCambioMercado",0)	AS "TipoCambio"
			,COALESCE(PAR."ParidadMercado",0)		AS "Paridad"
			,PAR."Fecha"							AS "FechaValor"
			,:p_IndFuente							AS "Fuente"
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
		INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
			ON (PAR."IdMoneda" = MON."IdMoneda")
		WHERE PAR."Fecha" = :p_Fecha
		;
	
END;
