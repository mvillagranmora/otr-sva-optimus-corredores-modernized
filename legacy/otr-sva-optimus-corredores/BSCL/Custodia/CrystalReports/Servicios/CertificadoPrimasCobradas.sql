SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CertificadoPrimasCobradas";
GO
--
--  Proposito: servicio para primas pagadas
--  Fecha Creacion: 28/12/2017
--

CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CertificadoPrimasCobradas"(
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
				TO_VARCHAR(PRE."FechaLiquidacion",'DD-MM-YYYY') as "FechaPago"
				,PRE."FolioPrestamo" AS "FolioPrestamo"
				,PRE."IdTipoComprobante" AS "TipoComprobante"
				,PRE."FolioComprobante" AS "NumeroDocumento"
				,YEAR("FechaLiquidacion") AS "FechaYear"
				,TO_VARCHAR(PRE."FechaPrestamo",'DD-MM-YYYY') AS "FechaDesdeOperacion"
				,TO_VARCHAR(PRE."FechaVencimientoPrestamo",'DD-MM-YYYY') AS "FechaHastaOperacion"
				,PRE."PlazoEnDiasOperacion"
				,PRE."NemoSubyacente"
				,PRE."INGCantidad"
				,PRE."INGTasaPrestamo"
				,PRE."INGMontoTotalPrestamo"
				,PRE."INGMontoPrima"
			FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" AS PRE
			WHERE PRE."IdEmpresa" = :p_IdEmpresa
			AND TO_DATE(PRE."FechaLiquidacion") BETWEEN :p_FechaDesde AND :p_FechaHasta
			AND PRE."IdCliente" = :p_IdCliente
			ORDER BY  PRE."FechaLiquidacion";
			
	SELECT COUNT(*) INTO v_Count_Mov FROM :v_Movimientos;
	
	IF v_Count_Mov  < 1 THEN		
		v_Movimientos = SELECT 
						'0000-00-00'  AS "FechaPago"
						,0            AS "FolioPrestamo"
						,' '		  AS "TipoComprobante"
						,0			  AS "NumeroDocumento"
						,'0000'       AS "FechaYear"
						,'0000-00-00' AS "FechaDesdeOperacion"
						,'0000-00-00' AS "FechaHastaOperacion"						
						,0			  AS "PlazoEnDiasOperacion"
						,' '		  AS "NemoSubyacente"	
						,0			  AS "INGCantidad"
						,0			  AS "INGTasaPrestamo"
						,0			  AS "INGMontoTotalPrestamo"
						,0			  AS "INGMontoPrima"
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
		"Telefono" AS "TelefonoCorredora" ,
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
	SELECT * FROM :v_Movimientos
	CROSS JOIN :v_logo
	CROSS JOIN :v_datosEmpresa
	CROSS JOIN :v_datosCliente
	CROSS JOIN :v_FechaParametros;
	
END;
GO