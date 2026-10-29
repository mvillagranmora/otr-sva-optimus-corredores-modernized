SET SCHEMA "BSCL"; 
GO

DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::GenerarComprobanteGarantiaIngresoRetiro"; 
GO

CREATE PROCEDURE
--
--  Proposito: Servicio para generar Comprobante de ingreso retiro titulos custodia.
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 09/02/2017
--
"BSCL.Custodia.CrystalReports.Servicios::GenerarComprobanteGarantiaIngresoRetiro" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
    IN p_IdCliente							BIGINT,
	IN p_IdPortafolio						BIGINT,
	IN p_FolioGarantia						BIGINT,
	IN p_IngresoRetiro 						NVARCHAR(10) DEFAULT 'INGRESO' 
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
		DECLARE v_cantidad BIGINT;
		DECLARE v_moneda NVARCHAR(5);
		DECLARE v_cantidad_texto NVARCHAR(1000);
		
		v_MovimientoGarantia = SELECT TOP 1
		M."FolioGarantia",
		M."IdOrigenGarantia",
		M."NombreCliente",
		M."DocumentoIdentidad",
		M."CodigoPortafolio",
		M."Cantidad",
		p_IngresoRetiro AS "TipoOperacion",
		TO_NVARCHAR(M."FechaIngresoRegistro",'DD-MM-YYYY HH24:MI:SS') AS "FechaIngresoRegistro",
		0 AS "IdCartera",
		M."PrecioGarantia",
		M."Nemotecnico",
		M."IdEmisor",
		E."NombreCorto" AS "NombreEmisor",
		M."IdBovedaOperacionOrigen",
		b."Nombre" AS "NombreBoveda",
		TO_NVARCHAR(M."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
		M."NombreConcepto",
		CASE WHEN COALESCE(M."DescripcionEspecie",'') = ''THEN  'MOVIMIENTO DE ' || M."NombreOrigenGarantia" ELSE M."DescripcionEspecie" END AS "Glosa",
		CPDF."NombreLargo" AS "DireccionFisica",
		CPT."Anexo" || ' ' || CPT."Numero" AS "Telefono",
		M."IdAgente",
		CE1."NombreCompleto" AS "Agente",
		M."IdUsuario",
		CE2."NombreCompleto" AS "AtendidoPor",
		M."IdMonedaInstrumento",
		M."MontoGarantiaMonedaInstrumento", M."IdCliente",M."IdPortafolio",
		D."Nombre" AS "NombreTipoInstrumento",
		MF."PrecioDecimales",
		M."RazonSocialContraparte"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" M
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" =  M."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" E ON E."IdEmisor" = M."IdEmisor"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" B ON B."IdBoveda" = M."IdBovedaOperacionOrigen" 	
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPDF ON CPDF."IdPersona" =  CC."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona" =  CC."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE1 ON CE1."IdEmpleado" = CASE WHEN M."IdAgente">0 THEN M."IdAgente" ELSE CC."IdEmpleadoEjecutivo" END 
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U2 ON U2."IdUsuario" = M."IdUsuario"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON U2."IdEntidad" = CE2."IdEmpleado" AND U2."TipoEntidad" = 'EMP'
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D ON D."IdTipoInstrumento" = M."IdTipoInstrumentoGarantizado"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = M."IdTipoInstrumento"
			AND MF."IdMoneda" = M."IdMonedaInstrumento"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdTipoOperacion" = ''
		WHERE M."IdCliente" = :p_IdCliente AND M."IdPortafolio" = :p_IdPortafolio AND M."FolioGarantia" = :p_FolioGarantia;
		
		--Obtener cantidad en texto.
		SELECT COALESCE("Cantidad",0) AS "Cantidad", COALESCE("IdMonedaInstrumento",'') AS "IdMonedaInstrumento" INTO v_cantidad, v_moneda FROM :v_MovimientoGarantia;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(v_cantidad,'CL', v_moneda, :v_cantidad_texto);
		v_cantidad_t = select ('(' || v_cantidad_texto || ')') AS "CantidadTexto" FROM DUMMY;
		
		--Obtener datos generales y logo.
		v_DatosGenerales = SELECT
		"RazonSocial" AS "RazonCorredora", 
		"Direccion" AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora" 
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		v_Logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		--------------------
		--Lógica de Negocio 
		--------------------
		SELECT * FROM :v_MovimientoGarantia
		CROSS JOIN :v_cantidad_t
		CROSS JOIN :v_DatosGenerales
		CROSS JOIN :v_logo;

		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
