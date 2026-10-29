SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPJ"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar Contrato Convenio uso plataforma Persona Juridica
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 22/11/2016

--
"BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPJ" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdUsuario				BIGINT,			--ID IdUsuario
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_Rut			NVARCHAR(20)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	BEGIN
		v_info_cliente = SELECT
			CC."IdPersona",
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY') AS "FECHAACTUALAAA",
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'MM') AS "FECHAACTUALMM",
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'DD') AS "FECHAACTUALDD",
			
			CC."ApellidoMaterno" AS "PERSONAAPELLIDOMATERNO",
			CC."ApellidoPaterno" AS "PERSONAAPELLIDOPATERNO",
			CC."NombreCompleto" AS "PERSONANOMBRES",
			CC."DocumentoIdentidad" AS "PERSONANUMERODOCUMENTO",
			
			CC."NombreCompleto" AS "PERSONARAZONSOCIAL",			
			
			CCDG."Apoderado1" AS "SOCIO_1NOMBRESOCIO",
			CCDG."DocumentoApoderado1" AS "SOCIO_1RUTSOCIO",
			CCDG."Apoderado2" AS "SOCIO_2NOMBRESOCIO",
			CCDG."DocumentoApoderado2" AS "SOCIO_2RUTSOCIO"
						
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = CC."IdEmpresa"
			WHERE CC."DocumentoIdentidad" = REPLACE(:p_Rut,'.','') AND CC."IdEmpresa" = :p_IdEmpresa AND CC."Tipo" = 'J';
		
		v_info_cliente_apo1 = SELECT TOP 1
			ifc."IdPersona",
			"FECHAACTUALAAA",
			"FECHAACTUALMM",
			"FECHAACTUALDD",
			
			"PERSONAAPELLIDOMATERNO",
			"PERSONAAPELLIDOPATERNO",
			"PERSONANOMBRES",
			"PERSONANUMERODOCUMENTO",
			
			"PERSONARAZONSOCIAL",
			
			"SOCIO_1NOMBRESOCIO",
			"SOCIO_1RUTSOCIO",
			"SOCIO_2NOMBRESOCIO",
			"SOCIO_2RUTSOCIO",
			
			CRP."ApellidoMaterno" AS "RELLEGAL_1APELLIDOMATERNO",
			CRP."ApellidoPaterno" AS "RELLEGAL_1APELLIDOPATERNO",
			CRP."Nombres" AS "RELLEGAL_1NOMBRES",
			CRP."DocumentoIdentidadDefecto" AS "RELLEGAL_1RUTDV"
			
			FROM :v_info_cliente ifc
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" CRP ON CRP."IdPersonaOrigen" = ifc."IdPersona" AND CRP."IdTipoRelacion" = 'APOFIR' AND CRP."IdEstado" = 'ACT'; 
		
		v_info_cliente_apo2 = SELECT TOP 1
			"FECHAACTUALAAA",
			"FECHAACTUALMM",
			"FECHAACTUALDD",
			
			"PERSONAAPELLIDOMATERNO",
			"PERSONAAPELLIDOPATERNO",
			"PERSONANOMBRES",
			"PERSONANUMERODOCUMENTO",
			
			"PERSONARAZONSOCIAL",
			
			"SOCIO_1NOMBRESOCIO",
			"SOCIO_1RUTSOCIO",
			"SOCIO_2NOMBRESOCIO",
			"SOCIO_2RUTSOCIO",
			
			"RELLEGAL_1APELLIDOMATERNO",
			"RELLEGAL_1APELLIDOPATERNO",
			"RELLEGAL_1NOMBRES",
			"RELLEGAL_1RUTDV",
			
			CRP."ApellidoMaterno" AS "RELLEGAL_2APELLIDOMATERNO",
			CRP."ApellidoPaterno" AS "RELLEGAL_2APELLIDOPATERNO",
			CRP."Nombres" AS "RELLEGAL_2NOMBRES",
			CRP."DocumentoIdentidadDefecto" AS "RELLEGAL_2RUTDV"
			
			FROM :v_info_cliente_apo1 ifc
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" CRP ON CRP."IdPersonaOrigen" = ifc."IdPersona" AND CRP."IdTipoRelacion" = 'APOFIR' AND CRP."IdEstado" = 'ACT' AND CRP."DocumentoIdentidadDefecto" != "RELLEGAL_1RUTDV";
			
			
			--SALIDA
			SELECT * FROM :v_info_cliente_apo2;

		--------------------
		--FIN SP 
		--------------------
	END;
END;