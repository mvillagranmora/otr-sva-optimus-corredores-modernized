SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarPrerequisitoPauta";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarPrerequisitoPauta" ( 	
	IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_Fecha								NVARCHAR(10) 	default '0001-01-01',     
    IN p_Proceso							NVARCHAR(40) 	default '',
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_ErroresContador 				BIGINT			,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS

BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

		p_out_ErroresContador := 0;
		p_out_ErroresDetalle	= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
	
		p_out_ErroresDetalle = 
	
		SELECT 
--			REQ."NombreFuncionProcesoB" 				AS "PROCESO" , 
			IFNULL(UPPER(REQ."Descripcion"), 'NO EJECUTADO') 	AS "Codigo",
			IFNULL(UPPER(EST."Nombre") 	, 'NO EJECUTADO') 	AS "Descripcion"
		FROM (
		SELECT
			MFA."NombreFuncionProceso"		AS "NombreFuncionProcesoA" 		,
			MFA."IdModuloFuncionProceso" 	AS "IdModuloFuncionProcesoA"	,
			MFB."NombreFuncionProceso"		AS "NombreFuncionProcesoB"		,
			MDP."IdModuloFuncionProceso" 	AS "IdModuloFuncionProcesoB"	, 
			MFB."Descripcion"				AS "Descripcion"				,
			CASE MDP."TipoDependencia"			
				WHEN 'A'	THEN 'Ejecutar Antes'
				ELSE 'Ejecutar Después'
			END
			AS "Tipo"		
			
		FROM 		"BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" 				MFA
		INNER JOIN 	"BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia"	MDP
		ON (MFA."IdModuloFuncionProceso" = MDP."IdModuloFuncionProcesoPrincipal")			
		INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso"				MFB
		ON (MFB."IdModuloFuncionProceso" = MDP."IdModuloFuncionProceso")
		WHERE MFA."NombreFuncionProceso" = :p_Proceso
		) as REQ
		
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Log" 									PLA
		ON (REQ."NombreFuncionProcesoB" = PLA."IdTipoPrograma" AND 
			PLA."FechaLog"	= :p_Fecha)
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"										EST
		ON (EST."IdEstado" = PLA."EstadoProceso")
		WHERE
			PLA."IdTipoPrograma" IS NULL	OR
			PLA."EstadoProceso"  <> 'COM';	

	
	END;


		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;										

--		select 1 into p_out_ErroresContador from Dummy;


END;


