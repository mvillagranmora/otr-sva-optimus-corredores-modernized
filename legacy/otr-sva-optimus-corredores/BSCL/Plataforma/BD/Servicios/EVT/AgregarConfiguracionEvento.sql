SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionEvento";
 
CREATE PROCEDURE
-- 
--  Proposito: Agrega una configuracion para que sea asociada a un tipo evento
--  Autor: - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionEvento"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN p_Agrupa 					NVARCHAR(1),
 									IN p_FormaDespacho				nvarchar(1),
 									IN p_FechaDespacho				NVARCHAR(20),
 									IN p_TiempoDespacho				nvarchar(1),
 									IN p_FechaVencimiento			BIGINT,
 									IN p_FechaRecordatorio	 		BIGINT,
 									IN p_RequiereConfirmacion		nvarchar(1),
 									IN p_RequiereRecordar			nvarchar(1),
 									IN p_Personalizable				nvarchar(1),
 									IN p_Descripcion				nvarchar(150),
 									IN p_Plantilla					BIGINT,
 									IN p_PlantillaRecordatorio		BIGINT,
 									IN p_Correos					TABLE("Correo" NVARCHAR(520)),
 									IN p_ExcluyeDestinatario		NVARCHAR(1),
 									IN p_IdRol 						BIGINT,
 									IN p_EnviarRemitente			NVARCHAR(1),
 									IN p_CopiasAdicionales			NVARCHAR(1),
 									
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
		declare fechaHoraUTC NVARCHAR(40);
		declare v_fechaDespacho NVARCHAR(40);
		declare v_IdConfiguracionEvento BIGINT;
		declare v_fechaVencimiento SECONDDATE;
		declare v_fechaRecordatorio SECONDDATE;
		declare v_CodigoTipoEvento NVARCHAR(40);
		declare p_Todos NVARCHAR(1);
		declare v_IdConfiguracionTipoEvento BIGINT;
		declare p_token NVARCHAR(2000);
		declare p_contador BIGINT;
		DECLARE v_diaHabilResultado NVARCHAR(1);
		p_token := '';
 		SELECT CURRENT_UTCTIMESTAMP into v_fechaVencimiento from dummy;
 		SELECT CURRENT_UTCTIMESTAMP into v_fechaRecordatorio from dummy;
		
		
		
		IF :p_TiempoDespacho = 'M' THEN
		
		 v_fechaVencimiento := '0001-01-01 00:00:00';
		ELSE
			p_contador :=0;
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
			p_contador :=0;
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
		
		
		
		
		SELECT "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento".nextval INTO v_IdConfiguracionEvento FROM DUMMY;

				
 		select to_char(CURRENT_UTCTIMESTAMP,'YYYY-MM-DD') INTO v_fechaDespacho FROM DUMMY; 
		v_fechaManual := '0001-01-01 00:00:00';
		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" 
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCopiasCorreo".NEXTVAL,
				 v_IdConfiguracionEvento,
				 "Correo",
				 p_IdUsuario,
				 to_char(CURRENT_UTCTIMESTAMP,'YYYY-MM-DD HH24:MI:SS')  
		FROM :p_Correos;
	    
		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" 
		(
			"IdEmpresa",
			"IdConfiguracionEvento",
			"Descripcion",
			"IdUsuario",
			"Agrupa",
			"FechaCreacion",
			"IdEstado",
			"PermitePersonalizar",
			"FormaDespacho",
			"TiempoDespacho",
			"FechaDespacho",
			"ConfirmacionDespacho",
			"FechaVencimiento",
			"FechaRecordatorio",
			"IdPlantilla",
			"IdPlantillaRecordatorio",
			"DiasVencimiento",
			"DiasRecordatorio",
			"RecordarDespacho",
			"CopiasAdicionales"
		)
		VALUES (
		 p_IdEmpresa,
		 v_IdConfiguracionEvento,
		 p_Descripcion, 
		 p_IdUsuario,
		 p_Agrupa,
		 CURRENT_UTCTIMESTAMP,
		 'ACT',
		 p_personalizable,
		 p_FormaDespacho,
		 p_TiempoDespacho,
		 CASE p_TiempoDespacho WHEN 'I' THEN  to_char(CURRENT_UTCTIMESTAMP,'YYYY-MM-DD HH24:MI:SS')
			WHEN 'M' THEN v_fechaManual WHEN 'H' THEN v_fechaDespacho||' '||p_FechaDespacho END,
		 p_requiereConfirmacion,
		 v_fechaVencimiento,
		 v_fechaRecordatorio,
		 p_plantilla,
		 p_plantillaRecordatorio,
		 p_FechaVencimiento,
		 p_FechaRecordatorio,
		 p_RequiereRecordar,
		 p_CopiasAdicionales
		 
		 );
		 
		IF :p_IdRol = 0 THEN
		
		  p_Todos := 'S';
		
		ELSE
		
		  p_Todos := 'N';
		 
		
		END IF;
		
  		INSERT INTO "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento"
		VALUES (
			"BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCodigoTipoEvento".NEXTVAL,
			v_IdConfiguracionEvento,
			p_Todos,
			p_ExcluyeDestinatario,
			'R',
			p_IdRol,
			p_EnviarRemitente);
		
		
		p_salida := 'Agregado correctamente';
		
	END;

END;