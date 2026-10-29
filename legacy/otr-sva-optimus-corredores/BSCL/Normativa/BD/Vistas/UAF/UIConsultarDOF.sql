
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::UAF.UIConsultarDOF"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::UAF.UIConsultarDOF" AS

	SELECT
			 DOF."IdComprobanteFormaDePago"
			,DOF."FechaLiquidacion"
			,DOF."IdComprobante"
			,DOF."IdTipoOperacion"
			,OPER."Nombre" AS "TipoOperacion"
			,DOF."IdPersona"
			,DOF."DocumentoIdentidad" 						    AS "Identificacion"
			,DOF."TipoDocumentoIdentidad"		 				AS "TipoDocumento"
			,DOF."Tipo"		 									AS "TipoPersona"
			,DOF."NombreCompleto"
			,DOF."RazonSocial"		
			,DOF."ApellidoPaterno" 
			,DOF."ApellidoMaterno" 
			,DOF."IdPersonaTitularPago"
			,DOF."MontoOperacion"
			,DOF."IdMonedaOperacion"
			,DOF."MontoComprobanteFormaDePago"
			,DOF."IdMonedaFormaDePago"
			,Moneda."Nombre" 									AS "MonedaFormaDePago"
			,DOF."IdMedioDePago"
			,DOF."NombreMedioDePago" 
			,DOF."IdModalidadDePago"
			,DOF."NombreModalidadDePago"
			,DOF."IdEmpleadoDigitador"
			,DOF."CodigoYNombreEmpleado" 							AS "NombreAgente"
			,DOF."IdSucursal"
			,DOF."CodigoSucursal"
			,DOF."NombreSucursal"
			,DOF."IdCliente"
			,DOF."IdDeclaracionOrigenFondos"
			,DOF."Comentario"
			,DOF."IdSucursalEmpleadoDigitador"
			,DOF."IdEmpleado"
		FROM "BSCL.Normativa.BD.Vistas::UAF.ConsultarDOF" DOF
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER 
     					ON OPER."IdTipoOperacion" = DOF."IdTipoOperacion"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda
		ON (Moneda."IdMoneda" = DOF."IdMonedaFormaDePago");	

	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::UAF.UIConsultarDOF" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


