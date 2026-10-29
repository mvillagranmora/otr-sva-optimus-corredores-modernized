SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPN"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar Contrato Convenio uso plataforma Persona Natural
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 22/11/2016

--
"BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPN" (
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
		--------------------
		--Variables
		--------------------		
		v_info_cliente = SELECT 
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY') AS "FECHAACTUALAAA",
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'MM') AS "FECHAACTUALMM",
			TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'DD') AS "FECHAACTUALDD",
			
			CC."ApellidoMaterno" AS "PERSONAAPELLIDOMATERNO",
			CC."ApellidoPaterno" AS "PERSONAAPELLIDOPATERNO",
			CC."Nombres" AS "PERSONANOMBRES",
			CC."DocumentoIdentidad" AS "PERSONANUMERODOCUMENTO",
			
			CCDG."Apoderado1" AS "SOCIO_1NOMBRESOCIO",
			CCDG."DocumentoApoderado1" AS "SOCIO_1RUTSOCIO",
			CCDG."Apoderado2" AS "SOCIO_2NOMBRESOCIO",
			CCDG."DocumentoApoderado2" AS "SOCIO_2RUTSOCIO"

		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
		INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = CC."IdEmpresa"
		WHERE CC."DocumentoIdentidad" = REPLACE(:p_Rut,'.','') AND CC."IdEmpresa" = :p_IdEmpresa AND CC."Tipo" = 'N';

		SELECT * FROM :v_info_cliente;

		--------------------
		--FIN SP 
		--------------------
	END;
END;

