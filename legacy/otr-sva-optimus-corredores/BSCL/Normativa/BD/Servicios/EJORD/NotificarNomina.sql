SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::EJORD.NotificarNomina";


CREATE PROCEDURE 
--
--  Proposito: Servicio para generar notificacion a la nomina con la Ejecucion de Ordenes del periodo.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 16/02/2017

"BSCL.Normativa.BD.Servicios::EJORD.NotificarNomina" ( 

    --VARIABLES DEL PROCESO
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT DEFAULT 0,--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN  p_Frecuencia        				 	   	NVARCHAR(14) DEFAULT 'DIARIA', -- DIARIA/MENSUAL/SEMANAL
	

    --VARIABLES DE SALIDA OBLIGATORIAS

	-- Parametros salida del servicio publico
	OUT p_out_IdEvento table ("IdEvento" BIGINT),
--	OUT p_out_RefId 					BIGINT,	
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
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA


	BEGIN
		--salida/error/advertencia controlado por este servicio
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
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

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_out_Evento TABLE("IdEvento" BIGINT);
		DECLARE v_count	BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        DECLARE v_fechaHoraSistema, v_out_MensajeEval		 NVARCHAR(40):='';
        DECLARE v_FechaDesde, v_FechaHasta,v_FechaOperacion NVARCHAR(10):='';
        DECLARE v_ValorIndicadorFrecuencia 	NVARCHAR(20):='DIA';
        DECLARE v_ValorIndicadorEnvio 		NVARCHAR(20):='MAIL';
		--------------------
		--Lógica Negocio
		--------------------
		
		--Inicializacion de variables:
	
		BEGIN -- NOTIFICACIONES 
		
		
			-- Identificar fecha de trabajo
		--------------------------------------------------------
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
		
			IF p_Frecuencia='DIARIA' OR p_Frecuencia='' THEN 
			  v_FechaDesde=v_FechaOperacion;
			  v_FechaHasta=v_FechaDesde;
			  v_ValorIndicadorFrecuencia='DIA';
			  ELSE
				IF p_Frecuencia='SEMANAL' THEN
					SELECT TOP 1 LEFT(:v_FechaOperacion,8) || '01' INTO v_FechaDesde  FROM DUMMY;
					SELECT TOP 1 TO_VARCHAR (LAST_DAY (TO_DATE(v_FechaOperacion, 'YYYY-MM-DD')), 'YYYY-MM-DD') INTO v_FechaHasta  FROM DUMMY;
					v_ValorIndicadorFrecuencia='SEM';
			  	END IF; 
			  	IF p_Frecuencia='MENSUAL' THEN
					SELECT TOP 1 LEFT(:v_FechaOperacion,8) || '01' INTO v_FechaDesde  FROM DUMMY;
					SELECT TOP 1 TO_VARCHAR (LAST_DAY (TO_DATE(v_FechaOperacion, 'YYYY-MM-DD')), 'YYYY-MM-DD') INTO v_FechaHasta  FROM DUMMY;
					v_ValorIndicadorFrecuencia='MES';
			  	END IF; 
			END IF;
		
		
		
			SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" EJE
							INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND --FORMA DE ENVIO
								ON (IND."IdCliente" =EJE."IdCliente" AND IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='REOE' AND IND."CodigoValorIndicador"=v_ValorIndicadorEnvio)
							INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" INDE --FRECUENCIA DE ENVIO
								ON (INDE."IdCliente" =EJE."IdCliente" AND INDE."IdUso"='IND' AND INDE."CodigoTipoIndicador"='REOP' AND INDE."CodigoValorIndicador"=v_ValorIndicadorFrecuencia)
							WHERE EJE."IdEstadoNotificacion"='PEN'
							AND  TO_DATE(EJE."FechaEjecucion") BETWEEN  TO_DATE(v_FechaDesde) AND TO_DATE(v_FechaHasta);
			
			IF v_count > 0 THEN 	
			
			--Generar grupo evento  de Notificar
			
			  v_tablaNomina =SELECT EJE."IdEjecucion" FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" EJE
			  				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND --FORMA DE ENVIO
								ON (IND."IdCliente" =EJE."IdCliente" AND IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='REOE' AND IND."CodigoValorIndicador"=v_ValorIndicadorEnvio)
							INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" INDE --FRECUENCIA DE ENVIO
								ON (INDE."IdCliente" =EJE."IdCliente" AND INDE."IdUso"='IND' AND INDE."CodigoTipoIndicador"='REOP' AND INDE."CodigoValorIndicador"=v_ValorIndicadorFrecuencia)
							WHERE EJE."IdEstadoNotificacion"='PEN'
								AND  TO_DATE(EJE."FechaEjecucion") BETWEEN  TO_DATE(v_FechaDesde) AND TO_DATE(v_FechaHasta);
			  	
				
				
					v_out_Evento = SELECT 0 AS "IdEvento" FROM DUMMY WHERE 1=2;	
						
		 			CALL "BSCL.Normativa.BD.Servicios::EJORD.NotificarCliente"(
		 					p_IdEmpresa					=> p_IdEmpresa,
		 					p_IdUsuario 				=> p_IdUsuario,
		 					p_IdModulo 					=> p_IdModulo,
		 					p_TablaNotificacion 		=> :v_tablaNomina,
		 					p_out_IdEvento 				=>v_out_Evento,
		 					p_out_AdvertenciasContador	=>V_out_AdvertenciasContador,
		 					p_out_AdvertenciasDetalle	=>v_out_AdvertenciasDetalle,
		 					p_out_ErroresContador		=>V_out_ErroresContador,
		 					p_out_ErroresDetalle		=>V_out_ErroresDetalle
		 					);
		 				p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Evento;		
		 		
		 				p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
		  			 
					 	p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
		                                          From :v_out_AdvertenciasDetalle Union All 
		                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
		                                          From :p_out_AdvertenciasDetalle;  
		                                          
		                                          
		             	p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
		                                         
		              	p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
		                                     From :v_out_ErroresDetalle Union All 
		                                     Select "Codigo", "Descripcion" 
		                                     From :p_out_ErroresDetalle; 
		                
		                                     
		                                     
					  if 	p_out_ErroresContador > 0 then
								SIGNAL SALIDA_CONTROLADA;
					  end if;								
	
					
	
	
		 					
			END IF;
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
