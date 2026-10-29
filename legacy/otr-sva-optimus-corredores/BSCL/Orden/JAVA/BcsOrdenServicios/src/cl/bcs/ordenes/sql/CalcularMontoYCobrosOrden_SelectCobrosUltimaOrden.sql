select "IdPortafolioCliente" as "IdPortafolio","IdTipoOperacion","PorcentajeCobros","IdOrden" 
from (
	select "IdPortafolioCliente","IdOrden","IdTipoOperacion","PorcentajeCobros",row_number() over (partition by "IdPortafolioCliente","IdTipoOperacion" order by "IdOrden" desc) as "orden"
	from "BSCL.Orden.BD::ORD.Orden" 
	where "IdPortafolioCliente" in (:idsPortafolio) and "IdTipoOperacion" in (:idsTipoOperacion)
) where "orden" = 1

