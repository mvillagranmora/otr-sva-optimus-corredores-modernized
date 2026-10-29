SET SCHEMA "BSCL";
GO
 
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::ConsultarAnexo2";
GO

CREATE PROCEDURE
--
--  Proposito: Obtener datos para la confeccion del Anexo 2
--  Autor: JLS
--  Fecha Creacion: 10-05-2017
--
 
"BSCL.Custodia.CrystalReports.Servicios::ConsultarAnexo2" (
	-- Parametros entrada 
	IN p_IdEmpresa				BIGINT,	
	IN p_FolioPrestamo			BIGINT,
	IN p_FolioEmprestito		BIGINT DEFAULT 0,
	IN p_ConsultaHistorica	    BIGINT DEFAULT 0
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_FechaSistema date;
		DECLARE v_NombreCorredor, v_RutCorredor NVARCHAR(250) := '';


		v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		
		SELECT Top 1 "Valor" into v_FechaSistema 
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
		WHERE "CodigoParametro" = 'PLA.FechaSistema' and "IdEmpresa" = :p_IdEmpresa ;
		
		SELECT top 1 "Nombre", "DocumentoIdentidad" INTO v_NombreCorredor, v_RutCorredor FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		IF p_ConsultaHistorica = 1 
			THEN
				SELECT top 1 
					prestamo."FolioPrestamo",
					IFNULL(emprestito."FolioEmprestito",0) 		AS "FolioEmprestito",
					prestamo."IdCliente" 						AS "IdClientePrestatario",
					COALESCE(prestamo."DocumentoIdentidad", '')	AS "DocumentoIdentidadPrestatario",
					COALESCE(prestamo."RazonSocial",'') 		AS "NombrePrestatario",
					prestamo."FechaVencimientoPrestamo",
					IFNULL(emprestito."IdCliente",0) 			AS "IdClientePrestamista",
					IFNULL(emprestito."DocumentoIdentidad",'') 	AS "DocumentoIdentidadPrestamista" ,
					COALESCE(emprestito."RazonSocial",'') 		AS "NombrePrestamista",
					prestamo."IdClasificacionTipoInstrumentoSubyacente" AS "IdMercado",
					mercado."Nombre" 							AS "NombreMercado",
					IFNULL(prestamo."NemoSubyacente",'') 		AS "NemotecnicoEmprestito",
					IFNULL(emprestito."MontoInstrumento",0) as "Monto",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGCantidad" ELSE prestamo."INGCantidad" END,0) AS "Cantidad",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGMontoPremio" ELSE prestamo."INGMontoPrima" END ,0) AS "Premio",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGMontoTotalEmprestito" ELSE prestamo."INGMontoTotalPrestamo" END,0) AS "MontoPlazo",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGTasaEmprestito" ELSE prestamo."INGTasaPrestamo" END,0) AS "PremioTasa",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."PrecioCompraInicial" ELSE prestamo."PrecioCompraInicial" END,0) AS "PrecioCompraInicial",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGPrecioInstrumento" ELSE prestamo."INGPrecioInstrumento" END,0) AS "PrecioInstrumento",
					:v_FechaSistema AS "FechaSistema",
			    	:v_NombreCorredor AS "NombreCorredor",
			    	:v_RutCorredor AS "RutCorredor",
					COALESCE(prestamo."FechaLiquidacion",'') AS "FechaPrestamo",
			    	"LOGO"."LogoCorredora"
			    	
				FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" AS prestamo
					LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico" AS emprestito ON prestamo."FolioPrestamo" = emprestito."FolioPrestamo" AND emprestito."FolioEmprestito" = p_FolioEmprestito
					LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS mercado on prestamo."IdClasificacionTipoInstrumentoSubyacente" = mercado."IdClasificacionTipoInstrumento"
					LEFT JOIN :v_logo AS "LOGO" ON ("LOGO"."LogoCorredora" IS NOT NULL)
				WHERE 
					prestamo."FolioPrestamo" = :p_FolioPrestamo;
			ELSE
				SELECT top 1 
					prestamo."FolioPrestamo",
					IFNULL(emprestito."FolioEmprestito",0) 		AS "FolioEmprestito",
					prestamo."IdCliente" 						AS "IdClientePrestatario",
					COALESCE(prestamo."DocumentoIdentidad", '')	AS "DocumentoIdentidadPrestatario",
					COALESCE(prestamo."RazonSocial",'') 		AS "NombrePrestatario",
					prestamo."FechaVencimientoPrestamo",
					IFNULL(emprestito."IdCliente",0) 			AS "IdClientePrestamista",
					IFNULL(emprestito."DocumentoIdentidad",'') 	AS "DocumentoIdentidadPrestamista" ,
					COALESCE(emprestito."RazonSocial",'') 		AS "NombrePrestamista",
					prestamo."IdClasificacionTipoInstrumentoSubyacente" AS "IdMercado",
					mercado."Nombre" 							AS "NombreMercado",
					IFNULL(prestamo."NemoSubyacente",'') 		AS "NemotecnicoEmprestito",
					IFNULL(emprestito."MontoInstrumento",0) as "Monto",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGCantidad" ELSE prestamo."INGCantidad" END,0) AS "Cantidad",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGMontoPremio" ELSE prestamo."INGMontoPrima" END ,0) AS "Premio",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGMontoTotalEmprestito" ELSE prestamo."INGMontoTotalPrestamo" END,0) AS "MontoPlazo",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGTasaEmprestito" ELSE prestamo."INGTasaPrestamo" END,0) AS "PremioTasa",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."PrecioCompraInicial" ELSE prestamo."PrecioCompraInicial" END,0) AS "PrecioCompraInicial",
					IFNULL(CASE WHEN p_FolioEmprestito > 0 THEN emprestito."INGPrecioInstrumento" ELSE prestamo."INGPrecioInstrumento" END,0) AS "PrecioInstrumento",
					:v_FechaSistema AS "FechaSistema",
			    	:v_NombreCorredor AS "NombreCorredor",
			    	:v_RutCorredor AS "RutCorredor",
					COALESCE(prestamo."FechaLiquidacion",'') AS "FechaPrestamo",
			    	"LOGO"."LogoCorredora"
			    	
				FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" AS prestamo
					LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS emprestito ON prestamo."FolioPrestamo" = emprestito."FolioPrestamo" AND emprestito."FolioEmprestito" = p_FolioEmprestito
					LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS mercado on prestamo."IdClasificacionTipoInstrumentoSubyacente" = mercado."IdClasificacionTipoInstrumento"
					LEFT JOIN :v_logo AS "LOGO" ON ("LOGO"."LogoCorredora" IS NOT NULL)
				WHERE 
					prestamo."FolioPrestamo" = :p_FolioPrestamo;
		END IF;
END;


