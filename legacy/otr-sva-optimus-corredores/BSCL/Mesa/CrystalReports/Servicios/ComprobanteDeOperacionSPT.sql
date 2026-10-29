SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ComprobanteDeOperacionSPT";
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ComprobanteDeOperacionSPT"(
	--Entrada
	IN p_IdEmpresa   BIGINT,
	IN p_IdOperacion BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE v_monedaPrincipal 				DECIMAL(26,8);
	DECLARE v_monedaSecundaria 				DECIMAL(26,8);
	DECLARE v_idMonedaPrincipal 			NVARCHAR(3);
	DECLARE v_idMonedaSecundaria		 	NVARCHAR(3);
	DECLARE v_out_montoEscritoPrincipal 	NVARCHAR(1000);
	DECLARE v_out_montoEscritoSecundario 	NVARCHAR(1000);
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	
	SELECT 
		"MontoMonedaPrincipal", "MontoMonedaSecundaria", "IdMonedaPrincipal", "IdMonedaSecundaria"
		INTO v_monedaPrincipal, v_monedaSecundaria, v_idMonedaPrincipal, v_idMonedaSecundaria
	FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
	WHERE "IdOperacion" = :p_IdOperacion;
		
	CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(
		:v_monedaPrincipal,
		'1',
		v_idMonedaPrincipal,
		v_out_montoEscritoPrincipal
	);
	
	CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(
		:v_monedaSecundaria,
		'1',
		v_idMonedaSecundaria,
		v_out_montoEscritoSecundario
	);
	
	v_Movimientos_Spot =
		SELECT
			 MSP."IdOperacionUI"					AS "FolioOperacion"
			,MSP."TipoOperacion"					AS "Operacion"
			,MSP."NombreInstrumento"				AS "Producto"
			,CLI."NombreCompleto"					AS "NombreCliente"
			--,CLI."DocumentoIdentidad"				AS "RUT"
			,CASE WHEN COALESCE(CLI."DocumentoIdentidad",'') <> '' AND CLI."TipoDocumentoIdentidad" = 'R' THEN
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(CLI."DocumentoIdentidad",'-'),'i',0)||
					'-'||SUBSTR_AFTER(CLI."DocumentoIdentidad",'-')
				  ELSE COALESCE(CLI."DocumentoIdentidad",'-') END	AS "RUT"
			,DIR."Calle"							AS "Direccion"
			,CASE WHEN CIE."IdPortafolioTitularPago" <> 0 THEN TO_VARCHAR(CIE."IdPortafolioTitularPago") 
				  ELSE MSP."CodigoPropositoPortafolio" END		AS "CodigoPortafolio"
			,TO_VARCHAR(MSP."FechaOperacion", 'DD-MM-YYYY')		AS "FechaOperacion"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraImpresion"
			,MSP."Estado"							AS "Estado"
			,MSP."IdTipoOperacion"					AS "TipoOperacion"
			,MSP."IdMonedaPrincipal"				AS "MonedaPrincipal"
			,MSP."IdMonedaSecundaria"				AS "MonedaSecundaria"
			,MSP."MontoMonedaPrincipal"				AS "MontoMonedaPrincipal"
			,:v_out_montoEscritoPrincipal			AS "MontoEscritoPrincipal"
			,:v_out_montoEscritoSecundario			AS "MontoEscritoSecundario"
			,MSP."TipoCambioCierre"					AS "TipoCambioCierre"
			,MSP."ParidadCierre"					AS "ParidadCierre"
			,MSP."MontoMonedaSecundaria"			AS "MontoMonedaSecundaria"
			,MSP."TipoCambioCosto"					AS "TipoCambioCosto"
			,MSP."ParidadCosto"						AS "ParidadCosto"
			,CASE MSP."IdTipoCanalIngreso" 
				WHEN 'MES' THEN 'MESA' WHEN 'DAT' THEN 'DATATEC' WHEN 'FWD' THEN 'FORWARD' 
				ELSE MSP."IdTipoCanalIngreso" END AS "Origen"
			,MSP."Observacion"						AS "Observaciones"
			,MSP."CodigoUsuario"					AS "UsuarioCreador"
			,CUC."CodigoUsuario"					AS "AutorizadoPor"
			,LG."CodigoUsuario"						AS "AnuladaPor"
			,TO_VARCHAR(LG."FechaRegistro", 'DD-MM-YYYY') AS "FechaAnulacion"
			,TO_VARCHAR(LG."FechaRegistro", 'HH24:MI:SS') AS "HoraAnulacion"
			,:p_IdEmpresa							AS "IdEmpresa"
			,COALESCE(MONPRI."MontoDecimales",4)	AS "MontoDecimalesPrincipal"
			,COALESCE(MONSEC."MontoDecimales",4)	AS "MontoDecimalesSecundaria"
			,LOGO."LogoCorredora"
		FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" MSP
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			ON (MSP."IdCliente" = CLI."IdCliente")
  		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
 			ON (DIR."IdPersona" = CLI."IdPersona" AND DIR."IdDireccionFisica" = CLI."IdDireccionFisicaPrincipal")
 		LEFT JOIN (
 				SELECT "IdOperacion", "IdPortafolioTitularPago"
 				FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso"
 				GROUP BY "IdOperacion", "IdPortafolioTitularPago"
 				) CIE
 			ON (MSP."IdOperacion" = CIE."IdOperacion")
		LEFT JOIN (
				SELECT TOP 1 "NumeroOperacionConcepto", "IdUsuario"
				FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento"
				WHERE TRIM("NumeroOperacionConcepto") = TO_CHAR(p_IdOperacion)
				  AND "IdEstado" = 'AUT'
				ORDER BY "Fecha", "HoraRegistro" DESC
				) CA
			ON (TO_CHAR(MSP."IdOperacion") = TRIM(CA."NumeroOperacionConcepto"))
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" CUC
			ON CA."IdUsuario" =  CUC."IdUsuario"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" LG
			ON (MSP."IdOperacion" = LG."Folio" AND LG."EstadoRegistro" = 'ANU')
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
			ON (MONPRI."IdUsoMonedaFormato" = 'SPT' AND MONPRI."IdMoneda" = MSP."IdMonedaPrincipal")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
			ON (MONSEC."IdUsoMonedaFormato" = 'SPT' AND MONSEC."IdMoneda" = MSP."IdMonedaSecundaria")
		LEFT JOIN :v_logo LOGO
			ON (LOGO."LogoCorredora" IS NOT NULL)
		WHERE MSP."IdOperacion" = :p_IdOperacion;
		
	--Salida del SP
	SELECT * FROM :v_Movimientos_Spot;
	
END;
