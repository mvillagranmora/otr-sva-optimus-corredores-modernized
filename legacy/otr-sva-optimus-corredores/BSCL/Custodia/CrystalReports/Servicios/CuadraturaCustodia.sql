SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CuadraturaCustodia";
GO

--
--  Proposito: servicio para consultar informacion de la cuadratura custodia.
--  Fecha Creacion: 18/01/2018
--

CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CuadraturaCustodia"(
	-- Parametros entrada 
	IN p_FechaCuadratura NVARCHAR(10),
	IN p_IdBoveda		NVARCHAR(3) DEFAULT ''
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
		
	v_CuadraturaCustodia = SELECT  CUSTODIA."IdCuadraturaCustodia" 			
									,CUSTODIA."FechaCuadraturaCustodia" 		
									,CUSTODIA."IdClasificacionTipoInstrumento"
									,CUSTODIA."IdTipoInstrumento"				
									,CUSTODIA."IdInstrumento" 				
									,CUSTODIA."Nemotecnico"
									,INSTRUMENTO."ISIN"
									,CUSTODIA."TipoCuenta"					
									,CUSTODIA."IdBoveda" 						
									,CUSTODIA."CuentaBoveda"					
									,CUSTODIA."CantidadCustodia"				
									,CUSTODIA."CantidadSimultaneaCompra" 		
									,CUSTODIA."CantidadSimultaneaVenta"	AS "SaldoReportado"	
									,CUSTODIA."CantidadPrestamo"				
									,CUSTODIA."CantidadEmprestito"			
									,CUSTODIA."CantidadGarantia" AS "SaldoBloqueado"				
									,CUSTODIA."CantidadTransitoIngreso"		
									,CUSTODIA."CantidadTransitoEgreso"		
									,CUSTODIA."CantidadTotal"					
									,CUSTODIA."MontoATasaMercadoActual"		
									,CUSTODIA."PosicionBoveda"				
									,CUSTODIA."PosicionBloqueada"			
									,CUSTODIA."PosicionReventa"				
									,CUSTODIA."PosicionRecompra"
									,CUSTODIA."PosicionSimultanea" AS "PosicionReportado"
									,CUSTODIA."Diferencia" AS "DiferenciaSaldoDisponible"
									,CUSTODIA."PosicionSimultanea" - CUSTODIA."CantidadSimultaneaVenta" AS "DiferenciaReportado"
									,CUSTODIA."PosicionBloqueada" - CUSTODIA."CantidadGarantia"	 AS "DiferenciaBloqueado"
									,CUSTODIA."PosicionReventa" - CUSTODIA."CantidadTransitoEgreso" AS "DiferenciaVenta"
									,CUSTODIA."PosicionRecompra" - CUSTODIA."CantidadTransitoIngreso" AS "DiferenciaCompra"
									,CUSTODIA."RutBeneficiario"				
									,CUSTODIA."NombreBeneficiario"			
									,CUSTODIA."FechaModificacion"				
									,CUSTODIA."IdUsuario" 					
									,CUSTODIA."IdEstado"
							FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" CUSTODIA
							INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INSTRUMENTO ON (CUSTODIA."IdInstrumento" = INSTRUMENTO."IdInstrumento")
							WHERE CUSTODIA."FechaCuadraturaCustodia" = p_FechaCuadratura
							AND (COALESCE(p_IdBoveda,'') = '' OR CUSTODIA."IdBoveda" = p_IdBoveda);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

	--Salida del SP
	SELECT * FROM :v_CuadraturaCustodia
	CROSS JOIN :v_logo;
	
END;
GO