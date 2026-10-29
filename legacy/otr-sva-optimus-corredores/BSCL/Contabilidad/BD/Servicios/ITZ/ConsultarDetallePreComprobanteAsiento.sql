SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarDetallePreComprobanteAsiento";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 11/09/2016
--  Id Servicio = 12014
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarDetallePreComprobanteAsiento" (
	IN p_IdEmpresa    				BIGINT			DEFAULT 0,
	IN p_IdCuentaContable   		NVARCHAR(12)	DEFAULT '',
	IN p_RutMovimiento   			NVARCHAR(11)	DEFAULT '0-0',
	IN p_CodigoUsuario   			NVARCHAR(40)	DEFAULT '',
	IN p_NumeroMovimiento   		BIGINT			DEFAULT 0,
	IN p_IdPreComprobante   		BIGINT			DEFAULT 0,
	IN p_IdPreComprobanteAsiento	BIGINT			DEFAULT 0,
	IN p_FechaContableInicial		NVARCHAR(10)	DEFAULT '',
	IN p_FechaContableFinal			NVARCHAR(10)	DEFAULT '',
	IN p_IdTipoMovimiento			NVARCHAR(10)	DEFAULT '',
	IN p_IdTipoEvento				NVARCHAR(40)	DEFAULT '0',
	IN p_IdEvento					BIGINT			DEFAULT 0,
	IN p_FechaProceso				NVARCHAR(10)	DEFAULT '',
	--Tabla que agrega filtros adicionales
	IN p_DatosFiltro						TABLE("IdTipoDatoEvento"				NVARCHAR(30),
												  "TipoDato"						NVARCHAR(1),
												  "ValorVarchar"					NVARCHAR(60),
												  "ValorDecimal"					DECIMAL(18,4)),
	-- Parametros salida del servicio publico
	OUT p_out_TabDetallePreComprobanteAsiento TABLE ( 
													"IdPreComprobanteAsiento"	BIGINT,
													"IdPreComprobante"			BIGINT,
													"IdEmpresa"					BIGINT,
													"Ejercicio"					BIGINT,
													"FechaContable"				NVARCHAR(10),
													"IdCuentaContable"			NVARCHAR(12),
													"NombreCuenta"				NVARCHAR(70),
													"Linea"						BIGINT,
													"Glosa"						NVARCHAR(120),
													"Operador"					NVARCHAR(10),
													"MontoDebe"					DECIMAL,
													"MontoHaber"				DECIMAL,
													"IdTipoEvento"				NVARCHAR(40),
													"Evento"					NVARCHAR(60),
													"Estado"					NVARCHAR(1),
													"IdMovimiento"				BIGINT,
													"IdTipoMovimiento"			NVARCHAR(10),
													"Numero"					BIGINT,
													"DocumentoIdentidad"		NVARCHAR(40),
													"Cuenta"					BIGINT,
													"CodigoPortafolio"			NVARCHAR(100),
													"IdMoneda"					NVARCHAR(3)
				                                 ) DEFAULT EMPTY,
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40), "Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	-- FIN Sección Fija
	)
      LANGUAGE SQLSCRIPT
      SQL SECURITY INVOKER
      DEFAULT SCHEMA "BSCL"
      --READS SQL DATA
AS
BEGIN
	DECLARE v_TotalRegistros			BIGINT default 0;
	DECLARE v_CantRegChar				BIGINT;
	DECLARE v_CantRegDeci				BIGINT;

	DECLARE v_TipoFiltro				NVARCHAR(10);
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_IdMoneda					NVARCHAR(3);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
    											SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
        v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo", "Descripcion" AS "Descripcion" FROM :v_out_hash
							   UNION ALL
		                       SELECT * FROM :p_out_ErroresDetalle;

		SELECT COUNT(1)
		  INTO p_out_ErroresContador
		  FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::SQL_ERROR_CODE, v_errorExt);

			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle 
								   UNION ALL
								   SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;	

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;
	v_IdMoneda				   :='';
	
	SELECT "Valor" INTO v_IdMoneda 
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.MonedaSistema';


	BEGIN
		SELECT COUNT(1) 
		  INTO v_TotalRegistros 
		  FROM :p_DatosFiltro;
		
		IF :v_TotalRegistros > 0 THEN 
			v_TipoFiltro := 'COMPUESTO';
		ELSE 
			v_TipoFiltro := 'SENCILLO';
		END IF;
		
		IF v_TipoFiltro = 'SENCILLO' THEN
			p_out_TabDetallePreComprobanteAsiento = 
				 SELECT CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"									AS "IdPreComprobanteAsiento",
						CTB_itzPreComprobanteAsiento."IdPreComprobante"											AS "IdPreComprobante",
						CTB_itzPreComprobanteAsiento."IdEmpresa"												AS "IdEmpresa",
						CTB_itzPreComprobanteAsiento."Ejercicio"												AS "Ejercicio",
						CAST(CAST(CTB_itzPreComprobanteAsiento."FechaContable" AS TIMESTAMP) AS NVARCHAR(10))	AS "FechaContable",
						CTB_itzPreComprobanteAsiento."IdCuentaContable"											AS "IdCuentaContable",
						CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
							'?'
						ELSE
							CTB_itzHomologaCuenta."NombreCuenta"
						END 																					AS "NombreCuenta",
						CTB_itzPreComprobanteAsiento."Linea"													AS "Linea",
						CTB_itzEventoPreComprobanteAsiento."GlosaDetalle" 										AS "Glosa",
						CTB_itzEventoPreComprobanteAsiento."Operador"											AS "Operador",
						CASE WHEN CTB_itzEventoPreComprobanteAsiento."DebeHaber" = '' THEN
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoDebe" > 0 THEN
								CAST(ABS(CTB_itzEvento."Monto") AS DECIMAL)
							ELSE
								0
							END
						WHEN CTB_itzEventoPreComprobanteAsiento."DebeHaber" = 'D' THEN
							CAST(ABS(CTB_itzEvento."Monto") AS DECIMAL)
						ELSE 
							0
						END 																					AS "MontoDebe",
						CASE WHEN CTB_itzEventoPreComprobanteAsiento."DebeHaber" = '' THEN
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoHaber" > 0 THEN
								CAST(ABS(CTB_itzEvento."Monto") AS DECIMAL)
							ELSE
								0
							END
						WHEN CTB_itzEventoPreComprobanteAsiento."DebeHaber" = 'H' THEN
							CAST(ABS(CTB_itzEvento."Monto") AS DECIMAL)
						ELSE
							0
						END																						AS "MontoHaber",
						CTB_itzEventoTipo."IdTipoEvento"														AS "IdTipoEvento",
						CASE WHEN CTB_itzEventoTipo."Nombre" = '' THEN
							'<COMPUESTO>'
						ELSE
							CTB_itzEventoTipo."Nombre"
						END 																					AS "Evento",
						CTB_itzPreComprobanteAsiento."Estado"													AS "Estado",
						CTB_itzMovimiento."IdMovimiento"														AS "IdMovimiento",
						CTB_itzMovimiento."IdTipoMovimiento"													AS "IdTipoMovimiento",
						CTB_itzMovimiento."Numero"																AS "Numero",
						CTB_itzMovimiento."DocumentoIdentidad"													AS "DocumentoIdentidad",
						CTB_itzMovimiento."Cuenta"																AS "Cuenta",
						Portafolio."CodigoPortafolio"															AS "CodigoPortafolio",
--						v_IdMoneda																				AS "IdMoneda"
						CTB_itzEvento."CodigoMoneda"															AS "IdMoneda"
				   FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
									LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON
															CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"
														AND (CTB_itzPreComprobanteAsiento."MontoDebe" <> 0 OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
									LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON
															CTB_itzEvento."IdEvento" = CTB_itzEventoPreComprobanteAsiento."IdEvento"
														AND (CTB_itzEvento."Monto" <> 0)
									LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
															CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento"
									LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON
															CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
				  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
				  									CTB_itzHomologaCuenta."CuentaContabilidad" = CTB_itzPreComprobanteAsiento."IdCuentaContable"
				  					 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio ON
				 									Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
				  WHERE (CTB_itzPreComprobanteAsiento."IdEmpresa" 				=  :p_IdEmpresa)
				    AND (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" =  :p_IdPreComprobanteAsiento 	OR :p_IdPreComprobanteAsiento = 0)
					AND (CTB_itzPreComprobanteAsiento."IdCuentaContable" 		=  :p_IdCuentaContable		 	OR :p_IdCuentaContable = '')
					AND (CTB_itzMovimiento."DocumentoIdentidad" 				=  :p_RutMovimiento			 	OR :p_RutMovimiento = '        0-0')
					AND (CTB_itzPreComprobanteAsiento."IdPreComprobante" 		=  :p_IdPreComprobante		 	OR :p_IdPreComprobante = 0)
					AND (CTB_itzEvento."CodigoUsuario" 							=  :p_CodigoUsuario			 	OR :p_CodigoUsuario = '')
					AND (CTB_itzMovimiento."Numero" 							=  :p_NumeroMovimiento		 	OR :p_NumeroMovimiento = 0)
					AND (CTB_itzEvento."FechaContable" 							BETWEEN :p_FechaContableInicial AND :p_FechaContableFinal)
					AND (CTB_itzMovimiento."IdTipoMovimiento" 					=  :p_IdTipoMovimiento 		 	OR :p_IdTipoMovimiento = '')
					AND (CTB_itzEvento."FechaProceso" 							=  :p_FechaProceso			 	OR :p_FechaProceso = '')
					AND (CTB_itzEvento."IdTipoEvento" 							=  :p_IdTipoEvento			 	OR :p_IdTipoEvento = 0)
					AND (CTB_itzEvento."IdEvento" 								=  :p_IdEvento				 	OR :p_IdEvento = 0)
					AND (CTB_itzPreComprobanteAsiento."MontoDebe" 				<> 0 			  			 	OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
					AND (CTB_itzEvento."Monto" 									<> 0)
			   ORDER BY CTB_itzPreComprobanteAsiento."IdPreComprobante",
			   			CTB_itzEventoPreComprobanteAsiento."Operador",
			   			CTB_itzPreComprobanteAsiento."Linea",
			   			CTB_itzPreComprobanteAsiento."IdCuentaContable";
		END IF;
		IF v_TipoFiltro = 'COMPUESTO' THEN
			--+ Cuenta los registros que son string  
			SELECT COUNT(*) 
			  INTO v_CantRegChar
			  FROM :p_DatosFiltro as p_DatosFiltro 
			 WHERE p_DatosFiltro."TipoDato" = 'V';
			 
			--+ Cuenta los registros que son numericos  
			SELECT COUNT(*) 
			  INTO v_CantRegDeci
			  FROM :p_DatosFiltro as p_DatosFiltro 
			 WHERE p_DatosFiltro."TipoDato" = 'D';

			IF :v_CantRegChar = 0 THEN 
				v_CantRegChar := -1;
			END IF;
			IF :v_CantRegDeci = 0 THEN 
				v_CantRegDeci := -1;
			END IF;

			p_out_TabDetallePreComprobanteAsiento = 
					 SELECT CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"									AS "IdPreComprobanteAsiento",
							CTB_itzPreComprobanteAsiento."IdPreComprobante"											AS "IdPreComprobante",
							CTB_itzPreComprobanteAsiento."IdEmpresa"												AS "IdEmpresa",
							CTB_itzPreComprobanteAsiento."Ejercicio"												AS "Ejercicio",
							CAST(CAST(CTB_itzPreComprobanteAsiento."FechaContable" AS TIMESTAMP) AS NVARCHAR(10))	AS "FechaContable",
							CTB_itzPreComprobanteAsiento."IdCuentaContable"											AS "IdCuentaContable",
							CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
								'?'
							ELSE
								CTB_itzHomologaCuenta."NombreCuenta"
							END 																					AS "NombreCuenta",
							CTB_itzPreComprobanteAsiento."Linea"													AS "Linea",
							CTB_itzEventoPreComprobanteAsiento."GlosaDetalle" 										AS "Glosa",
							CTB_itzEventoPreComprobanteAsiento."Operador"											AS "Operador",
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoDebe" > 0 THEN
								CAST(CTB_itzEvento."Monto" AS DECIMAL)
							ELSE
								0
							END 																					AS "MontoDebe",
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoHaber" > 0 THEN
								CAST(CTB_itzEvento."Monto" AS DECIMAL)
							ELSE
								0
							END 																					AS "MontoHaber",
							CTB_itzEventoTipo."IdTipoEvento"														AS "IdTipoEvento",
							CASE WHEN CTB_itzEventoTipo."Nombre" = '' THEN
								'<COMPUESTO>'
							ELSE
								CTB_itzEventoTipo."Nombre"
							END 																					AS "Evento",
							CTB_itzPreComprobanteAsiento."Estado"													AS "Estado",
							CTB_itzMovimiento."IdMovimiento"														AS "IdMovimiento",
							CTB_itzMovimiento."IdTipoMovimiento"													AS "IdTipoMovimiento",
							CTB_itzMovimiento."Numero"																AS "Numero",
							CTB_itzMovimiento."DocumentoIdentidad"													AS "DocumentoIdentidad",
							CTB_itzMovimiento."Cuenta"																AS "Cuenta",
							Portafolio."CodigoPortafolio"															AS "CodigoPortafolio",
--							v_IdMoneda																				AS "IdMoneda"
							CTB_itzEvento."CodigoMoneda"															AS "IdMoneda"
					   FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON 
																CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" 
															AND (CTB_itzPreComprobanteAsiento."MontoDebe" <> 0 OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0) 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON 
																CTB_itzEvento."IdEvento" = CTB_itzEventoPreComprobanteAsiento."IdEvento" 
															AND (CTB_itzEvento."Monto" <> 0) 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON 
																CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento" 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON 
																CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento" 
					  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
					  									CTB_itzHomologaCuenta."CuentaContabilidad" = CTB_itzPreComprobanteAsiento."IdCuentaContable"
					  					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio ON
				 									    Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
					  WHERE (CTB_itzPreComprobanteAsiento."IdEmpresa" 				=  :p_IdEmpresa)
					    AND (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" =  :p_IdPreComprobanteAsiento 	OR :p_IdPreComprobanteAsiento = 0)
						AND (CTB_itzPreComprobanteAsiento."IdCuentaContable" 		=  :p_IdCuentaContable		 	OR :p_IdCuentaContable = '')
						AND (CTB_itzMovimiento."DocumentoIdentidad" 				=  :p_RutMovimiento			 	OR :p_RutMovimiento = '        0-0')
						AND (CTB_itzPreComprobanteAsiento."IdPreComprobante" 		=  :p_IdPreComprobante		 	OR :p_IdPreComprobante = 0)
						AND (CTB_itzEvento."CodigoUsuario" 							=  :p_CodigoUsuario			 	OR :p_CodigoUsuario = '')
						AND (CTB_itzMovimiento."Numero" 							=  :p_NumeroMovimiento		 	OR :p_NumeroMovimiento = 0)
						AND (CTB_itzEvento."FechaContable" 							BETWEEN :p_FechaContableInicial AND :p_FechaContableFinal)
						AND (CTB_itzMovimiento."IdTipoMovimiento" 					=  :p_IdTipoMovimiento 		 	OR :p_IdTipoMovimiento = '')
						AND (CTB_itzEvento."FechaProceso" 							=  :p_FechaProceso			 	OR :p_FechaProceso = '')
						AND (CTB_itzEvento."IdTipoEvento" 							=  :p_IdTipoEvento			 	OR :p_IdTipoEvento = 0)
						AND (CTB_itzEvento."IdEvento" 								=  :p_IdEvento				 	OR :p_IdEvento = 0)
						AND (CTB_itzPreComprobanteAsiento."MontoDebe" 				<> 0 			  			 	OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
						AND (CTB_itzEvento."Monto" 									<> 0)
					    AND :v_CantRegChar = (SELECT COUNT(1)
					   						    FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
					   						 			  INNER JOIN :p_DatosFiltro AS p_DatosFiltro ON
					   						 							  p_DatosFiltro."IdTipoDatoEvento" 	= CTB_itzMovimientoDato."IdTipoDatoEvento"
					   						 						  AND p_DatosFiltro."ValorVarchar" 		= CTB_itzMovimientoDato."ValorVarchar"
					   						 						  AND p_DatosFiltro."TipoDato" 			= 'V'
					   						 						  AND p_DatosFiltro."ValorVarchar" 		<> ''
					   						   WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzEvento."IdMovimiento")
					   						  --+ Busca por los filtros de tipo char
				UNION ALL 
					 SELECT CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"									AS "IdPreComprobanteAsiento",
							CTB_itzPreComprobanteAsiento."IdPreComprobante"											AS "IdPreComprobante",
							CTB_itzPreComprobanteAsiento."IdEmpresa"												AS "IdEmpresa",
							CTB_itzPreComprobanteAsiento."Ejercicio"												AS "Ejercicio",
							CAST(CAST(CTB_itzPreComprobanteAsiento."FechaContable" AS TIMESTAMP) AS NVARCHAR(10))	AS "FechaContable",
							CTB_itzPreComprobanteAsiento."IdCuentaContable"											AS "IdCuentaContable",
							CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
								'?'
							ELSE
								CTB_itzHomologaCuenta."NombreCuenta"
							END 																					AS "NombreCuenta",
							CTB_itzPreComprobanteAsiento."Linea"													AS "Linea",
							CTB_itzEventoPreComprobanteAsiento."GlosaDetalle" 										AS "Glosa",
							CTB_itzEventoPreComprobanteAsiento."Operador"											AS "Operador",
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoDebe" > 0 THEN
								CAST(CTB_itzEvento."Monto" AS DECIMAL)
							ELSE
								0
							END 																					AS "MontoDebe",
							CASE WHEN CTB_itzPreComprobanteAsiento."MontoHaber" > 0 THEN
								CAST(CTB_itzEvento."Monto" AS DECIMAL)
							ELSE
								0
							END 																					AS "MontoHaber",
							CTB_itzEventoTipo."IdTipoEvento"														AS "IdTipoEvento",
							CASE WHEN CTB_itzEventoTipo."Nombre" = '' THEN
								'<COMPUESTO>'
							ELSE
								CTB_itzEventoTipo."Nombre"
							END 																					AS "Evento",
							CTB_itzPreComprobanteAsiento."Estado"													AS "Estado",
							CTB_itzMovimiento."IdMovimiento"														AS "IdMovimiento",
							CTB_itzMovimiento."IdTipoMovimiento"													AS "IdTipoMovimiento",
							CTB_itzMovimiento."Numero"																AS "Numero",
							CTB_itzMovimiento."DocumentoIdentidad"													AS "DocumentoIdentidad",
							CTB_itzMovimiento."Cuenta"																AS "Cuenta",
							Portafolio."CodigoPortafolio"															AS "CodigoPortafolio",
--							v_IdMoneda																				AS "IdMoneda"
							CTB_itzEvento."CodigoMoneda"															AS "IdMoneda"
					   FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON 
																CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" 
															AND (CTB_itzPreComprobanteAsiento."MontoDebe" <> 0 OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0) 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON 
																CTB_itzEvento."IdEvento" = CTB_itzEventoPreComprobanteAsiento."IdEvento" 
															AND (CTB_itzEvento."Monto" <> 0) 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON 
																CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento" 
										LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON 
																CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento" 
					  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
					  									CTB_itzHomologaCuenta."CuentaContabilidad" = CTB_itzPreComprobanteAsiento."IdCuentaContable"
					  					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio ON
				 									    Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
					  WHERE (CTB_itzPreComprobanteAsiento."IdEmpresa" 				=  :p_IdEmpresa)
					    AND (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" =  :p_IdPreComprobanteAsiento 	OR :p_IdPreComprobanteAsiento = 0)
						AND (CTB_itzPreComprobanteAsiento."IdCuentaContable" 		=  :p_IdCuentaContable		 	OR :p_IdCuentaContable = '')
						AND (CTB_itzMovimiento."DocumentoIdentidad" 				=  :p_RutMovimiento			 	OR :p_RutMovimiento = '        0-0')
						AND (CTB_itzPreComprobanteAsiento."IdPreComprobante" 		=  :p_IdPreComprobante		 	OR :p_IdPreComprobante = 0)
						AND (CTB_itzEvento."CodigoUsuario" 							=  :p_CodigoUsuario			 	OR :p_CodigoUsuario = '')
						AND (CTB_itzMovimiento."Numero" 							=  :p_NumeroMovimiento		 	OR :p_NumeroMovimiento = 0)
						AND (CTB_itzEvento."FechaContable" 							BETWEEN :p_FechaContableInicial AND :p_FechaContableFinal)
						AND (CTB_itzMovimiento."IdTipoMovimiento" 					=  :p_IdTipoMovimiento 		 	OR :p_IdTipoMovimiento = '')
						AND (CTB_itzEvento."FechaProceso" 							=  :p_FechaProceso			 	OR :p_FechaProceso = '')
						AND (CTB_itzEvento."IdTipoEvento" 							=  :p_IdTipoEvento			 	OR :p_IdTipoEvento = 0)
						AND (CTB_itzEvento."IdEvento" 								=  :p_IdEvento				 	OR :p_IdEvento = 0)
						AND (CTB_itzPreComprobanteAsiento."MontoDebe" 				<> 0 			  			 	OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
						AND (CTB_itzEvento."Monto" 									<> 0)
					    AND :v_CantRegChar = (SELECT COUNT(1)
					   						    FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
					   						 			  INNER JOIN :p_DatosFiltro AS p_DatosFiltro ON
					   						 							  p_DatosFiltro."IdTipoDatoEvento" 	= CTB_itzMovimientoDato."IdTipoDatoEvento"
					   						 						  AND p_DatosFiltro."ValorDecimal" 		= CTB_itzMovimientoDato."ValorDecimal"
					   						 						  AND p_DatosFiltro."TipoDato" 			= 'D'
					   						 						  AND p_DatosFiltro."ValorVarchar" 		<> 0
					   						   WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzEvento."IdMovimiento");
					   						  --+ Busca por los filtros de tipo char
		END IF;
	END;
END;