SET SCHEMA "BSCL"; 
 
DROP PROCEDURE "BSCL.SocioNegocio.CrystalReports.Servicios::ComprobanteCompleto"; 
 
CREATE PROCEDURE
--
--  Proposito: Servicio dummy para generar Comprobante de Orden
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 27/07/2016

--
"BSCL.SocioNegocio.CrystalReports.Servicios::ComprobanteCompleto" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdUsuario				BIGINT,			--ID IdUsuario
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_RutCliente			NVARCHAR(20),
	IN p_Portafolio			NVARCHAR(20)
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
		CREATE LOCAL TEMPORARY TABLE #contrato (
		"IdEmpresa" BIGINT,
		"RazonSocial" NVARCHAR(200),
		"RUT"	NVARCHAR(40),
		"Direccion"	NVARCHAR(200),
		"Telefono"	NVARCHAR(20),
		"Email"	NVARCHAR(100),
		"Fax"	NVARCHAR(20),
		"Apoderado1"	NVARCHAR(1000),
		"DocumentoApoderado1"	NVARCHAR(1000),
		"LogoPrincipal"	NVARCHAR(100),
		"LogoPrincipalBin"	BLOB,
		"LogoPrincipalTipo"	NVARCHAR(10),
		"LogoSecundario"	NVARCHAR(100),
		"LogoSecundarioBin"	BLOB,
		"LogoSecundarioTipo"	NVARCHAR(10),
		"RUTCLIENTE" NVARCHAR(200),
		"PORTAFOLIO" NVARCHAR(200)
		);
		
		BEGIN
			INSERT INTO #contrato("IdEmpresa","RazonSocial","RUT","Direccion","Telefono","Email","Fax","Apoderado1","DocumentoApoderado1","LogoPrincipal","LogoPrincipalBin","LogoPrincipalTipo","LogoSecundario","LogoSecundarioBin","LogoSecundarioTipo","RUTCLIENTE","PORTAFOLIO")
			(select
				"IdEmpresa",
				"RazonSocial",
				"RUT",
				"Direccion",
				"Telefono",
				"Email",
				"Fax",
				"Apoderado1",
				"DocumentoApoderado1",
				'' AS "LogoPrincipal",
				'' AS "LogoPrincipalBin",
				'' AS "LogoPrincipalTipo",
				'' AS "LogoSecundario",
				'' AS "LogoSecundarioBin",
				'' AS "LogoSecundarioTipo",
				:p_RutCliente AS "RUTCLIENTE",
				:p_Portafolio AS "PORTAFOLIO" 
			from "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales");
			
			UPDATE #contrato
			SET "LogoPrincipal" = "NombreImagen", "LogoPrincipalBin" = "Binario" 
			FROM #contrato, "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_BOLSA';


			select * from #contrato;
		END;
		DROP TABLE #contrato;
		--------------------
		--FIN SP 
		--------------------
	END;
END;
