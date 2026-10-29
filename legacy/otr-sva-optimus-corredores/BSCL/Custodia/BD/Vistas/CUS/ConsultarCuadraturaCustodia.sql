SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia"

AS 

SELECT 
			 A."IdCuadraturaCustodia" 			
			,A."FechaCuadraturaCustodia" 		
			,A."IdClasificacionTipoInstrumento"
			,i."Nombre" 										as "ClasificacionTipoInstrumento"
			,A."IdTipoInstrumento"				
			,D."Nombre" 										as "NombreTipoInstrumento"
			,A."IdInstrumento" 				
			,A."Nemotecnico"					
			,A."TipoCuenta"
			,CASE 
				WHEN A."TipoCuenta" = 'REL' THEN 'Relacionado'
				WHEN A."TipoCuenta" = 'TER' THEN 'Terceros'
				WHEN A."TipoCuenta" = 'CPRO' THEN 'Cuenta Propia'
			END													as "NombreTipoCuenta"
			,A."IdBoveda" 						
			,A."CuentaBoveda"					
			,A."CantidadCustodia"				
			,A."CantidadSimultaneaCompra"		
			,A."CantidadSimultaneaVenta"		
			,A."CantidadPrestamo"				
			,A."CantidadEmprestito"			
			,A."CantidadGarantia"				
			,A."CantidadTransitoIngreso"		
			,A."CantidadTransitoEgreso"		
			,A."CantidadTotal"
			,A."CantidadPactoCompra"
			,A."CantidadPactoVenta"
			,A."MontoATasaMercadoActual"		
			,A."PosicionBoveda"				
			,A."PosicionBloqueada"				
			,A."PosicionReventa"				
			,A."PosicionRecompra"				
			,A."PosicionTotal"
			,A."RutBeneficiario"				
			,A."NombreBeneficiario"			
			,A."Diferencia"
			,A."ExplicacionDescuadratura"
			,A."FechaModificacion"				
			,A."IdUsuario" 					
			,A."IdEstado"
	FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" A
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" i 
		ON i."IdClasificacionTipoInstrumento" = A."IdClasificacionTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D 
		ON D."IdTipoInstrumento" = A."IdTipoInstrumento"
	WHERE A."IdEstado" <> 'ANU'
	ORDER BY A."IdBoveda" , A."CuentaBoveda";
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia" TO BCS_DESARROLLADOR WITH GRANT OPTION;
