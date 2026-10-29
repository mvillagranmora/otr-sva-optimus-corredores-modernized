SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::ConsultarLibretaBanco";

CREATE PROCEDURE 
--  
--  Proposito: Calcula el SaldoSimulado de acuerdo a los movimientos y SaldoInicialSimulado
--             y retorna los movimientos de la cuenta banco para desplegarlos en la libreta banco.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 31/01/2017
--  
"BSCL.Tesoreria.BD.Servicios.BAN::ConsultarLibretaBanco" 
(
	IN  p_IdEmpresa 					BIGINT,
	IN  p_IdUsuario         			BIGINT,
	IN  p_IdModulo						NVARCHAR(3),	
	IN  p_UsaReglasNegocio 			    NVARCHAR(1),
	IN  p_IdCuentaBancariaCorredora		BIGINT,
	IN  p_IdMoneda         		  		NVARCHAR(3),
	IN  p_IdBancoCorredora 		  		BIGINT,
	IN  p_FechaConsulta 		  		NVARCHAR(10),  -- FechaAfectaSaldo
	IN  p_SaldoInicialSimulado 			BIGINT   	DEFAULT 0,
 
	OUT p_out_MovimientosCuentaBanco 	TABLE 
	(
			"IdConcepto"     	 		NVARCHAR(6),
			"NombreConcepto"    		NVARCHAR(100),
			"NombreMedioDePago"  		NVARCHAR(100),			
			"MontoIngreso"  			DECIMAL(26,8),
			"MontoEgreso"  				DECIMAL(26,8),
			"SaldoDisponible"  			DECIMAL(26,8),
			"SaldoContable"  			DECIMAL(26,8),
			"SaldoSimulado"  			DECIMAL(26,8),   -- Se calcula de acuerdo al SaldoInicialSimulado
			"SaldoFinalSimulado" 		DECIMAL(26,8),
			"NombreSocio"   			NVARCHAR(200),
			"DocumentoIdentidadSocio" 	NVARCHAR(40),
			"IdEstado"       			NVARCHAR(3),
			"NombreEstado"				NVARCHAR(100),
			"IdMoneda"       			NVARCHAR(3),
			"IdComprobante"  			BIGINT,
			"FechaAfectaSaldo"  		NVARCHAR(10),
			"FechaLiquidacion"  		NVARCHAR(10),
			"NumeroDocumentoCorredora"  BIGINT,
			"IdComprobanteFormaDePago"	BIGINT
	),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador      	INT,
	OUT p_out_AdvertenciasDetalle       	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador           	INT,
	OUT p_out_ErroresDetalle            	TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL" AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores 
	--------------------	
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_erroroutmsg 	 			NVARCHAR(5000);
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla      		NVARCHAR(100);
	DECLARE v_SaldoFinalSimulado  		TABLE ("SaldoFinalSimulado" DECIMAL(26,8));	 	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
		
	p_out_MovimientosCuentaBanco =  SELECT '' as "IdConcepto",	'' as "NombreConcepto", '' as "NombreMedioDePago",
											0  as "MontoIngreso", 0 as "MontoEgreso", 0 as "SaldoDisponible", 0 as "SaldoContable", 0 as "SaldoSimulado",
											0 as "SaldoFinalSimulado", '' as "NombreSocio", '' as "DocumentoIdentidadSocio", '' as "IdEstado", '' as "NombreEstado",
											'' as "IdMoneda", 0 as "IdComprobante", '0001-01-01' as "FechaAfectaSaldo",
											'0001-01-01' as "FechaLiquidacion", 0 as "NumeroDocumentoCorredora", 0 as "IdComprobanteFormaDePago"
									 FROM DUMMY  WHERE 1=2;
										 
	v_SaldoFinalSimulado  		 = SELECT 0 as "SaldoFinalSimulado" FROM DUMMY  WHERE 1=2;
											 		

	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		--validacion invalidante configuración básica del sistema
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalle,:v_resultadoRegla);

		p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_out_ErroresDetalle
							   UNION ALL SELECT * from :p_out_ErroresDetalle;

		IF :v_resultadoRegla = 'RECH' THEN
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
	END;
	END IF;

		 
	-------------------------
	--Negocio Aqui
	-------------------------
	BEGIN
	
		---------------------------------------------------------------
		-- Obtiene los movimientos de la cuenta bancaria consultada
		---------------------------------------------------------------
		v_MovimientosCuentaBanco = 
		SELECT 
			row_number () over (order by "FechaHoraAfectaSaldo") as "RowNum",
			"IdConcepto",
			"NombreConcepto",
			"NombreMedioDePago",
			"MontoIngreso",
			"MontoEgreso",			
			"SaldoDisponible",
			"SaldoContable",			
			"NombreSocio",
			"DocumentoIdentidadSocio",
			"IdEstado",
			"NombreEstado",
			"IdMoneda",
			"IdComprobante",
			"FechaAfectaSaldo",
			"FechaLiquidacion",
			"MontoConSigno",
			"FechaHoraAfectaSaldo",			
			"NumeroDocumentoCorredora",
			"IdComprobanteFormaDePago"
					
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria"
		WHERE "IdCuentaBancariaCorredora" = :p_IdCuentaBancariaCorredora
		AND   "IdMoneda"         		  = :p_IdMoneda
		AND   "IdBancoCorredora" 		  = :p_IdBancoCorredora
		AND   "FechaAfectaSaldo" 		  = :p_FechaConsulta
		AND   "IdEmpresa"                 = :p_IdEmpresa
		ORDER BY "FechaHoraAfectaSaldo" ASC;		
		--SELECT 'v_MovimientosCuentaBanco', * FROM :v_MovimientosCuentaBanco;   -- REVISAR


		---------------------------------------------------------------------------------------
		-- Calcula el saldo simulado de acuerdo a los movimientos y saldo inicial simulado
		---------------------------------------------------------------------------------------
		v_MovimientosCuentaBancoSaldoSimulado =
		SELECT
				"RowNum",
				"IdConcepto",
				"NombreConcepto",
				"NombreMedioDePago",
				"MontoIngreso",
				"MontoEgreso",			
				"SaldoDisponible",
				"SaldoContable",			
				"NombreSocio",
				"DocumentoIdentidadSocio",
				"IdEstado",
				"NombreEstado",
				"IdMoneda",
				"IdComprobante",
				"FechaAfectaSaldo",
				"FechaLiquidacion",
				"MontoConSigno",
				"FechaHoraAfectaSaldo",
				"NumeroDocumentoCorredora",
				"IdComprobanteFormaDePago",
				-- SaldoInicialSimulado lo suma sólo al monto del primer movimiento
				COALESCE(SUM("MontoConSigno" + (CASE WHEN ("RowNum" = 1) THEN :p_SaldoInicialSimulado ELSE 0 END)) Over (
					  						      ORDER BY "FechaHoraAfectaSaldo"
												  ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0) as "SaldoSimulado"		
		FROM :v_MovimientosCuentaBanco;		
		--SELECT 'v_MovimientosCuentaBancoSaldoSimulado', * FROM :v_MovimientosCuentaBancoSaldoSimulado;    -- REVISAR



		-----------------------------------------------------------------
		-- Calcula el Saldo simulado final
		-----------------------------------------------------------------
		v_SaldoFinalSimulado = 
		SELECT
		 	-- Corresponde al SaldoSimulado del último movimiento
			CASE
				WHEN (:p_SaldoInicialSimulado <> 0) THEN COALESCE(MCB."SaldoSimulado",0)
				ELSE 0
			END AS "SaldoFinalSimulado"
		FROM :v_MovimientosCuentaBancoSaldoSimulado AS MCB		
		INNER JOIN 
				(
					SELECT MAX("RowNum") As "RowNum"   -- obtiene el ultimo movimiento
					FROM :v_MovimientosCuentaBancoSaldoSimulado
				) AS MaxMCB 		ON (MCB."RowNum" = MaxMCB."RowNum");


		-----------------------------------------------------------------
		-- Prepara salida de movimientos cuenta banco con saldo simulado
		-----------------------------------------------------------------
		p_out_MovimientosCuentaBanco = 
		SELECT 
			MCB."IdConcepto",
			MCB."NombreConcepto",
			MCB."NombreMedioDePago",
			MCB."MontoIngreso",
			MCB."MontoEgreso",			
			MCB."SaldoDisponible",
			MCB."SaldoContable",
			CASE
				WHEN (:p_SaldoInicialSimulado <> 0) THEN MCB."SaldoSimulado"
				ELSE 0
			END AS "SaldoSimulado",			
			SSF."SaldoFinalSimulado",  -- podría ir una variable de salida aparte ya que es único para todos los registros
			MCB."NombreSocio",
			MCB."DocumentoIdentidadSocio",
			MCB."IdEstado",
			MCB."NombreEstado",
			MCB."IdMoneda",
			MCB."IdComprobante",
			MCB."FechaAfectaSaldo",
			MCB."FechaLiquidacion",
			COALESCE(MCB."NumeroDocumentoCorredora", 0) AS "NumeroDocumentoCorredora",
			"IdComprobanteFormaDePago"
		FROM :v_MovimientosCuentaBancoSaldoSimulado AS MCB		
		LEFT JOIN :v_SaldoFinalSimulado AS SSF ON (1=1);
		

	END;
END;



