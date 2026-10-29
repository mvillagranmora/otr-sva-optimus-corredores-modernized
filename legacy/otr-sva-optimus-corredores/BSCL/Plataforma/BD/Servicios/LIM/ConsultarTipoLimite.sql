SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::LIM.ConsultarTipoLimite";

CREATE PROCEDURE
-- 
--  Proposito: Consultar el tipo de limite, para se expuesto en la pantalla de mantenedores de limites
--  Autor: Oscar Carreno Tolmo (OCT)
--  Fecha Creacion: 26/10/2015 
--    
"BSCL.Plataforma.BD.Servicios::LIM.ConsultarTipoLimite" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),
    IN p_UsaReglasNegocio	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    IN p_IdTipoLimite		NVARCHAR(40),
    IN p_EsCuentaPropia		NVARCHAR(1),
    IN p_IdGrupoLimite      NVARCHAR(40),
    
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_Tipo_Limite	TABLE ( "IdTipoLimite" NVARCHAR(40),"Nombre" NVARCHAR(100),"EsCuentaPropia" NVARCHAR(1),
									"IdComportamiento" NVARCHAR(3), "DescripcionComportamiento" NVARCHAR(520),
									"IdGrupoLimite" NVARCHAR(40),
									"Categoria" NVARCHAR(40), "IdEstado" NVARCHAR(3),
									"DescripcionEstadoTipoLimite" NVARCHAR(520), "IdMonedaLimite" NVARCHAR(3),
									"MonedaLimite" NVARCHAR(520), "CantidadDecimales" INTEGER),

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
	
	
	p_out_TipoLimite            = SELECT '' AS "IdTipoLimite", '' AS "Nombre",'' AS "EsCuentaPropia", 
						                 '' AS "IdComportamiento",'' AS "DescripcionComportamiento",
						                 '' AS "IdGrupoLimite",'' AS "Categoria", '' AS "IdEstado",
						                 '' AS "DescripcionEstadoTipoLimite",'' AS "MonedaLimite",
						                 0 AS "CantidadDecimales"
								  FROM dummy where 1=2;
   v_ErrorNoData             := '';

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
	
        DECLARE v_IdTipoLimite		NVARCHAR(40) := NULL;
        DECLARE v_EsCuentaPropia	NVARCHAR(1) := NULL;
        DECLARE v_IdGrupoLimite     NVARCHAR(40):= NULL;
		DECLARE v_NombreLargo	    NVARCHAR(520) := NULL;
		DECLARE v_Decimales			INTEGER := 0;
		DECLARE v_IdMoneda          NVARCHAR(3) := NULL;
		
		BEGIN
				DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
				BEGIN
						v_ErrorNoData := 'MONEDA_NO_EXISTE';
						SIGNAL SALIDA_CONTROLADA_NO_DATA;
				END;
				SELECT (  ( "m"."IdMoneda" || ' - ' )  || "m"."Nombre" )  AS "NombreLargo", "m"."Decimales","m"."IdMoneda"
				INTO v_NombreLargo, v_Decimales, v_IdMoneda
		        FROM "BSCL.Plataforma.BD::PLA.Configuracion" AS "p"
		        LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "m" ON ("m"."IdMoneda" = "p"."Valor")
		        WHERE "p"."CodigoParametro" = 'PLA.MonedaSistema';
        END;
        
        
        IF (:p_IdTipoLimite != '') THEN 
			v_IdTipoLimite := p_IdTipoLimite;
		END IF;
	
		IF (:p_EsCuentaPropia != '') THEN 
			v_EsCuentaPropia := p_EsCuentaPropia;
		END IF;
        
		IF (:p_IdGrupoLimite != '') THEN 
			v_IdGrupoLimite := p_IdGrupoLimite;
		END IF;
        
        
        p_out_Tipo_Limite =
    	SELECT   "tl"."IdTipoLimite"
    			,"tl"."Nombre"
    			,"tl"."EsCuentaPropia"
    			,"tl"."IdComportamiento"
    			,"eco"."Descripcion" AS "DescripcionComportamiento"
				,"tl"."IdGrupoLimite"
				,"tl"."Categoria"
				,"tl"."IdEstado"
				,"etl"."Descripcion" AS "DescripcionEstadoTipoLimite"
				,:v_IdMoneda AS "IdMonedaLimite"
				,:v_NombreLargo AS "MonedaLimite"
				,:v_Decimales AS "CantidadDecimales"
    	FROM "BSCL.Plataforma.BD::LIM.TipoLimite" AS "tl"
    	LEFT JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" AS "etl" ON ("etl"."IdEstado" = "tl"."IdEstado")
    	LEFT JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" AS "eco" ON ("eco"."IdEstado" = "tl"."IdComportamiento")
    	WHERE "etl"."NombreEntidad" = 'Limites'
          AND "eco"."NombreEntidad" = 'Limites.Comportamiento'
          AND (IFNULL(:v_IdTipoLimite,'0')='0' OR ("tl"."IdTipoLimite" = :v_IdTipoLimite))
		  AND (IFNULL(:v_EsCuentaPropia,'0')='0' OR ("tl"."EsCuentaPropia" = :v_EsCuentaPropia))
		  AND (IFNULL(:v_IdGrupoLimite,'0')='0' OR ("tl"."IdGrupoLimite" = :v_IdGrupoLimite))
		  AND "tl"."IdEmpresa" = :p_IdEmpresa;
	END;
END;
