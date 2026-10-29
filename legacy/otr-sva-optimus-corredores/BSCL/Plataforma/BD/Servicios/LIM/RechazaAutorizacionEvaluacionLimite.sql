SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.RechazaAutorizacionEvaluacionLimite";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para Rechazar la evaluacion de un Limite aplicado.
--  Invocado desde el modulo de autorizaciones. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03-05-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.RechazaAutorizacionEvaluacionLimite" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdGrupoAutorizacion						BIGINT,
	IN 	p_IdEvaluacionLimite						TABLE ("IdEvaluacionLimite"	BIGINT),	
	IN  p_IdConcepto	       						NVARCHAR(6),
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
		--------------------
		--Declare Variables
		--------------------
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("IdInterno" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("IdInterno" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_count											BIGINT;
			DECLARE v_Prioridad										BIGINT:=0;
			--DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdEstadoAutorizacion							NVARCHAR(3):='RZD';
			DECLARE v_IdTipoEntidad									NVARCHAR(40):='';
			DECLARE v_IdPaisSistema				BIGINT;
		--------------------
		--Validaciones
		--------------------
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
		-- Existencia de la avaluacion.		
		BEGIN

			SELECT COUNT(SOL."IdEvaluacionLimite") INTO v_count
			FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite" SOL
			JOIN :p_IdEvaluacionLimite L ON L."IdEvaluacionLimite"=  SOL."IdEvaluacionLimite" 			
			WHERE SOL."IdTipoEvento"=p_IdConcepto
  			 	AND SOL."IdEstadoAutorizacion"='PEN';
			
			IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'EVALUACION_LIMITE_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			
		END;
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			SELECT "IdPais"
			INTO v_IdPaisSistema
			FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = :p_IdEmpresa;
		--Se termina el flujo con la actualizacion del registro de solicitud.
		SELECT MIN( EVA."Prioridad") INTO v_Prioridad FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite" EVA
		JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"
		WHERE EVA."IdTipoEvento"=p_IdConcepto AND  EVA."IdEstadoAutorizacion"='PEN' ;		

		IF v_Prioridad > 0 THEN --Actualiza segun prioridad

				---Actualiza el evento especifico 
					UPDATE EVA
						SET "IdEstadoAutorizacion"=v_IdEstadoAutorizacion
							,"IdUsuarioAutoriza"= p_IdUsuario
							,"FechaAutorizacion"= CURRENT_UTCTIMESTAMP
							,"IdEstado"= 'RZD'
						FROM  "BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA
						JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"	
						WHERE  EVA."IdTipoEvento"=p_IdConcepto
 		    			  AND  EVA."IdEstadoAutorizacion"='PEN'; --Solo Pendientes por autorizar/rechazar



				---Actualiza eventos con prioridad distinta al evento especifico (Con priorodad igual siguen flujo propio)
					UPDATE EVA
						SET "IdEstadoAutorizacion"=v_IdEstadoAutorizacion
							,"IdUsuarioAutoriza"= p_IdUsuario
							,"FechaAutorizacion"= CURRENT_UTCTIMESTAMP
							,"IdEstado"= 'RZD'
						FROM  "BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA
						JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"
						JOIN --Define Prioridad
								(
									SELECT EVA."IdEvaluacionLimite",MIN( EVA."Prioridad") AS "Prioridad"
									FROM  "BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA
											JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"
								    GROUP BY 	EVA."IdEvaluacionLimite"
							    ) AS PRI
							    ON ( EVA."IdEvaluacionLimite"= PRI."IdEvaluacionLimite"  AND EVA."Prioridad"<> PRI."Prioridad" )																		
						WHERE EVA."IdEstadoAutorizacion"='PEN'; --Solo Pendientes por autorizar/rechazar




		
		ELSE
					UPDATE EVA
						SET "IdEstadoAutorizacion"=v_IdEstadoAutorizacion
							,"IdUsuarioAutoriza"= p_IdUsuario
							,"FechaAutorizacion"= CURRENT_UTCTIMESTAMP
							,"IdEstado"= 'RZD'
						FROM  "BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA
						JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"								
						WHERE EVA."IdTipoEvento"=p_IdConcepto
					      AND EVA."IdEstadoAutorizacion"='PEN'; --Solo Pendientes por autorizar/rechazar
		END IF;				
		/* Se envia la respuesta de la operaciones evaluadas dependiendo del Negocio */				
		
		v_CambioEvento=SELECT EVA."IdOperacion",EVA."IdTipoEntidad",EVA."IdTipoInstrumento",EVA."IdClasificacionTipoInstrumento",EVA."IdTipoOperacion"
		FROM  "BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA
		JOIN :P_IdEvaluacionLimite L ON L."IdEvaluacionLimite" = EVA."IdEvaluacionLimite"	
		GROUP BY  EVA."IdOperacion",EVA."IdTipoEntidad",EVA."IdTipoInstrumento",EVA."IdClasificacionTipoInstrumento",EVA."IdTipoOperacion";
		
		v_IdTipoEntidad:='ORDEN';
		SELECT COUNT(1) into v_count FROM :v_CambioEvento WHERE "IdTipoEntidad" = v_IdTipoEntidad ;		
		IF (v_count > 0) THEN
			v_IdOperaciones = SELECT "IdOperacion" as "IdOrden" FROM :v_CambioEvento WHERE "IdTipoEntidad" = v_IdTipoEntidad;
			--CALL "BSCL.Orden.BD.Servicios::RechazaAutorizacionOrdenes"(p_IdEmpresa, p_IdUsuario, p_IdModulo,'N', :p_IdGrupoAutorizacion,:v_IdOperaciones,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
		END IF;				
		v_IdTipoEntidad:='MESA';
		SELECT COUNT(1) into v_count FROM :v_CambioEvento WHERE "IdTipoEntidad" = v_IdTipoEntidad ;		
		IF (v_count > 0) THEN
			v_TblNuevoEstadoFinal = SELECT eve."IdOperacion"
										,tip."IdTipoProducto"
										,'RZD' as "IdNuevoEstado"
										,0 AS "IdReserva"
										,0 AS "FolioCarteraCustodia"
										,0 AS "MontoOperacionAnticipo"
										,0 AS "MontoOperacionInicialAnticipo"
										,0 AS "MontoOperacionFinalAnticipo"
										,0 AS "TasaAnticipo"
										,0 AS "MontoOperacionMonedaAnticipo"
										,0 AS "MontoOperacionInicialMonedaAnticipo"
										,0 AS "MontoOperacionFinalMonedaAnticipo"
										,'' AS "IdMonedaLiquidacionAnticipo"
										,'' AS "IdCondicionLiquidacionAnticipo"
										,'0001-01-01' AS "FechaCondicionLiquidacionAnticipo"
										,'' AS "Glosa"
									FROM :v_CambioEvento eve 
										INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tip ON (eve."IdTipoInstrumento"= tip."IdTipoInstrumento" 
											and eve."IdClasificacionTipoInstrumento" = tip."IdClasificacionTipoInstrumento" and eve."IdTipoOperacion"= tip."IdTipoOperacion")
									WHERE "IdTipoEntidad" = v_IdTipoEntidad;
			
			INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"
			SELECT * FROM :v_TblNuevoEstadoFinal;
			
			CALL "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion" (
															p_IdEmpresa => p_IdEmpresa
		    												,p_IdUsuario => p_IdUsuario
		    												,p_IdModulo 	=> p_IdModulo
		    												,p_simularOperacion => 'N'
															,p_UsaReglasNegocio => p_UsaReglasNegocio
		  													,p_IdPais => v_IdPaisSistema
		    												--,p_TblNuevoEstado  => :v_TblNuevoEstadoFinal
		  													--,p_out_TblResultado => v_out_TblResultado --OK, ADV, ERR
															);
			
			v_out_TblResultado = SELECT * FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT";
			
			p_out_AdvertenciasDetalle = SELECT * FROM :p_out_AdvertenciasDetalle UNION ALL
									SELECT "MsgResultado" as "Codigo",'N' as "RequiereAutorizacion",'' as "Descripcion"
									FROM :v_out_TblResultado
									WHERE "CodResultado" = 'ADV';
			p_out_ErroresDetalle = 		SELECT * FROM :p_out_ErroresDetalle UNION ALL
										SELECT "MsgResultado" as "Codigo",'' as "Descripcion"
											FROM :v_out_TblResultado
											WHERE "CodResultado" = 'ERR';
		END IF;
		
						
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;
