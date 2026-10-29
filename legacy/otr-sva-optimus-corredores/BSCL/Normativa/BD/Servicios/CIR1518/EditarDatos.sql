SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR1518.EditarDatos";
GO

CREATE PROCEDURE 
--
--  Proposito: Servicio Invocado desde el JOB para cargar datos de la circular, para la edicion.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07/04/2017

"BSCL.Normativa.BD.Servicios::CIR1518.EditarDatos" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  			BIGINT,	
	IN p_IdCuadratura							BIGINT,
	IN p_CantidadCustodia						DECIMAL(15,4),
	IN p_CantidadGarantia						DECIMAL(15,4),
	IN p_CantidadPlazoVenta						DECIMAL(15,4),	
	IN p_CantidadPlazoCompra					DECIMAL(15,4),	
	IN p_CantidadTotal							DECIMAL(15,4),
	IN p_CantidadDCVRelacionado 				DECIMAL(15,4),
	IN p_CantidadDCVTerceros 					DECIMAL(15,4),
	IN p_CantidadDCVCuentaPropia 				DECIMAL(15,4),
	IN p_CantidadBCS	 						DECIMAL(15,4), --BCS 
	IN p_CantidadBEC	 						DECIMAL(15,4), --BEC 
	IN p_CantidadBOVALPO						DECIMAL(15,4), --BOVALPO 
	IN p_CantidadEmisores 						DECIMAL(15,4), --EMISORES
	IN p_CantidadTransitoIngreso				DECIMAL(15,4),
	IN p_CantidadTransitoEgreso					DECIMAL(15,4),
	IN p_Diferencia								DECIMAL(15,4),
	IN p_ExplicacionDescuadratura				NVARCHAR(100),
	IN p_IdUsuario   							BIGINT 	DEFAULT 0,
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
  )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA

	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
	
		----------------------------------------
		--Declare Variables

  		DECLARE v_fechaHoraSistema		 NVARCHAR(40):='';

		DECLARE v_Count 				 BIGINT :=0;
		---Parametros


		
		--------------------------------------------------------
		-- Identificar fecha de trabajo
		--------------------------------------------------------

		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);
			
--		INSERT INTO  "BSCL.Normativa.BD::CIR.Paso" VALUES(1,1);
	

	
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::NOR.DatosCIR1518"	WHERE "IdCuadratura"=p_IdCuadratura;
		
		IF v_Count>0 THEN	--Existe
			UPDATE "BSCL.Normativa.BD::NOR.DatosCIR1518"
			SET 
			"CantidadCustodia"=:p_CantidadCustodia,
			"CantidadGarantia"=:p_CantidadGarantia,
			"CantidadPlazoVenta"=:p_CantidadPlazoVenta,
			"CantidadPlazoCompra"=:p_CantidadPlazoCompra,
			"CantidadTotal"=:p_CantidadTotal,
			"CantidadDCVRelacionado"=:p_CantidadDCVRelacionado,
			"CantidadDCVTerceros" =:p_CantidadDCVTerceros,
			"CantidadDCVCuentaPropia"=:p_CantidadDCVCuentaPropia,
			"CantidadBCS"=:p_CantidadBCS,
			"CantidadBEC"=:p_CantidadBEC,
			"CantidadBOVALPO"=:p_CantidadBOVALPO,
			"CantidadEmisores" =:p_CantidadEmisores,
			"CantidadTransitoIngreso"=:p_CantidadTransitoIngreso,
			"CantidadTransitoEgreso"=:p_CantidadTransitoEgreso,
			"Diferencia"=:p_Diferencia,
			"ExplicacionDescuadratura"=:p_ExplicacionDescuadratura,
			--Auditoria
			"FechaModificacion"= CURRENT_UTCTIMESTAMP,
			"IdUsuarioModificacion" =:p_IdUsuario
			WHERE "IdCuadratura"=p_IdCuadratura;
		ELSE
		
			INSERT INTO "BSCL.Normativa.BD::NOR.DatosCIR1518"
			("IdCuadratura",
			"FechaCuadraturaCustodia",
			"Nemotecnico",
			"CantidadCustodia",
			"CantidadGarantia",
			"CantidadPlazoVenta",
			"CantidadPlazoCompra",
			"CantidadTotal",
			"CantidadDCVRelacionado",
			"CantidadDCVTerceros",
			"CantidadDCVCuentaPropia",
			"CantidadBCS",
			"CantidadBEC",
			"CantidadBOVALPO",
			"CantidadEmisores",
			"CantidadTransitoIngreso",
			"CantidadTransitoEgreso",
			"Diferencia",
			"ExplicacionDescuadratura",
			--Auditoria
			"FechaIngreso",
			"FechaCreacion",
			"FechaModificacion",
			"IdUsuarioCreador",
			"IdUsuarioModificacion")
			SELECT 
				"IdCuadratura",
				"FechaCuadraturaCustodia",
				"Nemotecnico",
				:p_CantidadCustodia AS "CantidadCustodia",
				:p_CantidadGarantia AS "CantidadGarantia",
				:p_CantidadPlazoVenta AS "CantidadPlazoVenta",
				:p_CantidadPlazoCompra AS "CantidadPlazoCompra",
				:p_CantidadTotal AS "CantidadTotal",
				:p_CantidadDCVRelacionado "CantidadDCVRelacionado",
				:p_CantidadDCVTerceros AS "CantidadDCVTerceros",
				:p_CantidadDCVCuentaPropia AS "CantidadDCVCuentaPropia",
				:p_CantidadBCS AS "CantidadBCS",
				:p_CantidadBEC AS "CantidadBEC",
				:p_CantidadBOVALPO AS "CantidadBOVALPO",
				:p_CantidadEmisores AS "CantidadEmisores",
				:p_CantidadTransitoIngreso AS "CantidadTransitoIngreso",
				:p_CantidadTransitoEgreso AS "CantidadTransitoEgreso",
				:p_Diferencia AS "Diferencia",
				:p_ExplicacionDescuadratura AS "ExplicacionDescuadratura",
				--Auditoria
				:v_fechaHoraSistema AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP AS "FechaModificacion",
				:p_IdUsuario AS "IdUsuarioCreador",
				:p_IdUsuario AS "IdUsuarioModificacion"
			FROM  "BSCL.Normativa.BD::PRO.CuadraturaCustodia"
			WHERE  "IdCuadratura"=p_IdCuadratura;
		
		
		END IF;		

		
	
	END;
	
	
			


END; 