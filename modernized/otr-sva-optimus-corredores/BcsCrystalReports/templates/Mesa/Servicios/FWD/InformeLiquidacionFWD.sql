SET SCHEMA "BSCL";
GO 
 
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FWD.InformeLiquidacionFWD"; 
GO

--
--  Proposito: Servicio CrystalReport para Liquidacion de Forward
--  Autor: FGUZMAN - BCS
--  Fecha Creacion: 07/02/2019
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FWD.InformeLiquidacionFWD" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdUsuario				BIGINT,
	
	-- Parametros entrada del servicio privado
	IN p_FechaConsulta			DATE

	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	BEGIN
		--------------------
		--Variables
		--------------------
		
		DECLARE v_FechaSistema	DATE;
		DECLARE v_HoraSistema	SECONDDATE;
		DECLARE	v_Registros		INT;
		DECLARE v_SegundosZona  INTEGER;
		DECLARE v_logo TABLE("LogoCorredora" BLOB);

		
		--Se obtiene fecha y hora de sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_HoraSistema);
		
		-- Se Obtiene fecha y hora local
		SELECT TO_INTEGER("Valor") INTO v_SegundosZona
	  	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.SegundosZonaHoraria';
	

		/** Obtener Logo  **/
		v_logo =	SELECT TOP 1
					"Binario" AS "LogoCorredora"
					FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
					WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		/** Obtener Indicadores **/
		v_Indicadores =  	Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
								"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'DO' AND "Fecha" = TO_VARCHAR(:p_FechaConsulta,'YYYY-MM-DD')
							UNION ALL
							Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
								"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'UF' AND "Fecha" = TO_VARCHAR(:p_FechaConsulta,'YYYY-MM-DD');
		/** Separar Indicadores por Moneda **/
		v_IndicadorDolar = 	SELECT 
									"IdIndicador"
									,"Fecha"
									,"TipoCambio"
									,CASE WHEN COALESCE("TipoCambio",0) =0 THEN 'No Hay Informacion'
										ELSE 'Tipo Cambio a la fecha' END  AS "Info"
								FROM :v_Indicadores
								WHERE "IdIndicador"='DO';
		v_IndicadorUf = 	SELECT 
									"IdIndicador"
									,"Fecha"
									,"TipoCambio"
									,CASE WHEN COALESCE("TipoCambio",0) =0 THEN 'No Hay Informacion'
										ELSE 'Tipo Cambio a la fecha' END  AS "Info"
								FROM :v_Indicadores
								WHERE  "IdIndicador"='UF';
								
		/** Obtener Decimales de Monedas **/
		v_decimalMoneda= SELECT
   		Mon."IdMoneda" AS "Moneda", Mon."CantidadDecimales" AS "Decimales" FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" Mon  
				WHERE Mon."IdUsoMonedaFormato"='GEN' 
				AND Mon."IdTipoOperacion" = '' 
				AND Mon."IdTipoInstrumento" = ''; 
		
		
	
		
		v_Operacion = 
			SELECT
				---------- DATOS OPERACION ----------
				Car."FolioCartera",
	   			Op."FolioOperacion",
	   			Car."IdEstado",
	   			Car."Cantidad",
				SUM(Car."EGRCantidad") OVER (PARTITION BY Car."FolioCartera" ORDER BY Car."IdCartera" DESC) AS "CantidadAcumulada",
				ROW_NUMBER() OVER(PARTITION BY Car."FolioCartera" ORDER BY Car."IdCartera" DESC) AS "RegistroPorFolio",
	 			TO_NVARCHAR(Op."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
	   			Op."Plazo",
	   			Op."NombreInstrumento",
	   			Op."IdMonedaPrincipal",
	   			Op."IdMonedaSecundaria",
	   			Car."MontoMonedaPrincipal" AS "Monto",
	   			Ope."TCCierre" AS "TCForward",
	   			Car."INGMontoValorRazonableActivo" AS "Activo",
	   			Car."INGMontoValorRazonablePasivo" AS "Pasivo",
	   			TO_CHAR(ADD_SECONDS(v_HoraSistema,v_SegundosZona),'HH24:MI') AS "HoraSistema",
	   			Op."MontoPagamos" AS "Pagamos",
	   			Op."IdMonedaMontoPagamos" AS "MonedaEntregamos",	   			
	   			CASE 
	   			  WHEN Op."IdMonedaMontoRecibimos" = 'CLP' THEN COALESCE(ROUND(Op."MontoRecibimos"),0)
	   			ELSE Op."MontoRecibimos" 
	   			END AS "Recibimos",
	   			Op."IdMonedaMontoRecibimos" AS "MonedaRecibimos",
	   			TO_NVARCHAR(:p_FechaConsulta,'DD-MM-YYYY') AS "FechaConsulta",
	   			Usu."CodigoUsuario" AS "Usuario",
	   			Op."NombreCliente" AS "NombreEmpresa",
	   			CASE WHEN COALESCE(Op."RutCliente",'') <> '' AND Op."TipoDocumentoIdentidad" = 'R' THEN
				"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(Op."RutCliente",'-'),'i',0)||
				'-'||SUBSTR_AFTER(Op."RutCliente",'-')
				ELSE COALESCE(Op."RutCliente",'-') END AS "DocumentoIdentidadEmpresa",
	   			COALESCE(Op."IdMonedaPrincipal",'')||'/'||COALESCE(Op."IdMonedaSecundaria",'') AS "MonedasOperacion",
	   			Op."IdTipoOperacion" AS "Operacion",
	   			Op."IdMonedaLiquidacion",
	   			--Indicadores
				TO_NVARCHAR(d."Fecha", 'DD-MM-YYYY') as "FechaDolar",
				CASE WHEN d."Info" = '' THEN 'Sin Información'
				ELSE d."Info" END  as "InfoDolar",
				COALESCE(d."TipoCambio",0) as "TipoCambioDolar",
				to_char(u."Fecha", 'DD-MM-YYYY') as "FechaUf",
				CASE WHEN u."Info" = '' THEN 'Sin Información'
				ELSE u."Info" END as "InfoUF",
				COALESCE(u."TipoCambio",0) as "TipoCambioUf",
				Op."ModalidadLiquidacion",
				Op."IdMonedaCompensacion" AS "MonedaCompensacion",
				--Logo
	   			Logo."LogoCorredora",
	   			-- Monedas
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'CLP')	AS "DecimalesCLP",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'USD')	AS "DecimalesUSD",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'UF')	AS "DecimalesUF",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'MXN')	AS "DecimalesMXN",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'EUR')	AS "DecimalesEUR",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'BRL')	AS "DecimalesBRL",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'COP')	AS "DecimalesCOP",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'PEN')	AS "DecimalesPEN",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'JPY')	AS "DecimalesJPY",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'AUD')	AS "DecimalesAUD",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'GBP')	AS "DecimalesGBP",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'RMB')	AS "DecimalesRMB",
				(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = 'CAD')	AS "DecimalesCAD"
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" Op
			INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS Ope ON (Op."FolioOperacion" = Ope."IdOperacion")
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS Car ON (Car."IdOperacion" = Op."FolioOperacion" AND Car."IdCartera" = Op."IdCartera")
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usu ON (Usu."IdUsuario" = p_IdUsuario)
			LEFT JOIN :v_IndicadorDolar d ON (d."IdIndicador" = 'DO')
			LEFT JOIN :v_IndicadorUf u ON (u."IdIndicador" = 'UF')
			CROSS JOIN :v_logo AS Logo
			WHERE Op."IdTipoProducto" = 'MEFW'
				AND Car."MontoMonedaPrincipal" > 0
				AND Op."EGRIdTipoOperacion" IN ('VCDFWD', 'ANTFWD')
				AND TO_NVARCHAR(Ope."FechaLiquidacion",'YYYY-MM-DD') = TO_NVARCHAR(:p_FechaConsulta,'YYYY-MM-DD');
				
				
		v_MaxRegistro = 
			SELECT "FolioCartera", MAX("RegistroPorFolio") AS "RegistroMax" 
			FROM :v_Operacion
			WHERE "CantidadAcumulada" <= "Cantidad"
			GROUP BY "FolioCartera";
				
		v_UltimoFolioCartera =
			SELECT "FolioCartera","RegistroPorFolio", "IdEstado"
			FROM :v_Operacion
			WHERE "RegistroPorFolio" = 1 AND "IdEstado" = 'LIQ';
			
		v_OperacionesFinales =
			SELECT 
				Op."FolioCartera",
	   			Op."FolioOperacion",
	 			Op."FechaOperacion",
	   			Op."Plazo",
	   			Op."NombreInstrumento",
	   			Op."IdMonedaPrincipal",
	   			Op."IdMonedaSecundaria",
	   			Op."Monto",
	   			Op."TCForward",
	   			Op."Activo",
	   			Op."Pasivo",
	   			Op."HoraSistema",
	   			Op."Pagamos",
	   			Op."MonedaEntregamos",
	   			Op."Recibimos",
	   			Op."MonedaRecibimos",
	   			Op."FechaConsulta",
	   			Op."Usuario",
	   			Op."NombreEmpresa",
	   			Op."DocumentoIdentidadEmpresa",
	   			Op."MonedasOperacion",
	   			Op."Operacion",
	   			Op."IdMonedaLiquidacion",
				Op."FechaDolar",
				Op."TipoCambioDolar",
				Op."FechaUf",
				Op."TipoCambioUf",
				Op."ModalidadLiquidacion",
				Op."MonedaCompensacion",
	   			Op."LogoCorredora",
				Op."DecimalesCLP",
				Op."DecimalesUSD",
				Op."DecimalesUF",
				Op."DecimalesMXN",
				Op."DecimalesEUR",
				Op."DecimalesBRL",
				Op."DecimalesCOP",
				Op."DecimalesPEN",
				Op."DecimalesJPY",
				Op."DecimalesAUD",
				Op."DecimalesGBP",
				Op."DecimalesRMB",
				Op."DecimalesCAD"
			FROM :v_Operacion AS Op
			LEFT JOIN :v_MaxRegistro AS Max ON (Op."FolioCartera" = Max."FolioCartera")
			LEFT JOIN :v_UltimoFolioCartera AS Ult ON (Op."FolioCartera" = Ult."FolioCartera")
			WHERE Op."RegistroPorFolio" <= Max."RegistroMax" AND Ult."FolioCartera" IS NOT NULL; 
				
			
		--------------------
		--Lógica de Negocio 
		--------------------
		
		SELECT	COUNT(1)
		INTO	v_Registros
		FROM	:v_OperacionesFinales OP;
		--Select v_Registros From DUMMY;
		
		IF v_Registros >= 1 THEN
		
		   
		   
		
			SELECT OP.*
			FROM :v_OperacionesFinales OP;
			

				
		ELSE
		
			SELECT
				0 	AS "FolioCartera",
	   			0 	AS "FolioOperacion",
	 			''	AS "FechaOperacion",
	   			0 	AS "PlazoEnDias",
	   			'' AS "NombreInstrumento",
	   			'' 	AS "IdMonedaPrincipal",
	   			''	AS "IdMonedaSecundaria",
	   			0 	AS "Monto",
	   			0	AS "TCForward",
	   			0 	AS "Activo",
	   			0 	AS "Pasivo",
	   			TO_CHAR(ADD_SECONDS(v_HoraSistema,v_SegundosZona),'HH24:MI') AS "HoraSistema",
	   			0 AS "Pagamos",
	   			'' AS "MonedaEntregamos",
	   			0 AS "Recibimos",
	   			'' AS "MonedaRecibimos",
	   			TO_NVARCHAR(:p_FechaConsulta,'DD-MM-YYYY') AS "FechaConsulta",
	   			Usu."CodigoUsuario" AS "Usuario",
	   			'' AS "NombreEmpresa",
	   			'' AS "DocumentoIdentidadEmpresa",
	   			'' AS "MonedasOperacion",
	   			'' AS "Operacion",
	   			'' AS "IdMonedaLiquidacion",
	   			--Indicadores
				'' as "FechaDolar",
				0 as "TipoCambioDolar",
				'' as "FechaUf",
				0 as "TipoCambioUf",
				'' AS "ModalidadLiquidacion",
				'' AS "MonedaCompensacion",
				--Logo
	   			Logo."LogoCorredora",
	   			-- Monedas
				0	AS "DecimalesCLP",
				0	AS "DecimalesUSD",
				0	AS "DecimalesUF",
				0	AS "DecimalesMXN",
				0	AS "DecimalesEUR",
				0	AS "DecimalesBRL",
				0	AS "DecimalesCOP",
				0	AS "DecimalesPEN",
				0	AS "DecimalesJPY",
				0	AS "DecimalesAUD",
				0	AS "DecimalesGBP",
				0	AS "DecimalesRMB",
				0	AS "DecimalesCAD"
			--FROM "BSCL.Mesa.BD::MES.Operacion" Op 
			FROM DUMMY
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usu ON (Usu."IdUsuario" = p_IdUsuario)
			CROSS JOIN :v_logo AS Logo;
			
		
		END IF;
		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO 
