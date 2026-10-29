set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Datos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Datos" as
    
select
    a."ID",
	a."IdDocumento",
	a."VboUsuario",
	COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones",
	a."FuenteDatos",
	a."RutCliente",
	a."IdCliente",
	a."IdPortafolio",
	a."Numero",
	a."TaxID",
	a."PaisOtorganteIdentificador",
	COALESCE(c."Descripcion",'') as "PaisOtorganteIdentificadorDescripcion",
	a."Nombre",
	a."CodigoMoneda",
	COALESCE(b."Descripcion",'') as "CodigoMonedaDescripcion",
	a."MontoEnPesos",
	a."NumeroTransaccion",
	a."CodigoModalidadOperacion",
	COALESCE(e."Descripcion",'') as "CodigoModalidadOperacionDescripcion",
	a."CodigoConcepto",
	COALESCE(d."Descripcion",'') as "CodigoConceptoDescripcion",
	a."FechaOperacion",
	a."NombreContraparte",
	a."TaxIDContraparte",
	a."CodigoPaisContraparte",
	a."BancoContraparte",
	a."TipoDocumento",
	a."NumeroDocumento",
	a."FechaDocumento",
	a."IdBaseDocumento",
	a."TipoTransaccion",
	a."IdTipoOperacion",
	a."IdTipoProducto",
	a."IdComprobante"
from "BSCL.Normativa.BD::TRI.DatosDJ1862" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='CodigoMoneda1862' AND a."CodigoMoneda"=b."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" c
	on c."Tipo"='CodigoPais1862' and a."PaisOtorganteIdentificador"=c."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" d
	on d."Tipo"='CodigoConceptoDJ1862' and a."CodigoConcepto"=d."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" e
	on e."Tipo"='CodigoOperacionDJ1862' and a."CodigoModalidadOperacion"=e."Codigo"
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1862" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Datos" to bcs_desarrollador with grant option;