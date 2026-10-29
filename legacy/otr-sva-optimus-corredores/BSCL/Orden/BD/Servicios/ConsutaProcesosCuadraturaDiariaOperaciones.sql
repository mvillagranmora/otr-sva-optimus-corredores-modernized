SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ConsutaProcesosCuadraturaDiariaOperaciones";
GO
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::ConsutaProcesosCuadraturaDiariaOperaciones" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
  	IN  p_FechaInicio               NVARCHAR(10),
  	
	-- Parametros salida del servicio publico
	OUT p_out_resumen		TABLE (	"IdCuadraturaDiariaOperaciones"			NVARCHAR(20),
									"OperacionCuadraturaDiariaOperaciones"	NVARCHAR(200),
									"FechaEjecucion"						DATE		,	
									"TotalOperaciones"						BIGINT		,
									"OperacionesConProblemas"				BIGINT		,
									"TipoOperacion"							NVARCHAR(10),
									"Resultado"								NVARCHAR(100)),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	    
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_Sistema			NVARCHAR(2);
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		v_Sistema:= '';		
		SELECT "Valor" INTO v_Sistema  
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.LocalizacionSistema';
		
		IF (v_Sistema = 'PE') THEN 
			p_out_resumen = SELECT 	"IdCuadraturaDiariaOperaciones",
								"OperacionCuadraturaDiariaOperaciones", 
								"FechaEjecucion", 
								"TotalOperaciones", 
								"OperacionesConProblemas", 
								"TipoOperacion", 
								"Resultado"
						FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
						WHERE "IdEmpresa" = :p_IdEmpresa AND "FechaEjecucion" <= :p_FechaInicio
						ORDER BY "Orden" ASC;
		ELSE 
			p_out_resumen = SELECT 	"IdCuadraturaDiariaOperaciones",
								"OperacionCuadraturaDiariaOperaciones", 
								"FechaEjecucion", 
								"TotalOperaciones", 
								"OperacionesConProblemas", 
								"TipoOperacion", 
								"Resultado"
						FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
						WHERE "IdEmpresa" = :p_IdEmpresa AND "FechaEjecucion" <= :p_FechaInicio
						AND "IdCuadraturaDiariaOperaciones" NOT IN ('CUAASIGOT','CUAASIMOVFACOT','CUAMOVFACOT','CUACAVALI')
						ORDER BY "Orden" ASC;
		END IF;
	END;
END;
