SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPuntasPorTramo";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPuntasPorTramo"(
	IN p_IdEmpresa		BIGINT,
	IN p_IdMoneda		NVARCHAR(3),
	IN p_MontoPorTramo	BIGINT,
	IN p_Canal			NVARCHAR(10) DEFAULT 'Punta',
	-- Salida  --------------------------------------------------
	OUT p_out_puntas TABLE (  "MontoPorTramo" 		DECIMAL(26,8)
							, "PuntaCompra"			DECIMAL(26,8)
							, "PuntaVenta"			DECIMAL(26,8)
							, "PuntaCompraDefault"	DECIMAL(26,8)							
							, "PuntaVentaDefault"	DECIMAL(26,8)
							, "CheckPrecioPorTramo" NVARCHAR(2)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg				table ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg				NVARCHAR(520);	        	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno" 
				                                    , ''  AS "Codigo"
				                                    , ''  AS "RequiereAutorizacion"
				                                    , ''  AS "ValorParametro"
				                                    , ''  AS "NombreParametro"
												FROM dummy WHERE 1=2;
				                               
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, 
												"Codigo" nvarchar(100),
												"ValorParametro" nvarchar(100),
												"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno" 
				                                    , ''  AS "Codigo"
				                                    , ''  AS "ValorParametro"
				                                    , ''  AS "NombreParametro" 
		                               			FROM dummy WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes =
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato"
			FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato"
			FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle =
			SELECT errores."IdInterno", errores."Codigo", msj."Descripcion"
			FROM :v_out_ErroresDetalle errores
			INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle =
			SELECT adv."IdInterno", adv."Codigo", adv."RequiereAutorizacion", msj."Descripcion"
			FROM :v_out_AdvertenciasDetalle adv
			INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle 	= SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle 		= SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	
    BEGIN
    	DECLARE valida_moneda		INT;
		DECLARE valida_existe_tramo	INT;
		DECLARE valida_tramo 		NVARCHAR(3);
    
		--VALIDAR SI MONEDA EXISTE
    	SELECT COUNT("CheckPrecioPorTramo") INTO valida_moneda
		FROM "BSCL.Mesa.BD::SPT.Puntas"
		WHERE "IdMonedaPunta"	= :p_IdMoneda
		  AND "IdEmpresa"		= :p_IdEmpresa;
		
		IF (valida_moneda = 0) THEN
			p_out_AdvertenciasDetalle =
      			SELECT
      				0 as "IdInterno",
      				'PUNTA' as "Codigo",
					'N' as "RequiereAutorizacion",
				    'Moneda no registras Puntas' as "Descripcion"
				from dummy;
		ELSE
    		SELECT
    			0 as "IdInterno",
			  	'' as "Codigo",
				'' as "RequiereAutorizacion",
			    '' as "Descripcion"
			from dummy;
		END IF;
		
		
		IF (valida_moneda > 0) THEN
			
		  	--VALIDAR SI MONEDA POSEE TRAMOS
			SELECT "CheckPrecioPorTramo" INTO valida_tramo
			FROM "BSCL.Mesa.BD::SPT.Puntas"
			WHERE "IdMonedaPunta"	= :p_IdMoneda
			  AND "IdEmpresa"		= :p_IdEmpresa;
			
			
			--DATOS DE SALIDA
			IF (valida_tramo = 'NO') THEN
				p_out_puntas =
					SELECT
						0 as "MontoPorTramo"
						, PTA."PuntaCompra"
						, PTA."PuntaVenta"
						, PTA."PuntaCompra" AS "PuntaCompraDefault"
						, PTA."PuntaVenta"	AS	"PuntaVentaDefault"						
						, PTA."CheckPrecioPorTramo"
					FROM "BSCL.Mesa.BD::SPT.Puntas" PTA
					WHERE PTA."IdMonedaPunta"	= :p_IdMoneda
					  AND PTA."IdEmpresa"		= :p_IdEmpresa;
			
			ELSE IF (valida_tramo = 'SI') THEN
				
				--VALIDAR SI EXISTE TRAMOS
				SELECT COUNT(PPT."MontoPorTramo") INTO valida_existe_tramo
				FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" PPT
				INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" P
					ON P."IdPuntas"=PPT."IdPuntas"
				WHERE P."IdMonedaPunta" = :p_IdMoneda
				  AND P."IdEmpresa" 	= :p_IdEmpresa
				  AND P."CheckPrecioPorTramo" = 'SI'
				  AND PPT."Canal"			= :p_Canal;
				
				IF (valida_existe_tramo = 0) THEN
					p_out_AdvertenciasDetalle =
						SELECT
							0 as "IdInterno",
						  	'TRAMO' as "Codigo",
							'N' as "RequiereAutorizacion",
						    'Moneda no registra Puntas por Tramo configuradas' as "Descripcion"
						from dummy;
				ELSE
					/*
					SELECT
						0 as "IdInterno",
					  	'' as "Codigo",
						'' as "RequiereAutorizacion",
					    '' as "Descripcion"
				    from dummy;
				    */
				    p_out_puntas =
						SELECT
							 COALESCE(PPT."MontoPorTramo",0)	AS "MontoPorTramo"
							,COALESCE(PPT."ValorCompra",0)		AS "PuntaCompra"
							,COALESCE(PPT."ValorVenta",0)		AS "PuntaVenta"
							,PTA."PuntaCompra"					AS "PuntaCompraDefault"
							,PTA."PuntaVenta"					AS "PuntaVentaDefault"	
							,COALESCE(PTA."CheckPrecioPorTramo",'NO') AS "CheckPrecioPorTramo"
						FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" PPT
						INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" PTA
							ON PTA."IdPuntas" = PPT."IdPuntas" 
						WHERE PTA."IdMonedaPunta"	= :p_IdMoneda
						  AND PTA."IdEmpresa"		= :p_IdEmpresa
						  AND PPT."Canal"			= :p_Canal
						ORDER BY COALESCE(PPT."MontoPorTramo",0);
				END IF;
				
			END IF;
			END IF;
		END IF;
		
		SELECT COUNT(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		SELECT COUNT(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
    END;
 END;