SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionEvento";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza una configuracion para que sea asociada a un tipo evento
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionEvento"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN p_IdConfiguracionEvento 		bigint,
	                                IN p_Agrupa						NVARCHAR(1),
 									IN p_FormaDespacho				nvarchar(1),
 									IN p_TiempoDespacho				nvarchar(1),
 									IN p_FechaDespacho				nvarchar(20),
 									IN p_FechaVencimiento			BIGINT,
 									IN p_FechaRecordatorio	 		BIGINT,
 									IN p_RequiereConfirmacion		nvarchar(1),
 									IN p_RequiereRecordar			nvarchar(1),
 									IN p_Personalizable				nvarchar(1),
 									IN p_Plantilla					BIGINT,
 									IN p_PlantillaRecordatorio		BIGINT,
 									IN p_Correos					TABLE("Correo" NVARCHAR(520)),
 									IN p_ExcluyeDestinatario		NVARCHAR(1),
 									IN p_IdRol 						BIGINT,
 									IN p_EnviarRemitente			NVARCHAR(1),
 									IN p_CopiasAdicionales			NVARCHAR(1),
 									IN p_ControlProceso				NVARCHAR(1),
 									
 									OUT p_salida 					NVARCHAR(520),
 									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 


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
		p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
		
		declare v_fechaManual SECONDDATE;
		declare fechaHoraUTC SECONDDATE;
		declare v_fechaDespacho NVARCHAR(40);
		declare v_fechaVencimiento SECONDDATE;
		declare v_fechaRecordatorio SECONDDATE;
		declare v_PlantillaRecordatorio BIGINT;
		declare p_contador BIGINT;
		DECLARE v_diaHabilResultado NVARCHAR(1);
		DECLARE tests NVARCHAR(10);
		
		select CURRENT_UTCTIMESTAMP into v_fechaVencimiento from dummy;
 		select CURRENT_UTCTIMESTAMP into v_fechaRecordatorio from dummy;
		
		
		IF :p_RequiereRecordar = 'N' THEN
			v_PlantillaRecordatorio := 0;
		ELSE
		 v_PlantillaRecordatorio := p_PlantillaRecordatorio;
		END IF;
		IF :p_TiempoDespacho = 'M' THEN
		
		 v_fechaVencimiento := '0001-01-01 00:00:00';
		ELSE
			p_contador := 0;
			WHILE p_contador < p_FechaVencimiento 
				DO
				SELECT add_days(v_fechaVencimiento,1) INTO v_fechaVencimiento FROM DUMMY;
				call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(v_fechaVencimiento,'YYYY-MM-DD'),1,v_diaHabilResultado);
				IF :v_diaHabilResultado = 'S' THEN
					p_contador := p_contador + 1;					
				END IF;
			END WHILE;
			
		END IF;
		
		IF :p_RequiereRecordar = 'S' THEN
			p_contador := 0;
			WHILE p_contador < p_FechaRecordatorio 
				DO
				SELECT add_days(v_fechaRecordatorio,1) INTO v_fechaRecordatorio FROM DUMMY;
				call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(v_fechaRecordatorio,'YYYY-MM-DD'),1,v_diaHabilResultado);
				IF :v_diaHabilResultado = 'S' THEN
					p_contador := p_contador + 1;
				END IF;
			END WHILE;
		ELSE
			v_fechaRecordatorio := '0001-01-01 00:00:00';
		END IF;
		
	
		v_fechaManual := '0001-01-01 00:00:00';
		
		
		
		
		
		DELETE FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" 
			WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento;

		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" 
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCopiasCorreo".NEXTVAL,
				 p_IdConfiguracionEvento,
				 cci."Correo",
				 p_IdUsuario,
				 to_char(CURRENT_UTCTIMESTAMP,'YYYY-MM-DD HH24:MI:SS')  
		FROM :p_Correos cci;
		
		UPDATE "BSCL.Plataforma.BD::EVT.Notificacion" SET "ConfirmacionDespacho" = p_RequiereConfirmacion 
		WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento;
 
		p_DetalleLog =
	 		 SELECT "Descripcion" as "SubRegistro", 'PermitePersonalizar' as "CampoRegistro", to_char("PermitePersonalizar") as "ValorAntiguo",  p_Personalizable  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'FormaDespacho' as "CampoRegistro", to_char("FormaDespacho") as "ValorAntiguo",  p_FormaDespacho  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'TiempoDespacho' as "CampoRegistro", to_char("TiempoDespacho") as "ValorAntiguo",  p_TiempoDespacho  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'FechaDespacho' as "CampoRegistro", to_char("FechaDespacho") as "ValorAntiguo",  p_FechaDespacho  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'ConfirmacionDespacho' as "CampoRegistro", to_char("ConfirmacionDespacho") as "ValorAntiguo",  p_RequiereConfirmacion  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'FechaVencimiento' as "CampoRegistro", to_char("FechaVencimiento") as "ValorAntiguo",  to_char(p_FechaVencimiento)  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'FechaRecordatorio' as "CampoRegistro", to_char("FechaRecordatorio") as "ValorAntiguo",  to_char(p_FechaRecordatorio)  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'IdPlantilla' as "CampoRegistro", to_char("IdPlantilla") as "ValorAntiguo",  to_char(p_Plantilla)  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento
	    	 UNION ALL
	    	 SELECT "Descripcion" as "SubRegistro", 'IdPlantillaRecordatorio' as "CampoRegistro", to_char("IdPlantillaRecordatorio") as "ValorAntiguo",  to_char(p_PlantillaRecordatorio)  as "ValorNuevo"			
	    		FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento;
	 
	
		UPDATE "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" 
		SET
		"Agrupa" = p_Agrupa,
	 	"PermitePersonalizar" =p_Personalizable,
	 	"FormaDespacho" = p_FormaDespacho,
	 	"TiempoDespacho" = p_TiempoDespacho,
	 	"FechaDespacho" =    p_FechaDespacho,
	 	"ConfirmacionDespacho" = p_RequiereConfirmacion,
	 	"RecordarDespacho" = p_RequiereRecordar,
	 	"FechaVencimiento" = v_fechaVencimiento,
	 	"FechaRecordatorio" = v_fechaRecordatorio,
	 	"IdPlantilla" = p_Plantilla,
		"IdPlantillaRecordatorio" = v_PlantillaRecordatorio,
		"DiasVencimiento" = p_FechaVencimiento,
		"DiasRecordatorio" = p_FechaRecordatorio,
		"CopiasAdicionales" = p_CopiasAdicionales,
		"ControlProceso" =p_ControlProceso
		WHERE "IdConfiguracionEvento" = p_IdConfiguracionEvento ;
		
		p_salida := 'Actualización Correcta';
		
 
		CALL "BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionTipoEvento"(p_IdConfiguracionEvento,p_ExcluyeDestinatario,p_IdRol,p_EnviarRemitente);
		

	    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'MODIFICACION' 
			,p_IdModulo => 'PLA' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ACTUALIZAR NOTIFICACION'
			,p_IdRegistro => p_IdConfiguracionEvento
			,p_DetalleLog => :p_DetalleLog
			);
		  
		  
	END;

END;
