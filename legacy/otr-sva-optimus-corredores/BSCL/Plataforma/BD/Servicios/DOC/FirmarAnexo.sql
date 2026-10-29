SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DOC.FirmarAnexo";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para firmar anexo de documento.
--  Autor: - BCS
--  Fecha Creacion: 17/12/2015 
--  
"BSCL.Plataforma.BD.Servicios::DOC.FirmarAnexo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3) DEFAULT 'N',  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdAnexo				BIGINT,	
	IN  p_IdDocumento			BIGINT,
	IN  p_FirmaEmpresa1			NVARCHAR(1)     DEFAULT 'N',
   	IN  p_FirmaEmpresa2    		NVARCHAR(1)     DEFAULT 'N',
   	IN  p_FirmaEmpresa3    		NVARCHAR(1)     DEFAULT 'N',
   	IN  p_FirmaCliente1        	NVARCHAR(1)     DEFAULT 'N',
   	IN  p_FirmaCliente2        	NVARCHAR(1)     DEFAULT 'N',
   	IN  p_FirmaCliente3        	NVARCHAR(1)		DEFAULT 'N',
   	IN  p_NumeroFirmantesEmpresa BIGINT DEFAULT 0,
  	IN  p_NumeroFirmantesCliente BIGINT DEFAULT 0,
   	IN  p_NombreFirmanteCliente1 NVARCHAR(100)   DEFAULT '',
	IN  p_NombreFirmanteCliente2 NVARCHAR(100)   DEFAULT '',
	IN  p_NombreFirmanteCliente3 NVARCHAR(100)   DEFAULT '',
	IN  p_NombreFirmanteEmpresa1 NVARCHAR(100)   DEFAULT '',
	IN  p_NombreFirmanteEmpresa2 NVARCHAR(100)   DEFAULT '',
	IN  p_NombreFirmanteEmpresa3 NVARCHAR(100)   DEFAULT '',
	IN  p_FechaFirmaEmpresa1	DATE	DEFAULT '0001-01-01',
	IN  p_FechaFirmaEmpresa2	DATE	DEFAULT '0001-01-01',
	IN  p_FechaFirmaEmpresa3	DATE	DEFAULT '0001-01-01',
	IN  p_FechaFirmaCliente1	DATE	DEFAULT '0001-01-01',
	IN  p_FechaFirmaCliente2	DATE	DEFAULT '0001-01-01',
	IN  p_FechaFirmaCliente3	DATE	DEFAULT '0001-01-01',
	IN  p_FechaVigencia 		SECONDDATE DEFAULT '2999-12-31 00:00:00',
	IN  p_IdTipoDocumento 		NVARCHAR(40),
	IN  p_IdCliente				BIGINT,
   	 
--	IN  p_Firmas TABLE (Firma	NVARCHAR(100), valorFirma NVARCHAR(1)),
--	IN  p_Firmante				NVARCHAR(100), --FirmaEmpresa1, FirmaEmpresa2, FirmaEmpresa3, FirmaCliente1, FirmaCliente2, FirmaCliente3
--	IN  p_valorFirma			NVARCHAR(1),
 	
		
	-- Parametros salida del servicio publico
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),	
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
	DECLARE v_out_Resultado 		NVARCHAR(1);
    DECLARE v_out_Mensaje 			NVARCHAR(520);
    DECLARE v_cantidadReg			BIGINT;
    DECLARE v_FechaHoraResgistro 	SECONDDATE;
    DECLARE v_continuaValidacion 	NVARCHAR(1);
    DECLARE v_requiereAutorizacion 	NVARCHAR(1);
    
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_erroroutmsg  nvarchar(520);	        	 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;	
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;	 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log será captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_contador INT;
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE v_FechaSistema NVARCHAR(40);
		DECLARE v_cantidadFirmas INT;
		DECLARE v_count1,v_count2,v_count3,v_count4,v_count5,v_count6 INT;
		DECLARE v_IdAnexo, v_IdDocumento BIGINT;
		DECLARE v_CodigoTipoIndicador NVARCHAR(10);
		DECLARE v_IdTipoIndicador BIGINT;
		DECLARE v_count int;
		DECLARE v_count_tipo_indicador int;
		DECLARE v_count_cantidad_clientes int DEFAULT 0;
		DECLARE v_count_cantidad_firmas_clientes int DEFAULT 0;
		DECLARE v_count_cantidad_empresas int DEFAULT 0;
		DECLARE v_count_cantidad_firmas_empresas int DEFAULT 0;
		
		p_out_AdvertenciasDetalle = SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
		--------------------
		--Validaciones
		--------------------		
		--Exista anexo en la tabla de anexos
		IF p_IdAnexo > 0 THEN
			SELECT COUNT(1) into v_contador 
			FROM "BSCL.Plataforma.BD::DOC.Anexo"
			WHERE "IdAnexo" = p_IdAnexo
			AND "IdDocumento" = p_IdDocumento;
			IF v_contador = 0 THEN
				p_out_ErroresDetalle =  SELECT '' AS "Codigo",'Anexo y/o Documento no existen' AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
			
		SELECT "NumeroFirmantesEmpresa", "NumeroFirmantesCliente" INTO v_count_cantidad_empresas, v_count_cantidad_clientes
		FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento" = :p_IdTipoDocumento;
		
		--Contar cuantas firmas vienen
		IF p_FirmaEmpresa1='S' THEN
			v_count_cantidad_firmas_empresas := v_count_cantidad_firmas_empresas+1;
		END IF;
		IF p_FirmaEmpresa2='S' THEN
			v_count_cantidad_firmas_empresas := v_count_cantidad_firmas_empresas+1;
		END IF;
		IF p_FirmaEmpresa3='S' THEN
			v_count_cantidad_firmas_empresas := v_count_cantidad_firmas_empresas+1;
		END IF;
		
		IF v_count_cantidad_empresas <> v_count_cantidad_firmas_empresas THEN
			v_hash= select 'DOCUMENTO' as "Codigo", '' as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_FALTAN_FIRMAS_EMPRESA',:v_hash,:v_erroroutmsg);
			p_out_AdvertenciasDetalle = select 'UI_FALTAN_FIRMAS_EMPRESA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_AdvertenciasDetalle;
		END IF;
		
		IF p_FirmaCliente1='S' THEN
			v_count_cantidad_firmas_clientes := v_count_cantidad_firmas_clientes+1;
		END IF;
		IF p_FirmaCliente2='S' THEN
			v_count_cantidad_firmas_clientes := v_count_cantidad_firmas_clientes+1;
		END IF;
		IF p_FirmaCliente3='S' THEN
			v_count_cantidad_firmas_clientes := v_count_cantidad_firmas_clientes+1;
		END IF;
		
		IF v_count_cantidad_clientes <> v_count_cantidad_firmas_clientes THEN
			v_hash= select 'DOCUMENTO' as "Codigo", '' as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_FALTAN_FIRMAS_CLIENTE',:v_hash,:v_erroroutmsg);
			p_out_AdvertenciasDetalle = select 'UI_FALTAN_FIRMAS_CLIENTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_AdvertenciasDetalle;
		END IF;
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------		
		BEGIN
		
		
		v_DetalleLog = Select '' as "SubRegistro", '' as "CampoRegistro",'' as "ValorAntiguo", '' as "ValorNuevo" from DUMMY WHERE 1=2;
		
		v_DetalleLog = 
		(SELECT "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
		UNION ALL
		Select 'USUARIOFIRMA' as "SubRegistro", 
		'IdUsuario' as "CampoRegistro",
		'' as "ValorAntiguo",
		 TO_VARCHAR(p_IdUsuario) as "ValorNuevo" from DUMMY
		UNION ALL
		Select 'CLIENTE' as "SubRegistro",
		 'IdCliente' as "CampoRegistro",
		 '' as "ValorAntiguo",
		  TO_VARCHAR(p_IdCliente) as "ValorNuevo" from DUMMY);
		  
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_FechaSistema);
		
		v_FechaSistema := SUBSTRING(v_FechaSistema,1,19);
		
		IF p_IdAnexo = 0 THEN
		
			SELECT "BSCL.Plataforma.BD.Secuencias::Anexo".NEXTVAL INTO v_IdAnexo FROM DUMMY;
			IF p_IdDocumento = 0 THEN		
				SELECT "BSCL.Plataforma.BD.Secuencias::Documento".NEXTVAL INTO v_IdDocumento FROM DUMMY;
				insert into "BSCL.Plataforma.BD::DOC.Documento" 
				(
					"IdDocumento",
					"IdTipoDocumento",
					"IdEmpresa",
					"IdCliente",
					"IdUltimoAnexo"
				)
				values( v_IdDocumento,
						p_IdTipoDocumento,
						p_IdEmpresa,
						p_IdCliente,
						v_IdAnexo);
			ELSE
				v_IdDocumento := p_IdDocumento;
				
				UPDATE "BSCL.Plataforma.BD::DOC.Documento"
				SET "IdUltimoAnexo" = v_IdAnexo
				WHERE "IdDocumento" = v_IdDocumento;
				
				
				
			END IF;
			
			insert into "BSCL.Plataforma.BD::DOC.Anexo"
				(
					"IdAnexo",
					"IdDocumento",
					"IdEstado",
					"NumeroFirmantesEmpresa",
					"NumeroFirmantesCliente",
					"NombreFirmanteEmpresa1", 
					"NombreFirmanteEmpresa2",
					"NombreFirmanteEmpresa3",
					"NombreFirmanteCliente1",
					"NombreFirmanteCliente2",
					"NombreFirmanteCliente3",
					"FechaIngreso",
					"FechaModificacion",
					"FechaVigencia",
					"DocumentoFisicoReferencia",
					"FechaFirmaEmpresa1",
					"FechaFirmaEmpresa2",
					"FechaFirmaEmpresa3",
					"FechaFirmaCliente1",
					"FechaFirmaCliente2",
					"FechaFirmaCliente3"
				)
				values(
				v_IdAnexo,
				v_IdDocumento,
				'PDF',
				:p_NumeroFirmantesEmpresa,
				:p_NumeroFirmantesCliente,
				:p_NombreFirmanteEmpresa1,
				:p_NombreFirmanteEmpresa2,
				:p_NombreFirmanteEmpresa3,
				:p_NombreFirmanteCliente1,
				:p_NombreFirmanteCliente2,
				:p_NombreFirmanteCliente3,
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				p_FechaVigencia,
				'',
				p_FechaFirmaEmpresa1,
				p_FechaFirmaEmpresa2,
				p_FechaFirmaEmpresa3,
				p_FechaFirmaCliente1,
				p_FechaFirmaCliente2,
				p_FechaFirmaCliente3);
				
		ELSE
			v_IdAnexo := p_IdAnexo;
			v_IdDocumento := p_IdDocumento;
		END IF;
		
		
		UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
		SET 
		"FirmaEmpresa1" = p_FirmaEmpresa1,
		"FirmaEmpresa2" = p_FirmaEmpresa2,
		"FirmaEmpresa3" = p_FirmaEmpresa3,
		"FirmaCliente1" = p_FirmaCliente1,
		"FirmaCliente2" = p_FirmaCliente2,
		"FirmaCliente3" = p_FirmaCliente3,
		"NombreFirmanteCliente1" = p_NombreFirmanteCliente1,
		"NombreFirmanteCliente2" = p_NombreFirmanteCliente2,
		"NombreFirmanteCliente3" = p_NombreFirmanteCliente3,
		"NombreFirmanteEmpresa1" = p_NombreFirmanteEmpresa1,
		"NombreFirmanteEmpresa2" = p_NombreFirmanteEmpresa2,
		"NombreFirmanteEmpresa3" = p_NombreFirmanteEmpresa3,
		"FechaModificacion" = TO_SECONDDATE(v_FechaSistema,'YYYY-MM-DD HH24:MI:SS'),
		"FechaVigencia" = p_FechaVigencia,
		"FechaFirmaEmpresa1" = p_FechaFirmaEmpresa1,
		"FechaFirmaEmpresa2" = p_FechaFirmaEmpresa2,
		"FechaFirmaEmpresa3" = p_FechaFirmaEmpresa3,
		"FechaFirmaCliente1" = p_FechaFirmaCliente1,
		"FechaFirmaCliente2" = p_FechaFirmaCliente2,
		"FechaFirmaCliente3" = p_FechaFirmaCliente3
		WHERE "IdAnexo" = v_IdAnexo
		AND "IdDocumento" = v_IdDocumento;
		
		
		
		SELECT "NumeroFirmantesEmpresa" + "NumeroFirmantesCliente" INTO v_cantidadFirmas
		FROM "BSCL.Plataforma.BD::DOC.Anexo" 
		WHERE "IdAnexo" = v_IdAnexo
		AND "IdDocumento" = v_IdDocumento;
		
		SELECT COUNT(1) INTO v_count1
		FROM DUMMY 
		WHERE p_FirmaEmpresa1 = 'S';
		SELECT COUNT(1) INTO v_count2
		FROM DUMMY 
		WHERE p_FirmaEmpresa2 = 'S';
		SELECT COUNT(1) INTO v_count3
		FROM DUMMY 
		WHERE p_FirmaEmpresa3 = 'S';
		SELECT COUNT(1) INTO v_count4
		FROM DUMMY 
		WHERE p_FirmaCliente1 = 'S';
		SELECT COUNT(1) INTO v_count5
		FROM DUMMY 
		WHERE p_FirmaCliente2 = 'S';
		SELECT COUNT(1) INTO v_count6
		FROM DUMMY 
		WHERE p_FirmaCliente3 = 'S';
		
		
		-- si se encuentran todas las firmas requeridas, se cambia el estado 
		IF(v_cantidadFirmas = v_count1 + v_count2 + v_count3 + v_count4 + v_count5 + v_count6 ) THEN
			UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
			SET 
			"IdEstado" = 'ACT'
			WHERE "IdAnexo" = v_IdAnexo
			AND "IdDocumento" = v_IdDocumento;
			
			select IFNULL("CodigoTipoIndicador",'') into v_CodigoTipoIndicador 
			from "BSCL.Plataforma.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
			
			IF v_CodigoTipoIndicador != '' THEN

				select COUNT("IdTipoIndicador") into v_count_tipo_indicador
				from "BSCL.Plataforma.BD::IND.TipoIndicador"
				where "CodigoTipoIndicador" = v_CodigoTipoIndicador  ;
			
				IF v_count_tipo_indicador > 0 THEN
				
					select "IdTipoIndicador" into v_IdTipoIndicador
					from "BSCL.Plataforma.BD::IND.TipoIndicador"
					where "CodigoTipoIndicador" = v_CodigoTipoIndicador  ;
				
					select count(1) into v_count from "BSCL.Plataforma.BD::IND.Indicador"
					where "IdTipoIndicador" = v_IdTipoIndicador
					AND "IdEmpresa" = p_IdEmpresa
					AND "IdEntidad" = p_IdCliente
					AND "IdTipoEntidad" = 'CLIENTE';
				
					IF v_count = 1 THEN
					
						UPDATE 	"BSCL.Plataforma.BD::IND.Indicador"
						SET "ValorIndicador" = 'S',
						"FechaModificacion" = CURRENT_UTCTIMESTAMP
						where "IdTipoIndicador" = v_IdTipoIndicador
								AND "IdEmpresa" = p_IdEmpresa
								AND "IdTipoEntidad" = 'CLIENTE'
								AND "IdEntidad" = p_IdCliente;	
				
					ELSE
				
						INSERT INTO  "BSCL.Plataforma.BD::IND.Indicador" 
							  (
							   "IdIndicador",
							   "IdEntidad",
							   "IdTipoEntidad",
							   "IdTipoIndicador",
							   "ModeloCliente", -- <TipoCliente>
							   "IdEmpresa",
							   "ValorIndicador",
							   "FechaIngreso",
							   "FechaCreacion",
							   "FechaModificacion",
							   "IdEstadoAutorizacion"
							   )SELECT 
								"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL,
								p_IdCliente,
								'CLIENTE',
								v_IdTipoIndicador,
								'',
								p_IdEmpresa,
								'S',--Valor inicial del indicador en blanco
								TO_SECONDDATE(v_FechaSistema,'YYYY-MM-DD HH24:MI:SS'),
								CURRENT_UTCTIMESTAMP,
								CURRENT_UTCTIMESTAMP,
								'ACT'				
								FROM dummy;	
					
					END IF;
				ELSE
					v_hash= select 'DOCUMENTO' as "Codigo", '' as "Valor" FROM DUMMY;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_NO_EXISTE_TIPO_INDICADOR',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'UI_NO_EXISTE_TIPO_INDICADOR' as "Codigo",:v_erroroutmsg || ' ' ||  v_CodigoTipoIndicador  as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
		ELSE
			UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
			SET 
			"IdEstado" = 'PDF',
			"FechaModificacion" = CURRENT_UTCTIMESTAMP
			WHERE "IdAnexo" = v_IdAnexo
			AND "IdDocumento" = v_IdDocumento;
		END IF;
		
		v_DetalleLog = Select * from :v_DetalleLog
		UNION ALL
		Select 'DOCUMENTO' as "SubRegistro",
		 'IdDocumento' as "CampoRegistro",
		 '' as "ValorAntiguo",
		  TO_VARCHAR(v_IdDocumento) as "ValorNuevo" from DUMMY
		  UNION ALL
		Select 'ANEXO' as "SubRegistro",
		 'IdAnexo' as "CampoRegistro",
		 '' as "ValorAntiguo",
		  TO_VARCHAR(v_IdAnexo) as "ValorNuevo" from DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'DOC' 
				,p_IdEmpresa => p_IdEmpresa
				,p_IdUsuario => p_IdUsuario 
				,p_FechaSistemaLog => SUBSTRING(v_FechaSistema,1,10)
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => 'DOCUMENTO'
				,p_IdRegistro => p_IdDocumento
				,p_DetalleLog => :v_DetalleLog
				,p_NombreProceso => 'Firma de Documento'
				);
		END;
		
		SIGNAL SALIDA_CONTROLADA;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;