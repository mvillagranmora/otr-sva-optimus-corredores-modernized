SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::MovimientosTesoreria";
GO
--
--  Proposito: servicio para consultar movimientos tesoreria.
--  Fecha Creacion: 05/02/2018
--

CREATE PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::MovimientosTesoreria"(

	-- Parametros entrada
	IN p_IdEmpresa					  BIGINT, 
	IN p_IdPersona					  BIGINT,
	IN p_IdTipoMovimiento			  NVARCHAR(10),
	IN p_IdPortafolioTitularPago      BIGINT,
	IN p_IdCaja		  				  BIGINT,
	IN p_IdMoneda    				  NVARCHAR(3),
	IN p_IdConcepto					  NVARCHAR(8),
	IN p_IdEstado					  NVARCHAR(3),
	IN p_FechaDesde					  NVARCHAR(20),
	IN p_FechaHasta					  NVARCHAR(20),
	IN p_IdTipoPersona				  NVARCHAR(3)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	-- Movimientos Tesoreria
	v_Movimientos =
		SELECT 
			"IdIngresoEgreso",
			"NombreTipoMovimiento",
			"NombreConcepto",
			TO_CHAR("FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion",
			TO_CHAR("FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaIngresoRegistro",
			"Monto",
			"NombreCaja",	
			"IdMoneda",
			"NombreTipoProducto",
			"NombrePersona",
			"NombreTipoSocioDeNegocio",			
			"DocumentoIdentidad",
			"NombrePersonaTitularPago"	
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso"
		WHERE (:p_IdPersona =0				   OR "IdPersona" 				 =:p_IdPersona )      
		AND   (:p_IdPortafolioTitularPago = 0  OR "IdPortafolioTitularPago"  =:p_IdPortafolioTitularPago )
		AND   (:p_IdTipoMovimiento = '' 	   OR "IdTipoMovimiento"         =:p_IdTipoMovimiento)
		AND	  (:p_IdCaja = 0 				   OR "IdCaja" 					 =:p_IdCaja)
		AND   (:p_IdMoneda='' 				   OR "IdMoneda" 				 =:p_IdMoneda)
		AND	  (:p_IdConcepto='' 			   OR "IdConcepto" 				 =:p_IdConcepto)
		AND   (:p_IdEstado='' 				   OR "IdEstado" 				 =:p_IdEstado)
		AND   (:p_IdTipoPersona='' 			   OR "IdTipoPersona" 			 =:p_IdTipoPersona)
		AND   TO_DATE("FechaLiquidacion",'YYYY-MM-DD')     BETWEEN TO_DATE(:p_FechaDesde,'YYYY-MM-DD')
															   AND TO_DATE(:p_FechaHasta,'YYYY-MM-DD');				
	
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

		
	--Salida del SP
	SELECT * FROM :v_Movimientos
	CROSS JOIN :v_logo
	CROSS JOIN :v_datosEmpresa;	
	
	
END;
GO