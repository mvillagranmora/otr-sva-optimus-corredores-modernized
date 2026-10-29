SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::LIM.ConsultarLineaRiesgo";

CREATE PROCEDURE
-- 
--  Proposito: Consultar linea de riesgo para un Agente, un Cliente y la Corredora enviados como parametros de entrada.
--  Autor: Oscar Carreno Tolmo (OCT)
--  Fecha Creacion: 27/10/2015 
--    
"BSCL.Plataforma.BD.Servicios::LIM.ConsultarLineaRiesgo" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),
    IN p_UsaReglasNegocio	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    IN p_CodigoAgente		NVARCHAR(40),
    IN p_IdTipoOperacion	NVARCHAR(6),
    IN p_IdPortafolio		BIGINT,

	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_linea_riesgo	TABLE ( "IdTipoLinea" NVARCHAR(40),"MontoDisponible" DECIMAL(26,8),
									"MontoUtilizado" DECIMAL(26,8)),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	 
	DECLARE v_ErrorNoData  NVARCHAR(100);
	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  select :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
								select * from :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
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
			--Error interno no manejado  (El log ser� captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	
	p_out_linea_riesgo          = SELECT '' AS "IdTipoLinea",
						                 0 AS "MontoDisponible",
						                 0 AS "MontoUtilizado"
								  FROM dummy where 1=2;
    v_ErrorNoData             := '';


	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
	
        DECLARE v_CodigoAgente		NVARCHAR(40) := NULL;
        DECLARE v_IdTipoOperacion	NVARCHAR(6) := NULL;
        DECLARE v_IdPortafolio      BIGINT:= NULL;
	
		BEGIN
				DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
				BEGIN
						v_ErrorNoData := 'MONEDA_NO_EXISTE';
						SIGNAL SALIDA_CONTROLADA_NO_DATA;
				END;
				--busqueda de informacion
        END;
        
        
        IF (:p_CodigoAgente != '') THEN 
			v_CodigoAgente := p_CodigoAgente;
		END IF;
	
		IF (:p_IdTipoOperacion != '') THEN 
			v_IdTipoOperacion := p_IdTipoOperacion;
		END IF;
        
		IF (:p_IdPortafolio != -1) THEN 
			v_IdPortafolio := p_IdPortafolio;
		END IF;
        
        
        p_out_linea_riesgo =
	    	SELECT 'AGENTE'    AS "IdTipoLinea",FLOOR(100000*RAND()) AS "MontoDisponible",FLOOR(100000*RAND()) AS "MontoUtilizado" FROM DUMMY UNION ALL
	    	SELECT 'CLIENTE'   AS "IdTipoLinea",FLOOR(100000*RAND()) AS "MontoDisponible",FLOOR(100000*RAND()) AS "MontoUtilizado" FROM DUMMY UNION ALL
	    	SELECT 'CORREDORA' AS "IdTipoLinea",FLOOR(100000*RAND()) AS "MontoDisponible",FLOOR(100000*RAND()) AS "MontoUtilizado" FROM DUMMY;
    	
	END;
END;
