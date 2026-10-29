SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ARC.ConsultarDataArchivoAnexo7SaldosDeClientes";
GO
CREATE PROCEDURE
-- 
--  Proposito: Obtiene la data para la generación del archivo Anexo 19 Libro de Ventas Electrónico
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo y tipo de registro
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 12/01/2018
--
"BSCL.CuentaInversion.BD.Servicios::ARC.ConsultarDataArchivoAnexo7SaldosDeClientes" (

	 -- Parametros fijos de entrada 
     IN p_IdEmpresa 					BIGINT,     
     IN p_IdUsuario 					BIGINT,     
     IN p_CodigoFormatoArchivo 			NVARCHAR(40),     
     IN p_IdPrograma 					BIGINT,
     -- El resto de los parametros de entrada son variables (Obtiener de "BSCL.Plataforma.BD::BAT.ParametrosPrograma")
     
	 -- Datos para generar el archivo (TipoRegistro 1)
	 OUT p_out_Data_TipoRegistro1       TABLE (
	 										"TipoRegistroOptimus"   		INTEGER,  	 	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)	 										
											"Codigo"						NVARCHAR(10),  	-- CHAR   |10  | Código RPJ del AI asignado por la SMV
											"Periodo"						NVARCHAR(6),  	-- CHAR   |6   | Fecha del periodo en formato mmaaaa
											"TipoCodigoCliente"				NVARCHAR(1),  	-- CHAR   |1   | 1: Si es RUT   2: Si es código interno del propio AI
											"CodigoCliente"					NVARCHAR(8),  	-- CHAR   |8   | Código asignado al cliente
											"TipoSaldoCuentaCliente"		NVARCHAR(1),  	-- CHAR   |1   | Tipo Según saldo en los estados de cuentas Corrientes del cliente al final del periodo (0: Saldo Deudor  1: Saldo Acreedor)
											"SaldoCuentaCliente"			DECIMAL(26,8),  -- DEC    |15,2| Saldo en los estados de cuentas corrientes del cliente al final del periodo expresado en nuevos soles
											"TipoSaldoCuentasPorcobrar"		NVARCHAR(1),  	-- CHAR   |1  | Tipo Según el saldo del cliente, que compone parte del rubro cuentas por cobrar comerciales del Estado de Situación Financiera (0: Saldo Deudor 1: Saldo Acreedor)
											"SaldoCuentasPorCobrar"			DECIMAL(26,8),  -- DEC    |15,2| Saldo deudor en el rubro Cuentas por Cobrar Comerciales, expresado en nuevos soles
											"TipoInformacion"				NVARCHAR(2)  	-- CHAR   |2   | Tipo de Información (01: Anual Individual  02: Anual Consolidada  03: Mensual Individual  04: Trimestral Consolidada)
											)

 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
     	
AS
  BEGIN
   	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_out_InformacionContador	INT := 0;
		DECLARE v_out_AdvertenciasContador 	INT := 0;
		DECLARE v_out_ErroresContador		INT := 0;		
		DECLARE v_SoloSaldoNegativo   		NVARCHAR(1); -- S/N 
		DECLARE v_SoloSaldoDisponible 		NVARCHAR(1); -- S/N
		DECLARE v_IncluyeOtrosSaldos  		NVARCHAR(1);  -- S/N
		DECLARE v_FechaDesde  				NVARCHAR(10); 
		DECLARE v_FechaHasta  				NVARCHAR(10);
		DECLARE v_TipoInformacion			NVARCHAR(2); -- 01/03
		DECLARE v_ParidadMensual 			DECIMAL(26,8);
		
		
		DECLARE v_ParametrosEntrada TABLE(  "IdEjecutivo" 				BIGINT,
											"IdMoneda"	 				NVARCHAR(3),		
											"IdPortafolio" 				BIGINT,
											"SoloSaldoNegativo"			NVARCHAR(1),
											"SoloSaldoDisponible"		NVARCHAR(1),
											"IncluyeOtrosSaldos"		NVARCHAR(1),
											"FechaDesde"	 			NVARCHAR(10),
											"FechaHasta"	 			NVARCHAR(10),
											"TipoInformacion"			NVARCHAR(2)
										  );
											

		v_ParametrosEntrada = SELECT 0 AS "IdEjecutivo", '' AS "IdMoneda", 0 AS "IdPortafolio", 
									 '' AS "SoloSaldoNegativo", '' AS "SoloSaldoDisponible", '' AS "IncluyeOtrosSaldos", 
									 '' AS "FechaDesde", '' AS "FechaHasta", '' AS "TipoInformacion"
							  FROM DUMMY WHERE 1=2;
										
		
								  
		------------------------------------------
		-- OBTIENE PARAMETROS DE ENTRADA 
		------------------------------------------
		v_ParametrosEntrada =
		SELECT
			MAX(CASE WHEN "NombreParametro" = 'IdEjecutivo' THEN "Valor" ELSE NULL END) "IdEjecutivo",
			MAX(CASE WHEN "NombreParametro" = 'IdMoneda' THEN "Valor" ELSE NULL END) "IdMoneda",
			MAX(CASE WHEN "NombreParametro" = 'IdPortafolio' THEN "Valor" ELSE NULL END) "IdPortafolio",
			MAX(CASE WHEN "NombreParametro" = 'SoloSaldoNegativo' THEN "Valor" ELSE NULL END) "SoloSaldoNegativo",
			MAX(CASE WHEN "NombreParametro" = 'SoloSaldoDisponible' THEN "Valor" ELSE NULL END) "SoloSaldoDisponible",
			MAX(CASE WHEN "NombreParametro" = 'IncluyeOtrosSaldos' THEN "Valor" ELSE NULL END) "IncluyeOtrosSaldos",
			MAX(CASE WHEN "NombreParametro" = 'FechaDesde' THEN "Valor" ELSE NULL END) "FechaDesde",
			MAX(CASE WHEN "NombreParametro" = 'FechaHasta' THEN "Valor" ELSE NULL END) "FechaHasta",
			MAX(CASE WHEN "NombreParametro" = 'TipoInformacion' THEN "Valor" ELSE NULL END) "TipoInformacion"

		FROM
		(
			SELECT "NombreParametro", "Valor" 
			FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"  
			WHERE "IdPrograma" = :p_IdPrograma
		);		
		--SELECT 'v_ParametrosEntrada', * FROM :v_ParametrosEntrada; -- REVISAR (activar solo a nivel local)			
		
		
		
		SELECT		
			COALESCE("SoloSaldoNegativo",'N')   AS "SoloSaldoNegativo",
			COALESCE("SoloSaldoDisponible",'N') AS "SoloSaldoDisponible",
			COALESCE("IncluyeOtrosSaldos",'N')  AS "IncluyeOtrosSaldos",
			COALESCE("FechaDesde",'0001-01-01') AS "FechaDesde",
			COALESCE("FechaHasta",'0001-01-01') AS "FechaHasta",
			COALESCE("TipoInformacion", '01')	AS "TipoInformacion"
		INTO v_SoloSaldoNegativo, v_SoloSaldoDisponible, v_IncluyeOtrosSaldos, v_FechaDesde, v_FechaHasta, v_TipoInformacion
		FROM :v_ParametrosEntrada;
		
		


		----------------------------------------------------
		-- OBTIENE SALDOS DE CLIENTES (CUENTA INVERSION) 
		----------------------------------------------------
		 
		v_CuentaInversionAConsultar = SELECT "IdEjecutivo",  "IdPortafolio",  "IdMoneda" FROM :v_ParametrosEntrada;
		--SELECT 'v_CuentaInversionAConsultar', * FROM :v_CuentaInversionAConsultar;  -- REVISION
		
				
		CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion" 
		(
		    p_IdEmpresa 					 => :p_IdEmpresa,
		    p_IdUsuario 					 => :p_IdUsuario, 
		    p_IdModulo  					 => 'CTA',        
			p_UsaReglasNegocio 				 => 'S',
			p_CuentaInversionAConsultar		 => :v_CuentaInversionAConsultar,
			p_FechaDesde  					 => :v_FechaHasta,   -- Se usa FechaHasta ya que el hay que obtener los saldos del final del periodo y no los saldos intermedios
		    p_FechaHasta  					 => :v_FechaHasta,    
		    p_SoloSaldoNegativo          	 => :v_SoloSaldoNegativo,
			p_SoloSaldoDisponible        	 => :v_SoloSaldoDisponible,    
			-- p_IncluyeCargosPendientes    => '',  -- NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye cargos certificados, S:Incluye cargos pendientes y certificados
			-- p_IncluyeAbonosPendientes    => '',  -- NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye abonos certificados, S:Incluye abonos pendientes y certificados
			p_IncluyeOtrosSaldos		 	 => :v_IncluyeOtrosSaldos,       -- N: Solo saldo CTA,   S: Incluye saldo CUS y saldo TES (solo para vistas analyticas)	
		    p_out_SaldoInicialFinal 		 => :v_out_SaldoInicialFinal, 
		    p_out_SaldoPorFechaEfectiva 	 => :v_out_SaldoPorFechaEfectiva,
			p_out_SaldoPorFechaMovimiento 	 => :v_out_SaldoPorFechaMovimiento,
			p_out_InformacionContador  	 	 => :v_out_InformacionContador,
			p_out_InformacionDetalle   		 => :v_out_InformacionDetalle,
			p_out_AdvertenciasContador       => :v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle        => :v_out_AdvertenciasDetalle,	
			p_out_ErroresContador            => :v_out_ErroresContador,
			p_out_ErroresDetalle             => :v_out_ErroresDetalle
		);
		--SELECT 'v_out_SaldoPorFechaEfectiva', * FROM :v_out_SaldoPorFechaEfectiva;  -- REVISION


		
		IF v_TipoInformacion = '01' THEN 
		------------------------------------------------------------------------------
		-- OBTIENE PARIDAD Y TIPO CAMBIO (Los saldos en USD se deben valorizar a PEN) 
		------------------------------------------------------------------------------
				
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada" ("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")		
			SELECT 1,"IdCuentaInversion" AS "IdInterno","FechaSaldo","IdMoneda",'PEN',0,"SaldoDisponible", (CASE WHEN "IdMoneda" = 'USD' THEN 'DOLAR_OBSERVADO' WHEN "IdMoneda" = 'EUR' THEN 'EURO' ELSE 'USO_DEFECTO' END) AS	"IdUso"
			FROM :v_out_SaldoPorFechaEfectiva
			WHERE "IdMoneda" <> 'PEN';  -- Lo que no esté en SOLES se valoriza a PEN 
	
			CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"
			(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => 1	
			);		
			
			v_SaldoClientes = WITH SalCli AS 
			(
				SELECT
							SAL."IdCuentaInversion" AS "IdInterno",  -- Se usa como IdInterno ya que es único
							SAL."IdCliente",
							SAL."FechaSaldo",
							SAL."IdMoneda",
							CASE
								WHEN "IdMoneda" = 'PEN' THEN SAL."SaldoDisponible"
								ELSE PAR."MontoDestino"   -- Los saldos que no estén en PEN, fueron valorizados a PEN 
							END AS "Saldo",
							CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN DFIC."DocumentoIdentidad" ELSE DOC."DocumentoIdentidad" END AS "RutCavali",
							CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN '2' ELSE '1' END AS "TipoCodigoCliente"
						FROM :v_out_SaldoPorFechaEfectiva AS SAL		
						LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS PAR ON (PAR."IdInterno" = SAL."IdCuentaInversion")
						LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = SAL."IdCliente" AND SOC."Borrado" = 'N')
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC 
							   ON (DOC."IdPersona" = SOC."IdPersona" 
							  AND DOC."IdTipoDocumentoIdentidad" = 'RUT' 
							  AND DOC."IdEstado" = 'ACT' 
							  AND DOC."Borrado" = 'N')
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DFIC 
						 	   ON (DFIC."IdPersona" = SOC."IdPersona" 
						      AND DFIC."IdTipoDocumentoIdentidad" = 'FIC' 
						      AND DOC."IdEstado" = 'ACT' 
						      AND DOC."Borrado" = 'N')
		 				)
		 				
		SELECT MAX("RutCavali") AS "RutCavali", MAX("FechaSaldo") AS "FechaSaldo", SUM("Saldo") AS "Saldo", MAX("TipoCodigoCliente") AS "TipoCodigoCliente"
		FROM SalCli  --WHERE "IdCliente" = 116606
		GROUP BY "IdCliente";
		
--		SELECT	"RutCavali" AS "RutCavali", "FechaSaldo" AS "FechaSaldo", "Saldo" AS "Saldo", "TipoCodigoCliente" AS "TipoCodigoCliente"
--		FROM SalCli WHERE "IdCliente" = 210188;--116606;

		
		/*****  SALIDA REAL  ANUAL *****/
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 			AS "TipoRegistroOptimus",   	-- Este campo ldebe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)	 										
			'S800080   ' 	AS "Codigo",					-- CHAR   |10  | Código RPJ del AI asignado por la SMV  (Dato fijo asignado por SMV a la corredora) 
			((CASE WHEN MONTH("FechaSaldo") < 10 THEN  '0' || CAST(MONTH("FechaSaldo") AS NVARCHAR(1)) ELSE CAST(MONTH("FechaSaldo") AS NVARCHAR(2)) END) || CAST(YEAR("FechaSaldo") AS NVARCHAR(4))) AS "Periodo",  -- CHAR   |6   | Fecha del periodo en formato mmaaaa			
			"TipoCodigoCliente" AS "TipoCodigoCliente",			-- CHAR   |1   | 1: Si es RUT   2: Si es código interno del propio AI  (1: Se informa RUT CAVALI)
			LPAD(COALESCE("RutCavali", ' '), 8, ' ')	AS "CodigoCliente",				-- CHAR   |8   | Código asignado al cliente
			(CASE                                       -- CHAR   |1   | Tipo Según saldo en los estados de cuentas Corrientes del cliente al final del periodo (0: Saldo Deudor  1: Saldo Acreedor)
				WHEN "Saldo" > 0 THEN '1'  -- 1: Saldo Acreedor
				ELSE '0'                   -- 0: Saldo Deudor 
			END) AS "TipoSaldoCuentaCliente",
			LPAD(TO_DECIMAL(ABS("Saldo"),18,2),18,0) AS "SaldoCuentaCliente",			-- DEC    |15,2| Saldo en los estados de cuentas corrientes del cliente al final del periodo expresado en nuevos soles
			'0' 		AS "TipoSaldoCuentasPorcobrar",	-- CHAR   |1  | Tipo Según el saldo del cliente, que compone parte del rubro cuentas por cobrar comerciales del Estado de Situación Financiera (0: Saldo Deudor)
			LPAD(TO_DECIMAL(ABS(0.0),18,2),18,0) 	AS "SaldoCuentasPorCobrar",			-- DEC    |15,2| Saldo deudor en el rubro Cuentas por Cobrar Comerciales, expresado en nuevos soles
			LPAD(:v_TipoInformacion,2,0)			AS "TipoInformacion"			-- CHAR   |2   | Tipo de Información (01: Anual Individual  02: Anual Consolidada  03: Mensual Individual  04: Trimestral Consolidada)									
		FROM :v_SaldoClientes
		WHERE "Saldo" != 0;		 				
		 				
		END IF; 
		
		IF v_TipoInformacion = '03' THEN 
		------------------------------------------------------------------------------
		-- OBTIENE PARIDAD Y TIPO CAMBIO (Los saldos en USD se deben valorizar a PEN) 
		------------------------------------------------------------------------------
				
--- PARIDAD MENUSAL ULTIMO DÏA HABIL
			SELECT top 1 IFNULL("Valor",1) INTO v_ParidadMensual FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
			WHERE (("IdParametroFinanciero" = 'US$' AND "Valor" > 0) AND LEFT("Fecha",7) =  LEFT(v_FechaHasta,7))
			ORDER BY "Fecha"  DESC;  			
			
			v_SaldoClientes = WITH SalCli AS 
			(
						SELECT
					SAL."IdCuentaInversion" AS "IdInterno",  -- Se usa como IdInterno ya que es único
					SAL."IdCliente",
					SAL."FechaSaldo",
					SAL."IdMoneda",
					CASE
						WHEN "IdMoneda" = 'PEN' THEN SAL."SaldoDisponible"
						ELSE  SAL."SaldoDisponible" *  v_ParidadMensual
					END AS "Saldo",
					CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN DFIC."DocumentoIdentidad" ELSE DOC."DocumentoIdentidad" END AS "RutCavali",
					CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN '2' ELSE '1' END AS "TipoCodigoCliente"
				FROM :v_out_SaldoPorFechaEfectiva AS SAL
				
				LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = SAL."IdCliente" AND SOC."Borrado" = 'N')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC 
					   ON (DOC."IdPersona" = SOC."IdPersona" 
					  AND DOC."IdTipoDocumentoIdentidad" = 'RUT' 
					  AND DOC."IdEstado" = 'ACT' 
					  AND DOC."Borrado" = 'N')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DFIC 
				 	   ON (DFIC."IdPersona" = SOC."IdPersona" 
				      AND DFIC."IdTipoDocumentoIdentidad" = 'FIC' 
				      AND DOC."IdEstado" = 'ACT' 
				      AND DOC."Borrado" = 'N')	
		 				)
		
		 	SELECT MAX("RutCavali") AS "RutCavali", MAX("FechaSaldo") AS "FechaSaldo", SUM("Saldo") AS "Saldo", MAX("TipoCodigoCliente") AS "TipoCodigoCliente"
			FROM SalCli  --WHERE "IdCliente" = 116606--210188--
			GROUP BY "IdCliente";

--		SELECT	"RutCavali" AS "RutCavali", "FechaSaldo" AS "FechaSaldo", "Saldo" AS "Saldo", "TipoCodigoCliente" AS "TipoCodigoCliente"
--		FROM SalCli WHERE "IdCliente" = 210188;--116606;		
		
		
		/*****  SALIDA REAL MENSUAL *****/
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 			AS "TipoRegistroOptimus",   	-- Este campo ldebe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)	 										
			'S800080   ' 	AS "Codigo",					-- CHAR   |10  | Código RPJ del AI asignado por la SMV  (Dato fijo asignado por SMV a la corredora) 
			((CASE WHEN MONTH("FechaSaldo") < 10 THEN  '0' || CAST(MONTH("FechaSaldo") AS NVARCHAR(1)) ELSE CAST(MONTH("FechaSaldo") AS NVARCHAR(2)) END) || CAST(YEAR("FechaSaldo") AS NVARCHAR(4))) AS "Periodo",  -- CHAR   |6   | Fecha del periodo en formato mmaaaa			
			"TipoCodigoCliente" AS "TipoCodigoCliente",			-- CHAR   |1   | 1: Si es RUT   2: Si es código interno del propio AI  (1: Se informa RUT CAVALI)
			LPAD(COALESCE("RutCavali", ' '), 8, ' ')	AS "CodigoCliente",				-- CHAR   |8   | Código asignado al cliente
			(CASE                                       -- CHAR   |1   | Tipo Según saldo en los estados de cuentas Corrientes del cliente al final del periodo (0: Saldo Deudor  1: Saldo Acreedor)
				WHEN "Saldo" > 0 THEN '1'  -- 1: Saldo Acreedor
				ELSE '0'                   -- 0: Saldo Deudor 
			END) AS "TipoSaldoCuentaCliente",
			LPAD(TO_DECIMAL(ABS("Saldo"),18,2),18,0) AS "SaldoCuentaCliente",			-- DEC    |15,2| Saldo en los estados de cuentas corrientes del cliente al final del periodo expresado en nuevos soles
			'0' 		AS "TipoSaldoCuentasPorcobrar",	-- CHAR   |1  | Tipo Según el saldo del cliente, que compone parte del rubro cuentas por cobrar comerciales del Estado de Situación Financiera (0: Saldo Deudor)
			LPAD(TO_DECIMAL(ABS(0.0),18,2),18,0) 	AS "SaldoCuentasPorCobrar",			-- DEC    |15,2| Saldo deudor en el rubro Cuentas por Cobrar Comerciales, expresado en nuevos soles
			LPAD(:v_TipoInformacion,2,0)			AS "TipoInformacion"			-- CHAR   |2   | Tipo de Información (01: Anual Individual  02: Anual Consolidada  03: Mensual Individual  04: Trimestral Consolidada)									
		FROM :v_SaldoClientes
		WHERE "Saldo" != 0;		 				
		 				
		END IF; 		
		
		/*
		IF v_TipoInformacion = '03' THEN
		---------------------------------------------------------
		--- PARIDAD MENUSAL ULTIMO DÏA HABIL
			SELECT top 1 IFNULL("Valor",1) INTO v_ParidadMensual FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
			WHERE (("IdParametroFinanciero" = 'US$' AND "Valor" > 0) AND LEFT("Fecha",7) =  LEFT(v_FechaHasta,7))
			ORDER BY "Fecha"  DESC;  		
		
			SELECT
					SAL."IdCuentaInversion" AS "IdInterno",  -- Se usa como IdInterno ya que es único
					SAL."IdCliente",
					SAL."FechaSaldo",
					SAL."IdMoneda",
					CASE
						WHEN "IdMoneda" = 'PEN' THEN SAL."SaldoDisponible"
						ELSE  SAL."SaldoDisponible" *  v_ParidadMensual
					END AS "Saldo",
					CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN DFIC."DocumentoIdentidad" ELSE DOC."DocumentoIdentidad" END AS "RutCavali",
					CASE WHEN DFIC."DocumentoIdentidad" = DOC."DocumentoIdentidad" THEN '2' ELSE '1' END AS "TipoCodigoCliente"
				FROM :v_out_SaldoPorFechaEfectiva AS SAL
				
				LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = SAL."IdCliente" AND SOC."Borrado" = 'N')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC 
					   ON (DOC."IdPersona" = SOC."IdPersona" 
					  AND DOC."IdTipoDocumentoIdentidad" = 'RUT' 
					  AND DOC."IdEstado" = 'ACT' 
					  AND DOC."Borrado" = 'N')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DFIC 
				 	   ON (DFIC."IdPersona" = SOC."IdPersona" 
				      AND DFIC."IdTipoDocumentoIdentidad" = 'FIC' 
				      AND DOC."IdEstado" = 'ACT' 
				      AND DOC."Borrado" = 'N')		
		)
		END IF;
*/

	
		
			
		
		
		
		
	
		
		
		/*** SALIDA EN DURO PARA PRUEBAS  ***
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 			AS "TipoRegistroOptimus",   	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)	 										
			'S800080' 	AS "Codigo",					-- CHAR   |10  | Código RPJ del AI asignado por la SMV
			'012018' 	AS "Periodo",					-- CHAR   |6   | Fecha del periodo en formato mmaaaa
			'1' 		AS "TipoCodigoCliente",			-- CHAR   |1   | 1: Si es RUT   2: Si es código interno del propio AI
			'12969673' 	AS "CodigoCliente",				-- CHAR   |8   | Código asignado al cliente
			'1' 		AS "TipoSaldoCuentaCliente",	-- CHAR   |1   | Tipo Según saldo en los estados de cuentas Corrientes del cliente al final del periodo (0: Saldo Deudor  1: Saldo Acreedor)
			9999.88 	AS "SaldoCuentaCliente",		-- DEC    |15,2| Saldo en los estados de cuentas corrientes del cliente al final del periodo expresado en nuevos soles
			'1' 		AS "TipoSaldoCuentasPorcobrar",	-- CHAR   |1  | Tipo Según el saldo del cliente, que compone parte del rubro cuentas por cobrar comerciales del Estado de Situación Financiera (0: Saldo Deudor 1: Saldo Acreedor)
			7777.66 	AS "SaldoCuentasPorCobrar",		-- DEC    |15,2| Saldo deudor en el rubro Cuentas por Cobrar Comerciales, expresado en nuevos soles
			'01' 		AS "TipoInformacion"			-- CHAR   |2   | Tipo de Información (01: Anual Individual  02: Anual Consolidada  03: Mensual Individual  04: Trimestral Consolidada)									
		FROM DUMMY;
		************************************/		
		
  	END;    
END;