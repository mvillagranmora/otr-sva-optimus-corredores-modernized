SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::UAF.ConsultarDOF"; 
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::UAF.ConsultarDOF" AS

	SELECT
			 TES."IdComprobanteFormaDePago"
			,TES."FechaLiquidacion"
			,TES."IdComprobante"
			,TES."IdTipoOperacion"
			,TES."IdPersona"
			,PERC."DocumentoIdentidad" 						   -- AS "Identificacion"
			,PERC."TipoDocumentoIdentidad"		 				--AS "TipoDocumento"
			,PERC."Tipo"		 								--AS "TipoPersona"
			,PERC."NombreCompleto"								   
			,PERC."RazonSocial"								   
			,PERC."ApellidoPaterno" 							
			,PERC."ApellidoMaterno"
			,PERC."IdTelefonoPrincipal" 							
			,TES."IdPersonaTitularPago"
			,TES."MontoOperacion"
			,TES."IdMonedaOperacion"
			,TES."MontoComprobanteFormaDePago"
			,TES."IdMonedaFormaDePago"
			,TES."IdMedioDePago"
			,TES."NombreMedioDePago" 
			,TES."IdModalidadDePago"
			,TES."NombreModalidadDePago"
			,TES."IdEmpleadoDigitador"
			,TES."IdSucursalEmpleadoDigitador"
			,COALESCE(EMP."CodigoYNombreEmpleado",'') 			AS "CodigoYNombreEmpleado"			--AS "NombreAgente"
			,COALESCE(SUC."IdSucursal",0)						AS "IdSucursal"
			,COALESCE(SUC."CodigoSucursal",'')					AS "CodigoSucursal"
			,COALESCE(SUC."NombreSucursal",'')					AS "NombreSucursal"
			,SOC."IdCliente"
			,COALESCE(DEC."IdDeclaracionOrigenFondos",0)		AS "IdDeclaracionOrigenFondos"
			,COALESCE(DEC."Comentario",'') 						AS "Comentario"
			,COALESCE(EMP."IdEmpleado",0)						AS "IdEmpleado"
			,TES."IdEmpresa"
			,CASE COALESCE(DEC."IdDeclaracionOrigenFondos",0) WHEN 0 THEN 'N' ELSE 'S' END AS "Declaracion"
			,CASE COALESCE(DEC."IdDeclaracionOrigenFondos",0) WHEN 0 THEN 'UI_NO' ELSE 'UI_SI' END AS "DeclaracionUI"
			
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen" TES
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
		ON (TES."IdPersona"=SOC."IdPersona")
		INNER JOIN "BSCL.Normativa.BD::UAF.MedioDePagoDOF" PAG
		ON (PAG."IdEmpresa"=TES."IdEmpresa" AND PAG."IdMedioDePago" = TES."IdMedioDePago" AND PAG."IdEstado"='ACT')
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERC
		ON (TES."IdPersona"=PERC."IdPersona")
		--tiene o no DOF
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos" DEC
					ON (DEC."IdCliente" =SOC."IdCliente" AND TO_DATE(DEC."FechaDeclaracion")= TO_DATE(TES."FechaLiquidacion")
						AND TES."IdTipoOperacion"=DEC."IdTipoOperacionOrigenFondos" AND TES."IdEmpresa"=DEC."IdEmpresa")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP
		ON (EMP."IdPersona"=TES."IdEmpleadoDigitador")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC
		ON (TES."IdSucursalEmpleadoDigitador"=SUC."IdSucursal")
		LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" spt ON spt."IdOperacionSpot" = TES."IdOperacion" AND TES."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
		WHERE COALESCE(spt."OperacionFicticia", 'N') = 'N';		

GO	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::UAF.ConsultarDOF" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


