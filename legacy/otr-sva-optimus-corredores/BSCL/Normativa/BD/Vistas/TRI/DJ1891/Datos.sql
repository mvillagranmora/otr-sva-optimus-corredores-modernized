set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Datos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Datos" (
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "TipoDocumento",
    "TipoDocumentoDescripcion",
    "NumeroDocumento",
    "FechaDocumento",
    "RutEmisor",    
    "Nemotecnico",
    "RutCliente",
    "Cuenta",
    "Cantidad",
--    "Monto",
	"MontoCompras",							
	"MontoVentas",
	"ValorAdquisicion",
    "TipoTransaccion",
    "TipoTransaccionDescripcion",
    "IdTipoOperacion",
    "IdTipoOperacionDescripcion",
    "IdTipoProducto",
    "IdTipoProductoDescripcion",
    "IdBaseDocumento",
    "IdCliente",
    "IdPortafolio",
    "LineaFactura"
    
) as
select
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones",
    a."FuenteDatos",
    a."TipoDocumento",
    b."Descripcion" as "TipoDocumentoDescripcion",
    a."NumeroDocumento",
    a."FechaDocumento",
    a."RutEmisor",    
    a."Nemotecnico",
    trim(a."RutCliente") AS "RutCliente",
    a."Cuenta",
    a."Cantidad",
--    a."Monto",
	a."MontoCompras",							
	a."MontoVentas",
	a."ValorAdquisicion",
    a."TipoTransaccion",
    c."Descripcion" as "TipoTransaccionDescripcion",
    a."IdTipoOperacion",
    d."Descripcion" as "IdTipoOperacionDescripcion",
    a."IdTipoProducto",
    e."Descripcion" as "IdTipoProductoDescripcion",
    a."IdBaseDocumento",
    a."IdCliente",
    a."IdPortafolio",
    a."LineaFactura"
    
from "BSCL.Normativa.BD::TRI.DatosDJ1891" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='TipoDocu' and a."TipoDocumento"=b."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" c
    on c."Tipo"='TipoTransac' and a."TipoTransaccion"=c."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" d
    on d."Tipo"='TipoOperacion' and a."IdTipoOperacion"=d."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" e
    on e."Tipo"='TipoProducto' and a."IdTipoProducto"=e."Codigo"
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
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1891" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Datos" to bcs_desarrollador with grant option;
	