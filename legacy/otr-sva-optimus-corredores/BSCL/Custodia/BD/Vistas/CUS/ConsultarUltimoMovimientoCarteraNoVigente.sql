SET SCHEMA "BSCL";  
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraNoVigente";
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraNoVigente" AS

select
c2."IdEstado",c2."FolioCartera", c2."IdCartera", c2."IdTipoInstrumento", c2."IdInstrumento"
from "BSCL.Custodia.BD::CUS.Cartera" as c2
inner join (
			select
			MAX(c."IdCartera") AS "IdCartera"
			from "BSCL.Custodia.BD::CUS.Cartera" as c
			inner join (SELECT sim."FolioCartera", MAX(sim."IdCartera") as "IdCartera","IdEstado"
			            FROM "BSCL.Custodia.BD::CUS.Cartera" as sim
			            GROUP BY sim."FolioCartera", sim."IdEstado") as sim on (sim."IdCartera"= c."IdCartera")
			group by c."FolioCartera"
)as c1 on (c1."IdCartera"= c2."IdCartera" and c2."IdEstado" != 'VIG');
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraNoVigente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;