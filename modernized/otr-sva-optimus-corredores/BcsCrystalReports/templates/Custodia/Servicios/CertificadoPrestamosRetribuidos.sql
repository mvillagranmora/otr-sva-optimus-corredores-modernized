SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CertificadoPrestamosRetribuidos";
GO
--
--  Proposito: servicio para consultar prestamos retribuidos para certificado.
--  Fecha Creacion: 28/12/2017
--

CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CertificadoPrestamosRetribuidos"(
	-- Parametros entrada 
	IN p_IdEmpresa		BIGINT,
	IN p_FechaDesde		DATE,
	IN p_FechaHasta		DATE,
	IN p_IdCliente		BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN

	DECLARE v_Count_Mov BIGINT;
	v_Movimientos =
	
		--Prestamos
				SELECT 
				TO_VARCHAR(PRE."FechaPago",'DD-MM-YYYY') as "FechaPago"
				,PRE."Secuencia" AS "Folio"
				,EMI."NombreCompleto" AS "NombreSA"
				,PRE."DescripcionEvento" AS "DescripcionVC"
				,EMI."DocumentoIdentidad" AS "RutSA"
				,PRE."Nemo"
				,PRE."CantidadDerecho"
				,PRE."Factor"
				,PRE."Total"
				,YEAR(PRE."FechaCierre") AS "FechaYear"
			FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" AS PRE
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (PRE."IdInstrumento" = INS."IdInstrumento")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON (INS."IdEmisor" = EMI."IdEmisor")
			WHERE PRE."IdEmpresa" = :p_IdEmpresa
			AND PRE."IdTipoCartera" = 'PRE'
			AND TO_DATE(PRE."FechaCierre") BETWEEN :p_FechaDesde AND :p_FechaHasta
			AND PRE."IdCliente" = :p_IdCliente
			ORDER BY  PRE."FechaCierre";
			
			
	SELECT COUNT(*) INTO v_Count_Mov FROM :v_Movimientos;
	
	IF v_Count_Mov  < 1 THEN		
		v_Movimientos = SELECT 
						'0000-00-00'  AS "FechaPago"
						,0            AS "Folio"
						,' '		  AS "NombreSA"
						,' '		  AS "DescripcionVC"
						,' '		  AS "RutSA"
						,' '		  AS "Nemo"			
						,0			  AS "CantidadDerecho"
						,0			  AS "Factor"
						,0			  AS "Total"
						,'0000'       AS "FechaYear"
		FROM DUMMY;	
	END IF;
	
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	v_datosEmpresa = SELECT
		"RazonSocial" AS "RazonCorredora", 
		"Direccion" AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora",
		"Email" AS "EmailCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
	
	v_datosCliente = SELECT CLIENTE."NombreCompleto",CLIENTE."DocumentoIdentidad",DIRECCION."NombreLargo",DIRECCION."NombrePais" 
						FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION ON (CLIENTE."IdDireccionFisicaPrincipal" = DIRECCION."IdDireccionFisica")
						WHERE "IdCliente" = :p_IdCliente;
	
	v_FechaParametros = SELECT TO_CHAR(:p_FechaDesde,'DD-MM-YYYY') AS "FechaDesde",
							   TO_CHAR(:p_FechaHasta,'DD-MM-YYYY') AS "FechaHasta"
							   FROM DUMMY;
							   
							   
	
	--Salida del SP
	SELECT * FROM :v_datosCliente 
	CROSS JOIN :v_logo
	CROSS JOIN :v_datosEmpresa
	CROSS JOIN :v_FechaParametros
	CROSS JOIN :v_Movimientos;
	
END;
GO