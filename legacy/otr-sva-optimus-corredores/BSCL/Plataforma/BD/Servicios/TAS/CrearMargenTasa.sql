SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearMargenTasa";

-- 
--  Proposito: Crear Margen Tasa
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 21/11/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearMargenTasa" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_TipoEntidad	NVARCHAR(40),
	IN p_IdMoneda		NVARCHAR(3),
	IN p_MontoDesde		DECIMAL(26,8)	DEFAULT 0,
	IN p_MontoHasta		DECIMAL(26,8)	DEFAULT 0,
	IN p_PlazoDesde		INTEGER			DEFAULT 0,
	IN p_PlazoHasta		INTEGER			DEFAULT 0,
	IN p_Tasa			DECIMAL(26,8)	DEFAULT 0,
	--IN p_IdEstado		NVARCHAR(3),
	
	-- Parametros salida del servicio
    OUT p_out_RefId		BIGINT,
    
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
		DECLARE v_FechaHoraIngreso	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdMargenTasa		BIGINT:=0;
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- Validacion rangos incorrectos
		IF(p_MontoDesde > p_MontoHasta) THEN
			p_out_ErroresDetalle = select 'UI_RANGO_MONTO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF(p_PlazoDesde > p_PlazoHasta) THEN
			p_out_ErroresDetalle = select 'UI_RANGO_PLAZO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- Validacion Margen Tasa existe
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa"
		WHERE "TipoEntidad"	= p_TipoEntidad
		  AND "IdMoneda"	= p_IdMoneda
		  AND "MontoDesde"	= p_MontoDesde
		  AND "MontoHasta"	= p_MontoHasta
		  AND "PlazoDesde"	= p_PlazoDesde
		  AND "PlazoHasta"	= p_PlazoHasta
		  AND "Tasa"		= p_Tasa
		  AND "IdEstado"	= 'ACT'
		  AND "IdEmpresa"	= p_IdEmpresa;
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'UI_MARGEN_TASA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		--validacion entre rangos, no debe haber intersecciones entre ellos, a exepcion que el otro rango sea diferente
		--Valida: doble validación para rango monto y plazo si nuevo existe entre rango antiguo
		
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa"
		WHERE "TipoEntidad" = p_TipoEntidad
			AND "IdMoneda" = p_IdMoneda
			AND "IdEstado" = 'ACT'
			AND "IdEmpresa" = p_IdEmpresa
			AND (("PlazoDesde" BETWEEN p_PlazoDesde AND p_PlazoHasta)
			OR ("PlazoHasta" BETWEEN p_PlazoDesde AND p_PlazoHasta)
			OR (p_PlazoDesde BETWEEN "PlazoDesde" AND "PlazoHasta")
			OR (p_PlazoHasta BETWEEN "PlazoDesde" AND "PlazoHasta"))
			AND (("MontoDesde" BETWEEN p_MontoDesde AND p_MontoHasta)
			OR ("MontoHasta" BETWEEN p_MontoDesde AND p_MontoHasta)
			OR (p_MontoDesde BETWEEN "MontoDesde" AND "MontoHasta")
			OR (p_MontoHasta BETWEEN "MontoDesde" AND "MontoHasta"));
		
		IF v_Count > 0 THEN
		    p_out_ErroresDetalle = SELECT 'UI_RANGO_MONTO_NO_PERMITIDO' AS "Codigo", :v_erroroutmsg AS "Descripcion" FROM dummy;
		    SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			p_out_RefId:=1;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			SELECT "BSCL.Plataforma.BD.Secuencias::TAS.IdMargenTasa".NEXTVAL INTO v_IdMargenTasa FROM DUMMY;
			p_out_RefId := v_IdMargenTasa;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraIngreso);
			
			INSERT INTO "BSCL.Plataforma.BD::TAS.MargenTasa"(
				 "IdMargenTasa"
				,"TipoEntidad"
				,"IdMoneda"
				,"MontoDesde"
				,"MontoHasta"
				,"PlazoDesde"
				,"PlazoHasta"
				,"Tasa"
				,"IdEstado"
				,"IdEmpresa"
				,"FechaIngreso"
				,"FechaCreacion"
				,"FechaModificacion"
				,"IdUsuarioCreador"
				)
			VALUES(
				 v_IdMargenTasa
				,p_TipoEntidad
				,p_IdMoneda
				,p_MontoDesde
				,p_MontoHasta
				,p_PlazoDesde
				,p_PlazoHasta
				,p_Tasa
				,'ACT'
				,p_IdEmpresa
				,v_FechaHoraIngreso
				,CURRENT_UTCTIMESTAMP
				,CURRENT_UTCTIMESTAMP
				,p_IdUsuario
			);
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
