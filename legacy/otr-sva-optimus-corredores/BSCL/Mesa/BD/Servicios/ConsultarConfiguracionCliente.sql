SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionCliente";
GO
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionCliente" (
	IN p_IdEmpresa BIGINT,		
	IN p_Clientes          table ("IdCliente" BIGINT , "IdAgente" BIGINT),

	--Salida
   	OUT P_OUT_ERROR			NVARCHAR(500),
   	OUT p_out_configuracion table ("IdCliente" BIGINT, "Codigo" NVARCHAR(100),"Valor" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

	--- Controlado
	DECLARE TipoComprobanteDefecto NVARCHAR(3);
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		P_OUT_ERROR := SUBSTRING(::SQL_ERROR_MESSAGE,1,500);
	END;
		
	CALL "BSCL.Mesa.BD.Servicios::SPT.ConsultarHabitualidadSpot" (p_IdEmpresa,:p_Clientes,P_OUT_ERROR,p_out_Habitualidad);
	
	--SELECT ':p_out_Habitualidad',* FROM :p_out_Habitualidad;
		
	p_out_configuracion =		
		SELECT Cliente."IdCliente" as "IdCliente", 
			  'ESHABITUALIDAD' as "Codigo",
			  CASE WHEN Habitualidad."IndHabitualidad" = 'S' THEN Habitualidad."TipoComprobanteHabitualidad" ELSE null END as "Valor", 
			  Habitualidad."CodigoRegla" as "Descripcion"
		FROM :p_Clientes Cliente
		LEFT JOIN :p_out_Habitualidad Habitualidad 
			ON Cliente."IdCliente" = Habitualidad."IdCliente"
		UNION ALL
		SELECT Cliente."IdCliente" as "IdCliente", 
			  'AGENTE' as "Codigo",
			  (Agente."Nombres" ||' '||Agente."ApellidoPaterno") as "Valor", 
			  '' as "Descripcion"
		FROM :p_Clientes Cliente
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" Agente
			ON Cliente."IdAgente" = Agente."IdPersona";

	SIGNAL SALIDA_CONTROLADA;		     
END;