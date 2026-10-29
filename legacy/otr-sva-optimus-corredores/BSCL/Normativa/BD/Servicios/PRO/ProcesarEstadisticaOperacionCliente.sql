SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::PRO.ProcesarEstadisticaOperacionCliente";
GO

CREATE PROCEDURE 
--
--  Proposito: Servicio Invocado desde el JOB para calcular el monto promedio total diario por cliente a un determinado periodo segun parametros.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 10/01/2017

"BSCL.Normativa.BD.Servicios::PRO.ProcesarEstadisticaOperacionCliente" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  BIGINT,	
	IN p_IdSolicitudCircular 		  BIGINT, 
	IN p_IdProceso 		  			  BIGINT 

  )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		
		DECLARE v_IdNormativa 								 NVARCHAR(15):='UAF';
		DECLARE v_IdTipoDocumento   		 	  			 NVARCHAR(10):='UAFSOIEST';
		DECLARE v_TipoTarea  	              				 NVARCHAR(40):='ProcesarEstadisticaOperacion';
		
		DECLARE v_procesoRecalculo 							 NVARCHAR(1):='N';
		DECLARE v_FechaOperacionDesde,v_FechaOperacionHasta,v_out_FechaSiguente	 NVARCHAR(10);--'YYYY-MM-DD'
		DECLARE v_out_MensajeSalida							 NVARCHAR(200):='CALCULO INACTIVO';
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
		DECLARE v_PRO_HAB_ACTMONTOPROM_CONPROM, v_PRO_HAB_BUSCLI_SINPROM,v_PRO_CAL_ACTMONTOPROM_CANTDIAS 	 	 NVARCHAR(520):='';
		DECLARE v_out_error			  						 NVARCHAR(500);
		DECLARE	v_Count,v_IdPersona,v_IdDocumento, v_IdPais,v_NumeroDias,v_IdProceso  BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
      

		---Parametros


		
		--------------------------------------------------------
		-- Identificar fecha de trabajo
		--------------------------------------------------------
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);

		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::PRO.Proceso" WHERE "IdEmpresa"=p_IdEmpresa
		AND "IdNormativa"=v_IdNormativa AND "IdTipoDocumento"=v_IdTipoDocumento AND "IdTipoTarea" =v_TipoTarea;
		
		IF v_Count > 0 THEN --Se busca ultima fecha de proceso

			SELECT TOP 1 TO_VARCHAR("FechaIngreso",'YYYY-MM-DD') AS "Fecha" INTO  v_FechaOperacionDesde
			FROM "BSCL.Normativa.BD::PRO.Proceso" WHERE "IdEmpresa"=p_IdEmpresa
			AND "IdNormativa"=v_IdNormativa AND "IdTipoDocumento"=v_IdTipoDocumento AND "IdTipoTarea" =v_TipoTarea
			ORDER BY "FechaIngreso" DESC;
			
			v_FechaOperacionHasta=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
		ELSE 
			v_FechaOperacionDesde='';
			v_FechaOperacionHasta=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
		END IF;
		

		--------------------------------------------------------
		-- Parametro de habilitacion
		--------------------------------------------------------
		SELECT COUNT(1) INTO v_Count  -- PARAMETRO : RECALCULO
		FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_ACTMONTOPROM_CONPROM' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN	
			SELECT "Valor" INTO v_PRO_HAB_ACTMONTOPROM_CONPROM
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_ACTMONTOPROM_CONPROM' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_PRO_HAB_ACTMONTOPROM_CONPROM:= 'N';			

		END IF;		
		
		SELECT COUNT(1) INTO v_Count  -- PARAMETRO : CLIENTES NUEVOS
		FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_BUSCLI_SINPROM' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN	
			SELECT "Valor" INTO v_PRO_HAB_BUSCLI_SINPROM
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_BUSCLI_SINPROM' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_PRO_HAB_BUSCLI_SINPROM:= 'N';			

		END IF;		
		
		--------------------------------------------------------------------------------
		-- Recalculo del promedio una vez que ha transcurrido la cantidad de dias configurados
		--------------------------------------------------------------------------------
		v_procesoRecalculo='N';
		IF v_PRO_HAB_ACTMONTOPROM_CONPROM='S' AND v_FechaOperacionDesde<>'' THEN-- PARAMETRO : RECALCULO
				--------------------------------------------------------
				-- Determinar cantidad de dias para hacer calculo
				--------------------------------------------------------
				SELECT "IdPais" INTO  v_IdPais FROM  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
				WHERE "IdEmpresa"=p_IdEmpresa;
				
				SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_CAL_ACTMONTOPROM_CANTDIAS' AND "IdNormativa"=v_IdNormativa;
				IF v_Count>0 THEN	
					SELECT "Valor" INTO v_PRO_CAL_ACTMONTOPROM_CANTDIAS
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_CAL_ACTMONTOPROM_CANTDIAS' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_PRO_CAL_ACTMONTOPROM_CANTDIAS:= '5';			
		
				END IF;
				v_NumeroDias=	CAST(TRIM(v_PRO_CAL_ACTMONTOPROM_CANTDIAS) AS BIGINT);
				
				
				
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( p_Fecha=>v_FechaOperacionDesde,p_NumeroDias=>v_NumeroDias, p_EsHabil=>'S',	p_IdPais=>v_IdPais,p_out_FechaSiguente=>v_out_FechaSiguente);
				
				IF v_out_FechaSiguente= v_FechaOperacionHasta THEN ---Cumple con los dias para recalculo

					CALL "BSCL.Normativa.BD.Servicios::PRO.CalcularMontoPromedioCliente" ( 
							 p_IdEmpresa			=>p_IdEmpresa
							,p_FechaOperacionDesde 	=>v_FechaOperacionDesde
							,p_FechaOperacionHasta 	=>v_FechaOperacionHasta
							,p_ModoProceso 			=>'RECALCULO'
							,p_out_MensajeSalida    =>v_out_MensajeSalida
						  );
						  
					v_procesoRecalculo='S';
				END IF;	

 		END IF;
		
		
		--------------------------------------------------------------------------------
		-- Busqueda diaria de operaciones para Clientes que no tienen Promedio Calculado
		--------------------------------------------------------------------------------

		IF v_PRO_HAB_BUSCLI_SINPROM='S' AND v_procesoRecalculo='N' THEN  			-- PARAMETRO : CLIENTES NUEVOS

			CALL "BSCL.Normativa.BD.Servicios::PRO.CalcularMontoPromedioCliente" ( 
							 p_IdEmpresa			=>p_IdEmpresa
							,p_FechaOperacionDesde 	=>v_FechaOperacionDesde
							,p_FechaOperacionHasta 	=>v_FechaOperacionHasta
							,p_ModoProceso 			=>'NUEVOS'
							,p_out_MensajeSalida    =>v_out_MensajeSalida
						  );

 		END IF;
	   
						
		--------------------------------------------------------------------------------
		-- Registrar tabla Procesos
		--------------------------------------------------------------------------------

		SELECT "BSCL.Normativa.BD.Secuencias::PRO.IdProceso".NEXTVAL INTO v_IdProceso FROM DUMMY;
		
		INSERT INTO "BSCL.Normativa.BD::PRO.Proceso" (
				   "IdProceso",
				   "IdEmpresa",
				   "IdModulo",
				   "IdNormativa",
				   "IdTipoDocumento",
				   "IdTipoTarea", --Tarea de inscripcion en BAT
				   "IdSolicitudCircular",
				   "IdProcesoBatch",
				   "IdEstado",
				   "Descripcion",
				   "FechaIngreso",
				   "FechaCreacion"
				   )
					SELECT
					v_IdProceso,
					p_IdEmpresa,
					'NOR',
					v_IdNormativa,
					v_IdTipoDocumento,
					v_TipoTarea,
					p_IdSolicitudCircular,
					p_IdProceso,
					'TER',
					v_out_MensajeSalida,
					v_FechaOperacionHasta,
					CURRENT_UTCTIMESTAMP
					FROM DUMMY;
		
		--------------------------------------------------------------------------------
		-- Agregar proceso de Evaluacion SOI - para su ejecucion
		--------------------------------------------------------------------------------			
/*

			v_TablaParametros = 
			select 'IdEmpresa' as "NombreParametro",to_varchar(p_IdEmpresa) as "Valor" from dummy
			union all
			select 'IdTipoTarea' as "NombreParametro",'PROCESOEVALUACIONMONTOSOI' as "Valor" from dummy
			union all
			select 'IdSolicitudCircular' as "NombreParametro",'0' as "Valor" from dummy;
			
			CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
					 (
						 p_IdEmpresa  	 =>p_IdEmpresa,
						 p_IdUsuario 	 =>0,
						 p_IdModulo  	 =>'NOR',
						 p_IdAgenda	  	 =>0, 
						 p_IdTipoTarea	 =>'PROCESOEVALUACIONMONTOSOI',  
						 p_Segundos 	 =>0, 
						 p_TablaParametros  		=>:v_TablaParametros,
						 p_CreaParam		 		=>'S',
						 p_out_IdPrograma			=>v_out_IdPrograma,
						 p_out_AdvertenciasContador	=>v_out_AdvertenciasContador,
						 p_out_AdvertenciasDetalle 	=>v_out_AdvertenciasDetalle,
						 p_out_ErroresContador 		=>v_out_ErroresContador,
						 p_out_ErroresDetalle 		=>v_out_ErroresDetalle
					);

*/
END; 
