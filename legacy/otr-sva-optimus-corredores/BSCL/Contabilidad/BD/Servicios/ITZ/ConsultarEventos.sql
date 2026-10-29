SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventos";
GO
CREATE PROCEDURE 
--
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 08/09/2016
--  Id Servicio = 12007
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventos" (
	IN p_IdEmpresa    				NVARCHAR(3)		default '' ,
	IN p_RutMovimiento   			NVARCHAR(11) 	default '        0-0',
	IN p_SecRutMovimiento   		INT				default 0,
	IN p_CodigoUsuario   			NVARCHAR(40)	default '',
	IN p_IdTipoMovimiento   		NVARCHAR(10)	default '',
	IN p_NumeroMovimiento   		NUMERIC(18)		default 0,
	IN p_IdPreComprobante   		DECIMAL(18,0)	default 0,
	IN p_IdPreComprobanteAsiento 	DECIMAL(18,0)	default 0,
	IN p_FechaContableInicial  		NVARCHAR(10)	default '',
	IN p_FechaContableFinal  		NVARCHAR(10)	default '',
	IN p_IdTipoContabilidad  		DECIMAL(18,0)	default 0,
	IN p_IdTipoEvento    			NVARCHAR(40) 	default '',
	IN p_IdEvento     				DECIMAL(18,0)	default 0,
	IN p_FechaMovimiento   			NVARCHAR(10)	default '',
	IN p_IdCentralizacion   		DECIMAL(18,0)	default 0,
	IN p_ConDatosAsiento   			NVARCHAR(1)		default 'N',
	IN p_Estado     				NVARCHAR(1)		default '',
	--Tabla que agrega filtros adicionales
	IN p_DatosFiltro						TABLE("IdTipoDatoEvento"				NVARCHAR(30),
												  "TipoDato"						NVARCHAR(1),
												  "ValorVarchar"					NVARCHAR(60),
												  "ValorDecimal"					DECIMAL(18,4)) default empty,
-- Parametros salida del servicio publico
	OUT p_out_TabEventos TABLE ( 
								"IdMovimiento"                  BIGINT,
								"IdEvento"                      BIGINT,
								"NombreEvento"                  NVARCHAR(60),
								"IdMoneda"						NVARCHAR(3),
								"Monto"                         DECIMAL, --(18,2),
								"CodigoMoneda"                  NVARCHAR(3),
								"FechaProceso"                  NVARCHAR(10),
								"HoraProceso"                   NVARCHAR(8),
								"EstadoEvento"                  NVARCHAR(1),
								"NombreEstadoEvento"            NVARCHAR(50),								
								"Contabilizado"                 NVARCHAR(3),
								"NumeroMovimiento"              BIGINT,
								"FechaMovimiento"               NVARCHAR(10),
								"RutMovimiento"                 NVARCHAR(11),
								"CodigoUsuario"                 NVARCHAR(40),
								"IdPreComprobante"              DECIMAL, --(18,0),
								"IdPreComprobanteAsiento"       DECIMAL, --(18,0),
								"IdTipoEvento"                  NVARCHAR(40),
								"IdTipoMovimiento"              NVARCHAR(10),
								"NombreMovimiento"              NVARCHAR(60),
								"CuentaMovimiento"              BIGINT,
								"EstadoMovimiento"              NVARCHAR(1),
								"Operador"                      NVARCHAR(10),
								"EstadoDetallePreComprobante"   NVARCHAR(1),
								"EstadoPreComprobante"          NVARCHAR(1),
								"NombreCentralizacion"          NVARCHAR(50)
                                 ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)),
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador           int,
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT);	        	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 

		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select 0 As "IdComprobante",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 As "IdComprobante", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_ResultadoProceso	    = select '' as "Campo",    0 as "Id", '' as "Servicio", '' as "Resultado" From dummy where 1=2;	
	v_RechazadosPorValidacion   = select 0 As "IdComprobante" from dummy where 1=2;

	BEGIN

		SELECT COUNT(1) INTO v_TotalRegistros FROM :p_DatosFiltro;
		
		IF :v_TotalRegistros > 0 THEN 
			v_TipoFiltro := 'COMPUESTO';
		ELSE 
			v_TipoFiltro := 'SENCILLO';
		END IF;
		
		IF v_TipoFiltro = 'SENCILLO' THEN
			p_out_TabEventos = 
				 SELECT 
				 		CTB_itzEvento."IdMovimiento" 										AS "IdMovimiento",
				 		--CTB_itzMovimiento."Numero"											AS "IdMovimiento",
						CTB_itzEvento."IdEvento"											AS "IdEvento",
						CTB_itzEventoTipo."Nombre" 											AS "NombreEvento",
						CTB_itzEvento."CodigoMoneda" 										as "IdMoneda",
						ABS(CAST(CTB_itzEvento."Monto" AS DECIMAL)) 						AS "Monto",
						CTB_itzEvento."CodigoMoneda"										AS "CodigoMoneda",
						CAST(CAST(CTB_itzEvento."FechaProceso" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaProceso", 
						CTB_itzEvento."HoraProceso"											AS "HoraProceso",
						CTB_itzEvento."Estado" 												AS "EstadoEvento",
						EstadoContabilidad."NombreEstado"									AS "NombreEstadoEvento",
						CASE WHEN (CTB_itzPreComprobante."Estado" = 'C'
						        OR CTB_itzPreComprobante."Estado" = 'V'
						       AND CTB_itzPreComprobanteAsiento."Estado" = 'V') THEN
							'CTB'
						ELSE
							''
						END 																AS "Contabilizado",
						CTB_itzMovimiento."Numero" 											AS "NumeroMovimiento",
						CAST(CAST(CTB_itzMovimiento."Fecha" AS timestamp) AS char(10)) 		AS "FechaMovimiento",
						CTB_itzMovimiento."DocumentoIdentidad" 								AS "RutMovimiento",
						CTB_itzEvento."CodigoUsuario" 										AS "CodigoUsuario",
						p_IdPreComprobante 													AS "IdPreComprobante",
						p_IdPreComprobanteAsiento 											AS "IdPreComprobanteAsiento",
						CTB_itzEvento."IdTipoEvento"										AS "IdTipoEvento",
						CTB_itzMovimientoTipo."IdTipoMovimiento"							AS "IdTipoMovimiento",
						CTB_itzMovimientoTipo."Nombre" 										AS "NombreMovimiento",
						CTB_itzMovimiento."Cuenta" 											AS "CuentaMovimiento",
						CTB_itzMovimiento."Estado" 											AS "EstadoMovimiento",
						IFNULL(CTB_itzEventoPreComprobanteAsiento."Operador", '') 			AS "Operador",
						IFNULL(CTB_itzPreComprobanteAsiento."Estado", '') 					AS "EstadoDetallePreComprobante",
						IFNULL(CTB_itzPreComprobante."Estado", '') 							AS "EstadoPreComprobante",
						CTB_itzCentralizacionTipo."Nombre" 									AS "NombreCentralizacion"
				   FROM "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento
								--* Rescata el nombre del Tipo de Evento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
				   								CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento"
								--* Rescata nombre Empresa
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS CTB_itzEmpresa ON 
				   								CTB_itzEmpresa."IdEmpresa" = CTB_itzEvento."IdEmpresa" 
								--* Se obtienen los datos del Movimiento 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON 
				   								CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
								--*Rescata el Tipo de Movimiento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo ON 
				   								CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento" 
								--* Valida el Id del PreComprobanteDetale (asiento) a traves de la relacion Evento-Detalle PreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON 
				   								CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento" 
				   							AND p_ConDatosAsiento = 'S'
								--* Se obtiene el IdPreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON 
												CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = p_IdPreComprobanteAsiento OR p_IdPreComprobanteAsiento = 0) 
				   							AND p_ConDatosAsiento = 'S'
								--* Valida el Id del PreComprobante
								LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante ON 
				   								CTB_itzPreComprobante."IdPreComprobante" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzPreComprobante."IdPreComprobante" = p_IdPreComprobante OR p_IdPreComprobante = 0) 
				   							AND (CTB_itzPreComprobante."IdTipoContabilidad" = p_IdTipoContabilidad OR p_IdTipoContabilidad = 0) 
				   							AND p_ConDatosAsiento = 'S'
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion ON 
				   								CTB_itzCentralizacion."IdCentralizacion" = CTB_itzEvento."IdCentralizacion" 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS CTB_itzCentralizacionTipo ON 
				   								CTB_itzCentralizacionTipo."IdTipoCentralizacion" = CTB_itzCentralizacion."IdTipoCentralizacion" 
				   				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  								EstadoContabilidad."IdEstadoContabilidad" = CTB_itzEvento."Estado"	 
				  WHERE CTB_itzEvento."IdEmpresa" = p_IdEmpresa  --* Condiciones mínimas para la consulta: IdEmpresa, Fecha Inicio y Fecha Final
					AND CTB_itzEvento."Monto" <> 0 
				    AND (CTB_itzMovimiento."DocumentoIdentidad" = p_RutMovimiento OR p_RutMovimiento = '        0-0') 
				    AND (CTB_itzMovimiento."Cuenta" = p_SecRutMovimiento OR p_SecRutMovimiento = 0) 
				    AND (CTB_itzCentralizacionTipo."Nombre" = p_CodigoUsuario OR p_CodigoUsuario = '') 
				    AND (CTB_itzMovimiento."IdTipoMovimiento" = p_IdTipoMovimiento OR p_IdTipoMovimiento = '') 
				    AND (CTB_itzMovimiento."Numero" = p_NumeroMovimiento OR p_NumeroMovimiento = 0) 
				    AND (CTB_itzMovimiento."Fecha" = p_FechaMovimiento OR p_FechaMovimiento = '') 
				    AND (CTB_itzEvento."FechaContable" BETWEEN p_FechaContableInicial AND p_FechaContableFinal) 
				    AND (CTB_itzEvento."IdTipoEvento" = p_IdTipoEvento OR p_IdTipoEvento = '') 
				    AND (CTB_itzEvento."IdEvento" = p_IdEvento OR p_IdEvento = 0) 
				    AND (CTB_itzEvento."IdCentralizacion" = p_IdCentralizacion OR p_IdCentralizacion = 0) 
				    AND (CTB_itzEvento."Estado" = p_Estado OR p_Estado = '') 
				ORDER BY CTB_itzEvento."FechaContable", CTB_itzEvento."IdEvento";
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

			p_out_TabEventos = 
 				 SELECT CTB_itzEvento."IdMovimiento" 										AS "IdMovimiento",
						CTB_itzEvento."IdEvento"											AS "IdEvento",
						CTB_itzEventoTipo."Nombre" 											AS "NombreEvento",
						CTB_itzEvento."CodigoMoneda" 										as "IdMoneda",
						ABS(CAST(CTB_itzEvento."Monto" AS DECIMAL)) 						AS "Monto",
						CTB_itzEvento."CodigoMoneda"										AS "CodigoMoneda",
						CAST(CAST(CTB_itzEvento."FechaProceso" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaProceso", 
						CTB_itzEvento."HoraProceso"											AS "HoraProceso",
						CTB_itzEvento."Estado" 												AS "EstadoEvento",
						EstadoContabilidad."NombreEstado"									AS "NombreEstadoEvento",
						CASE WHEN (CTB_itzPreComprobante."Estado" = 'C'
						        OR CTB_itzPreComprobante."Estado" = 'V'
						       AND CTB_itzPreComprobanteAsiento."Estado" = 'V') THEN
							'CTB'
						ELSE
							''
						END 																AS "Contabilizado",
						CTB_itzMovimiento."Numero" 											AS "NumeroMovimiento",
						CAST(CAST(CTB_itzMovimiento."Fecha" AS timestamp) AS char(10)) 		AS "FechaMovimiento",
						CTB_itzMovimiento."DocumentoIdentidad" 											AS "RutMovimiento",
						CTB_itzEvento."CodigoUsuario" 										AS "CodigoUsuario",
						p_IdPreComprobante 													AS "IdPreComprobante",
						p_IdPreComprobanteAsiento 											AS "IdPreComprobanteAsiento",
						CTB_itzEvento."IdTipoEvento"										AS "IdTipoEvento",
						CTB_itzMovimientoTipo."IdTipoMovimiento"							AS "IdTipoMovimiento",
						CTB_itzMovimientoTipo."Nombre" 										AS "NombreMovimiento",
						CTB_itzMovimiento."Cuenta" 											AS "CuentaMovimiento",
						CTB_itzMovimiento."Estado" 											AS "EstadoMovimiento",
						IFNULL(CTB_itzEventoPreComprobanteAsiento."Operador", '') 			AS "Operador",
						IFNULL(CTB_itzPreComprobanteAsiento."Estado", '') 					AS "EstadoDetallePreComprobante",
						IFNULL(CTB_itzPreComprobante."Estado", '') 							AS "EstadoPreComprobante",
						CTB_itzCentralizacionTipo."Nombre" 									AS "NombreCentralizacion"
				   FROM "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento
								--* Rescata el nombre del Tipo de Evento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
				   								CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento"
								--* Rescata nombre Empresa
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS CTB_itzEmpresa ON 
				   								CTB_itzEmpresa."IdEmpresa" = CTB_itzEvento."IdEmpresa" 
								--* Se obtienen los datos del Movimiento 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON 
				   								CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
								--*Rescata el Tipo de Movimiento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo ON 
				   								CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento" 
								--* Valida el Id del PreComprobanteDetale (asiento) a traves de la relacion Evento-Detalle PreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON 
				   								CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento" 
				   							AND p_ConDatosAsiento = 'S'
								--* Se obtiene el IdPreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON 
												CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = p_IdPreComprobanteAsiento OR p_IdPreComprobanteAsiento = 0) 
				   							AND p_ConDatosAsiento = 'S'
								--* Valida el Id del PreComprobante
								LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante ON 
				   								CTB_itzPreComprobante."IdPreComprobante" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzPreComprobante."IdPreComprobante" = p_IdPreComprobante OR p_IdPreComprobante = 0) 
				   							AND (CTB_itzPreComprobante."IdTipoContabilidad" = p_IdTipoContabilidad OR p_IdTipoContabilidad = 0) 
				   							AND p_ConDatosAsiento = 'S'
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion ON 
				   								CTB_itzCentralizacion."IdCentralizacion" = CTB_itzEvento."IdCentralizacion" 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS CTB_itzCentralizacionTipo ON 
				   								CTB_itzCentralizacionTipo."IdTipoCentralizacion" = CTB_itzCentralizacion."IdTipoCentralizacion" 
				   				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  								EstadoContabilidad."IdEstadoContabilidad" = CTB_itzEvento."Estado"
				  WHERE CTB_itzEvento."IdEmpresa" = p_IdEmpresa  --* Condiciones mínimas para la consulta: IdEmpresa, Fecha Inicio y Fecha Final
				    AND (CTB_itzMovimiento."DocumentoIdentidad" = p_RutMovimiento OR p_RutMovimiento = '        0-0') 
				    AND (CTB_itzMovimiento."Cuenta" = p_SecRutMovimiento OR p_SecRutMovimiento = 0) 
				    AND (CTB_itzCentralizacionTipo."Nombre" = p_CodigoUsuario OR p_CodigoUsuario = '') 
				    AND (CTB_itzMovimiento."IdTipoMovimiento" = p_IdTipoMovimiento OR p_IdTipoMovimiento = '') 
				    AND (CTB_itzMovimiento."Numero" = p_NumeroMovimiento OR p_NumeroMovimiento = 0) 
				    AND (CTB_itzMovimiento."Fecha" = p_FechaMovimiento OR p_FechaMovimiento = '') 
				    AND (CTB_itzEvento."FechaContable" BETWEEN p_FechaContableInicial AND p_FechaContableFinal) 
				    AND (CTB_itzEvento."IdTipoEvento" = p_IdTipoEvento OR p_IdTipoEvento = '') 
				    AND (CTB_itzEvento."IdEvento" = p_IdEvento OR p_IdEvento = 0) 
				    AND (CTB_itzEvento."IdCentralizacion" = p_IdCentralizacion OR p_IdCentralizacion = 0) 
				    AND (CTB_itzEvento."Estado" = p_Estado OR p_Estado = '') 
					AND (CTB_itzEvento."Monto" <> 0)
				    AND :v_CantRegChar = (SELECT COUNT(1)
				   						    FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
				   						 			  INNER JOIN :p_DatosFiltro AS p_DatosFiltro ON
				   						 							  p_DatosFiltro."IdTipoDatoEvento" 	= CTB_itzMovimientoDato."IdTipoDatoEvento"
				   						 						  AND p_DatosFiltro."ValorVarchar" 		= CTB_itzMovimientoDato."ValorVarchar"
				   						 						  AND p_DatosFiltro."TipoDato" 			= 'V'
				   						 						  AND p_DatosFiltro."ValorVarchar" 		<> ''
				   						   WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzMovimiento."IdMovimiento")
				   						  --+ Busca por los filtros de tipo char
			UNION ALL 
 				 SELECT CTB_itzEvento."IdMovimiento" 										AS "IdMovimiento",
						CTB_itzEvento."IdEvento"											AS "IdEvento",
						CTB_itzEventoTipo."Nombre" 											AS "NombreEvento",
						CTB_itzEvento."CodigoMoneda" 										as "IdMoneda",
						ABS(CAST(CTB_itzEvento."Monto" AS DECIMAL)) 						AS "Monto",
						CTB_itzEvento."CodigoMoneda"										AS "CodigoMoneda",
						CAST(CAST(CTB_itzEvento."FechaProceso" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaProceso", 
						CTB_itzEvento."HoraProceso"											AS "HoraProceso",
						CTB_itzEvento."Estado" 												AS "EstadoEvento",
						EstadoContabilidad."NombreEstado"									AS "NombreEstado",	
						CASE WHEN (CTB_itzPreComprobante."Estado" = 'C'
						        OR CTB_itzPreComprobante."Estado" = 'V'
						       AND CTB_itzPreComprobanteAsiento."Estado" = 'V') THEN
							'CTB'
						ELSE
							''
						END 																AS "Contabilizado",
						CTB_itzMovimiento."Numero" 											AS "NumeroMovimiento",
						CAST(CAST(CTB_itzMovimiento."Fecha" AS timestamp) AS char(10)) 		AS "FechaMovimiento",
						CTB_itzMovimiento."DocumentoIdentidad" 											AS "RutMovimiento",
						CTB_itzEvento."CodigoUsuario" 										AS "CodigoUsuario",
						p_IdPreComprobante 													AS "IdPreComprobante",
						p_IdPreComprobanteAsiento 											AS "IdPreComprobanteAsiento",
						CTB_itzEvento."IdTipoEvento"										AS "IdTipoEvento",
						CTB_itzMovimientoTipo."IdTipoMovimiento"							AS "IdTipoMovimiento",
						CTB_itzMovimientoTipo."Nombre" 										AS "NombreMovimiento",
						CTB_itzMovimiento."Cuenta" 											AS "CuentaMovimiento",
						CTB_itzMovimiento."Estado" 											AS "EstadoMovimiento",
						IFNULL(CTB_itzEventoPreComprobanteAsiento."Operador", '') 			AS "Operador",
						IFNULL(CTB_itzPreComprobanteAsiento."Estado", '') 					AS "EstadoDetallePreComprobante",
						IFNULL(CTB_itzPreComprobante."Estado", '') 							AS "EstadoPreComprobante",
						CTB_itzCentralizacionTipo."Nombre" 									AS "NombreCentralizacion"
				   FROM "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento
								--* Rescata el nombre del Tipo de Evento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
				   								CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento"
								--* Rescata nombre Empresa
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS CTB_itzEmpresa ON 
				   								CTB_itzEmpresa."IdEmpresa" = CTB_itzEvento."IdEmpresa" 
								--* Se obtienen los datos del Movimiento 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON 
				   								CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
								--*Rescata el Tipo de Movimiento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo ON 
				   								CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento" 
								--* Valida el Id del PreComprobanteDetale (asiento) a traves de la relacion Evento-Detalle PreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON 
				   								CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento" 
				   							AND p_ConDatosAsiento = 'S'
								--* Se obtiene el IdPreComprobante
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON 
												CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = p_IdPreComprobanteAsiento OR p_IdPreComprobanteAsiento = 0) 
				   							AND p_ConDatosAsiento = 'S'
								--* Valida el Id del PreComprobante
								LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante ON 
				   								CTB_itzPreComprobante."IdPreComprobante" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" 
				   							AND (CTB_itzPreComprobante."IdPreComprobante" = p_IdPreComprobante OR p_IdPreComprobante = 0) 
				   							AND (CTB_itzPreComprobante."IdTipoContabilidad" = p_IdTipoContabilidad OR p_IdTipoContabilidad = 0) 
				   							AND p_ConDatosAsiento = 'S'
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion ON 
				   								CTB_itzCentralizacion."IdCentralizacion" = CTB_itzEvento."IdCentralizacion" 
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS CTB_itzCentralizacionTipo ON 
				   								CTB_itzCentralizacionTipo."IdTipoCentralizacion" = CTB_itzCentralizacion."IdTipoCentralizacion" 
				   				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  								EstadoContabilidad."IdEstadoContabilidad" = CTB_itzEvento."Estado"
				  WHERE CTB_itzEvento."IdEmpresa" = p_IdEmpresa  --* Condiciones mínimas para la consulta: IdEmpresa, Fecha Inicio y Fecha Final
				    AND (CTB_itzMovimiento."DocumentoIdentidad" = p_RutMovimiento OR p_RutMovimiento = '        0-0') 
				    AND (CTB_itzMovimiento."Cuenta" = p_SecRutMovimiento OR p_SecRutMovimiento = 0) 
				    AND (CTB_itzCentralizacionTipo."Nombre" = p_CodigoUsuario OR p_CodigoUsuario = '') 
				    AND (CTB_itzMovimiento."IdTipoMovimiento" = p_IdTipoMovimiento OR p_IdTipoMovimiento = '') 
				    AND (CTB_itzMovimiento."Numero" = p_NumeroMovimiento OR p_NumeroMovimiento = 0) 
				    AND (CTB_itzMovimiento."Fecha" = p_FechaMovimiento OR p_FechaMovimiento = '') 
				    AND (CTB_itzEvento."FechaContable" BETWEEN p_FechaContableInicial AND p_FechaContableFinal) 
				    AND (CTB_itzEvento."IdTipoEvento" = p_IdTipoEvento OR p_IdTipoEvento = '') 
				    AND (CTB_itzEvento."IdEvento" = p_IdEvento OR p_IdEvento = 0) 
				    AND (CTB_itzEvento."IdCentralizacion" = p_IdCentralizacion OR p_IdCentralizacion = 0) 
				    AND (CTB_itzEvento."Estado" = p_Estado OR p_Estado = '') 
					AND (CTB_itzEvento."Monto" <> 0)
				    AND :v_CantRegDeci = (SELECT COUNT(1)
				   						    FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
				   						 			  INNER JOIN :p_DatosFiltro AS p_DatosFiltro ON
				   						 							  p_DatosFiltro."IdTipoDatoEvento" 	= CTB_itzMovimientoDato."IdTipoDatoEvento"
				   						 						  AND p_DatosFiltro."ValorVarchar" 		= CTB_itzMovimientoDato."ValorVarchar"
				   						 						  AND p_DatosFiltro."TipoDato" 			= 'D'
				   						 						  AND p_DatosFiltro."ValorDecimal" 		<> 0
				   						   WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzMovimiento."IdMovimiento");
										    --+ Busca por los filtros de tipo decimal
			
		END IF;		
	END;
END;