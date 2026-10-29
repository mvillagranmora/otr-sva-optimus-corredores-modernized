SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarDatosPortafolio";


CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarDatosPortafolio" AS
SELECT
    POR."IdCliente" AS "IdCliente"
	,POR."IdPortafolio" AS "IdPortafolio"
	,POR."CodigoPortafolio" AS "CodigoPortafolio"
	,POR."IdPerfilComercialBaseOrigen" "IdPerfilComercial"
	,POR."IdPerfilCobros" AS "IdPerfilCobros"
	,POR."IdPerfilProductos" AS "IdPerfilProductos"
	,VP."NombreValorIndicador" AS "IdFormaCobro"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS VP 
	ON VP."IdPortafolio" = POR."IdPortafolio" 
	AND VP."CodigoTipoIndicador" = 'COBROCUST';

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarDatosPortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION;
