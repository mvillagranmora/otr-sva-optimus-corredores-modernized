SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Orden.BD.Servicios.CAR::ValidarCargaMasiva";
GO
CREATE PROCEDURE
   
"BSCL.Orden.BD.Servicios.CAR::ValidarCargaMasiva"(


	IN  p_CargaMasivaValidar         TABLE  (  -- Cargas masivas a validar
											"IdCargaMasiva"       	BIGINT,
											"CodigoTipoCargaMasiva"	NVARCHAR(40)   -- Ej. CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS
										 	),										   

	OUT p_out_CargaMasivaInvalida    TABLE ("IdCargaMasiva"       	BIGINT),  -- Cargas masivas con registros invalidos	
	OUT p_out_ErroresValidacion      TABLE (                                  -- Detalle registros invalidos
											"IdCargaMasiva"			 BIGINT,
											"IdCargaMasivaDetalle"   BIGINT,
											"ObservacionValidacion"	 NVARCHAR(1000)
											)

) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT;
	DECLARE v_MensajesValidacion	TABLE (
											"Codigo"		NVARCHAR(100),
											"Descripcion"	NVARCHAR(520)											
										  );

	p_out_CargaMasivaInvalida   = SELECT 0 AS "IdCargaMasiva" FROM dummy WHERE 1=2;
	p_out_ErroresValidacion		= SELECT 0 AS "IdCargaMasiva", 0 AS "IdCargaMasivaDetalle", '' AS "ObservacionValidacion" FROM dummy WHERE 1=2;	
	v_MensajesValidacion		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1=2;

	
	BEGIN	
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
		DECLARE v_TablaValidaciones    	TABLE ("Cumple" BIGINT, "CodigoError" NVARCHAR(40), 
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCargaMasivaDetalle" BIGINT, "IdCargaMasiva" BIGINT ,"CodigoTipoCargaMasiva" NVARCHAR(60) );
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva", '' AS "CodigoTipoCargaMasiva"  FROM dummy WHERE (1=2);		



		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------		

		-- Obtiene detalles de carga masiva a validar
		v_CargaMasivaDetalle = 
		SELECT		
			CMD."IdCargaMasiva",
			CMV."CodigoTipoCargaMasiva",
			CMD."IdCargaMasivaDetalle",
			
			-- detalle de la carga: CARGA_INGRESO_ORDEN_MASIVO			
			CMD."DocumentoIdentidad",
			CMD."CodigoPortafolio",
			CMD."TipoOperacion",
			CMD."IdMecanismoCaptacion",			
			CMD."Nemotecnico",
			CMD."Cantidad",
			CMD."Precio",
			CMD."IdCondicionDeLiquidacion",
			CMD."IdTipoVigenciaOrden",
			CMD."FechaVigencia",
			CMD."IdFormaCustodia",
			CMD."IdBolsa",
			CMD."IdBoveda",
			CMD."IdTipoPrecioOrden",
			CMD."IdPersona",
			CMD."IdPortafolio"
		FROM :p_CargaMasivaValidar AS CMV
		INNER JOIN "BSCL.Orden.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva");
		
		
		-- Valida IdBolsa (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(BOL."IdBolsa",'') = '') THEN 0 ELSE  1 END AS "Cumple",				
				CASE WHEN (COALESCE(BOL."IdBolsa",'') = '') THEN 'BOLSA_NO_VALIDA'	ELSE  '' END AS "CodigoError",				
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS BOL ON (BOL."IdBolsa" = CMD."IdBolsa" AND BOL."IdEstado" = 'ACT')		
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;	
		
		
		-- Valida IdBoveda (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(BOV."IdBoveda",'') = '')  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(BOV."IdBoveda",'') = '') THEN 'BOVEDA_NO_VALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD	
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOV ON (BOV."IdBoveda" = CMD."IdBoveda" AND BOV."IdEstado" = 'VIG')		
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida IdCondicionDeLiquidacion (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(CON."IdCondicionDeLiquidacion",'') = '')  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(CON."IdCondicionDeLiquidacion",'') = '') THEN 'CONDICION_LIQUIDACION_CONTADO_NO_VALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CON ON (CON."IdCondicionDeLiquidacion" = CMD."IdCondicionDeLiquidacion")		
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida IdTipoVigenciaOrden (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(VIG."IdTipoVigenciaOrden",'') = '')  THEN 0 ELSE  1 END AS "Cumple",	
				CASE WHEN (COALESCE(VIG."IdTipoVigenciaOrden",'') = '') THEN 'TIPO_VIGENCIA_NO_VALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD 
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoVigenciaOrden" AS VIG ON (VIG."IdTipoVigenciaOrden" = CMD."IdTipoVigenciaOrden")
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		
		-- Valida IdTipoFormaCustodiaOrden (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(FCUS."IdTipoFormaCustodiaOrden",'') = '')  THEN 0 ELSE  1 END AS "Cumple",	
				CASE WHEN (COALESCE(FCUS."IdTipoFormaCustodiaOrden",'') = '') THEN 'FORMA_CUSTODIA_NO_VALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS FCUS ON (FCUS."IdTipoFormaCustodiaOrden" = CMD."IdFormaCustodia")		
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida Nemotecnico (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(INS."Nemotecnico",'') = '') THEN 0	 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(INS."Nemotecnico",'') = '') THEN 'NEMOTECNICO_NO_VALIDO' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = CMD."Nemotecnico")
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		

		-- Valida Cantidad (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(CMD."Cantidad",0) <= 0)  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(CMD."Cantidad",0) <= 0)  THEN 'UI_CANTIDAD_INVALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida TipoOperacion (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(CMD."TipoOperacion",'') <> 'C' AND COALESCE(CMD."TipoOperacion",'') <> 'V')  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(CMD."TipoOperacion",'') <> 'C' AND COALESCE(CMD."TipoOperacion",'') <> 'V')  THEN 'TIPO_OPERACION_NO_VALIDA' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		
		-- Valida IdTipoPrecioOrden (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(TPRE."IdTipoPrecioOrden",'') = '')  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(TPRE."IdTipoPrecioOrden",'') = '') THEN 'TIPO_PRECIO_NO_VALIDO' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" AS TPRE ON (TPRE."IdTipoPrecioOrden" = CMD."IdTipoPrecioOrden")		
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		

		-- Valida IdPersona (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(CMD."IdPersona",0) = 0)  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(CMD."IdPersona",0) = 0)  THEN 'SOCIO_DE_NEGOCIO_NO_VALIDO' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida IdPortafolio (se valida por separado para concatenar todos las validaciones al final)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN (COALESCE(CMD."IdPortafolio",0) = 0)  THEN 0 ELSE  1 END AS "Cumple",
				CASE WHEN (COALESCE(CMD."IdPortafolio",0) = 0)  THEN 'PORTAFOLIO_INVALIDO' ELSE  '' END AS "CodigoError",
				'' AS "CodigoDato",
		       	'' AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva",
				CMD."CodigoTipoCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		WHERE CMD."CodigoTipoCargaMasiva"='CARGA_INGRESO_ORDEN_MASIVO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva","CodigoTipoCargaMasiva" FROM :v_TablaValidaciones;

		
		-------------------------------------------------
		-- ANALISIS DE DATOS
		-----------------------------------------------	   	
	   	
		-- Cargas Masivas con registros invalidos
	    p_out_CargaMasivaInvalida =
	    SELECT "IdCargaMasiva"
	    FROM :v_TablaValidaciones
	    WHERE "Cumple" = 0
		GROUP BY "IdCargaMasiva";
	    
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaValidaciones
		WHERE "Cumple" = 0;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_MensajesValidacion);


		
		p_out_ErroresValidacion = 		
		SELECT 
			"IdCargaMasiva",
			"IdCargaMasivaDetalle",			
			(
				CASE WHEN (MAX("SOCIO_DE_NEGOCIO_NO_VALIDO")  IS NOT  NULL) THEN MAX("SOCIO_DE_NEGOCIO_NO_VALIDO") ELSE ' ' END ||
				CASE WHEN (MAX("PORTAFOLIO_INVALIDO")  IS NOT  NULL) THEN MAX("PORTAFOLIO_INVALIDO") ELSE ' ' END ||			
				CASE WHEN (MAX("BOLSA_NO_VALIDA") IS NOT NULL)  THEN MAX("BOLSA_NO_VALIDA")  ELSE ' ' END ||
				CASE WHEN (MAX("BOVEDA_NO_VALIDA")  IS NOT  NULL) THEN MAX("BOVEDA_NO_VALIDA") ELSE ' ' END  ||				
				CASE WHEN (MAX("CONDICION_LIQUIDACION_CONTADO_NO_VALIDA")  IS NOT  NULL) THEN MAX("CONDICION_LIQUIDACION_CONTADO_NO_VALIDA") ELSE ' ' END  ||				
				CASE WHEN (MAX("TIPO_VIGENCIA_NO_VALIDA")  IS NOT  NULL) THEN MAX("TIPO_VIGENCIA_NO_VALIDA") ELSE ' ' END  ||			
				CASE WHEN (MAX("FORMA_CUSTODIA_NO_VALIDA")  IS NOT  NULL) THEN MAX("FORMA_CUSTODIA_NO_VALIDA") ELSE ' ' END  ||			
				CASE WHEN (MAX("NEMOTECNICO_NO_VALIDO")  IS NOT  NULL) THEN MAX("NEMOTECNICO_NO_VALIDO") ELSE ' ' END  ||				
				CASE WHEN (MAX("UI_CANTIDAD_INVALIDA")  IS NOT  NULL) THEN MAX("UI_CANTIDAD_INVALIDA") ELSE ' ' END  ||		
				CASE WHEN (MAX("TIPO_OPERACION_NO_VALIDA")  IS NOT  NULL) THEN MAX("TIPO_OPERACION_NO_VALIDA") ELSE ' ' END  ||			
				CASE WHEN (MAX("TIPO_PRECIO_NO_VALIDO")  IS NOT  NULL) THEN MAX("TIPO_PRECIO_NO_VALIDO") ELSE ' ' END
			) AS "ObservacionValidacion"
					
		FROM 
		(
				SELECT			
					TV."IdCargaMasiva",
					TV."IdCargaMasivaDetalle",	
					TV."CodigoTipoCargaMasiva",
					CASE WHEN (MV."Codigo" = 'BOLSA_NO_VALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "BOLSA_NO_VALIDA",
					CASE WHEN (MV."Codigo" = 'BOVEDA_NO_VALIDA') THEN MV."Descripcion" || ' - '  ELSE NULL END AS "BOVEDA_NO_VALIDA",
					CASE WHEN (MV."Codigo" = 'CONDICION_LIQUIDACION_CONTADO_NO_VALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "CONDICION_LIQUIDACION_CONTADO_NO_VALIDA",					
					CASE WHEN (MV."Codigo" = 'TIPO_VIGENCIA_NO_VALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "TIPO_VIGENCIA_NO_VALIDA",			
					CASE WHEN (MV."Codigo" = 'FORMA_CUSTODIA_NO_VALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "FORMA_CUSTODIA_NO_VALIDA",				
					CASE WHEN (MV."Codigo" = 'NEMOTECNICO_NO_VALIDO')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "NEMOTECNICO_NO_VALIDO",				
					CASE WHEN (MV."Codigo" = 'UI_CANTIDAD_INVALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "UI_CANTIDAD_INVALIDA",			
					CASE WHEN (MV."Codigo" = 'TIPO_OPERACION_NO_VALIDA')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "TIPO_OPERACION_NO_VALIDA",			
					CASE WHEN (MV."Codigo" = 'TIPO_PRECIO_NO_VALIDO')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "TIPO_PRECIO_NO_VALIDO",	
					CASE WHEN (MV."Codigo" = 'SOCIO_DE_NEGOCIO_NO_VALIDO')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "SOCIO_DE_NEGOCIO_NO_VALIDO",
					CASE WHEN (MV."Codigo" = 'PORTAFOLIO_INVALIDO')  THEN MV."Descripcion" || ' - ' ELSE NULL END AS "PORTAFOLIO_INVALIDO"
					
					
				FROM  :v_TablaValidaciones AS TV
				INNER JOIN :v_MensajesValidacion AS MV  ON (TV."CodigoError" = MV."Codigo")
				WHERE TV."Cumple" = 0
		)
		WHERE "CodigoTipoCargaMasiva" = 'CARGA_INGRESO_ORDEN_MASIVO'
		GROUP BY "IdCargaMasiva", "IdCargaMasivaDetalle";

				
		 
    END;
	
END;
