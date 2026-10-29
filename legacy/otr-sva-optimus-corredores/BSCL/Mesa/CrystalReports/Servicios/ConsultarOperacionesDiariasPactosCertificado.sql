SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactosCertificado"; 
GO

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactosCertificado" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_FechaDesde		NVARCHAR(10),
	IN p_FechaHasta		NVARCHAR(10),
	IN p_IdCliente		TABLE ("IdCliente" BIGINT) DEFAULT EMPTY,
	IN p_IdEstado		TABLE ("IdEstado" NVARCHAR(10)) DEFAULT EMPTY,
	--IN p_Corredora	BIGINT,
	IN p_IdMesa			BIGINT,
	IN p_IdTipoProducto	NVARCHAR(4),
	IN p_OrdenarPor		NVARCHAR(40)
	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--Variables
	--------------------
	
	DECLARE v_CodigoUsuario		NVARCHAR(100);
	DECLARE v_FechaHoraSistema	SECONDDATE;
	DECLARE v_count INT;
	DECLARE v_contRep INT;
	DECLARE v_contFir INT;
	
	DECLARE v_FechaSistemaUTC   				NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   				NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   			NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL 			SECONDDATE;
	DECLARE v_FechaHoraUTC   					NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   					NVARCHAR(40);
	
	DECLARE v_FechaDesdeAux TIMESTAMP := TO_TIMESTAMP(p_FechaDesde||' 00:00:00','YYYY-MM-DD HH24:MI:SS');
	DECLARE v_FechaHastaAux TIMESTAMP := TO_TIMESTAMP(p_FechaHasta||' 23:59:59','YYYY-MM-DD HH24:MI:SS');
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_firmaRepresentanteLegal TABLE("FirmaSolicitante" BLOB);
	DECLARE v_firmaRepresentanteLegal_fin TABLE("FirmaSolicitante" BLOB);
	DECLARE v_IdRepLegal BIGINT;
	DECLARE v_IdDocumentoRepLegal BIGINT;
	--------------------
	--Lógica de Negocio 
	--------------------
	/** Obtener Logo  **/
		v_logo =	SELECT TOP 1
						"Binario" AS "LogoCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;	
	-- se obtiene Usuario
	SELECT "CodigoUsuario" INTO v_CodigoUsuario
	FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"
	WHERE "IdUsuario" = p_IdUsuario;
	/** DAtos Corredora**/
	v_DatosCorredora =
		SELECT 
			TOP 1
			--"EMPCORR"."IdPersona", --idPersona
			"PERCORR"."RazonSocial" AS "RazonSocialCorr",--razonSocial
			"CTACORR"."NombreBanco" AS "NombreBancoCorredora",--nombreBanco
			"DIBAN"."DocumentoIdentidad" AS "DocumentoIdentidadBancoCorredora", --rutbanco
			"REPCORR"."IdPersonaDestino" AS "IdRepresentanteLegalCorredora" --idPersonarepresentantelegal 26173
			FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS "EMPCORR"
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "PERCORR" ON "EMPCORR"."IdPersona"= "PERCORR"."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS "CTACORR" ON "CTACORR"."IdPersona" = "EMPCORR"."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS "BAN" ON  "BAN"."IdBanco"= "CTACORR"."IdBanco"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS "DIBAN" ON "DIBAN"."IdPersona" = "BAN"."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona" AS "REPCORR" ON "EMPCORR"."IdPersona"= "REPCORR"."IdPersona" AND "REPCORR"."IdTipoRelacion" IN ('RELEEN','REP','REPLEG') AND "REPCORR"."IdEstado" = 'ACT'
			WHERE "EMPCORR"."IdEmpresa" = p_IdEmpresa;
		

			SELECT REP."IdRepresentanteLegalCorredora"
			INTO v_IdRepLegal
			FROM :v_DatosCorredora AS REP;
   
	v_DatosRepresentanteLegal=
		SELECT 
			"DOCID"."IdPersona" AS "IdRepresentante",
			"DATREP"."NombreCompleto",--nombre
			"DOCID"."IdDocumentoIdentidad",--idDocumentoIdentidad 138895
			COALESCE("DOCID"."DocumentoIdentidad",' ') AS "DocumentoIdentidad"--documentoIdentidad
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DOCID" 
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "DATREP" ON "DOCID"."IdPersona" = "DATREP"."IdPersona"
			WHERE "DOCID"."IdPersona" = :v_IdRepLegal;

	
	SELECT COUNT(*)INTO v_contRep FROM :v_DatosRepresentanteLegal;
	IF (v_contRep = 0) THEN
		v_DatosRepresentanteLegal =
			SELECT
				0 	AS "IdRepresentante",
				'' 	AS "NombreCompleto",
				0 	AS "IdDocumentoIdentidad",
				'' 	AS "DocumentoIdentidad"
				FROM DUMMY;
	END IF;
	

	SELECT DAREP."IdDocumentoIdentidad"
	INTO v_IdDocumentoRepLegal
	FROM :V_DatosRepresentanteLegal AS DAREP;

	v_firmaRepresentanteLegal=
	SELECT  
			"Binario" AS "FirmaSolicitante"
			FROM "BSCL.SocioNegocio.BD::PER.ImagenesFirmas"
			WHERE "IdDocumentoIdentidad" = :v_IdDocumentoRepLegal
			ORDER BY "Version" DESC;
	SELECT COUNT(*) INTO v_contFir FROM :v_firmaRepresentanteLegal;
	IF (v_contFir = 0) THEN
	
	v_firmaRepresentanteLegal_fin= 
		SELECT 
		NULL AS "FirmaSolicitante"
		FROM DUMMY;
	ELSE
	v_firmaRepresentanteLegal_fin=
		SELECT FIR.* FROM :v_firmaRepresentanteLegal AS FIR;
	END IF;
		
	-- se obtiene fecha y hora de sistema
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaHoraSistema);
	
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
	
	v_OperacionesDiariasPactos= 
	SELECT
		 DISTINCT
		 OPE."FolioOperacion"
		,OPE."FechaOperacion"
		,OPE."IdTipoOperacion"
		,OPE."IdCliente"
		,COALESCE(OPE."DocumentoIdentidad",'') AS "DocumentoIdentidad"
		,OPE."RazonSocial"
		,OPE."IdClienteCorredor"
		,CASE WHEN CRR."Tipo"='J' THEN COALESCE(CRR."RazonSocial",'-') ELSE COALESCE(CRR."NombreCompleto",'-') END AS "RazonSocialCorredora"
		,OPE."Tasa"
		,OPE."IdTipoReajuste"
		,TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
		,TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento"
		,COALESCE((CASE AGE."Tipo" 
				WHEN 'N' THEN AGE."Nombres" ||' '|| AGE."ApellidoPaterno" || ' ' ||AGE."ApellidoMaterno" 
				ELSE AGE."RazonSocial"
			END),TO_NVARCHAR(OPE."IdAgente")) AS "Agente"
		,OPE."MontoOperacionInicial" AS "MontoOperacion"
		,OPE."MontoOperacionFinal" AS "MontoCompromiso"
		,OPE."IdEstado"
		,OPE."IdMesaOrigen"
		,OPE."NombreMesaOrigen"
		,OPE."IdMonedaLiquidacion"
		,CAR."INGMontoInteresCompraProximoMonedaInstrumento" AS "ValorInteresCartera"
		,OPE."IdTipoInstrumento"
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
		ON (OPE."IdOperacion" = CAR."FolioComprobante" AND CAR."IdTipoInstrumento" = 'PAC')
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLICRR
		ON (OPE."IdClienteCorredor" = CLICRR."IdCliente" AND OPE."IdEmpresa" = CLICRR."IdEmpresa")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" CRR
		ON (CLICRR."IdPersona" = CRR."IdPersona" AND CLICRR."IdEmpresa" = CRR."IdEmpresa" AND CRR."Corredora" = 'S' AND CRR."IdEstado" = 'ACT')
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AGE
		ON (OPE."IdAgente" = AGE."IdPersona" AND AGE."Borrado"='N')
	WHERE OPE."IdEstado" <> 'PEN'
	AND OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC');
		
-- se obtienen operaciones			
	v_Operaciones = 
		SELECT
		--------------- DATOS OPERACIONES ---------------
			--OPE."IdOperacion",
			OPE."FolioOperacion", 
			TO_VARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS  "FechaOperacion",
			OPE."FechaVencimiento",
			OPE."IdTipoOperacion",
			OPE."FechaLiquidacion",
			OPE."IdTipoReajuste",
			OPE."IdClienteCorredor",
			OPE."RazonSocialCorredora",
			OPE."Tasa",
			OPE."MontoOperacion",
			OPE."MontoCompromiso",
			OPE."ValorInteresCartera" AS "Interes",
			OPE."IdCliente",
			OPE."IdEstado",
			OPE."DocumentoIdentidad" AS "DocumentoIdentidadCliente",
			OPE."RazonSocial" AS "NombreCliente",
			OPE."IdMonedaLiquidacion",
			--------------- DATOS FILTROS ---------------
			TO_VARCHAR(:p_FechaDesde,'DD-MM-YYYY') AS "FiltroFechaOperacionDesde",
			TO_VARCHAR(:p_FechaHasta,'DD-MM-YYYY') AS "FiltroFechaOperacionHasta"
			--,CASE WHEN p_Corredora = 0 THEN 'TODOS' ELSE OPE."RazonSocialCorredora" END AS "FiltroCorredora"
			,'' AS "FiltroCorredora"
			,CASE WHEN p_IdMesa = 0 THEN 'TODOS' ELSE OPE."NombreMesaOrigen" END AS "Mesa" 
			,CASE WHEN p_OrdenarPor = 'CLIENTE' THEN 'NOMBRE CLIENTE' ELSE 'NOMBRE AGENTE' END AS "OrdenarPor"
			--------------- DATOS GENERALES ---------------
			,v_CodigoUsuario AS "Usuario"
			,TO_DATE(v_FechaHoraSistemaLOCAL,'YYYY-MM-DD HH24:MI:SS') AS "FechaHoraSistema"
			,MF."TasaDecimales"
		FROM :v_OperacionesDiariasPactos OPE
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = OPE."IdTipoInstrumento"
			AND MF."IdMoneda" = OPE."IdMonedaLiquidacion"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdTipoOperacion" = ''
		WHERE
		OPE."FechaOperacion" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux
		AND (OPE."IdMesaOrigen" = p_IdMesa OR p_IdMesa = 0)
		--AND (OPE."IdTipoProducto" = p_IdTipoProducto OR p_IdTipoProducto = '')
		ORDER BY (CASE WHEN p_OrdenarPor = 'CLIENTE' THEN OPE."RazonSocial" ELSE OPE."Agente" END) ASC;

	SELECT COUNT(*) INTO v_count FROM :p_IdCliente;
	IF (v_count != 0) THEN
	
		v_Operaciones = SELECT OP.* FROM :v_Operaciones AS OP INNER JOIN :p_IdCliente AS C ON C."IdCliente" = OP."IdCliente";
	
	END IF;

	SELECT COUNT(*) INTO v_count FROM :p_IdEstado;
	IF (v_count != 0) THEN
	
		v_Operaciones = SELECT OP.* FROM :v_Operaciones AS OP INNER JOIN :p_IdEstado AS C ON C."IdEstado" = OP."IdEstado";
	
	ELSE
	
		v_Operaciones = SELECT OP.* FROM :v_Operaciones AS OP WHERE OP."IdEstado" = 'VIG';
	
	END IF;
			
	SELECT COUNT(*) INTO v_count FROM :v_Operaciones;
	IF (v_count = 0) THEN
	v_Operacion_Fin=
	SELECT 
		0 			  AS "FolioOperacion"
		,'' AS "FechaOperacion"
		,'' AS "FechaVencimiento"
		,''			  AS "IdTipoOperacion"
		,'' AS "FechaLiquidacion"
		,'' 		  AS "IdTipoReajuste"
		,0 			  AS "IdClienteCorredor"
		,''			  AS "RazonSocialCorredora"
		,0 			  AS "Tasa"
		,0 			  AS "MontoOperacion"
		,0 			  AS "MontoCompromiso"
		,0			  AS   "Interes"
		,0	          AS "IdCliente"
		,''	          AS "IdEstado"
		,''			  AS "DocumentoIdentidadCliente"
		,''			  AS "NombreCliente"
		,''			  AS "IdMonedaLiquidacion"
		
		--------------- DATOS FILTROS ---------------
		,'' AS "FiltroFechaOperacionDesde"
		,'' AS "FiltroFechaOperacionHasta"
		,''			  AS "FiltroCorredora"
		,'' 		  AS "Mesa" 
		,'' 		  AS "OrdenarPor"
			
		--------------- DATOS GENERALES ---------------
		,''			  AS "Usuario"
		,'0001-01-01 00:00:00' AS "FechaHoraSistema"
		,0  AS "TasaDecimales"

	FROM DUMMY;
	
	ELSE
	v_Operacion_Fin=
	SELECT
		  OPE.*
	FROM :v_Operaciones AS OPE;
	/**Obtener Firma **/

	
	END IF;

	v_cross=
	SELECT * FROM :v_Operacion_Fin
			CROSS JOIN :v_DatosCorredora
			CROSS JOIN :v_logo
			CROSS JOIN :v_DatosRepresentanteLegal;
			
	-- NO BBORRAR CASE NI INNER JOIN 1=1
	
	IF (v_contFir != 0) THEN
	
		SELECT C.*,
		CASE WHEN  FIR."FirmaSolicitante" IS NULL THEN FIR."FirmaSolicitante" ELSE FIR."FirmaSolicitante" END  AS "FirmaSolicitante" FROM :v_firmaRepresentanteLegal_fin AS FIR inner JOIN :v_cross  AS C ON  1=1;
	
	ELSE

		SELECT * FROM :v_cross;

	END IF;

	--------------------
	--FIN SP 
	--------------------
END;
GO
