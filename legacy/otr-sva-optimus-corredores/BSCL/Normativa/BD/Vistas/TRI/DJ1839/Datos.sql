set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Datos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Datos" (
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "Numero",
    "RutCliente",
	"TaxID",
	"RutClienteB",
	"TaxIDB",
	"CodigoPaisA",
    "CodigoPaisB",
	"AcuerdoMarcoTipo",
	"AcuerdoMarcoNumero",
	"AcuerdoMarcoFechaSuscripcion",
	"ContratoNumero",
	"ContratoFechaSuscripcion",
	"ContratoTipo",
	"ContratoNombreInstrumento",
	"FechaSuscripcion",
	"IdCliente",
	"IdPortafolio"
) as
select
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones",
    a."FuenteDatos",
    a."Numero",
    a."RutCliente",
	a."TaxID",
	a."RutClienteB",
	a."TaxIDB",
	COALESCE(b."Descripcion",'') as "CodigoPaisA",
    COALESCE(c."Descripcion",'') as "CodigoPaisB",
	a."AcuerdoMarcoTipo",
	a."AcuerdoMarcoNumero",
	a."AcuerdoMarcoFechaSuscripcion",
	a."ContratoNumero",
	a."ContratoFechaSuscripcion",
	a."ContratoTipo",
	a."ContratoNombreInstrumento",
	a."FechaSuscripcion",
	a."IdCliente",
	a."IdPortafolio"
from "BSCL.Normativa.BD::TRI.DatosDJ1839" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='CodigoPais' and a."CodigoPaisA"=b."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" c
    on c."Tipo"='CodigoPais' and a."CodigoPaisB"=c."Codigo"
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
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1839" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Datos" to bcs_desarrollador with grant option;
