SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearTasa";

-- 
--  Proposito: Crear Tasa
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 05/10/2016  
-- 
  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearTasa" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdFuente			BIGINT			DEFAULT 0,
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(3)	DEFAULT '',
	IN p_IdTipoInstrumento	NVARCHAR(3)		DEFAULT '',
	IN p_IdInstrumento		BIGINT			DEFAULT 0,
	IN p_Nemotecnico		NVARCHAR(40) 	DEFAULT '',
	IN p_IdMoneda			NVARCHAR(3) 	DEFAULT '',
	IN p_TipoRegistro		NVARCHAR(10),
	IN p_IdTipoTasa			NVARCHAR(40)	DEFAULT NULL,
	IN p_Tasa				DECIMAL(26,8)	DEFAULT NULL,
	IN p_TasaEstimada		DECIMAL(26,8)	DEFAULT NULL,
	IN p_IdTipoPrecio		NVARCHAR(40)	DEFAULT NULL,
	IN p_Precio				DECIMAL(26,8)	DEFAULT NULL,
	IN p_Plazo				BIGINT,
	IN p_FechaCreSol		NVARCHAR(10),
	
	-- Parametros salida del servicio
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;										
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
        
		DECLARE v_Count				BIGINT:=0;
		DECLARE v_FechaIngreso		NVARCHAR(10);	-- FORMATO 'YYYY-MM-DD'
		DECLARE v_FechaHoraIngreso	SECONDDATE;		-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_FechaHoraUI		SECONDDATE;		-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdPrecioTasa		BIGINT:=0;
		DECLARE v_IdMoneda   		NVARCHAR(3);
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa,:v_FechaIngreso);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa,:v_FechaHoraIngreso);
		
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
		v_FechaHoraUI := p_FechaCreSol || ' ' || TO_NVARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI:SS');
		
		
		--------------------
		--Validaciones
		--------------------
		
		---Verifica que Precio/Tasa no existe
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
		WHERE "IdFuente"		= :p_IdFuente
		  AND "IdClasificacionTipoInstrumento"	= :p_IdClasificacionTipoInstrumento
		  AND ("IdTipoInstrumento"	= :p_IdTipoInstrumento	OR (:p_IdTipoTasa IN ('CURVA','PUNTO') AND "IdTipoInstrumento" = '' AND :p_IdTipoInstrumento = ''))
		  --AND ("IdInstrumento"		= :p_IdInstrumento		OR (:p_IdTipoTasa IN ('CURVA','PUNTO') AND "IdInstrumento" = 0 AND :p_IdInstrumento = 0))
		  AND ("Nemotecnico"		= :p_Nemotecnico		OR (:p_IdTipoTasa IN ('CURVA','PUNTO') AND "Nemotecnico" = '' AND :p_Nemotecnico = ''))
		  AND (((:p_TipoRegistro = 'TASA' OR :p_TipoRegistro ='PUNTO') AND "IdTipoTasa" = :p_IdTipoTasa) OR (:p_TipoRegistro = 'PRECIO' AND "IdTipoPrecio" = :p_IdTipoPrecio))
		  AND "IdMoneda"			= :p_IdMoneda
		  AND "Plazo"				= :p_Plazo
		  AND TO_NVARCHAR("FechaSolicitud",'YYYY-MM-DD') = :p_FechaCreSol
		  AND "IdEmpresa"			= :p_IdEmpresa
		  AND "Borrado" = 'N';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle =  select 'PRECIO/TASA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		------------------------------------------
		--Crear 
		------------------------------------------
		/*
		IF :p_IdInstrumento <> 0 THEN
			SELECT DISTINCT("IdMoneda") INTO v_IdMoneda
			FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
			WHERE "IdInstrumento" = :p_IdInstrumento AND "IdEstado" = 'ACT';
		END IF;
		*/
		
		SELECT "BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa".NEXTVAL INTO v_IdPrecioTasa FROM DUMMY;
		p_out_RefId := v_IdPrecioTasa;
		
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
			,"FechaSolicitud"
			,"FechaIngreso"
			,"FechaCreacion"
			,"FechaModificacion"
			,"TipoRegistro"
			,"IdMoneda"
			,"IdClasificacionTipoInstrumento"
			)
		VALUES(
			 :v_IdPrecioTasa
			,:p_IdFuente
			,:p_IdTipoInstrumento
			,:p_IdInstrumento
			,:p_Nemotecnico
			,CASE WHEN :p_TipoRegistro = 'TASA' OR :p_TipoRegistro = 'PUNTO' THEN :p_IdTipoTasa ELSE NULL END
			,:p_Tasa
			,:p_TasaEstimada
			,CASE WHEN :p_TipoRegistro = 'PRECIO' THEN :p_IdTipoPrecio ELSE NULL END
			,:p_Precio
			,:p_Plazo
			,'CRS'
			,:p_IdEmpresa
			,:p_IdUsuario
			,:v_FechaHoraUI
			--,:v_FechaHoraIngreso
			--,:v_FechaHoraSistemaLOCAL
			,:v_FechaHoraIngreso
			,CURRENT_UTCTIMESTAMP
			,CURRENT_UTCTIMESTAMP
			,:p_TipoRegistro
			--,CASE WHEN COALESCE(:v_IdMoneda,'') = '' THEN :p_IdMoneda ELSE :v_IdMoneda END
			,:p_IdMoneda
			,:p_IdClasificacionTipoInstrumento
		);
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
