SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion";
GO

CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de una cuentas de inversion
--  Autor: - BCS 
--  Fecha Creacion: 20/08/2015  
-- 
"BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,
    IN  p_IdUsuario                     BIGINT, 
    IN  p_IdModulo                      NVARCHAR(3),        
	IN  p_UsaReglasNegocio	 		    NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
    -- Parametros entrada del servicio publico
	IN  p_CuentaInversionAConsultar		TABLE	("IdEjecutivo"			BIGINT,
											  	 "IdPortafolio"			BIGINT,
											  	 "IdMoneda"				NVARCHAR(3)),
	
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
    IN  p_SoloSaldoNegativo             NVARCHAR(1)		DEFAULT 'N', --S: Trae solo saldos negativos, N: Trae saldos positivos o negativos
	IN  p_SoloSaldoDisponible           NVARCHAR(1) 	DEFAULT 'N', --S: Trae solo saldos disponible (mayor a cero), N: Trae saldos positivos o negativos    
    IN  p_IncluyeCargosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye cargos certificados, S:Incluye cargos pendientes y certificados
    IN  p_IncluyeAbonosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye abonos certificados, S:Incluye abonos pendientes y certificados
	IN	p_IncluyeOtrosSaldos			NVARCHAR(1)		DEFAULT 'N', --N: Solo saldo CTA, S: Incluye otros saldos(A PLazo, IE, Garantia) 
	

	-- Estos parámetros se usan solo para APV
	IN	p_IncluyeProductoApv			NVARCHAR(1)		DEFAULT 'N',   -- N: No incluye los cambios de Producto APV, S: Incluye los cambios de Producto APV										  	 
	IN  p_IdEjecutivo					BIGINT			DEFAULT NULL,  -- Solo para APV
	IN  p_IdCliente						BIGINT			DEFAULT NULL,  -- Solo para APV
	IN  p_IdPortafolio					BIGINT			DEFAULT NULL,  -- Solo para APV
	IN  p_IdProductoAgrupador			BIGINT			DEFAULT NULL,  -- Solo para APV
	IN  p_IdMoneda						NVARCHAR(3)		DEFAULT NULL,  -- Solo para APV



    -- Parametros salida del servicio publico
    OUT p_out_SaldoInicialFinal			 TABLE	("IdCuentaInversion"      	BIGINT,
    										  	 "SaldoEfectivoInicial"		DECIMAL(26,8),
    										  	 "SaldoEfectivoFinal"		DECIMAL(26,8),
    										  	 "SaldoMovimientoInicial"	DECIMAL(26,8),
    										  	 "SaldoMovimientoFinal"		DECIMAL(26,8)),
	
    OUT p_out_SaldoPorFechaEfectiva      TABLE	("IdEjecutivo"				BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
    											 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,  
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazoSimultanea"	DECIMAL(26,8),
                                              	 "SaldoAPlazoVentaCorta"	DECIMAL(26,8),
                                              	 "SaldoAPlazoCompromiso"	DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100)),  --Tabla con proyección de Saldo 
	
	OUT p_out_SaldoPorFechaMovimiento	TABLE	("IdEjecutivo"				BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
												 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "PropositoPortafolio"		NVARCHAR(100),
                                              	 "CodigoPortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazoSimultanea"	DECIMAL(26,8),
                                              	 "SaldoAPlazoVentaCorta"	DECIMAL(26,8),
                                              	 "SaldoAPlazoCompromiso"	DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100)), --Tabla con proyección de Saldo
	
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
	DECLARE v_SaldoAPlazo_Simultaneas	TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
	DECLARE v_SaldoAPlazo_PrestamosEmprestitos	TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
	DECLARE v_SaldoAPlazo_Compromisos	TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
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

	p_out_SaldoPorFechaEfectiva  	= SELECT  0 as "IdEjecutivo",'' as "NombreEjecutivo",'' as "CodigoSucursal",'' as "NombreSucursal",
											  0 as "IdCuentaInversion",'' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
											 '' as "DocumentoIdentidad",0 as "IdPortafolio",'' as "NombrePortafolio",'0001-01-01' as "FechaSaldo",
											 '' as "IdMoneda",'' as "CodigoMoneda",'' as "NombreMoneda", 0 as "SaldoDisponible",
											  0 as "SaldoEnGarantia",0 as "SaldoAPlazoSimultanea",0 as "SaldoAPlazoVentaCorta",0 as "SaldoAPlazoCompromiso",0 as "SaldoRetiro", '' as "Producto" FROM dummy WHERE 1=2;
											  
	p_out_SaldoPorFechaMovimiento  	= SELECT  0 as "IdEjecutivo",'' as "NombreEjecutivo",'' as "CodigoSucursal",'' as "NombreSucursal",
											  0 as "IdCuentaInversion",'' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
											 '' as "DocumentoIdentidad",0 as "IdPortafolio",'' as "NombrePortafolio",'' AS "PropositoPortafolio", '' AS "CodigoPortafolio",
											 '0001-01-01' as "FechaSaldo", '' as "IdMoneda",'' as "CodigoMoneda",'' as "NombreMoneda", 0 as "SaldoDisponible", 
											  0 as "SaldoEnGarantia",0 as "SaldoAPlazoSimultanea",0 as "SaldoAPlazoVentaCorta",0 as "SaldoAPlazoCompromiso",0 as "SaldoRetiro", '' as "Producto" FROM dummy WHERE 1=2;											 
	p_out_SaldoInicialFinal			= SELECT 0 as "IdCuentaInversion",0 as "SaldoEfectivoInicial",0 as "SaldoEfectivoFinal",0 as "SaldoMovimientoInicial",
    										 0 as "SaldoMovimientoFinal" FROM dummy WHERE 1=2;    										 
	v_SaldoEnGarantia				= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;	
	v_SaldoAPlazo_Compromisos		= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
	v_SaldoAPlazo_Simultaneas       = SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;	
	v_SaldoAPlazo_PrestamosEmprestitos  		= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
	v_SaldoTesoreria				= SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
	
	
	IF :p_IncluyeProductoApv = 'N' THEN	
	
		
		-- OBTIENE DATOS DE LAS CUENTAS INVERSION A CONSULTAR
		------------------------------------------------------------

		v_CuentaInversionAConsultar =
		SELECT
			ROW_NUMBER() OVER (ORDER BY "IdEjecutivo","IdPortafolio","IdMoneda") AS "IdInterno"
		   ,CASE WHEN COALESCE(T."IdEjecutivo",0) = 0 THEN E."IdEmpleado" ELSE T."IdEjecutivo" END AS "IdEjecutivo"
		   ,"IdPortafolio"
		   ,"IdMoneda"
		FROM
		(
			SELECT DISTINCT
				"IdEjecutivo"
			   ,"IdPortafolio"
			   ,"IdMoneda"
			FROM :p_CuentaInversionAConsultar
		) AS T,
		(
			SELECT
				E."IdEmpleado"
			FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS E
			INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS U ON U."IdEntidad" = E."IdEmpleado"
			WHERE
				U."TipoEntidad" = 'EMP'
			AND "IdUsuario" = :p_IdUsuario
		) AS E;
	

		
		IF :p_UsaReglasNegocio = 'S' THEN
			BEGIN
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
				
				IF :v_resultadoRegla = 'RECH' THEN
					 SIGNAL SALIDA_CONTROLADA; 	
				END IF;
				
				
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
					 SELECT "IdInterno","IdEjecutivo","IdPortafolio","IdMoneda"
					   FROM :v_CuentaInversionAConsultar 
					  WHERE "IdInterno" NOT IN (SELECT "IdInterno" FROM :v_RechazadosPorValidacion);
		
					p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
					SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
				END IF;
			END;
		END IF;   -- IF :p_UsaReglasNegocio = 'S' THEN
		
	END IF; -- IF :p_IncluyeProductoApv = 'N' THEN
	
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
	
		DECLARE v_DiasProyeccion 	BIGINT;
		DECLARE v_KeyFlujoCustodia	BIGINT;
		DECLARE v_IdEmpleado		BIGINT;
		DECLARE v_IdMoneda			NVARCHAR(3);
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		
		IF :p_IncluyeProductoApv = 'N' THEN
		    
			
			-- RECUPERA LOS PORTAFOLIOS VISIBLES PARA EL EMPLEADO
			------------------------------------------------------------------
			
			SELECT DISTINCT "IdEjecutivo", "IdMoneda"
			INTO v_IdEmpleado, v_IdMoneda
			FROM :v_CuentaInversionAConsultar;
			
			SELECT TOP 1 "IdEjecutivo" 
			INTO v_ejecutivoConsultado
			FROM :p_CuentaInversionAConsultar;
			
			CALL "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI" (
				p_IdEmpresa			=> :p_IdEmpresa,
				p_IdUsuario			=> :p_IdUsuario,
				p_IdModulo			=> :p_IdModulo,
				p_IdEmpleado		=> :v_IdEmpleado,
				p_out_Portafolios	=> v_Portafolios
			);
			-- SELECT 'v_Portafolios', * FROM :v_Portafolios;  -- PARA REVISAR
			
		
			
			-- OBTIENE MAS INFORMACION DE CTAS. INVERSION A CONSULTAR
			------------------------------------------------------------------
			
			v_InfoCuentasDeInversion =
			SELECT DISTINCT		 		 
				EPOR."IdEmpleadoEjecutivo" as "IdEjecutivo"
			   ,EPOR."NombreCompleto" as "NombreEjecutivo"
			   ,EPOR."CodigoSucursal"
			   ,EPOR."NombreSucursal"
			   ,CLI."IdPersona"
			   ,CTAINV."IdCuentaInversion"
			   ,CTAINV."IdCliente"
			   ,CLI."DocumentoIdentidad"
			   ,CLI."NombreCompleto" as "NombreCliente"
			   ,CTAINV."IdPortafolio"
			   ,POR."DescripcionPortafolio" as "NombrePortafolio"
			   ,POR."Proposito" AS "PropositoPortafolio"
			   ,POR."CodigoPortafolio"
			   ,CTAINV."IdMoneda"
			   ,Mon."Codigo" as "CodigoMoneda"
			   ,(Mon."Codigo" ||' - '|| Mon."Nombre") as "NombreMoneda"
			   ,CTAINV."CuentaInversionPrincipal"
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
					ON PORV."IdPortafolio"	= POR."IdPortafolio"
				   AND PORV."IdEmpleadoEjecutivo" = CASE WHEN IFNULL(v_ejecutivoConsultado,-98989) = -98989 THEN PORV."IdEmpleadoEjecutivo" ELSE v_IdEmpleado END
			 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" as EPOR
			 		ON EPOR."IdEmpleadoEjecutivo"	= PORV."IdEmpleadoEjecutivo";
			 -- SELECT 'v_InfoCuentasDeInversion', * FROM :v_InfoCuentasDeInversion;  -- PARA REVISAR

		
		ELSEIF :p_IncluyeProductoApv = 'S' THEN
			
			IF :p_IdEjecutivo IS NULL THEN
		
				SELECT E."IdEmpleado"
				INTO v_IdEmpleado
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS E
				INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS U ON U."IdEntidad" = E."IdEmpleado"
				WHERE U."TipoEntidad" = 'EMP'
				AND "IdUsuario" = :p_IdUsuario;
			
			ELSE
			
				v_IdEmpleado = :p_IdEjecutivo;
			
			END IF;
			
			CALL "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioUI" (
				p_IdEmpresa			=> :p_IdEmpresa,
				p_IdUsuario			=> :p_IdUsuario,
				p_IdModulo			=> :p_IdModulo,
				p_IdEmpleado		=> :v_IdEmpleado,
				p_out_Portafolios	=> v_Portafolios
			);
			
			v_InfoCuentasDeInversion =
			SELECT DISTINCT		 		 
				EPOR."IdEmpleadoEjecutivo" AS "IdEjecutivo"
			   ,EPOR."NombreCompleto" AS "NombreEjecutivo"
			   ,EPOR."CodigoSucursal"
			   ,EPOR."NombreSucursal"
			   ,CLI."IdPersona"
			   ,CTAINV."IdCuentaInversion"
			   ,CTAINV."IdCliente"
			   ,CLI."DocumentoIdentidad"
			   ,CLI."NombreCompleto" AS "NombreCliente"
			   ,CTAINV."IdPortafolio"
			   ,POR."DescripcionPortafolio" as "NombrePortafolio"
			   ,POR."Proposito" AS "PropositoPortafolio"
			   ,POR."CodigoPortafolio"
			   ,CTAINV."IdMoneda"
			   ,Mon."Codigo" AS "CodigoMoneda"
			   ,(Mon."Codigo" ||' - '|| Mon."Nombre") AS "NombreMoneda"
			   ,CTAINV."CuentaInversionPrincipal"
			FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV 
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR
					ON POR."IdPortafolio" = CTAINV."IdPortafolio"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
					ON CLI."IdCliente" = CTAINV."IdCliente"
			INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon
					ON Mon."IdMoneda" = CTAINV."IdMoneda"
			INNER JOIN :v_Portafolios AS PORV
					ON PORV."IdPortafolio" = POR."IdPortafolio"
				   AND PORV."IdEmpleadoEjecutivo" = CASE WHEN IFNULL(:v_IdEmpleado,-98989) = -98989 THEN PORV."IdEmpleadoEjecutivo" ELSE :v_IdEmpleado END
			 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS EPOR
			 		ON EPOR."IdEmpleadoEjecutivo" = PORV."IdEmpleadoEjecutivo"
			 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
			 		ON MatrizAgrupador."IdMatrizAgrupadorAPV" = MAP(POR."IdMatrizAgrupadorAPV",9999,0,POR."IdMatrizAgrupadorAPV")
			WHERE
				(:p_IdMoneda IS NULL OR CTAINV."IdMoneda" = :p_IdMoneda)
			AND (:p_IdCliente IS NULL OR CTAINV."IdCliente" = :p_IdCliente)
			AND (:p_IdPortafolio IS NULL OR CTAINV."IdPortafolio" = :p_IdPortafolio)
			AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
			
		END IF;     -- ELSEIF :p_IncluyeProductoApv = 'S' THEN
		
		
		
		
		IF NOT IS_EMPTY(:v_InfoCuentasDeInversion) THEN
			
			-- OBTIENE EL SALDO DISPONIBLE DE CUENTA INVERSION
			-------------------------------------------------------- 
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
			
			
			
			-- OBTIENE OTROS SALDOS (Operaciones que afectarán el saldo)
			---------------------------------------------------------------
			IF :p_IncluyeOtrosSaldos = 'S' THEN
			
				-- Consulta operaciones a Plazo (SIMUTANEAS)
				v_SaldoAPlazo_Simultaneas = 
				WITH
					OperacionesAPlazoSimultaneas AS
					(
						SELECT
							CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
						   ,MAP(CAR."IdTipoOperacion",'VENSIM',-1*CAR."INGMontoAPlazo",CAR."INGMontoAPlazo" ) AS "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"							
						FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
						INNER JOIN :v_InfoCuentasDeInversion AS CI
								ON CI."IdCliente" 		= CAR."IdCliente"
							   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
							   AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
						WHERE
							"IdTipoOperacion" 					IN ('COMSIM','VENSIM')
						AND CAR."FechaVencimientoInstrumento" 	>= :p_FechaDesde
						AND CAR."IdEmpresa" 					= :p_IdEmpresa
						AND CAR."IdEstado"						= 'VIG'
					)
					
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
				FROM OperacionesAPlazoSimultaneas
				GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";	
				
				
				
				-- Consulta operaciones a Plazo (PRESTAMOS)
				v_SaldoAPlazo_PrestamosEmprestitos = 
				WITH
					OperacionesAPlazoPrestamosEmprestitos AS
					(
						SELECT
							CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
						   --,MAP(CAR."IdTipoOperacion", 'EMPRES',-1*CAR."INGMontoATasaCompraActual", CAR."INGMontoATasaCompraActual" ) AS "Monto"
						   ,CASE
						      -- EMPRESTITO (Prestamista) --> Monto Premio
						   	  WHEN (CAR."IdTipoOperacion" = 'EMPRES') THEN CAR."INGPrecioAPlazo" 
						   	  -- PRESTAMO (Financiado) --> -1 x (PxQ + Prima)
						   	  WHEN (CAR."IdTipoOperacion" = 'PRESTA') THEN    -1 * (CAR."INGMontoATasaCompraActual" + CAR."INGPrecioAPlazo") 						   	  
						   END  AS "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"							
						FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
						INNER JOIN :v_InfoCuentasDeInversion AS CI 
								ON CI."IdCliente" 		= CAR."IdCliente"
							   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
							   AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
						WHERE
							"IdTipoOperacion" 			IN ('PRESTA','EMPRES')
						AND CAR."INGFechaLiquidacion" 	>= :p_FechaDesde
						AND CAR."IdEmpresa" 			= :p_IdEmpresa
						AND CAR."IdEstado"				= 'VIG'						
						----AND CAR."IdTipoPrestamo"        = 'PRTVTC'   -- Prestamos Venta Corta
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
				FROM OperacionesAPlazoPrestamosEmprestitos
				GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";	
				
				
				-- Consulta operaciones a Plazo (COMPROMISOS)
				v_SaldoAPlazo_Compromisos = 
				WITH
					OperacionesAPlazo AS
					(
						SELECT
							CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
						   ,MAP(CAR."IdTipoOperacion",'EMPRES',-1*CAR."INGMontoATasaCompraActual",CAR."INGMontoATasaCompraActual" ) AS "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"							
						FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
						INNER JOIN :v_InfoCuentasDeInversion AS CI 
								ON CI."IdCliente" 		= CAR."IdCliente"
							   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
							   AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
						WHERE
							"IdTipoOperacion" 			IN ('PRESTA','EMPRES')
						AND CAR."INGFechaLiquidacion" 	>= :p_FechaDesde
						AND CAR."IdEmpresa" 			= :p_IdEmpresa
						AND CAR."IdEstado"				= 'VIG'
						AND CAR."IdTipoPrestamo"        <> 'PRTVTC'   -- NO deben ser prestamos Venta Corta
					),
				
					OperacionesAPlazoPactos AS
					(
						SELECT
							CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
						   ,MAP(CAR."IdTipoOperacion",'COMPAC',-1*CAR."INGMontoATasaCompraActual",CAR."INGMontoATasaCompraActual" ) AS "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"							
						FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" as CAR
						INNER JOIN :v_InfoCuentasDeInversion as CI
								ON CI."IdCliente" 		= CAR."IdCliente"
							   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
							   AND CI."IdMoneda" 		= CAR."IdTipoReajuste"
						WHERE
							"IdTipoOperacion" 			IN ('COMPAC','VENPAC')
						AND CAR."INGFechaLiquidacion" 	>= :p_FechaDesde
						AND CAR."IdEmpresa" 			= :p_IdEmpresa
						AND CAR."IdEstado"				= 'VIG'
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
				FROM
				(				  		
					SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
					FROM OperacionesAPlazo
					UNION ALL
					SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
					FROM OperacionesAPlazoPactos
				)
				GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";				 
				
				
				--3) Consultar Garantias
				v_SaldoEnGarantia =
				WITH
					Garantias AS
					(
						SELECT
							GAR."IdCliente"
						   ,GAR."IdPortafolio"
						   ,GAR."IdMonedaInstrumento" AS "IdMoneda"
						   ,GAR."INGCantidad" AS "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"
						FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" as GAR
						INNER JOIN :v_InfoCuentasDeInversion as CI
								ON CI."IdCliente" 		= GAR."IdCliente"
							   AND CI."IdPortafolio"	= GAR."IdPortafolio"
							   AND CI."IdMoneda"		= GAR."IdMonedaInstrumento"
						WHERE
							GAR."IdEstado" 			= 'VIG'
						AND GAR."IdOrigenGarantia" 	= 'CTA'
						AND GAR."IdEmpresa" 			= :p_IdEmpresa
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
				FROM Garantias
				GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
				
				
				--4) Ingresos y Egresos
				v_SaldoTesoreria =
				WITH
					MovimientosTesoreria AS 
					(
						SELECT
							0 AS "IdCliente"
						   ,IE."IdPortafolioTitularPago" as "IdPortafolio"
						   ,IE."IdMoneda"
						   ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"
						FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS IE
						INNER JOIN :v_InfoCuentasDeInversion as CI
								ON CI."IdPortafolio" 	= IE."IdPortafolioTitularPago"
							   AND CI."IdMoneda"		= IE."IdMoneda"
						WHERE
							IE."IdEstado" 			= 'ING'
						AND IE."FechaLiquidacion"	>= :p_FechaDesde
						AND IE."IdEmpresa" 			= :p_IdEmpresa
					),
					ComprobanteTesoreria AS
					(
						SELECT
							0 AS "IdCliente"
						   ,COM."IdPortafolioTitularPago" as "IdPortafolio"
						   ,COM."IdMoneda"
						   ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "Monto"
						   ,:p_FechaDesde AS "FechaSaldo"
						FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM
						INNER JOIN :v_InfoCuentasDeInversion as CI
								ON CI."IdPortafolio" 	= COM."IdPortafolioTitularPago"
							   AND CI."IdMoneda"		= COM."IdMoneda"
						WHERE
							COM."IdEstado" 			NOT IN ('ANU','MOD','LIQ')
						AND COM."FechaLiquidacion"	>= :p_FechaDesde
						AND COM."IdEmpresa" 		= :p_IdEmpresa
					)
				SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
				FROM
				(
					SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
					FROM MovimientosTesoreria
					UNION ALL
					SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
					FROM ComprobanteTesoreria
				)
				GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
			
				
			END IF;   -- IF :p_IncluyeOtrosSaldos = 'S' THEN
			
			

			
			-- PROCESAR DATOS PARA SALIDA FINAL
			---------------------------------------------------------------			
			
			p_out_SaldoInicialFinal =
			SELECT
				"IdCuentaInversion"
			   ,"SaldoEfectivoInicial"
			   ,"SaldoEfectivoFinal"
			   ,"SaldoMovimientoInicial"
			   ,"SaldoMovimientoFinal"
			FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoInicialFinal";
			
			
			p_out_SaldoPorFechaEfectiva =
			SELECT
				InfCtaInv."IdEjecutivo"
			   ,CAST(InfCtaInv."NombreEjecutivo" as NVARCHAR(300)) as "NombreEjecutivo"
			   ,InfCtaInv."CodigoSucursal"
			   ,CAST(InfCtaInv."NombreSucursal" as NVARCHAR(100)) as "NombreSucursal"
			   ,SPE."IdCuentaInversion"
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
			   ,CAST(COALESCE(sldPlzSim."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoSimultanea"
			   ,CAST(COALESCE(sldPlzVC."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoVentaCorta"
			   ,CAST(COALESCE(sldPlzCom."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoCompromiso"
			   ,CAST(COALESCE(SldTes."Monto",0) as DECIMAL(26,8)) as "SaldoRetiro"
			   ,ProductoAgrupador."NombreProductoAgrupador" as "Producto"
			FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaEfectiva" as SPE
			INNER JOIN :v_InfoCuentasDeInversion as InfCtaInv
					ON SPE."IdCuentaInversion" = InfCtaInv."IdCuentaInversion"
			 LEFT JOIN :v_SaldoEnGarantia as sldGar
			 		ON sldGar."IdCliente" 		= SPE."IdCliente"
				   AND sldGar."IdPortafolio" 	= SPE."IdPortafolio"
				   AND sldGar."IdMoneda" 		= SPE."IdMoneda"
				   AND sldGar."FechaSaldo" 		= SPE."FechaSaldo"
			 LEFT JOIN :v_SaldoAPlazo_Simultaneas as sldPlzSim   -- Operaciones a Plazo Simultanea
			 		ON sldPlzSim."IdCliente" 		= SPE."IdCliente"
			 	   AND sldPlzSim."IdPortafolio" 	= SPE."IdPortafolio"
			 	   AND sldPlzSim."IdMoneda" 		= SPE."IdMoneda"
			 	   AND sldPlzSim."FechaSaldo" 		= SPE."FechaSaldo"
			 	   
			 LEFT JOIN :v_SaldoAPlazo_PrestamosEmprestitos as sldPlzVC   -- Operaciones a Plazo Venta Corta
			 		ON sldPlzVC."IdCliente" 		= SPE."IdCliente"
			 	   AND sldPlzVC."IdPortafolio" 		= SPE."IdPortafolio"
			 	   AND sldPlzVC."IdMoneda" 			= SPE."IdMoneda"
			 	   AND sldPlzVC."FechaSaldo" 		= SPE."FechaSaldo"

			 LEFT JOIN :v_SaldoAPlazo_Compromisos as sldPlzCom   -- Operaciones a Plazo CompromisoS
			 		ON sldPlzCom."IdCliente" 		= SPE."IdCliente"
			 	   AND sldPlzCom."IdPortafolio" 	= SPE."IdPortafolio"
			 	   AND sldPlzCom."IdMoneda" 		= SPE."IdMoneda"
			 	   AND sldPlzCom."FechaSaldo" 		= SPE."FechaSaldo"
			 LEFT JOIN :v_SaldoTesoreria as SldTes
					ON SldTes."IdPortafolio"	= SPE."IdPortafolio"
				   AND SldTes."IdMoneda"		= SPE."IdMoneda"
				   AND SldTes."FechaSaldo"		= SPE."FechaSaldo"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
					ON Portafolio."IdPortafolio" = SPE."IdPortafolio" 
				   AND Portafolio."IdCliente"    = SPE."IdCliente"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador 
					ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			 LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador 
					ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			ORDER BY SPE."IdPortafolio",InfCtaInv."CuentaInversionPrincipal" DESC,SPE."IdMoneda",SPE."FechaSaldo";
			
			
			
				
			p_out_SaldoPorFechaMovimiento =
			SELECT
				InfCtaInv."IdEjecutivo"
			   ,CAST(InfCtaInv."NombreEjecutivo" as NVARCHAR(300)) as "NombreEjecutivo"
			   ,InfCtaInv."CodigoSucursal"
			   ,CAST(InfCtaInv."NombreSucursal" as NVARCHAR(100)) as "NombreSucursal"
			   ,SPM."IdCuentaInversion"
			   ,SPM."CodigoCuentaInversion"
			   ,SPM."IdCliente"
			   ,CAST(InfCtaInv."NombreCliente" as NVARCHAR(300)) as "NombreCliente"
			   ,InfCtaInv."DocumentoIdentidad"
			   ,SPM."IdPortafolio"
			   ,CAST(InfCtaInv."NombrePortafolio" as NVARCHAR(100)) as "NombrePortafolio"
			   ,CAST(InfCtaInv."PropositoPortafolio" as NVARCHAR(100)) as "PropositoPortafolio"
			   ,CAST(InfCtaInv."CodigoPortafolio" as NVARCHAR(100)) as "CodigoPortafolio"
			   ,SPM."FechaSaldo"
			   ,SPM."IdMoneda"
			   ,InfCtaInv."CodigoMoneda"
			   ,CAST(InfCtaInv."NombreMoneda" as NVARCHAR(100)) as "NombreMoneda"
			   ,SPM."Saldo" as "SaldoDisponible"
			   ,CAST(COALESCE(sldGar."Monto",0) as DECIMAL(26,8)) as "SaldoEnGarantia"
			   ,CAST(COALESCE(sldPlzSim."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoSimultanea"
			   ,CAST(COALESCE(sldPlzVC."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoVentaCorta"
			   ,CAST(COALESCE(sldPlzCom."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoCompromiso"
			   ,CAST(COALESCE(SldTes."Monto",0) as DECIMAL(26,8)) as "SaldoRetiro"
			   ,ProductoAgrupador."NombreProductoAgrupador" as "Producto"
			FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaMovimiento" as SPM
			INNER JOIN :v_InfoCuentasDeInversion AS InfCtaInv ON (SPM."IdCuentaInversion" = InfCtaInv."IdCuentaInversion")
			 LEFT JOIN :v_SaldoEnGarantia AS sldGar
					ON sldGar."IdCliente" 		= SPM."IdCliente"
				   AND sldGar."IdPortafolio" 	= SPM."IdPortafolio"
				   AND sldGar."IdMoneda" 		= SPM."IdMoneda"
				   AND sldGar."FechaSaldo" 		= SPM."FechaSaldo"
			 LEFT JOIN :v_SaldoAPlazo_Simultaneas as sldPlzSim   -- Operaciones a Plazo Simultanea
			 		ON sldPlzSim."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlzSim."IdPortafolio" 	= SPM."IdPortafolio"
			 	   AND sldPlzSim."IdMoneda" 		= SPM."IdMoneda"
			 	   AND sldPlzSim."FechaSaldo" 		= SPM."FechaSaldo"

			 LEFT JOIN :v_SaldoAPlazo_PrestamosEmprestitos as sldPlzVC   -- Operaciones a Plazo Venta Corta (Prestamos/Emprestitos)
			 		ON sldPlzVC."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlzVC."IdPortafolio" 		= SPM."IdPortafolio"
			 	   AND sldPlzVC."IdMoneda" 			= SPM."IdMoneda"
			 	   AND sldPlzVC."FechaSaldo" 		= SPM."FechaSaldo"			 	   

			 LEFT JOIN :v_SaldoAPlazo_Compromisos as sldPlzCom
			 		ON sldPlzCom."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlzCom."IdPortafolio" 	= SPM."IdPortafolio"
			 	   AND sldPlzCom."IdMoneda" 		= SPM."IdMoneda"
			 	   AND sldPlzCom."FechaSaldo" 		= SPM."FechaSaldo"
			 LEFT JOIN :v_SaldoTesoreria as SldTes
			 		ON SldTes."IdPortafolio"		= SPM."IdPortafolio"
			 	   AND SldTes."IdMoneda"			= SPM."IdMoneda"
			 	   AND SldTes."FechaSaldo"		= SPM."FechaSaldo"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
					ON Portafolio."IdPortafolio" = SPM."IdPortafolio" 
				   AND Portafolio."IdCliente"    = SPM."IdCliente"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador 
					ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			 LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador 
					ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			ORDER BY SPM."IdPortafolio",InfCtaInv."CuentaInversionPrincipal" DESC,SPM."IdMoneda",SPM."FechaSaldo";

			
			
			
			
		END IF;
	END;
END;
GO