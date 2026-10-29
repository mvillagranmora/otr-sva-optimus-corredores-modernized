SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::GTA.ConsultarCoberturaGarantiaResumen";
GO
CREATE PROCEDURE
-- 
--  Proposito: Resumen de garantias por cliente
--  Autor: JVILLAGRAN - BCS 
--  Fecha Creacion: 07/12/2016
--    
"BSCL.Custodia.BD.Servicios::GTA.ConsultarCoberturaGarantiaResumen" (
	 IN  p_IdEmpresa					 BIGINT
	,IN  p_IdUsuario					 BIGINT
	,IN  p_IdModulo						 NVARCHAR(3)

	-- Parametros entrada
	,IN p_IdCliente						 	BIGINT      	DEFAULT 0
	,IN p_IdPortafolio					 	BIGINT	     	DEFAULT 0
	,IN p_IdTipoInstrumentoGarantizado 		NVARCHAR(3) 	DEFAULT '' 	-- Ej.: SIM
	,IN p_IdInstrumentoCarteraGarantizada   BIGINT 	 		DEFAULT 0   -- Rj.: 2249 (SIMULTANEA VENTA)
	,IN p_IdContraparteGarantia		 	    BIGINT 	        DEFAULT 0   -- Contraparte Entidad Destino
	

	-- Parametros salida
	,OUT p_out_GarantiasResumen				TABLE (
												"IdCliente"    						BIGINT,
												"IdPortafolio"						BIGINT,	  								
												"PropositoPortafolio"               NVARCHAR(520),
												"NombreCliente"       				NVARCHAR(302),
												"DocumentoIdentidad"  				NVARCHAR(40),
												"IdTipoInstrumentoGarantizado"    	NVARCHAR(40),  	-- Ej. SIM
			                                    "IdInstrumentoCarteraGarantizada" 	BIGINT,        	-- SIMVEN -- ver/filtar
			             						"NemoGarantizado"          			NVARCHAR(100),  -- SIMVEN
												"IdContraparteGarantia"             BIGINT,         -- Entidad Destino (Bolsa de Comercio de Santiago) 
												"RazonSocialContraparte"   			NVARCHAR(302), 	-- Entidad Destino (Bolsa de Comercio de Santiago)												 
												"EsEspecifica"             			NVARCHAR(1),	-- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)
												"IdOperacionOrigenGarantia"			BIGINT,			-- Folio de la operacion(orden) que originó la garantía (solo para específicas)
												"MontoCoberturaEntidad"             DECIMAL(26,8),
												"MontoCoberturaCorredora"           DECIMAL(26,8),												
												"MontoExigibilidadEntidad"   		DECIMAL(26,8),
												"MontoExigibilidadCorredora" 		DECIMAL(26,8),												
												"EstadoCoberturaEntidad"            NVARCHAR(20),
												"EstadoCoberturaCorredora"			NVARCHAR(20),
												"IdMonedaLiquidacionOperacion"			NVARCHAR(20)
									  		)	default empty
	

	-- Salidas para mensajes de advertencias y errores
	,OUT p_out_AdvertenciasContador 		INT
	,OUT p_out_AdvertenciasDetalle 			TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))	default empty
	,OUT p_out_ErroresContador 				INT
	,OUT p_out_ErroresDetalle 				TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))	default empty
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

					
	p_out_GarantiasResumen		= select 0 as "IdCliente", 0 as "IdPortafolio", '' as "PropositoPortafolio",
										'' as "NombreCliente", '' as "DocumentoIdentidad",
										 '' as "IdTipoInstrumentoGarantizado", 0 as "IdInstrumentoCarteraGarantizada",
	             						 '' as "NemoGarantizado", 0 as "IdContraparteGarantia",'' as "RazonSocialContraparte", '' as "EsEspecifica",	             						 
	             						 0 as "IdOperacionOrigenGarantia", 0 as "MontoCoberturaEntidad", 0 as "MontoCoberturaCorredora",
	             						 0 as "MontoExigibilidadEntidad", 0 as "MontoExigibilidadCorredora",
										 '' as "EstadoCoberturaEntidad", '' as "EstadoCoberturaCorredora",
										 '' as "IdMonedaLiquidacionOperacion"
							  	  from dummy where 1=2;
					  					 
	
	BEGIN	


		--------------------------------------------
		-- VALIDACIONES BASICAS
		-------------------------------------------- 
		-- Validación básica de Empresa, Usuario, Módulo
		BEGIN		
			DECLARE v_resultadoRegla nvarchar(40);
	
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_Errores_ReqBasicos,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_Errores_ReqBasicos;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		--------------------
		-- Negocio Aqui
		--------------------

	v_OperacYCoberturas = 
		SELECT 
			 GAR."NombreCliente"
			,GAR."DocumentoIdentidad"
			,GAR."IdCliente"
			,GAR."IdPortafolio"
			,GAR."PropositoPortafolio"
			,GAR."FolioCarteraGarantizada"
			,GAR."IdTipoInstrumentoGarantizado"                 -- Ej.: SIM
			,GAR."IdInstrumentoCarteraGarantizada"              -- Ej.: 999 (SIMVEN)
			,GAR."NemoGarantizado" 								-- Ej.: SIMULTANE VENTA
			,GAR."IdContraparteGarantia"                        -- Entidad Destino 
			,GAR."RazonSocialContraparte"  						-- Entidad Destino
			,GAR."EsEspecifica"                                 -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)			
			,COALESCE(GAR."IdOperacionOrigenGarantia",0) AS "IdOperacionOrigenGarantia"	    -- Folio de la operacion(orden) que originó la garantía (solo para específicas)
			,GAR."MontoCoberturaEntidad" 
			,GAR."MontoCoberturaCorredora"
			,GAR."IdBolsaOperacionOrigen" as "IdBolsa"
			
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle" AS GAR
		WHERE GAR."IdCliente"	 					= (CASE WHEN COALESCE(:p_IdCliente,0) <> 0 THEN :p_IdCliente ELSE GAR."IdCliente" END)
		AND	  GAR."IdPortafolio" 					= (CASE WHEN COALESCE(:p_IdPortafolio,0) <> 0 THEN :p_IdPortafolio ELSE GAR."IdPortafolio" END)		
		AND	  GAR."IdTipoInstrumentoGarantizado"    = (CASE WHEN COALESCE(:p_IdTipoInstrumentoGarantizado,'') <> '' THEN  :p_IdTipoInstrumentoGarantizado ELSE GAR."IdTipoInstrumentoGarantizado" END)
		AND	  GAR."IdInstrumentoCarteraGarantizada" = (CASE WHEN COALESCE(:p_IdInstrumentoCarteraGarantizada,0) <> 0 THEN :p_IdInstrumentoCarteraGarantizada ELSE GAR."IdInstrumentoCarteraGarantizada" END)
		AND	  GAR."IdContraparteGarantia"           = (CASE WHEN COALESCE(:p_IdContraparteGarantia,0) <> 0 THEN  :p_IdContraparteGarantia ELSE GAR."IdContraparteGarantia" END)
		AND   GAR."IdEstado"						= 'VIG';

--		SELECT 'v_OperacYCoberturas', * FROM  :v_OperacYCoberturas; -- PARA REVISAR

		
		
		
		v_CoberturaGarantiaDetalle = 
		SELECT 
			 MAX(GAR."NombreCliente") 																AS "NombreCliente"
			,MAX(GAR."DocumentoIdentidad") 															AS "DocumentoIdentidad"
			,GAR."IdCliente"
			,GAR."IdPortafolio"
			,GAR."PropositoPortafolio"
			,GAR."IdTipoInstrumentoGarantizado"                 -- Ej.: SIM
			,GAR."IdInstrumentoCarteraGarantizada"              -- Ej.: 999 (SIMVEN)
			,MAX(GAR."NemoGarantizado") 															AS "NemoGarantizado"	-- Ej.: SIMULTANE VENTA
			,GAR."IdContraparteGarantia"                        -- Entidad Destino 
			,GAR."FolioCarteraGarantizada"
			,MAX(GAR."RazonSocialContraparte") 														AS "RazonSocialContraparte" -- Entidad Destino
			,GAR."EsEspecifica"         									 -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)
			,GAR."IdOperacionOrigenGarantia"														AS "IdOperacionOrigenGarantia"
			,cast(SUM(GAR."MontoCoberturaEntidad") as DECIMAL(26,8))   								AS "MontoCoberturaEntidad"
			,cast(SUM(GAR."MontoCoberturaCorredora") as DECIMAL(26,8)) 								AS "MontoCoberturaCorredora"		 
			,MAX(GAR."IdBolsa") 																	as "IdBolsa"
			
		FROM :v_OperacYCoberturas AS GAR
		GROUP BY
			GAR."IdCliente",
			GAR."IdPortafolio",
			GAR."PropositoPortafolio",
			GAR."IdTipoInstrumentoGarantizado",
			GAR."IdInstrumentoCarteraGarantizada",
			GAR."IdContraparteGarantia",
			GAR."EsEspecifica",
			GAR."IdOperacionOrigenGarantia",
			GAR."FolioCarteraGarantizada";			
		
		--SELECT 'v_CoberturaGarantiaDetalle', * FROM  :v_CoberturaGarantiaDetalle; -- PARA REVISAR	

		
		---------------------------
		-- SALIDA
		---------------------------
		
		p_out_GarantiasResumen =
		SELECT
			CGD."IdCliente",
			CGD."IdPortafolio",
			CGD."PropositoPortafolio",
			CGD."NombreCliente",
			CGD."DocumentoIdentidad",
			CGD."IdTipoInstrumentoGarantizado",             -- Ej.: SIM
			CGD."IdInstrumentoCarteraGarantizada",          -- Ej.: 999 (SIMVEN)
			COALESCE(CGD."NemoGarantizado",'') as "NemoGarantizado",   					    -- Ej.: SIMULTANE VENTA
			CGD."IdContraparteGarantia",					-- Entidad Destino (Ej.: Bolsa Comercio de Santiago)
			CGD."RazonSocialContraparte",                   -- Entidad Destino (Ej.: Bolsa Comercio de Santiago)
			CGD."EsEspecifica",                             -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)
			CGD."IdOperacionOrigenGarantia",                -- Id de la operación asociada a la garantía(cuen es especifica)
			cast(CGD."MontoCoberturaEntidad" as DECIMAL(26,8)) as "MontoCoberturaEntidad",
			cast(CGD."MontoCoberturaCorredora" as DECIMAL(26,8)) as "MontoCoberturaCorredora",
			cast(EXI."MontoExigibilidadEntidad" as DECIMAL(26,8)) as "MontoExigibilidadEntidad",
			cast(EXI."MontoExigibilidadCorredora" as DECIMAL(26,8)) as "MontoExigibilidadCorredora",
			CASE WHEN (IFNULL(CGD."MontoCoberturaEntidad",0) - EXI."MontoExigibilidadEntidad") < 0 THEN 'DESCUBIERTO'
				ELSE 'CUBIERTO'
			END AS "EstadoCoberturaEntidad",			 
			CASE WHEN (IFNULL(CGD."MontoCoberturaCorredora",0) - EXI."MontoExigibilidadCorredora") < 0 THEN 'DESCUBIERTO'
				ELSE 'CUBIERTO'
			END AS "EstadoCoberturaCorredora",
			--Esto aplica solo para reportes, es posible que se requiera agregar nuevas consultas para otras operaciones si requiere.
			(Select "IdMonedaLiquidacionOrden" FROM "BSCL.Orden.BD::ORD.Orden" AS ORD 
			Where ORD."IdOrden" = CGD."IdOperacionOrigenGarantia") as "IdMonedaLiquidacionOperacion"
			
		FROM :v_CoberturaGarantiaDetalle AS CGD
		LEFT JOIN  -- Suma de exigibilidad de las operaciones
				(SELECT G."IdCliente"	
						,G."IdPortafolio"
						,G."IdTipoInstrumentoGarantizado"   -- SIM
						,G."IdInstrumentoGarantizado"       -- 2249 (VENSIM)  
						,G."IdContraparteGarantia"          -- Entidad Destino	
						,G."IdOperacionOrigenGarantia"
						,SUM(G."ExigibilidadEntidad") AS "MontoExigibilidadEntidad"
						,SUM(G."ExigibilidadCorredora") AS "MontoExigibilidadCorredora"
					FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones" G
					WHERE   G."IdCliente" 					= (CASE WHEN COALESCE(p_IdCliente,0) <> 0 THEN p_IdCliente ELSE G."IdCliente" END)
						AND G."IdPortafolio" 					= (CASE WHEN COALESCE(p_IdPortafolio,0) <> 0 THEN p_IdPortafolio ELSE G."IdPortafolio" END)
						AND G."IdTipoInstrumentoGarantizado"	= (CASE WHEN COALESCE(p_IdTipoInstrumentoGarantizado,'') <> '' THEN p_IdTipoInstrumentoGarantizado ELSE G."IdTipoInstrumentoGarantizado" END)
						AND G."IdInstrumentoGarantizado" 		= (CASE WHEN COALESCE(p_IdInstrumentoCarteraGarantizada,0) <> 0 THEN p_IdInstrumentoCarteraGarantizada ELSE G."IdInstrumentoGarantizado" END)
						AND G."IdContraparteGarantia" 		= (CASE WHEN COALESCE(p_IdContraparteGarantia,0) <> 0 THEN p_IdContraparteGarantia ELSE G."IdContraparteGarantia" END)
					GROUP BY G."IdCliente", G."IdPortafolio", G."IdTipoInstrumentoGarantizado",
						     G."IdInstrumentoGarantizado", G."IdContraparteGarantia",
							 G."IdOperacionOrigenGarantia"
		    	) AS EXI ON  (      CGD."IdCliente" 						= EXI."IdCliente"
								AND CGD."IdPortafolio" 						= EXI."IdPortafolio"
								AND CGD."IdTipoInstrumentoGarantizado" 		= EXI."IdTipoInstrumentoGarantizado"
								AND CGD."IdInstrumentoCarteraGarantizada" 	= EXI."IdInstrumentoGarantizado"
								AND CGD."IdContraparteGarantia" 			= EXI."IdContraparteGarantia"
								AND CGD."IdOperacionOrigenGarantia" 		= EXI."IdOperacionOrigenGarantia"
				    		 );
				    		 
--		SELECT 'p_out_GarantiasResumen', * FROM  :p_out_GarantiasResumen; -- PARA REVISAR


	END;	
END;
