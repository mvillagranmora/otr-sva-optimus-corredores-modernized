---------------------------------------------------------------
-- VISTA PARA bcs-combobox-cliente-portafolio.
-- NO UTILIZAR EN EL NEGOCIO, PARA ESTO HACER USO DE LA VISTA  
-- "BSCL.Portafolio.BD.Vistas::POR.ConsultarPortafolio"
---------------------------------------------------------------

SET SCHEMA BSCL;
GO 
DROP VIEW BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolioSpot";
GO
CREATE VIEW BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolioSpot" AS

SELECT
								cli."DocumentoIdentidad" || ' ' || cli."NombreCompleto" || CASE WHEN COALESCE(ProductoAgrupador."NombreProductoAgrupador",'') != '' THEN ' - ' || ProductoAgrupador."NombreProductoAgrupador" ELSE '' END || CASE WHEN COALESCE(port."Alias",'') != '' THEN ' - ' || port."Alias" ELSE '' END || CASE WHEN fan."DescripcionNombreFantasia" IS NOT NULL THEN ' - ' || fan."DescripcionNombreFantasia" ELSE '' END ||' (' || port."CodigoPortafolio" || ')' as "NombreCompletoPortafolio",
								LPAD(port."CodigoPortafolio",3,' ') AS "CodigoPortafolioPad",
								port."IdPortafolio", 
								port."IdCliente", 
								port."IdEstado",
								cli."NombreCompleto",
								cli."IdEmpresa",
								cli."IdPersonaEjecutivo",
								cli."IdEstado" as "IdEstadoCliente",
								cli."Tipo" as "TipoPersona",
								COALESCE("IdContraparte", 0) AS "IdContraparte"
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
							LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON soc."IdCliente" = cli."IdCliente"
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" fan on port."IdNombreFantasia" = fan."IdNombreFantasia"
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
									ON ProductoAgrupador."IdProductoAgrupador" = port."IdProductoAgrupador";
GO
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolioSpot" TO BCS_DESARROLLADOR WITH GRANT OPTION; 