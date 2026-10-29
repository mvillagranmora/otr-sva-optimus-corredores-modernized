SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente";
GO
CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de una cuentas de inversion pero por IdCliente (se replica SP BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion)  
--  Autor: PRONET IT 
--  Fecha Creacion: 28/12/2017  
-- 
"BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente" ( 
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,
    IN  p_IdUsuario                     BIGINT, 
    IN  p_IdModulo                      NVARCHAR(3),        
	IN  p_UsaReglasNegocio	 		    NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
        
    -- Parametros entrada del servicio publico
    IN  p_TipoAgrupador					NVARCHAR(2), -- PL: Plan Inversion, PR: Producto, RE: Regimen, PO: Portafolio  
    IN  p_RegimenTributario				NVARCHAR(10) DEFAULT NULL, 
	IN  p_IdProductoAgrupador			BIGINT DEFAULT NULL,
	IN  p_IdPlanTipoInversion		    BIGINT DEFAULT NULL, 

	IN  p_CuentaInversionAConsultar		TABLE	("IdEjecutivo"			BIGINT,
												 "IdCliente"			BIGINT,
											  	 "IdPortafolio"			BIGINT,
											  	 "IdMoneda"				NVARCHAR(3)),
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
    IN  p_SoloSaldoNegativo             NVARCHAR(1) 	DEFAULT 'N', --S: Trae solo saldos negativos, N: Trae saldos positivos o negativos
	IN  p_SoloSaldoDisponible           NVARCHAR(1) 	DEFAULT 'N', --S: Trae solo saldos disponible (mayor a cero), N: Trae saldos positivos o negativos    
    IN  p_IncluyeCargosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye cargos certificados, S:Incluye cargos pendientes y certificados
    IN  p_IncluyeAbonosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye abonos certificados, S:Incluye abonos pendientes y certificados
	IN	p_IncluyeOtrosSaldos			NVARCHAR(1)		DEFAULT 'N', --N: Solo saldo CTA, S: Incluye saldo CUS y saldo TES (solo para vistas analyticas)
	                                           	 
	OUT p_out_SaldoPorFechaMovimiento	TABLE	("CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"				BIGINT,
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "NombrePlan"				NVARCHAR(10),
                                              	 "IdPlanTipoInversion"		BIGINT,
	                							 "IdProductoAgrupador"		BIGINT,
	                							 "Producto"					NVARCHAR(40),
	                							 "IdRegimenTributario"		BIGINT,
	                							 "Regimen"					NVARCHAR(50),
	                							 "NombreAgrupador"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(28,8),
                                              	 "SaldoEnGarantia"			DECIMAL(28,8),
                                              	 "SaldoAPlazo"				DECIMAL(28,8),
                                              	 "SaldoRetiro"				DECIMAL(28,8),
                                              	 "IdEstado"					NVARCHAR(3), --Tabla con proyección de Saldo
		                                      	 "Instrumento"				NVARCHAR(40),
		                                      	 "CantidadCuotas"			DECIMAL(26,8),
		                                      	 "ValorCuota"				DECIMAL(26,8),
		                                      	 "Porcentaje"				BIGINT,
		                                      	 "UFTraspaso"				DECIMAL(26,8)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
 
	--------------------
	--DECLARE Manejo de Errores 
	--------------------	
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;
	DECLARE v_errorExt	 				NVARCHAR(40);
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_errorhashmsg 				TABLE ("Codigo" nvarchar(100),"Valor" nvarchar(5000));					   
	DECLARE v_SaldoEnGarantia			TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
	DECLARE v_SaldoAPlazo				TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));							   
	DECLARE v_SaldoTesoreria			TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
	DECLARE v_ejecutivoConsultado 		BIGINT;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	p_out_InformacionContador		:= 0;
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_InformacionDetalle		= SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy WHERE 1=2;
	/*
	p_out_SaldoPorFechaEfectiva  	= SELECT  0 as "IdCuentaInversion",'' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
											 '' as "DocumentoIdentidad",0 as "IdPortafolio",'' as "NombrePortafolio",'0001-01-01' as "FechaSaldo",
											 '' as "IdMoneda",'' as "CodigoMoneda",'' as "NombreMoneda", 0 as "SaldoDisponible",
											  0 as "SaldoEnGarantia",0 as "SaldoAPlazo",0 as "SaldoRetiro" FROM dummy WHERE 1=2;
	*/											 
	p_out_SaldoPorFechaMovimiento  	= SELECT '' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
											 '' as "DocumentoIdentidad",0 AS "IdPortafolio", '' as "NombrePortafolio", '' AS "NombrePlan",
	                						  0 AS "IdPlanTipoInversion",0 AS "IdProductoAgrupador",'' AS "Producto",0 AS "IdRegimenTributario",
	                						 '' AS "Regimen",'' AS "NombreAgrupador",'0001-01-01' as "FechaSaldo",
											 '' as "IdMoneda",'' as "CodigoMoneda",'' as "NombreMoneda", 0 as "SaldoDisponible", 
											  0 as "SaldoEnGarantia",0 as "SaldoAPlazo",0 as "SaldoRetiro",'' as "IdEstado",
											 '' AS "Instrumento", 0 AS "CantidadCuotas", 0 AS "ValorCuota", 0 AS "Porcentaje", 0 AS "UFTraspaso"  FROM dummy WHERE 1=2;
	/*											  
	p_out_SaldoInicialFinal			= SELECT 0 as "IdCuentaInversion",0 as "SaldoEfectivoInicial",0 as "SaldoEfectivoFinal",0 as "SaldoMovimientoInicial",
    										 0 as "SaldoMovimientoFinal" FROM dummy WHERE 1=2;
	*/    										     										 
	v_SaldoEnGarantia				= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
	v_SaldoAPlazo					= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
	v_SaldoTesoreria				= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;

	v_CuentaInversionAConsultar =
	 SELECT ROW_NUMBER() over (ORDER BY "IdEjecutivo","IdPortafolio","IdMoneda") as "IdInterno",
	 		CASE WHEN COALESCE(T."IdEjecutivo",0) = 0 THEN E."IdEmpleado" ELSE T."IdEjecutivo" END as "IdEjecutivo",
	 		"IdCliente",
			"IdPortafolio",
			"IdMoneda"
	   FROM (SELECT DISTINCT
			 		"IdEjecutivo",
			 		"IdCliente",
				   	"IdPortafolio",
					"IdMoneda"
			   FROM :p_CuentaInversionAConsultar
			) as T,
			(SELECT E."IdEmpleado"
			   FROM "BSCL.SocioNegocio.BD::EMP.Empleado" as E
			  INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdEntidad" = E."IdEmpleado"
			  WHERE U."TipoEntidad" = 'EMP'
			    AND "IdUsuario" = :p_IdUsuario
			) as E;
	
	IF :p_UsaReglasNegocio = 'S' THEN
	BEGIN
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);		
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		--FALTA AGREGAR VALIDACIONES
		CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarSaldoCuentaInversion"(
			 p_IdEmpresa					=>	:p_IdEmpresa
			,p_FechaDesde					=>	:p_FechaDesde
			,p_FechaHasta					=>	:p_FechaHasta
			,p_CuentaInversionAConsultar	=>	:v_CuentaInversionAConsultar
			,p_out_MovimientosRech			=>	:v_RechazadosPorValidacion
			,p_out_ErroresDetalle			=>	:v_ErroresDetalle
			,p_out_resultadoRegla			=>	:v_resultadoRegla
		);

		IF :v_resultadoRegla = 'RECH' THEN
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA;
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere

			v_CuentaInversionAConsultar = 
			 SELECT "IdInterno","IdEjecutivo","IdCliente","IdPortafolio","IdMoneda"
			   FROM :v_CuentaInversionAConsultar 
			  WHERE "IdInterno" NOT IN (SELECT "IdInterno" FROM :v_RechazadosPorValidacion);

			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END;
	END IF;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		DECLARE v_DiasProyeccion 	BIGINT;
		DECLARE v_KeyFlujoCustodia	BIGINT;
		DECLARE v_IdEmpleado		BIGINT;
		DECLARE v_FechaSistema		NVARCHAR(10);
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299		
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		SELECT "Valor" INTO v_FechaSistema 
		  FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
		 WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
		
		SELECT :p_SoloSaldoNegativo, :p_SoloSaldoDisponible FROM DUMMY;
		
		/*
		--Recupera los portafoios visibles al empleado
		SELECT DISTINCT "IdEjecutivo" INTO v_IdEmpleado FROM :v_CuentaInversionAConsultar;
		CALL "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI" (
			p_IdEmpresa			=> :p_IdEmpresa,
			p_IdUsuario			=> :p_IdUsuario,
			p_IdModulo			=> :p_IdModulo,
			p_IdEmpleado		=> :v_IdEmpleado,
			p_out_Portafolios	=> v_Portafolios
		);
		*/
		v_Portafolios = SELECT Portafolio.*,PortafolioCliente."IdMatrizAgrupadorAPV"
						FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS Portafolio
						INNER JOIN :v_CuentaInversionAConsultar AS CuentaInversionAConsultar
								ON CuentaInversionAConsultar."IdCliente" = Portafolio."IdCliente"
						INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioCliente
								ON PortafolioCliente."IdCliente" = Portafolio."IdCliente"
							   AND PortafolioCliente."IdPortafolio" = Portafolio."IdPortafolio"
							   AND PortafolioCliente."Proposito" = Portafolio."Proposito"
						WHERE Portafolio."Proposito" = 'PROPOSITOAPV';
		
		
		SELECT top 1 "IdEjecutivo" INTO v_ejecutivoConsultado FROM :p_CuentaInversionAConsultar;
		
		v_InfoCuentasDeInversion =
		 SELECT DISTINCT		 		 
		 		 PORV."IdEmpleadoEjecutivo" as "IdEjecutivo"
		 		,CLI."IdPersona"
		 		,CTAINV."IdCuentaInversion"
		 		,CTAINV."IdCliente"
		 		,CLI."DocumentoIdentidad"
		 		,CLI."NombreCompleto" as "NombreCliente"
				,CTAINV."IdPortafolio"
				,POR."CodigoPropositoPortafolio" as "NombrePortafolio"
				,CTAINV."IdMoneda"
				,Mon."Codigo" as "CodigoMoneda"
				,(Mon."Codigo" ||' - '|| Mon."Nombre") as "NombreMoneda"
				,CTAINV."CuentaInversionPrincipal"
				,PORV."IdMatrizAgrupadorAPV"
				,POR."IdEstado"
		   FROM :v_CuentaInversionAConsultar AS CINV
		  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV 
		 		  ON (CINV."IdPortafolio" 	IS NULL OR CTAINV."IdPortafolio" = CINV."IdPortafolio")
		 		 AND (CINV."IdMoneda" 		IS NULL OR CTAINV."IdMoneda" = CINV."IdMoneda")
		  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR 
		  		  ON POR."IdPortafolio" 	= CTAINV."IdPortafolio"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
		  		  ON CLI."IdCliente" 		= CTAINV."IdCliente"
		  INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon
		  		  ON Mon."IdMoneda" 		= CTAINV."IdMoneda"
		  INNER JOIN :v_Portafolios as PORV
		  		  ON PORV."IdPortafolio"	= POR."IdPortafolio"; 
		  		 /*
		  		  AND PORV."IdEmpleadoEjecutivo" = CASE WHEN IFNULL(v_ejecutivoConsultado,-98989) = -98989 THEN PORV."IdEmpleadoEjecutivo" ELSE v_IdEmpleado END
		   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" as EPOR
		  		  ON EPOR."IdEmpleadoEjecutivo"	= PORV."IdEmpleadoEjecutivo";
		 		*/
		IF NOT IS_EMPTY(:v_InfoCuentasDeInversion) THEN
			--1) Consulta Flujo Cuenta Inversion
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar"
			 SELECT DISTINCT "IdPortafolio", "IdMoneda" 
			   FROM :v_InfoCuentasDeInversion;
								
			CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversion" (
				 p_IdEmpresa					=> p_IdEmpresa
				,p_FechaDesde					=> p_FechaDesde
			    ,p_FechaHasta					=> p_FechaHasta
			    ,p_SoloSaldoNegativo			=> p_SoloSaldoNegativo
			    ,p_SoloSaldoDisponible			=> p_SoloSaldoDisponible
			);

			IF :p_IncluyeOtrosSaldos = 'S' THEN
				
				--2) Consulta operaciones a Plazo
				v_SaldoAPlazo = 
				WITH
					OperacionesAPlazo as (
						SELECT   CAR."IdCliente"
								,CAR."IdPortafolio"								
								,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"								
								,CASE WHEN CAR."IdTipoOperacion" IN ('EMPRES') THEN -1*CAR."INGMontoATasaCompraActual" 
									  ELSE CAR."INGMontoATasaCompraActual" 
								  END AS "Monto"
								,:p_FechaDesde AS "FechaSaldo"							
						  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" as CAR
						 INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdCliente" 		= CAR."IdCliente"
						 		AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						 		AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
						 WHERE "IdTipoOperacion" 			IN ('PRESTA','EMPRES')
						   AND CAR."INGFechaLiquidacion" 	>= :p_FechaDesde
						   AND CAR."IdEmpresa" 				= :p_IdEmpresa
						   AND CAR."IdEstado"				= 'VIG'
					),
					OperacionesAPlazoSimultaneas as (
						SELECT   CAR."IdCliente"
								,CAR."IdPortafolio"								
								,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
								,CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM') THEN -1*CAR."INGMontoAPlazo" 
									  ELSE CAR."INGMontoAPlazo" 
								  END AS "Monto"
								,:p_FechaDesde AS "FechaSaldo"							
						  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" as CAR
						 INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdCliente" 		= CAR."IdCliente"
						 		AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						 		AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
						 WHERE "IdTipoOperacion" 					IN ('COMSIM','VENSIM')
						   AND CAR."FechaVencimientoInstrumento" 	>= :p_FechaDesde
						   AND CAR."IdEmpresa" 						= :p_IdEmpresa
						   AND CAR."IdEstado"						= 'VIG'
					),
					OperacionesAPlazoPactos as (
						SELECT   CAR."IdCliente"
								,CAR."IdPortafolio"								
								,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
								,CASE WHEN CAR."IdTipoOperacion" IN ('COMPAC') THEN -1*CAR."INGMontoATasaCompraActual" 
									  ELSE CAR."INGMontoATasaCompraActual" 
								  END AS "Monto"
								,:p_FechaDesde AS "FechaSaldo"							
						  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" as CAR
						 INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdCliente" 		= CAR."IdCliente"
						 		AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						 		AND CI."IdMoneda" 		= CAR."IdTipoReajuste"
						 WHERE "IdTipoOperacion" 			IN ('COMPAC','VENPAC')
						   AND CAR."INGFechaLiquidacion" 	>= :p_FechaDesde
						   AND CAR."IdEmpresa" 				= :p_IdEmpresa
						   AND CAR."IdEstado"				= 'VIG'
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
				  FROM (				  		
				  		SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
				  		  FROM OperacionesAPlazo
				  		 UNION ALL
				  		SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
				  		  FROM OperacionesAPlazoSimultaneas
				  		 UNION ALL
				  		SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
				  		  FROM OperacionesAPlazoPactos
				  	   )
				 GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";				 
				
				
				--3) Consultar Garantias
				v_SaldoEnGarantia = 
				WITH
					Garantias as (
						SELECT   GAR."IdCliente"
								,GAR."IdPortafolio"								
								,GAR."IdMonedaInstrumento" AS "IdMoneda"
								,GAR."INGCantidad" AS "Monto"
								,:p_FechaDesde AS "FechaSaldo"
						  FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" as GAR
						 INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdCliente" 		= GAR."IdCliente"
						 		AND CI."IdPortafolio"	= GAR."IdPortafolio"
						 		AND CI."IdMoneda"		= GAR."IdMonedaInstrumento"
						 WHERE GAR."IdEstado" 			= 'VIG'
						   AND GAR."IdOrigenGarantia" 	= 'CTA'
						   AND GAR."IdEmpresa" 			= :p_IdEmpresa
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
				  FROM Garantias
				 GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
				
				
				--4) Ingresos y Egresos
				v_SaldoTesoreria = 
				WITH 
					MovimientosTesoreria as (
						SELECT   0 as "IdCliente"
								,IE."IdPortafolioTitularPago" as "IdPortafolio"
								,IE."IdMoneda"
								,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "Monto"
								,:p_FechaDesde AS "FechaSaldo"
						  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS IE
						  INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdPortafolio" 	= IE."IdPortafolioTitularPago"
						 		AND CI."IdMoneda"		= IE."IdMoneda"
						 WHERE IE."IdEstado" 			= 'ING'
						   AND IE."FechaLiquidacion"	>= :p_FechaDesde
						   AND IE."IdEmpresa" 			= :p_IdEmpresa
				   ),
				   ComprobanteTesoreria as (
				   		SELECT   0 as "IdCliente"
								,COM."IdPortafolioTitularPago" as "IdPortafolio"
								,:p_FechaDesde AS "FechaSaldo"
								,COM."IdMoneda"
								,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "Monto"
						  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM
						  INNER JOIN :v_InfoCuentasDeInversion as CI 
						 		 ON CI."IdPortafolio" 	= COM."IdPortafolioTitularPago"
						 		AND CI."IdMoneda"		= COM."IdMoneda"
						 WHERE COM."IdEstado" 			NOT IN ('ANU','MOD','LIQ')
						   AND COM."FechaLiquidacion"	>= :p_FechaDesde
						   AND COM."IdEmpresa" 			= :p_IdEmpresa
				   )
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
				  FROM (
				  			SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
				  			  FROM MovimientosTesoreria
				  			 UNION ALL
				  			SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
				  			  FROM ComprobanteTesoreria
				  	   )
				 GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
			END IF;
			
			
			--4) Procesar datos salida final
			/*
			p_out_SaldoInicialFinal =
			 SELECT  "IdCuentaInversion"
	    			,"SaldoEfectivoInicial"
	    			,"SaldoEfectivoFinal"
	    			,"SaldoMovimientoInicial"
	    			,"SaldoMovimientoFinal"
			   FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoInicialFinal";
					
			*/
			   
			v_out_SaldoPorFechaMovimiento =
			SELECT   SPM."CodigoCuentaInversion"
	                ,SPM."IdCliente"
	                ,CAST(InfCtaInv."NombreCliente" as NVARCHAR(300)) as "NombreCliente"
	                ,InfCtaInv."DocumentoIdentidad"
	                ,SPM."IdPortafolio"
	                ,CAST(InfCtaInv."NombrePortafolio" as NVARCHAR(100)) as "NombrePortafolio"
	                ,MatrizAgrupador."NombrePlan"
	                ,MatrizAgrupador."IdPlanTipoInversion"
	                ,MatrizAgrupador."IdProductoAgrupador"
	                ,MatrizAgrupador."Producto"
	                ,MatrizAgrupador."IdRegimenTributario"
	                ,MatrizAgrupador."Codigo"					AS "Regimen"
	                ,MatrizAgrupador."NombreAgrupador"
	                ,SPM."FechaSaldo"
	                ,SPM."IdMoneda"
	                ,InfCtaInv."CodigoMoneda"
	                ,CAST(InfCtaInv."NombreMoneda" as NVARCHAR(100)) as "NombreMoneda"
	                ,SPM."Saldo" as "SaldoDisponible"	                
	                ,CAST(COALESCE(sldGar."Monto",0) as DECIMAL(26,8)) as "SaldoEnGarantia"
	                ,CAST(COALESCE(sldPlz."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazo"
	                ,CAST(COALESCE(SldTes."Monto",0) as DECIMAL(26,8)) as "SaldoRetiro"
	                ,InfCtaInv."IdEstado"
			FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaMovimiento" as SPM
			INNER JOIN :v_InfoCuentasDeInversion AS InfCtaInv ON (SPM."IdCuentaInversion" = InfCtaInv."IdCuentaInversion")
			LEFT JOIN :v_SaldoEnGarantia AS sldGar
			   		  ON sldGar."IdCliente" 		= SPM."IdCliente"
			   		 AND sldGar."IdPortafolio" 		= SPM."IdPortafolio"
			   		 AND sldGar."IdMoneda" 			= SPM."IdMoneda"
			   		 AND sldGar."FechaSaldo" 		= SPM."FechaSaldo"
			LEFT JOIN :v_SaldoAPlazo as sldPlz
			   		  ON sldPlz."IdCliente" 		= SPM."IdCliente"
			   		 AND sldPlz."IdPortafolio" 		= SPM."IdPortafolio"
			   		 AND sldPlz."IdMoneda" 			= SPM."IdMoneda"
			   		 AND sldPlz."FechaSaldo" 		= SPM."FechaSaldo"
			LEFT JOIN :v_SaldoTesoreria as SldTes
			   		  ON SldTes."IdPortafolio"		= SPM."IdPortafolio"
			   		 AND SldTes."IdMoneda"			= SPM."IdMoneda"
			   		 AND SldTes."FechaSaldo"		= SPM."FechaSaldo"
			LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS MatrizAgrupador
				   ON MatrizAgrupador."IdMatrizAgrupadorAPV" = InfCtaInv."IdMatrizAgrupadorAPV";
			
				   
			IF (:p_TipoAgrupador = 'PL') THEN
				SaldoPorFechaMovimiento = SELECT "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,0 AS "IdPortafolio"
									                ,'' AS "NombrePortafolio"
									                ,"NombrePlan"
									                ,"IdPlanTipoInversion"
									                ,0 AS "IdProductoAgrupador"
									                ,'' AS "Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,'' AS "NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,"SaldoDisponible"               
									                ,"SaldoEnGarantia"
									                ,"SaldoAPlazo"
									                ,"SaldoRetiro"
									                ,"IdEstado"
												FROM :v_out_SaldoPorFechaMovimiento;
			END IF;
			
			IF (:p_TipoAgrupador = 'PR') THEN
				SaldoPorFechaMovimiento = SELECT "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,0 AS "IdPortafolio"
									                ,'' AS "NombrePortafolio"
									                ,'' AS "NombrePlan"
									                ,0 AS "IdPlanTipoInversion"
									                ,"IdProductoAgrupador"
									                ,"Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,'' AS "NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,"SaldoDisponible"               
									                ,"SaldoEnGarantia"
									                ,"SaldoAPlazo"
									                ,"SaldoRetiro"
									                ,"IdEstado"
												FROM :v_out_SaldoPorFechaMovimiento;
			END IF;
			/*
			IF (:p_TipoAgrupador = 'RE') THEN
				SaldoPorFechaMovimiento = SELECT "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,0 AS "IdPortafolio"
									                ,'' AS "NombrePortafolio"
									                ,'' AS "NombrePlan"
									                ,0 AS "IdPlanTipoInversion"
									                ,0 AS "IdProductoAgrupador"
									                ,'' AS "Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,'' AS "NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,"SaldoDisponible"               
									                ,"SaldoEnGarantia"
									                ,"SaldoAPlazo"
									                ,"SaldoRetiro"
												FROM :v_out_SaldoPorFechaMovimiento;
			END IF;
			*/
			IF (:p_TipoAgrupador = 'PO' OR :p_TipoAgrupador = 'RE') THEN
				SaldoPorFechaMovimiento = SELECT "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,"IdPortafolio"
									                ,"NombrePortafolio"
									                ,"NombrePlan"
									                ,"IdPlanTipoInversion"
									                ,"IdProductoAgrupador"
									                ,"Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,"NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,"SaldoDisponible"               
									                ,"SaldoEnGarantia"
									                ,"SaldoAPlazo"
									                ,"SaldoRetiro"
									                ,"IdEstado"
												FROM :v_out_SaldoPorFechaMovimiento
												WHERE (:p_RegimenTributario IS NULL OR "Regimen" = :p_RegimenTributario ) 
												AND (:p_IdProductoAgrupador IS NULL OR "IdProductoAgrupador" = :p_IdProductoAgrupador) 
												AND (:p_IdPlanTipoInversion IS NULL OR "IdPlanTipoInversion" = :p_IdPlanTipoInversion);
			END IF;
			
			p_out_SaldoPorFechaMovimiento = SELECT "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,"IdPortafolio"
									                ,"NombrePortafolio"
									                ,"NombrePlan"
									                ,"IdPlanTipoInversion"
									                ,"IdProductoAgrupador"
									                ,"Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,"NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,SUM("SaldoDisponible")	AS "SaldoDisponible"               
									                ,SUM("SaldoEnGarantia")	AS "SaldoEnGarantia"
									                ,SUM("SaldoAPlazo")		AS "SaldoAPlazo"
									                ,SUM("SaldoRetiro")		AS "SaldoRetiro"
									                ,"IdEstado"
									                ,'' 					AS "Instrumento"
									                ,0 						AS "CantidadCuotas"
									                ,0 						AS "ValorCuota"
									                ,0 						AS "Porcentaje"
									                ,0						AS "UFTraspaso"
									        FROM :SaldoPorFechaMovimiento
									        GROUP BY "CodigoCuentaInversion"
									                ,"IdCliente"
									                ,"NombreCliente"
									                ,"DocumentoIdentidad"
									                ,"IdPortafolio"
									                ,"NombrePortafolio"
									                ,"NombrePlan"
									                ,"IdPlanTipoInversion"
									                ,"IdProductoAgrupador"
									                ,"Producto"
									                ,"IdRegimenTributario"
									                ,"Regimen"
									                ,"NombreAgrupador"
									                ,"FechaSaldo"
									                ,"IdMoneda"
									                ,"CodigoMoneda"
									                ,"NombreMoneda"
									                ,"IdEstado";
			/*			
			p_out_SaldoPorFechaEfectiva =
			 SELECT  SPE."IdCuentaInversion"
	        		,SPE."CodigoCuentaInversion"
	                ,SPE."IdCliente"
	                ,CAST(InfCtaInv."NombreCliente" as NVARCHAR(300)) as "NombreCliente"
	                ,InfCtaInv."DocumentoIdentidad"
	                ,SPE."IdPortafolio"
	                ,CAST(InfCtaInv."NombrePortafolio" as NVARCHAR(100)) as "NombrePortafolio"
	                ,SPE."FechaSaldo"
	                ,SPE."IdMoneda"
	                ,InfCtaInv."CodigoMoneda"
	                ,CAST(InfCtaInv."NombreMoneda" as NVARCHAR(100)) as "NombreMoneda"               
	                ,SPE."Saldo" as "SaldoDisponible"
	                ,CAST(COALESCE(sldGar."Monto",0) as DECIMAL(26,8)) as "SaldoEnGarantia"
	                ,CAST(COALESCE(sldPlz."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazo"	                
					,CAST(COALESCE(SldTes."Monto",0) as DECIMAL(26,8)) as "SaldoRetiro"               		
			   FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaEfectiva" as SPE
			  INNER JOIN :v_InfoCuentasDeInversion as InfCtaInv 
			  		  ON SPE."IdCuentaInversion" = InfCtaInv."IdCuentaInversion"
			   LEFT JOIN :v_SaldoEnGarantia as sldGar
			   		  ON sldGar."IdCliente" 		= SPE."IdCliente"
			   		 AND sldGar."IdPortafolio" 		= SPE."IdPortafolio"
			   		 AND sldGar."IdMoneda" 			= SPE."IdMoneda"
			   		 AND sldGar."FechaSaldo" 		= SPE."FechaSaldo"
			   LEFT JOIN :v_SaldoAPlazo as sldPlz
			   		  ON sldPlz."IdCliente" 		= SPE."IdCliente"
			   		 AND sldPlz."IdPortafolio" 		= SPE."IdPortafolio"
			   		 AND sldPlz."IdMoneda" 			= SPE."IdMoneda"
			   		 AND sldPlz."FechaSaldo" 		= SPE."FechaSaldo"
			   LEFT JOIN :v_SaldoTesoreria as SldTes
			   		  ON SldTes."IdPortafolio"		= SPE."IdPortafolio"
			   		 AND SldTes."IdMoneda"			= SPE."IdMoneda"
			   		 AND SldTes."FechaSaldo"		= SPE."FechaSaldo"
			  ORDER BY SPE."IdPortafolio",InfCtaInv."CuentaInversionPrincipal" desc,SPE."IdMoneda",SPE."FechaSaldo";		
			 */	 
		END IF;
	END;
END;
