SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarHabitualidadSpot";
GO
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarHabitualidadSpot" (
    IN p_IdEmpresa         BIGINT,
	IN p_Clientes          TABLE ("IdCliente" BIGINT),
									
	--Salida
   	OUT P_OUT_ERROR			NVARCHAR(500),
   	OUT p_out_Habitualidad TABLE ("IdCliente" BIGINT, "IndHabitualidad" NVARCHAR(1), "CodigoRegla" NVARCHAR(40),"TipoComprobanteHabitualidad" NVARCHAR(3))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

    DECLARE v_FechaSistema	NVARCHAR(10);
	DECLARE TipoComprobanteDefecto NVARCHAR(3);
	--- Controlado;
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
	END;
				
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		P_OUT_ERROR := SUBSTRING(::SQL_ERROR_MESSAGE,1,500);
	END;
		
	select "Valor" into TipoComprobanteDefecto
	from "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	where "CodigoParametro" = 'CODFACTURAHABITUALIDAD'
	  AND "IdEmpresa" = :p_IdEmpresa;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, :v_FechaSistema);	
	
	--Habitualidad Codigo Tipo Comprobante Factura
	p_out_Habitualidad =
		SELECT
			 "IdCliente"
			,'S' as "IndHabitualidad"
			,'CODFACTURAHABITUALIDAD' as "CodigoRegla"
			,:TipoComprobanteDefecto as "TipoComprobanteHabitualidad"
		FROM :p_Clientes;

	--Habitualidad si es juridico
	p_out_Habitualidad = 
		SELECT "IdCliente", "IndHabitualidad", "CodigoRegla", "TipoComprobanteHabitualidad" FROM :p_out_Habitualidad
		UNION ALL
		SELECT
			 spot."IdCliente"
			,CASE WHEN cliente."Tipo" = 'J' THEN 'S' ELSE 'N' END as "IndHabitualidad"
			,'REGLAHABITUALIDADJURIDICO' as "CodigoRegla"
			,tabHabitualidad."TipoComprobanteHabitualidad"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" spot
		INNER JOIN :p_out_Habitualidad tabHabitualidad
			on spot."IdCliente" = tabHabitualidad."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cliente
			on spot."IdCliente" = cliente."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" configuracion
			on "CodigoParametro" = 'REGLAHABITUALIDADJURIDICO'
		WHERE configuracion."Valor" = 'S'
		GROUP BY spot."IdCliente", configuracion."Valor", tabHabitualidad."TipoComprobanteHabitualidad", cliente."IdCliente", cliente."Tipo";

	--Habitualidad por mes
	p_out_Habitualidad = 
		SELECT
			 spot."IdCliente"
			,CASE WHEN count(*) >= configuracion."Valor" THEN 'S' ELSE 'N' END as "IndHabitualidad"
			,'REGLAHABITUALIDADPORMES' as "CodigoRegla"
			,tabHabitualidad."TipoComprobanteHabitualidad"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" spot
		INNER JOIN :p_out_Habitualidad tabHabitualidad
			on spot."IdCliente" = tabHabitualidad."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" configuracion
			on "CodigoParametro" = 'REGLAHABITUALIDADPORMES'
		WHERE (TO_CHAR("FechaOperacion", 'yyyyMM')) = (TO_CHAR(v_FechaSistema, 'yyyyMM'))
		GROUP BY spot."IdCliente", configuracion."Valor", tabHabitualidad."TipoComprobanteHabitualidad"
		UNION
		SELECT "IdCliente","IndHabitualidad", "CodigoRegla","TipoComprobanteHabitualidad"
		FROM :p_out_Habitualidad
		WHERE "IndHabitualidad" = 'S';
	
	--Habitualidad anual
	p_out_Habitualidad = 
		SELECT
			 spot."IdCliente"
			,CASE WHEN count(*) >= configuracion."Valor" THEN 'S' ELSE 'N' END as "IndHabitualidad"
			,'REGLAHABITUALIDADPORANO' as "CodigoRegla"
			,tabHabitualidad."TipoComprobanteHabitualidad"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" spot
		INNER JOIN :p_out_Habitualidad tabHabitualidad
			on spot."IdCliente" = tabHabitualidad."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" configuracion
			on "CodigoParametro" = 'REGLAHABITUALIDADPORANO'
		WHERE tabHabitualidad."IndHabitualidad" = 'N'
		  AND (TO_CHAR("FechaOperacion", 'yyyy')) = (TO_CHAR(v_FechaSistema, 'yyyy'))
		GROUP BY spot."IdCliente", configuracion."Valor",tabHabitualidad."TipoComprobanteHabitualidad"
		UNION
		SELECT "IdCliente","IndHabitualidad", "CodigoRegla","TipoComprobanteHabitualidad"
		FROM :p_out_Habitualidad
		WHERE "IndHabitualidad" = 'S';
	
	--Habitualidad monto
	p_out_Habitualidad =
		SELECT
			 spot."IdCliente"
		 	,CASE WHEN MAX("MontoMonedaPrincipal") >= configuracion."Valor" THEN 'S' ELSE 'N' END as "IndHabitualidad"
		 	,'REGLAHABITUALIDADPORMONTO' as "CodigoRegla"
			,tabHabitualidad."TipoComprobanteHabitualidad" 
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" spot
		INNER JOIN :p_out_Habitualidad tabHabitualidad
			on spot."IdCliente" = tabHabitualidad."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" configuracion
			on "CodigoParametro" = 'REGLAHABITUALIDADPORMONTO'
		WHERE tabHabitualidad."IndHabitualidad" = 'N'
		GROUP BY spot."IdCliente", configuracion."Valor", tabHabitualidad."TipoComprobanteHabitualidad"
		UNION 
		SELECT "IdCliente","IndHabitualidad", "CodigoRegla","TipoComprobanteHabitualidad" 
		FROM :p_out_Habitualidad
		WHERE "IndHabitualidad" = 'S';
	
	SIGNAL SALIDA_CONTROLADA;		     
END;
