SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarTasa";

-- 
--  Proposito: Modificar Tasa
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 05/10/2016  
-- 
  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarTasa" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdPrecioTasa		BIGINT,
	IN p_Tasa				DECIMAL(26,8)	DEFAULT NULL,
	IN p_TasaEstimada		DECIMAL(26,8)	DEFAULT NULL,
	IN p_Precio				DECIMAL(26,8)	DEFAULT NULL,
	IN p_Plazo				BIGINT,
	
	-- Parametro para registro en Log
	IN p_DetalleLog			TABLE (	"SubRegistro"	NVARCHAR(40),
									"CampoRegistro"	NVARCHAR(40),
									"ValorAntiguo"	NVARCHAR(250),
									"ValorNuevo"	NVARCHAR(250)),
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId			BIGINT,	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        
		DECLARE v_Count			BIGINT:=0;
		DECLARE v_FechaSistema		NVARCHAR(10);
		DECLARE v_FechaHoraSistema	SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		
		p_out_RefId := 0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa,:v_FechaSistema);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
		
		-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
			p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
			p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
			p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
			p_out_FechaHoraUTC			=> v_FechaHoraUTC,
			p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		--------------------
		--Validaciones
		--------------------
		BEGIN
			---Verifica que Precio/Tasa exista
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
			WHERE "IdPrecioTasa" = :p_IdPrecioTasa;
			
			IF(v_Count = 0) THEN
				p_out_ErroresDetalle = select 'PRECIO/TASA NO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			---Verifica que Precio/Tasa no este Oficializada o Publicada
			/*SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
			WHERE "IdPrecioTasa" = :p_IdPrecioTasa
			  AND "IdEstado" IN ('OFI','PUB');
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle = select 'PRECIO/TASA NO SE PUEDE MODIFICAR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;*/
			
			
			---Verifica que Plazo modificado no esté dentro de otro Tramo para los mismos valores
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" PT1
			INNER JOIN (SELECT
							"IdFuente",
							"IdTipoInstrumento",
							"IdInstrumento",
							"Nemotecnico",
							"IdTipoTasa",
							"IdTipoPrecio",
							--"Plazo",
							--"IdEstado",
							"IdEmpresa",
							"FechaSolicitud",
							"TipoRegistro",
							"IdMoneda",
							"IdClasificacionTipoInstrumento"
						FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
						WHERE "IdPrecioTasa" = :p_IdPrecioTasa) PT2
				ON (PT1."IdFuente"		= PT2."IdFuente" AND
					PT1."IdTipoInstrumento"	= PT2."IdTipoInstrumento" AND
					--PT1."IdInstrumento"	= PT2."IdInstrumento" AND
					PT1."Nemotecnico"	= PT2."Nemotecnico" AND
					COALESCE(PT1."IdTipoTasa",'')	= COALESCE(PT2."IdTipoTasa",'') AND 
					COALESCE(PT1."IdTipoPrecio",'')	= COALESCE(PT2."IdTipoPrecio",'') AND
					--PT1."Plazo"			= PT2."Plazo" AND
					--PT1."IdEstado"	= PT2."IdEstado" AND
					PT1."IdEmpresa"		= PT2."IdEmpresa" AND
					TO_VARCHAR(PT1."FechaSolicitud",'YYYY-MM-DD') = TO_VARCHAR(PT2."FechaSolicitud",'YYYY-MM-DD') AND
					PT1."TipoRegistro"	= PT2."TipoRegistro" AND
					PT1."IdMoneda"	= PT2."IdMoneda" AND
					PT1."IdClasificacionTipoInstrumento" = PT2."IdClasificacionTipoInstrumento"
					)
			WHERE PT1."IdPrecioTasa" <> :p_IdPrecioTasa
			  AND PT1."Plazo"	= :p_Plazo
			  AND PT1."Borrado"	= 'N';
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle = select 'PRECIO/TASA YA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			---Verifica que nuevo Precio/Tasa Publicada no exista en la Fecha Proceso con los mismos datos
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" PT1
			INNER JOIN (SELECT
							"IdFuente",
							"IdTipoInstrumento",
							"IdInstrumento",
							"Nemotecnico",
							"IdTipoTasa",
							"IdTipoPrecio",
							"Plazo",
							"IdEmpresa",
							"FechaSolicitud",
							"Borrado",
							"TipoRegistro",
							"IdMoneda",
							"IdClasificacionTipoInstrumento"
						FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
						WHERE "IdPrecioTasa" = :p_IdPrecioTasa
						  AND TO_VARCHAR("FechaSolicitud",'YYYY-MM-DD') <> :v_FechaSistema
						  AND TO_VARCHAR("FechaPublicacion",'YYYY-MM-DD') <> :v_FechaSistema
						  AND "IdEmpresa" = :p_IdEmpresa
						  AND "IdEstado" = 'PUB'
						  AND "Borrado" = 'N') PT2
				ON (PT1."IdFuente"		= PT2."IdFuente" AND
					PT1."IdTipoInstrumento"	= PT2."IdTipoInstrumento" AND
					PT1."Nemotecnico"	= PT2."Nemotecnico" AND
					COALESCE(PT1."IdTipoTasa",'')	= COALESCE(PT2."IdTipoTasa",'') AND 
					COALESCE(PT1."IdTipoPrecio",'')	= COALESCE(PT2."IdTipoPrecio",'') AND
					PT1."Plazo"			= PT2."Plazo" AND
					PT1."IdEmpresa"		= PT2."IdEmpresa" AND
					PT1."TipoRegistro"	= PT2."TipoRegistro" AND
					PT1."IdMoneda"	= PT2."IdMoneda" AND
					PT1."IdClasificacionTipoInstrumento" = PT2."IdClasificacionTipoInstrumento" AND
					PT1."Borrado" = PT2."Borrado"
					)
			WHERE PT1."IdPrecioTasa" <> :p_IdPrecioTasa
			  AND TO_VARCHAR(PT1."FechaSolicitud",'YYYY-MM-DD') = :v_FechaSistema
			  AND TO_VARCHAR(PT1."FechaPublicacion",'YYYY-MM-DD') = :v_FechaSistema;
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'PRECIO/TASA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			
			/*p_DetalleLog =
				SELECT 'TASA '||p_IdPrecioTasa AS "SubRegistro", 'Tasa' AS "CampoRegistro", TAS."Tasa" AS "ValorAntiguo", p_Tasa AS "ValorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				WHERE TAS."IdPrecioTasa"= p_IdPrecioTasa AND TAS."IdEmpresa" = p_IdEmpresa AND TAS."Tasa" <> p_Tasa
				UNION ALL
				SELECT 'TASA '||p_IdPrecioTasa AS "SubRegistro", 'TasaEstimada' AS "CampoRegistro", TAS."TasaEstimada" AS "ValorAntiguo", p_TasaEstimada AS "ValorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				WHERE TAS."IdPrecioTasa"= p_IdPrecioTasa AND TAS."IdEmpresa" = p_IdEmpresa AND TAS."TasaEstimada" <> p_TasaEstimada
				UNION ALL
				SELECT 'TASA '||p_IdPrecioTasa AS "SubRegistro", 'Precio' AS "CampoRegistro", TAS."Precio" AS "ValorAntiguo", p_Precio AS "ValorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				WHERE TAS."IdPrecioTasa"= p_IdPrecioTasa AND TAS."IdEmpresa" = p_IdEmpresa AND TAS."Precio" <> p_Precio
				UNION ALL
				SELECT 'TASA '||p_IdPrecioTasa AS "SubRegistro", 'Plazo' AS "CampoRegistro", TAS."Plazo" AS "ValorAntiguo", p_Plazo AS "ValorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				WHERE TAS."IdPrecioTasa"= p_IdPrecioTasa AND TAS."IdEmpresa" = p_IdEmpresa AND TAS."Plazo" <> p_Plazo;*/
			
			UPDATE TAS
			SET
				TAS."Tasa"				= p_Tasa,
				TAS."TasaEstimada"		= p_TasaEstimada,
				TAS."Precio"			= p_Precio,
				TAS."Plazo"				= p_Plazo,
				TAS."IdEstado"			= CASE WHEN TAS."IdEstado" IN ('OFI','PUB') THEN TAS."IdEstado" ELSE 'MOD' END,
				TAS."IdUsuarioModificacion"	= p_IdUsuario,
				TAS."FechaModificacion"		= CURRENT_UTCTIMESTAMP,
				TAS."FechaOficializacion"	= CASE WHEN TAS."IdEstado" = 'OFI' THEN :v_FechaHoraSistemaLOCAL ELSE TAS."FechaOficializacion" END,
				TAS."FechaPublicacion"		= CASE WHEN TAS."IdEstado" = 'PUB' THEN :v_FechaHoraSistemaLOCAL ELSE TAS."FechaPublicacion" END
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
			WHERE TAS."IdPrecioTasa"= p_IdPrecioTasa
			  AND TAS."IdEmpresa"	= p_IdEmpresa
			  AND (TAS."IdEstado" <> 'PUB' OR (TAS."IdEstado" = 'PUB' AND TO_VARCHAR(TAS."FechaSolicitud",'YYYY-MM-DD') = :v_FechaSistema));
			
			
			INSERT INTO "BSCL.Plataforma.BD::TAS.PrecioTasa"(
				 "IdPrecioTasa"
				,"IdFuente"
				,"IdTipoInstrumento"
				,"IdInstrumento"
				,"Nemotecnico"
				,"IdTipoTasa"
				,"Tasa"
				,"TasaEstimada"
				,"IdTipoPrecio"
				,"Precio"
				,"Plazo"
				,"IdEstado"
				,"IdEmpresa"
				,"IdUsuarioCreador"
				,"IdUsuarioModificacion"
				,"FechaSolicitud"
				,"FechaIngreso"
				,"FechaCreacion"
				,"FechaModificacion"
				,"FechaPublicacion"
				,"RutaArchivo"
				,"NombreArchivo"
				,"Borrado"
				,"TipoRegistro"
				,"IdMoneda"
				,"IdClasificacionTipoInstrumento"
				,"FechaOficializacion"
				)
			SELECT
				 "BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa".NEXTVAL
				,"IdFuente"
				,"IdTipoInstrumento"
				,"IdInstrumento"
				,"Nemotecnico"
				,"IdTipoTasa"
				,:p_Tasa
				,:p_TasaEstimada
				,"IdTipoPrecio"
				,:p_Precio
				,:p_Plazo
				,"IdEstado"
				,:p_IdEmpresa
				,:p_IdUsuario
				,:p_IdUsuario
				,:v_FechaSistema || ' ' || TO_NVARCHAR(:v_FechaHoraSistemaLOCAL,'HH24:MI:SS')
				,:v_FechaHoraSistema
				,CURRENT_UTCTIMESTAMP
				,CURRENT_UTCTIMESTAMP
				,:v_FechaHoraSistemaLOCAL 
				,"RutaArchivo"
				,"NombreArchivo"
				,'N'
				,"TipoRegistro"
				,"IdMoneda"
				,"IdClasificacionTipoInstrumento"
				,:v_FechaHoraSistemaLOCAL 
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa"
			WHERE "IdPrecioTasa" = :p_IdPrecioTasa
			  AND TO_VARCHAR("FechaSolicitud",'YYYY-MM-DD') <> :v_FechaSistema
			  AND TO_VARCHAR("FechaPublicacion",'YYYY-MM-DD') <> :v_FechaSistema
			  AND "IdEmpresa" = :p_IdEmpresa
			  AND "IdEstado" = 'PUB'
			  AND "Borrado" = 'N';
			
			
			p_out_RefId := 1;
			
			-- LOG DE AUDITORIA
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => 'MODIFICACION'
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'TASA'
				,p_NombreProceso 	 => 'MODIFICACION TASA'
				,p_DetalleLog 		 => :p_DetalleLog
			);
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
