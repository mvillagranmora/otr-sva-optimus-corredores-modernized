SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Tesoreria.BD.Servicios.CAR::ValidarCargaMasiva";
GO
CREATE PROCEDURE
-- 
--  Proposito: Valida el detalle de los requisitos de la carga masiva
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 05/10/2016
--   
"BSCL.Tesoreria.BD.Servicios.CAR::ValidarCargaMasiva"(


	IN  p_CargaMasivaValidar         TABLE  (  -- Cargas masivas a validar
											"IdCargaMasiva"       	BIGINT,
											"CodigoTipoCargaMasiva"	NVARCHAR(40)   -- Ej. CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS
										 	),										   

	OUT p_out_CargaMasivaInvalida    TABLE ("IdCargaMasiva"       	BIGINT),  -- Cargas masivas con registros invalidos	
	OUT p_out_ErroresValidacion      TABLE (                                  -- Detalle registros invalidos
											"IdCargaMasiva"			 BIGINT,
											"IdCargaMasivaDetalle"   BIGINT,
											"ObservacionValidacion"	 NVARCHAR(520)
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
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCargaMasivaDetalle" BIGINT, "IdCargaMasiva" BIGINT );
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		



		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------		

		-- Obtiene detalles de carga masiva a validar
		v_CargaMasivaDetalle = 
		SELECT
			CMD."IdPersona",
			CMD."IdPortafolio",
			CMD."IdCargaMasiva",
			CMV."CodigoTipoCargaMasiva",  -- Ej. CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS
			CMD."IdCargaMasivaDetalle",
		    CMD."IdTipoMovimiento",
			CMD."DocumentoIdentidad",
			CMD."Monto",
			CMD."CodigoPortafolio",
			CMD."BCP_CON_IdentificacionDepositante",
			-- Dastos de origen entidad externa
			CMD."Ori_DocumentoIdentidad"
			
		FROM :p_CargaMasivaValidar AS CMV
		INNER JOIN "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva");
		
 
		-- Valida Tipo de Movimiento
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN 0					 
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN 'TIPO_DE_MOVIMIENTO_NO_EXISTE '
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoMovimiento' AS "CodigoDato",
		       	CAST(CMD."IdTipoMovimiento" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"		       	
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TM ON (TM."IdTipoMovimiento" = CMD."IdTipoMovimiento" AND "IdModulo" = 'TES')
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;



		-- Valida que exista el socio de negocio
		v_TablaValidaciones = 
		SELECT  
				CASE 
					WHEN COALESCE(CMD."IdPersona",0) = 0 THEN 0					 
					ELSE  1
				END AS "Cumple",
				CASE 
					WHEN COALESCE(CMD."IdPersona",0) = 0 THEN 'SOCIO_DE_NEGOCIO_NO_EXISTE'
					ELSE  ''
				END AS "CodigoError",
				'IdSocioDeNegocio' AS "CodigoDato",
				CASE 
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS')   THEN CAST(CMD."DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG')   THEN CAST(CMD."DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN CAST(CMD."BCP_CON_IdentificacionDepositante" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC')   THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC')  THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					ELSE  ''
				END AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;		


		

		-- Valida que cliente y persona no estén bloqueados ni desactivos
		v_TablaValidaciones = 
		SELECT  
				CASE
					WHEN (COALESCE(PER."IdPersona",0) > 0 AND COALESCE(PER."IdEstado",'') <> 'ACT') THEN 0
					WHEN (COALESCE(CLI."IdCliente",0) > 0 AND COALESCE(CLI."IdEstado",'') <> 'ACT') THEN 0
					ELSE  1
				END AS "Cumple",
				
				CASE 
					WHEN (COALESCE(PER."IdPersona",0) > 0 AND COALESCE(PER."IdEstado",'') <> 'ACT') THEN 'PERSONA_NO_ACTIVA_SISTEMA'
					WHEN (COALESCE(CLI."IdCliente",0) > 0 AND COALESCE(CLI."IdEstado",'') <> 'ACT') THEN 'CLIENTE_NO_ACTIVO'
					ELSE  ''
				END AS "CodigoError",

				CASE
					WHEN (COALESCE(PER."IdPersona",0) > 0 AND COALESCE(PER."IdEstado",'') <> 'ACT') THEN 'IdPersona'
					WHEN (COALESCE(CLI."IdCliente",0) > 0 AND COALESCE(CLI."IdEstado",'') <> 'ACT') THEN 'IdCliente'
					ELSE  ''
				END AS "CodigoDato",
				
				CASE 
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS')   THEN CAST(CMD."DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG')   THEN CAST(CMD."DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN CAST(CMD."BCP_CON_IdentificacionDepositante" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC')   THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC')  THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					ELSE  ''
				END AS "ValorDato",				
		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CMD."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = CMD."IdPersona")
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		

		-- Valida que exista el portafolio
		v_TablaValidaciones = 
		SELECT  
				CASE
					WHEN (COALESCE(CMD."IdPortafolio",0) = 0) THEN 0
					ELSE  1
				END AS "Cumple",
				
				CASE 
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS'   AND COALESCE(CMD."IdPortafolio",0) = 0) THEN 'PORTAFOLIO_NO_EXISTE'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG'   AND COALESCE(CMD."IdPortafolio",0) = 0) THEN 'PORTAFOLIO_DEFECTO_NO_EXISTE' 					
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' AND COALESCE(CMD."IdPortafolio",0) = 0) THEN 'PORTAFOLIO_DEFECTO_NO_EXISTE'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC'   AND COALESCE(CMD."IdPortafolio",0) = 0) THEN 'PORTAFOLIO_DEFECTO_NO_EXISTE'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC'  AND COALESCE(CMD."IdPortafolio",0) = 0) THEN 'PORTAFOLIO_DEFECTO_NO_EXISTE'
					ELSE  ''
				END AS "CodigoError",
				
				CASE 
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS')   THEN 'IdPortafolio'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG')   THEN 'IdCliente'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN 'IdCliente'					
					WHEN (CMD."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC')   THEN 'IdCliente'
					WHEN (CMD."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC')  THEN 'IdCliente'
					ELSE  ''
				END AS "CodigoDato",
				
				
				CASE 
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS')   THEN CAST(CMD."CodigoPortafolio" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG')   THEN CAST(CMD."DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN CAST(CMD."BCP_CON_IdentificacionDepositante" AS NVARCHAR(50))					
					WHEN (CMD."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC')   THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					WHEN (CMD."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC')  THEN CAST(CMD."Ori_DocumentoIdentidad" AS NVARCHAR(50))
					ELSE  ''
				END AS "ValorDato",
				
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"				
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		
		
		-- Valida que el monto sea mayor a cero
		v_TablaValidaciones = 
		SELECT  
				CASE WHEN COALESCE(CMD."Monto",0) <= 0 THEN 0					 
					 ELSE  1
				END AS "Cumple",				
				CASE WHEN COALESCE(CMD."Monto",0) <= 0 THEN 'MONTO_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'Monto' AS "CodigoDato",
		       	CAST(CMD."Monto" AS NVARCHAR(50))AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"				
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		

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
			TV."IdCargaMasiva",
			TV."IdCargaMasivaDetalle",
			MV."Descripcion" AS "ObservacionValidacion"
		FROM  :v_TablaValidaciones AS TV
		INNER JOIN :v_MensajesValidacion AS MV  ON (TV."CodigoError" = MV."Codigo")
		WHERE TV."Cumple" = 0;

		 
    END;
	
END;
