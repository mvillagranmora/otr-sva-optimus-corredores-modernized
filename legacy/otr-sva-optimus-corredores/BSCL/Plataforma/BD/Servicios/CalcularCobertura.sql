SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CalcularCobertura";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Calcula la cobertura de los saldos de garantias por portafolio y por simulación
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 27/10/2015
--  
"BSCL.Plataforma.BD.Servicios::CalcularCobertura"(
    IN p_IdEmpresa            			BIGINT,
    IN p_IdUsuario            			BIGINT,
    IN p_IdModulo             			NVARCHAR(3),
	IN p_UsaReglasNegocio	  			NVARCHAR(1),	
	IN p_IdPortafolio 					BIGINT,
	IN p_IdEntidadDestinoGarantia 		BIGINT,

	IN p_IdTipoInstrumentoExigeGarantia NVARCHAR(3), -- SIM  (SIMULTANEA)
    IN p_IdInstrumentoExigeGarantia     BIGINT,      -- 2249 (SIMULTANEA VENTA)

	IN p_Simulacion   					TABLE( 
												"IdReferencial"     BIGINT, 
											    "IdInstrumento" 	BIGINT, 
											    "Cantidad" 			DECIMAL(26,8),
											    "PrecioMercado" 	DECIMAL(26,8),
											    "Monto" 			DECIMAL(26,8),
											    "MontoCoberturaSimular"  	DECIMAL(26,8)
											 ),
											 
    OUT p_out_TableCoberturaSimulacion 	TABLE( 
    											"IdReferencial"     			BIGINT,
    										    "IdInstrumento" 				BIGINT,
    										    "Cantidad"  					DECIMAL(26,8),
    										    "PrecioMercado" 				DECIMAL(26,8),
    										    "Monto" 						DECIMAL(26,8), 
    										    "PorcentajeCoberturaEntidad" 	DECIMAL(26,8),
    										    "MontoCoberturaEntidad" 		DECIMAL(26,8),
    										    "PorcentajeCoberturaCorredora" 	DECIMAL(26,8),
    										    "MontoCoberturaCorredora" 		DECIMAL(26,8)
    										    
    										 ),
    										   
	OUT p_out_TableCoberturaPortafolio 	TABLE("MontoCoberturaEntidad" DECIMAL(26,8), "MontoCoberturaCorredora" DECIMAL(26,8)),
        
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle  TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador      INT,
	OUT p_out_ErroresDetalle       TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_erroresReqBasicos TABLE("Codigo" NVARCHAR(100),"Descripcion" nvarchar(520),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	
	-----------------------------------------------------
	--Variable GLOBAL
	-----------------------------------------------------
	
	
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
			--Error interno no manejado  (El log ser captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
    	DECLARE v_Obligatorios	 				NVARCHAR(250);
		DECLARE v_resultadoRegla 				NVARCHAR(40);    
		DECLARE v_out_Resultado  				CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 				NVARCHAR(520); --MENSAJE SALIDA
	    DECLARE v_out_CodigoErrorSQL 			BIGINT;	    
        DECLARE v_Existe						BIGINT:=0;

		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_erroresReqBasicos,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_erroresReqBasicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			IF ((:p_IdPortafolio = 0) or (:p_IdEntidadDestinoGarantia = 0) or (:p_IdTipoInstrumentoExigeGarantia = '') or (:p_IdInstrumentoExigeGarantia = 0 and :p_IdTipoInstrumentoExigeGarantia != 'DER' )) THEN
				v_Obligatorios := 'IdPortafolio-IdEntidadDestinoGarantia-IdTipoInstrumentoExigeGarantia-IdInstrumentoExigeGarantia';

				v_hash = select 'CodigoParametros' as "Codigo", v_Obligatorios as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PARAMETROS_OBLIGATORIOS',:v_hash,:v_erroroutmsg);
				
				p_out_ErroresDetalle =  select 'PARAMETROS_OBLIGATORIOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy
										UNION ALL select * from :p_out_ErroresDetalle; 
										
				SIGNAL SALIDA_CONTROLADA; 
			END IF;		
		
		END;
	END;
	 
	--------------------
	--Negocio Aqui 
	--------------------	
	BEGIN

	    DECLARE v_porcentajeCoberturaEfectivoEspecies  BIGINT;  
	    DECLARE v_porcentajeCoberturaSinDefinir        BIGINT;  
        

		SELECT
			 COALESCE(MAX(CASE WHEN ("CodigoParametro" = 'PLA.CoberturaEfectivoEspecies') THEN CAST("Valor" AS BIGINT) END),0) AS "CoberturaEfectivoEspecies",
			 COALESCE(MAX(CASE WHEN ("CodigoParametro" = 'PLA.CoberturaSinDefinir') 	  THEN CAST("Valor" AS BIGINT) END),0) AS "CoberturaEfectivoEspecies"
		INTO v_porcentajeCoberturaEfectivoEspecies, v_porcentajeCoberturaSinDefinir
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE ("CodigoParametro" = 'PLA.CoberturaEfectivoEspecies' OR "CodigoParametro" = 'PLA.CoberturaSinDefinir')
		AND "IdEmpresa" = :p_IdEmpresa;
		
		--------------------------------------------
		-- SIMULACION DE COBERTURA
		--------------------------------------------
		
		vTablaRankingClasificacion = 
		SELECT * FROM (
			SELECT  i."IdInstrumento",
					RANK() over (PARTITION BY i."IdInstrumento" ORDER BY
					CASE WHEN TICR."EntidadClasificadoraRiesgoPrincipal" = 'S' THEN 1 ELSE 2 END, CR."Nivel" ASC)	AS RANK,
					COALESCE(CR."ClasificacionRiesgo", NULL) 								AS "ValorClasificacionRiesgo",
					COALESCE(CR."IdTipoClasificacionRiesgo", NULL) 							AS "IdTipoClasificacionRiesgo",
					COALESCE(CR."IdEntidadClasificadoraRiesgo", NULL) 						AS "IdEntidadClasificadoraRiesgo",
					TICR."EntidadClasificadoraRiesgoPrincipal"
			FROM :p_Simulacion	 AS SIM
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS i ON SIM."IdInstrumento" = i."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = i."IdInstrumento"		
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR 
			ON (TICR."IdTipoInstrumento" = i."IdTipoInstrumento" 
			AND TICR."IdEntidadClasificadoraRiesgo" = ICR."IdEntidadClasificadoraRiesgo")		
			LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS CR ON CR."IdTipoClasificacionRiesgo" = TICR."IdTipoClasificacionRiesgo" 
															AND CR."ClasificacionRiesgo" = ICR."ValorClasificacionRiesgo" 
															AND CR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"							
			GROUP BY i."IdInstrumento", CR."Nivel", CR."ClasificacionRiesgo", 
			CR."IdTipoClasificacionRiesgo", CR."IdEntidadClasificadoraRiesgo", TICR."EntidadClasificadoraRiesgoPrincipal"
		) AS q WHERE q.RANK = 1;

		p_out_TableCoberturaSimulacion = 
		SELECT 
			SIM."IdReferencial",  
            SIM."IdInstrumento",
			SIM."Cantidad",
			SIM."PrecioMercado",
			CASE WHEN COALESCE(SIM."Monto",0) = 0 AND COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir) <>0 THEN ((COALESCE(SIM."MontoCoberturaSimular",0)) / (COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100)) 
			ELSE SIM."Monto" END AS "Monto",	
			COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir) 								AS "PorcentajeCoberturaEntidad",
			CASE WHEN COALESCE(SIM."Monto",0) = 0 AND COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir) <>0 THEN 
			(COALESCE(SIM."MontoCoberturaSimular",0)/(COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100) * (COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir)/100))
			ELSE (COALESCE(SIM."Monto", 0) * (COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir)/100)) END AS "MontoCoberturaEntidad",	 						
			COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir) 							AS "PorcentajeCoberturaCorredora",
			CASE WHEN COALESCE(SIM."Monto",0) = 0 AND COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir) <>0 THEN 
			(COALESCE(SIM."MontoCoberturaSimular",0)/(COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100) * (COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100))
			ELSE (COALESCE(SIM."Monto", 0) * (COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100)) END AS "MontoCoberturaCorredora"
		FROM :p_Simulacion AS SIM
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON INS."IdInstrumento" = SIM."IdInstrumento"
		LEFT JOIN :vTablaRankingClasificacion AS CLAINS ON CLAINS."IdInstrumento" = SIM."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::GTA.CoberturaGarantia" AS COB
			ON  COB."IdTipoInstrumentoComoGarantia"  = INS."IdTipoInstrumento"
			AND (COB."ClasificacionRiesgo" 			 = CLAINS."ValorClasificacionRiesgo" or CLAINS."ValorClasificacionRiesgo" IS NULL)
			AND (COB."IdEntidadClasificadoraRiesgo" 	 = CLAINS."IdEntidadClasificadoraRiesgo" or CLAINS."IdEntidadClasificadoraRiesgo" IS NULL)
			AND (COB."IdTipoClasificacionRiesgo" 	 = CLAINS."IdTipoClasificacionRiesgo" or CLAINS."IdTipoClasificacionRiesgo" IS NULL)
			AND COB."IdEntidadDestinoGarantia" 		 = :p_IdEntidadDestinoGarantia
			AND COB."IdTipoInstrumentoExigeGarantia" = :p_IdTipoInstrumentoExigeGarantia 
			AND (COB."IdInstrumentoExigeGarantia" 	 = :p_IdInstrumentoExigeGarantia or :p_IdInstrumentoExigeGarantia = 0)    	
			AND COB."IdEstado" = 'ACT'; 

		--------------------------------------------
		-- COBERTURA POR PORTAFOLIO
		--------------------------------------------
		IF (:p_IdTipoInstrumentoExigeGarantia = 'SIM') THEN 
			p_out_TableCoberturaPortafolio = 		
			  SELECT
					  COALESCE(SUM
					  (
						  (CASE (GAR."IdOrigenGarantia")
						      WHEN 'ESP' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CTA' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CUS' THEN  
									(GAR."INGMontoATasaMercadoActual") * (COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir)/100)
					      END)
					  ),0) As "MontoCoberturaEntidad",	
					  
					  COALESCE(SUM
					  (
						  (CASE (GAR."IdOrigenGarantia")
						      WHEN 'ESP' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CTA' THEN 
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CUS' THEN  
									(GAR."INGMontoATasaMercadoActual") * (COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100)
					      END)
					  ),0) As "MontoCoberturaCorredora"
					  
			  FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" AS GAR
			  INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
				ON INS."IdInstrumento" = GAR."IdInstrumento"
			  LEFT JOIN :vTablaRankingClasificacion AS CLAINS
				ON CLAINS."IdInstrumento" = GAR."IdInstrumento"
			  LEFT JOIN "BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia" AS COB ON (
						    GAR."IdContraparteGarantia" 		  = COB."IdContraparteDestinoGarantia"
						AND (GAR."IdTipoInstrumentoGarantizado"   = COB."IdTipoInstrumentoExigeGarantia")
						AND (GAR."IdInstrumentoCarteraGarantizada" = COB."IdInstrumentoExigeGarantia")
						AND GAR."IdTipoInstrumentoAval" 		  = COB."IdTipoInstrumentoComoGarantia"
					    AND GAR."IdEntidadClasificadoraRiesgo" 	  = COB."IdEntidadClasificadoraRiesgo"
						AND CLAINS."ValorClasificacionRiesgo" 	  = COB."NombreClasificacionRiesgo"
						AND GAR."IdTipoClasificacionRiesgo" 	  = COB."IdTipoClasificacionRiesgo"									
						)
			  WHERE GAR."IdPortafolio" 						= :p_IdPortafolio
			  AND   (GAR."IdTipoInstrumentoGarantizado" 		= :p_IdTipoInstrumentoExigeGarantia)
			  AND   (GAR."IdInstrumentoCarteraGarantizada" 	= :p_IdInstrumentoExigeGarantia)
			  AND   GAR."IdConcepto" != 'GARDEV'
			  AND   COB."IdEstado" = 'ACT';		
		ELSE 
			p_out_TableCoberturaPortafolio = 		
			  SELECT
					  COALESCE(SUM
					  (
						  (CASE (GAR."IdOrigenGarantia")
						      WHEN 'ESP' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CTA' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CUS' THEN  
									(GAR."INGMontoATasaMercadoActual") * (COALESCE(COB."CoberturaEntidad",:v_porcentajeCoberturaSinDefinir)/100)
					      END)
					  ),0) As "MontoCoberturaEntidad",	
					  
					  COALESCE(SUM
					  (
						  (CASE (GAR."IdOrigenGarantia")
						      WHEN 'ESP' THEN  
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CTA' THEN 
									  (GAR."MontoGarantia") * (:v_porcentajeCoberturaEfectivoEspecies/100)
						      WHEN 'CUS' THEN  
									(GAR."INGMontoATasaMercadoActual") * (COALESCE(COB."CoberturaCorredora",:v_porcentajeCoberturaSinDefinir)/100)
					      END)
					  ),0) As "MontoCoberturaCorredora"
					  
			  FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" AS GAR
			  INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
				ON INS."IdInstrumento" = GAR."IdInstrumento"
			  LEFT JOIN :vTablaRankingClasificacion AS CLAINS
				ON CLAINS."IdInstrumento" = GAR."IdInstrumento"
			  LEFT JOIN "BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia" AS COB ON (
						    GAR."IdContraparteGarantia" 		  = COB."IdContraparteDestinoGarantia"
						AND GAR."IdTipoInstrumentoAval" 		  = COB."IdTipoInstrumentoComoGarantia"
					    AND GAR."IdEntidadClasificadoraRiesgo" 	  = COB."IdEntidadClasificadoraRiesgo"
						AND CLAINS."ValorClasificacionRiesgo" 	  = COB."NombreClasificacionRiesgo"
						AND GAR."IdTipoClasificacionRiesgo" 	  = COB."IdTipoClasificacionRiesgo"									
						)
			  WHERE GAR."IdPortafolio" 						= :p_IdPortafolio
			  AND   COB."IdTipoInstrumentoExigeGarantia" = 'DER'
			  AND   COB."IdInstrumentoExigeGarantia" = 0
			  AND   GAR."IdConcepto" != 'GARDEV'
			  AND   COB."IdEstado" = 'ACT';		
		END IF;
	END;    
END;
