SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearForward";

--ENTRADAS
--BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_IN_DatosOperacion

--SALIDAS
--BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_OUT_SalidaDetalle
--BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_OUT_Salida

CREATE PROCEDURE "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearForward"(
	IN p_IdEmpresa		BIGINT,
	IN p_IdTrabajo		BIGINT
	
	--p_out_Salida :	--OK=Cumple todos los requisitos
						--ADV=Advertencia
						--AUT=Autorizacion
						--ERR=Error
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE v_FechaSistema NVARCHAR(10);
	
	SELECT COALESCE("Valor",'') INTO v_FechaSistema 
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = :p_IdEmpresa
       AND "CodigoParametro" = 'PLA.FechaSistema';
    
    p_DatosOperacion =
    	SELECT * FROM "BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_IN_DatosOperacion"
    	WHERE "IdTrabajo" = :p_IdTrabajo;
    
    
	v_matriz_validaciones =
		SELECT
			 ope."FolioCartera"
			,1 AS "DOLAR_OBSERVADO_CERO_NEGATIVO"
			,1 AS "MESA_FORWARD_CERRADA"
			,1 AS "MONTO_MAYOR_CERO"
			,1 AS "MONTO_INICIAL_MAYOR_CERO"
			,1 AS "MONTO_FINAL_MAYOR_CERO"
			,1 AS "FECHA_OPERACION_DISTINTA_FECHA_PROCESO"
			,1 AS "DOLAR_OBSERVADO_DEL_DIA"
			,1 AS "PARIDAD_DEL_DIA"
			,1 AS "FECHA_VENCIMIENTO_DISTINTO_OPERACION"
			,1 AS "PLAZO_MAYOR_CERO"	 
			,1 AS "MARGEN_MAYOR_CERO"
			,1 AS "MONTO_DIFERIDO_MAYOR_CERO"	
			,1 AS "MONEDA_HABILITADA_VALIDA"
			,1 AS "FIRMA_CCG_FWD_CLIENTE"
			,1 AS "MODULO_ACTIVO"
			,1 AS "PRODUCTO_NO_HABILITADO"
								 					 
			-- REGLAS DE NEGOCIO FORWARD CALCE

			-- SEGURO CAMBIO NOMINAL
			,CASE WHEN INSTRUMENTO."Nemotecnico" = 'SECANO' THEN 1 ELSE 0 END AS "CAMBIO_NOMINAL"
            ,1 AS "CLIENTE_HABILITADO_SECANO"
            ,1 AS "PORTAFOLIO_HABILITADO_SECANO"
            ,1 AS "TIPO_COSTO_MAYOR_CERO"
            ,1 AS "TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO"
			,1 AS "TC_CIERRE_MAYOR_CERO"
			,1 AS "TC_CIERRE_DISTINTO_TC_COSTO"
			 								 
            -- SEGURO CAMBIO REAJUSTABLE
			,CASE WHEN INSTRUMENTO."Nemotecnico" = 'SECARE' THEN 1 ELSE 0 END	AS "REAJUSTABLE"
			,1 AS "CLIENTE_HABILITADO_SECARE"
            ,1 AS "PORTAFOLIO_HABILITADO_SECARE"
            ,1 AS "UF_SIN_VALOR_SECARE"
             
            -- SEGURO DE INFLACIÓN
			,CASE WHEN INSTRUMENTO."Nemotecnico" = 'SEINFL' THEN 1 ELSE 0 END	AS "INFLACION"
			,1 AS "CLIENTE_HABILITADO_SEINFL"
            ,1 AS "PORTAFOLIO_HABILITADO_SEINFL"
            ,1 AS "PRECIO_COTIZACION_SPOT"

            -- ARBITRAJES
			,CASE WHEN INSTRUMENTO."Nemotecnico" = 'ARBITR' THEN 1 ELSE 0 END	AS "ARBITRAJE"
   			,1 AS "CLIENTE_HABILITADO_ARBITR"
            ,1 AS "PORTAFOLIO_HABILITADO_ARBITR"
            ,1 AS "PARIDAD_CIERRE_MAYOR_CERO"
 		FROM :p_DatosOperacion AS OPE
 		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
 			ON INSTRUMENTO."IdInstrumento" = OPE."IdInstrumento";
 	
 	v_PortafolioCliente =
 		SELECT
 			 ope."FolioCartera"
 			,ope."IdTipoOperacion"
 			,por.* 
 		FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
 		INNER JOIN :p_DatosOperacion ope
 			ON (por."IdPortafolio"= ope."IdPortafolio");
 								
 	--select 'v_PortafolioCliente', * from :v_PortafolioCliente;
	
	v_TablaRequisitos =
		--********** MESA_FORWARD_CERRADA **********
		SELECT
        	 ope."FolioCartera"
        	,CASE WHEN val."MESA_FORWARD_CERRADA" = 0 OR Mesa."IdEstadoMesa" = 'A' THEN 1 ELSE 4 END as "IdResultado"
        	,'UI_MESA_CERRADA' 								AS "Codigo"
        	,CAST(ope."IdMesa" AS NVARCHAR(100))			AS "ValorParametro"
        	,'IdMesa'										AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
       		ON ope."FolioCartera" = val."FolioCartera"
       	LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" Mesa 
	    	ON Mesa."IdMesa" = ope."IdMesa" AND Mesa."IdEmpresa" = :p_IdEmpresa
        UNION ALL
        
	    --********** MONTO_MAYOR_CERO **********
	    SELECT
        	 ope."FolioCartera"
        	,CASE WHEN val."MONTO_MAYOR_CERO" = 0 OR ope."MontoOperacion" > 0 THEN 1 ELSE 4 END AS "IdResultado"
        	,'MONTO_MAYOR_CERO' 							AS "Codigo"
        	,CAST(ope."MontoOperacion" AS NVARCHAR(100))	AS "ValorParametro"
        	,'MontoOperacion'								AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
       		ON ope."FolioCartera" = val."FolioCartera"
        UNION ALL
        
        --********** MONTO_INICIAL_MAYOR_CERO **********
        SELECT
        	 ope."FolioCartera"
        	,CASE WHEN val."MONTO_INICIAL_MAYOR_CERO" = 0 OR ope."MontoInicial" > 0 THEN 1 ELSE 4 END AS "IdResultado"
        	,'MONTO_INICIAL_MAYOR_CERO'						AS "Codigo"
        	,CAST(ope."MontoInicial" AS NVARCHAR(100))		AS "ValorParametro"
        	,'MontoInicial'									AS "NombreParametro"
       	FROM :p_DatosOperacion ope
       	INNER JOIN :v_matriz_validaciones val
       		ON ope."FolioCartera" = val."FolioCartera"
       	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
       		ON INSTRUMENTO."IdInstrumento" = OPE."IdInstrumento" AND INSTRUMENTO."Nemotecnico" IN ('SECARE','SEINFL')
       	UNION ALL
       	
       	--********** MONTO_FINAL_MAYOR_CERO **********
       	SELECT
       		 ope."FolioCartera"
       		,CASE WHEN val."MONTO_FINAL_MAYOR_CERO" = 0 OR ope."MontoFinal" > 0 THEN 1 ELSE 4 END AS "IdResultado"
       		,'MONTO_FINAL_MAYOR_CERO' 						AS "Codigo"
       		,CAST(ope."MontoFinal" AS NVARCHAR(100))		AS "ValorParametro"
       		,'MontoFinal' 									AS "NombreParametro"
       	FROM :p_DatosOperacion ope
       	INNER JOIN :v_matriz_validaciones val
       		ON ope."FolioCartera" = val."FolioCartera"
       	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
       		ON INSTRUMENTO."IdInstrumento" = OPE."IdInstrumento" AND INSTRUMENTO."Nemotecnico" IN ('SECARE','SEINFL')
       	UNION ALL
       	
       	--********** FECHA_OPERACION_DISTINTA_FECHA_PROCESO **********
       	SELECT
			 ope."FolioCartera"
			,CASE WHEN (val."FECHA_OPERACION_DISTINTA_FECHA_PROCESO" = 0 OR ope."FechaOperacion" = v_FechaSistema) THEN 1
				  ELSE 4 END AS "IdResultado"
			,'FECHA_OPERACION_DISTINTA_FECHA_PROCESO'		AS "Codigo"
		 	,CAST(ope."FolioCartera" AS NVARCHAR(100))		AS "ValorParametro"
		 	,'FolioCartera'									AS "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."FolioCartera" = val."FolioCartera"
		  AND ope."IdTipoOperacion" IN ('COMFWD','VENFWD')
		UNION ALL
		
		--********** FECHA_VENCIMIENTO_DISTINTO_OPERACION **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."FECHA_VENCIMIENTO_DISTINTO_OPERACION" = 0 OR TO_CHAR(ope."FechaVencimiento",'YYYY-MM-DD') <> TO_CHAR(:v_FechaSistema,'YYYY-MM-DD') THEN 1
				  ELSE 4 END AS "IdResultado"
		 	,'FECHA_VENCIMIENTO_DISTINTO_OPERACION'			AS "Codigo"
		 	,CAST(ope."FechaVencimiento" AS NVARCHAR(100))	AS "ValorParametro"
		 	,'FechaVencimiento'								AS "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."FolioCartera" = val."FolioCartera"
		  AND ope."IdTipoOperacion" <> 'VCDFWD'
		UNION ALL
		
		--********** PLAZO_MAYOR_CERO **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."PLAZO_MAYOR_CERO" = 0 OR ope."Plazo" > 0 THEN 1 ELSE 4 END AS "IdResultado"
			,'PLAZO_MAYOR_CERO'								AS "Codigo"
			,CAST(ope."Plazo" AS NVARCHAR(100))				AS "ValorParametro"
			,'Plazo'										AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
       		ON ope."FolioCartera" = val."FolioCartera"
	    UNION ALL
       	
		/* PREGUNTAR SOBRE ESTA REGLA DE NEGOCIO
		 SELECT ope."FolioCartera"
			  , CASE WHEN val."MARGEN_MAYOR_CERO" = 0 OR ope."MontoMargen" > 0 THEN 1 ELSE 4 END AS "IdResultado"
			  , 'MARGEN_MAYOR_CERO' 						AS "Codigo"
			  , CAST(ope."FolioCartera" AS NVARCHAR(100)) 	AS "ValorParametro"
			  , 'FolioCartera' 								AS "NombreParametro"
		   FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		  WHERE ope."FolioCartera" = val."FolioCartera"		                  
		  
		  UNION ALL
		 */
		 /* 
		 SELECT ope."FolioCartera"
			  , CASE WHEN val."MONTO_DIFERIDO_MAYOR_CERO" = 0 OR ope."MontoDiferido" > 0 THEN 1 ELSE 4 END AS "IdResultado"
			  , 'MONTO_DIFERIDO_MAYOR_CERO' 				AS "Codigo"
			  , CAST(ope."FolioCartera" AS NVARCHAR(100)) 	AS "ValorParametro"
			  , 'FolioCartera' 								AS "NombreParametro"
		   FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		  WHERE ope."FolioCartera" = val."FolioCartera"
		  
		  UNION ALL
		 */
		
		--********** MONEDA_HABILITADA_VALIDA **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."MONEDA_HABILITADA_VALIDA" = 0 OR COALESCE(CuentaInversion."IdEstado",'INA') = 'ACT' THEN 1 ELSE 4 END AS "IdResultado"
			,'MONEDA_NO_HABILITADA_CUENTA_INVERSION'		AS "Codigo"
			,CAST(ope."IdCliente" AS NVARCHAR(100)) 		AS "ValorParametro"
			,'IdCliente' 									AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" CuentaInversion
			ON CuentaInversion."IdCliente" = ope."IdCliente" AND 
			   CuentaInversion."IdPortafolio" = ope."IdPortafolio" AND
			   CuentaInversion."IdMoneda" = ope."IdMonedaPrincipal" AND
			   val."INFLACION" = 0
		UNION ALL
		
		--********** FIRMA_CCG_FWD_CLIENTE **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."FIRMA_CCG_FWD_CLIENTE" = 0 OR COALESCE(REQ."ValorIndicador",'N') = 'S' THEN 1 ELSE 4 END AS "IdResultado"
			,'FIRMA_CCG_FWD_CLIENTE'						AS "Codigo"
			,CAST(ope."IdCliente" AS NVARCHAR(100)) 		AS "ValorParametro"
			,'IdCliente' 									AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente" REQ
			ON REQ."IdCliente" = ope."IdCliente" AND REQ."IdEmpresa" = :p_IdEmpresa
		UNION ALL
		
		--********** MODULO_ACTIVO **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."MODULO_ACTIVO" = 0 OR (CCM."IdEstado" = 'ACT' AND CCM."TipoEstado" = 'A') THEN 1 ELSE 4 END AS "IdResultado"
			,'UI_MODULO_CERRADO'							AS "Codigo"
			,CAST(CCM."NombreTipoEstado" AS NVARCHAR(100))	AS "ValorParametro"
			,'TipoEstado' 									AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CCM
	    	ON CCM."IdModulo" = 'MFW' AND CCM."IdEmpresa" = :p_IdEmpresa
	    UNION ALL
	    
	    --********** PRODUCTO_NO_HABILITADO **********
		SELECT
			 por."FolioCartera"
			,CASE 
				WHEN val."PRODUCTO_NO_HABILITADO" = 0 THEN 1 
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND 
					 IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND 
					(IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR 
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = por."IdTipoOperacion" OR
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 1
			 	ELSE 4 END									AS "IdResultado"
  			,CASE
  				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
  				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'				THEN 'PRODUCTO_INACTIVO'
  				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO' THEN 'PERFIL_PRODUCTO_BLOQUEADO'
  				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'				THEN 'PRODUCTO_BLOQUEADO'
  				WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',por."IdTipoOperacion") THEN 'TIPO_OPERACION_SIN_VALOR'
  				ELSE 'ERROR_PRODUCTO' END						AS "Codigo"
  			,CAST(por."FolioCartera" AS NVARCHAR(100))		AS "ValorParametro"
  			,'FolioCartera'									AS "NombreParametro"
		FROM :v_PortafolioCliente por
		INNER JOIN :v_matriz_validaciones val
			ON por."FolioCartera" = val."FolioCartera"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfExtra
			ON (por."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
				perfExtra."IdTipoProducto" = 'MEFW' AND 
				perfExtra."IdEmpresa" = :p_IdEmpresa AND 
				(perfExtra."IdTipoOperacion" = por."IdTipoOperacion" OR perfExtra."IdTipoOperacion" = '0'))
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfBase
			ON (por."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
				perfBase."IdTipoProducto" = 'MEFW' AND 
				perfBase."IdEmpresa" = :p_IdEmpresa AND 
				(perfBase."IdTipoOperacion" = por."IdTipoOperacion" OR perfBase."IdTipoOperacion" = '0'))
		UNION ALL
		
	    --********** CLIENTE_HABILITADO **********
	    SELECT
			 ope."FolioCartera"
			,CASE
				WHEN val."CAMBIO_NOMINAL" = 1 THEN 	
		 			CASE WHEN val."CLIENTE_HABILITADO_SECANO" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
		 																COALESCE(cliente."IdCliente",-1) > 0 )
		 			THEN 1 ELSE 4 END
		 		WHEN val."REAJUSTABLE" = 1 THEN
		 			CASE WHEN val."CLIENTE_HABILITADO_SECARE" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
		 																COALESCE(cliente."IdCliente",-1) > 0 )
		 			THEN 1 ELSE 4 END
		 		WHEN val."INFLACION" = 1 THEN
		 			CASE WHEN val."CLIENTE_HABILITADO_SEINFL" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
		 																COALESCE(cliente."IdCliente",-1) > 0 )
		 			THEN 1 ELSE 4 END
		 		WHEN val."ARBITRAJE" = 1 THEN
		 			CASE WHEN val."CLIENTE_HABILITADO_ARBITR" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
		 																COALESCE(cliente."IdCliente",-1) > 0 )
		 			THEN 1 ELSE 4 END
		      ELSE 4 END									AS "IdResultado"
			,'CLIENTE_BLOQUEADO'							AS "Codigo"
			,CAST(ope."IdCliente" AS NVARCHAR(100))			AS "ValorParametro"
			,'IdCliente'									AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cliente
			ON cliente."IdCliente" = ope."IdCliente"
		UNION ALL
		
		--********** PORTAFOLIO_HABILITADO **********
		SELECT
			ope."FolioCartera"
			,CASE
				WHEN val."CAMBIO_NOMINAL" = 1 THEN
					CASE WHEN val."PORTAFOLIO_HABILITADO_SECANO" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
																			COALESCE(cliente."IdCliente",-1) > 0 )
					THEN 1 ELSE 4 END
				WHEN val."REAJUSTABLE" = 1 THEN
					CASE WHEN val."PORTAFOLIO_HABILITADO_SECARE" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
																			COALESCE(cliente."IdCliente",-1) > 0 )
					THEN 1 ELSE 4 END
				WHEN val."INFLACION" = 1 THEN
					CASE WHEN val."PORTAFOLIO_HABILITADO_SEINFL" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
																			COALESCE(cliente."IdCliente",-1) > 0 )
					THEN 1 ELSE 4 END
				WHEN val."ARBITRAJE" = 1 THEN
					CASE WHEN val."PORTAFOLIO_HABILITADO_ARBITR" = 0 OR (	COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND
																			COALESCE(cliente."IdCliente",-1) > 0 )
					THEN 1 ELSE 4 END
			  ELSE 4 END									AS "IdResultado"
			,'PORTAFOLIO_BLOQUEADO'							AS "Codigo"
			,CAST(ope."IdPortafolio" AS NVARCHAR(100))		AS "ValorParametro"
			,'IdPortafolio'									AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" cliente
			ON cliente."IdCliente" = ope."IdCliente" AND cliente."IdPortafolio" = ope."IdPortafolio"
		UNION ALL
		
		--********** TIPO_COSTO_MAYOR_CERO **********
		SELECT
			ope."FolioCartera"
		 	,CASE WHEN val."TIPO_COSTO_MAYOR_CERO" = 0 OR ope."TCCosto" > 0 THEN 1 ELSE 4 END AS "IdResultado"
		 	,'TIPO_COSTO_MAYOR_CERO'						AS "Codigo"
		 	,CAST(ope."FolioCartera" AS NVARCHAR(100))		AS "ValorParametro"
		 	,'FolioCartera'									AS "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val,"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins
		WHERE ope."FolioCartera" = val."FolioCartera"
		  AND ope."IdInstrumento" = ins."IdInstrumento"
		  AND ins."IdTipoInstrumento" = 'FWD'
		  AND ins."Nemotecnico" != 'ARBITR'
		UNION ALL
		
		--********** TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO **********
		SELECT
		 	 ope."FolioCartera"
		 	,CASE WHEN val."TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO" = 0 OR ope."TCCosto" > 0 THEN 1 ELSE 4 END AS "IdResultado"
		 	,'TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO'		AS "Codigo"
		 	,CAST(ope."TCCosto" AS NVARCHAR(100))			AS "ValorParametro"
		 	,'TCCosto'										AS "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val,"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins
        WHERE ope."FolioCartera" = val."FolioCartera"
          AND ope."IdInstrumento" = ins."IdInstrumento"	
          AND ins."IdTipoInstrumento" = 'FWD'
          AND ins."Nemotecnico" != 'ARBITR'
        UNION ALL
        
        --********** TC_CIERRE_MAYOR_CERO **********
        SELECT
         	 ope."FolioCartera"
         	,CASE WHEN val."TC_CIERRE_MAYOR_CERO" = 0 OR ope."TCCierre" > 0 THEN 1 ELSE 4 END AS "IdResultado"
         	,'TC_CIERRE_MAYOR_CERO'							AS "Codigo"
         	,CAST(ope."TCCierre" AS NVARCHAR(100))			AS "ValorParametro"
         	,'TCCierre'										AS "NombreParametro"
        FROM :p_DatosOperacion ope
        INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
        INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
         	ON INSTRUMENTO."IdInstrumento" = OPE."IdInstrumento" AND INSTRUMENTO."Nemotecnico" != 'ARBITR'
		UNION ALL
		
		/*         
        SELECT
        	  ope."FolioCartera"
        	 ,CASE WHEN val."TC_CIERRE_DISTINTO_TC_COSTO" = 0 OR (ope."TCCierre" <> ope."TCCosto" AND ope."TCCosto" <= ope."TCCierre") THEN
        	 	1 ELSE 4 END								AS "IdResultado"
        	 ,'TC_CIERRE_DISTINTO_TC_COSTO' 				AS "Codigo"
			 ,CAST(ope."FolioCartera" AS NVARCHAR(100)) 	AS "ValorParametro"
			 ,'FolioCartera'
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."FolioCartera" = val."FolioCartera"
		UNION ALL
		*/
		
		--********** PARIDAD_CIERRE_MAYOR_CERO **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."PARIDAD_CIERRE_MAYOR_CERO" = 0 OR ope."ParidadCierre" > 0 THEN 1 ELSE 4 END AS "IdResultado"
			,'PARIDAD_CIERRE_MAYOR_CERO'					AS "Codigo"
			,CAST(ope."ParidadCierre" AS NVARCHAR(100))		AS "ValorParametro"
			,'ParidadCierre'								AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
			ON INSTRUMENTO."IdInstrumento" = OPE."IdInstrumento" AND INSTRUMENTO."Nemotecnico" = 'ARBITR'
		UNION ALL
		
		--********** UF_SIN_VALOR_SECARE **********
		SELECT
			 ope."FolioCartera"
			,CASE WHEN val."UF_SIN_VALOR_SECARE" = 0 OR COALESCE(PARUF."TipoCambioMercado",0) > 0 THEN 1 ELSE 4 END AS "IdResultado"
			,'SIN_VALOR_TIPO_CAMBIO_UF_SECARE'							AS "Codigo"
			,CAST(COALESCE(PARUF."TipoCambioMercado",0) AS NVARCHAR(100)) AS "ValorParametro"
			,'TipoCambioMercado'							AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val
			ON ope."FolioCartera" = val."FolioCartera"
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
			ON INSTRUMENTO."IdInstrumento" = ope."IdInstrumento" AND INSTRUMENTO."Nemotecnico" = 'SECARE'
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
			ON (PARUF."IdMoneda" = ope."IdMonedaSecundaria" AND PARUF."Fecha" = ope."FechaOperacion")
		;
	
	
	INSERT INTO "BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_OUT_SalidaDetalle"
	SELECT
		 :p_IdTrabajo
		,tb."FolioCartera"
		,CASE "IdResultado" WHEN 1 THEN 'OK' WHEN 2 THEN 'ADV' WHEN 3 THEN 'AUT' WHEN 4 THEN 'ERR' END as "Resultado"
		,tb."Codigo"
		,tb."ValorParametro"
		,tb."NombreParametro"
	FROM :v_TablaRequisitos tb
	WHERE "IdResultado" != 1
	ORDER BY "FolioCartera";
	
	
	INSERT INTO	"BSCL.Mesa.BD::GBL.ServicioCumpleRequisitosCrearForward_OUT_Salida"
	SELECT
		 :p_IdTrabajo
		,tb."FolioCartera"
		,CASE MAX("IdResultado") WHEN 1 THEN 'OK' WHEN 2 THEN 'ADV' WHEN 3 THEN 'AUT' WHEN 4 THEN 'ERR' END AS "Resultado"
	FROM :v_TablaRequisitos tb
	GROUP BY "FolioCartera"
	ORDER BY "FolioCartera";
	
END;
