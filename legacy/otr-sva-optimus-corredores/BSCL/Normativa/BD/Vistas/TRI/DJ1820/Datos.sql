set schema "BSCL";
GO
drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Datos";
GO
create view "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Datos" AS
SELECT
	a."ID",
    a."IdDocumento",
    a."VboUsuario",
    COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones",
    a."FuenteDatos",
	a."IdCliente",
   	a."IdPortafolio",
   	a."IdCartera",
   	a."FolioOperacion",
   	a."FechaOperacion",
	a."FechaLiquidacion",
	a."FechaAnticipo",
   	a."IdInstrumento",
   	a."IdTipoOperacion",
   	a."IdClasificacionTipoInstrumento",
   	a."IdMonedaPrincipal",
   	a."IdMonedaSecundaria",
   	a."MontoMonedaPrincipal",
	a."MontoMonedaSecundaria",
    a."RutCliente",
    a."TaxID",
    a."CodigoPaisResidencia",
   	COALESCE(b."Descripcion",'') 				AS	"CodigoPaisResidenciaDescripcion",
    a."CodigoRelacion",
	COALESCE(c."Descripcion",'') 				AS	"CodigoRelacionDescripcion",    
    a."CodigoModoContrato",
    COALESCE(e."Descripcion",'') 				AS	"CodigoModoContratoDescripcion",
    a."TipoAcuerdoMarco",
    COALESCE(F."Descripcion",'') 				AS	"TipoAcuerdoMarcoDescripcion",
    a."NumeroAcuerdoMarco",
 	a."FechaSuscripcionAcuerdo",
	a."NumeroContrato",
	a."FechaSuscripcionContrato",
	a."TipoEvento",
	COALESCE(d."Descripcion",'') 				AS	"TipoEventoDescripcion",
	a."TipoContrato",
	COALESCE(G."Descripcion",'') 				AS	"TipoContratoDescripcion",	
	a."Instrumento",
	a."ModalidadCumplimiento",
	COALESCE(h."Descripcion",'') 				AS	"ModalidadCumplimientoDescripcion",		
	a."CodigoPosicionDeclarante",
	COALESCE(i."Descripcion",'') 				AS	"CodigoPosicionDeclaranteDescripcion",		
	a."TipoActivoSubyacente",
	COALESCE(j."Descripcion",'') 				AS	"TipoActivoSubyacenteDescripcion",
	a."CodigoActivoSubyacente",
	a."OtroActivoSubyacente",
	a."TasaActivoSubyacente",
	a."TipoSegundoActivoSubyacente",
	a."CodigoSegundoActivoSubyacente",
	a."OtroSegundoActivoSubyacente",
	a."TasaSegundoActivoSubyacente",
	a."CodigoPrecioFuturo",
	COALESCE(k."Descripcion",'') 				AS	"CodigoPrecioFuturoDescripcion",
	a."PrecioFuturo",
	a."CodigoMonedaFuturo",
	a."CodigoUnidad",
	COALESCE(l."Descripcion",'') 				AS	"CodigoUnidadDescripcion",
	a."MontoNocional",
	a."CodigoSegundaUnidad",
	COALESCE(m."Descripcion",'') 				AS	"CodigoSegundaUnidadDescripcion",
	a."SegundoMontoNocional",
	a."FechaVencimiento"
from "BSCL.Normativa.BD::TRI.DatosDJ1820" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='DJ1820PAIS' and a."CodigoPaisResidencia"=b."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" c
    on c."Tipo"='DJ1820RELACION' and a."CodigoRelacion"=c."Codigo"    
left join "BSCL.Normativa.BD::DOC.Diccionario" d
    on d."Tipo"='DJ1820TIPOEVENTO' and a."TipoEvento"=d."Codigo" 
left join "BSCL.Normativa.BD::DOC.Diccionario" e
    on e."Tipo"='DJ1820MODALIDADCONTRATO' and a."CodigoModoContrato"=e."Codigo"    
left join "BSCL.Normativa.BD::DOC.Diccionario" F
    on F."Tipo"='DJ1820ACUERDOMARCO' and a."TipoAcuerdoMarco"=F."Codigo" 
left join "BSCL.Normativa.BD::DOC.Diccionario" G
    on G."Tipo"='DJ1820TIPOCONTRATO' and a."TipoContrato"=G."Codigo"  
left join "BSCL.Normativa.BD::DOC.Diccionario" h
    on h."Tipo"='DJ1820MODALIDADCUMPLIMIENTO' and a."ModalidadCumplimiento"=h."Codigo"    
left join "BSCL.Normativa.BD::DOC.Diccionario" i
    on i."Tipo"='DJ1820POSICIONDECLARANTE' and a."CodigoPosicionDeclarante"=i."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" j
    on j."Tipo"='DJ1820TIPOACTIVOSUBYACENTE' and a."TipoActivoSubyacente"=j."Codigo"       
left join "BSCL.Normativa.BD::DOC.Diccionario" k
    on k."Tipo"='DJ1820CODIGOPRECIOFUTURO' and a."CodigoPrecioFuturo"=k."Codigo"  
left join "BSCL.Normativa.BD::DOC.Diccionario" l
    on l."Tipo"='DJ1820UNIDADMONEDA' and a."CodigoUnidad"=l."Codigo"  
left join "BSCL.Normativa.BD::DOC.Diccionario" m
    on m."Tipo"='DJ1820UNIDADMONEDA' and a."CodigoSegundaUnidad"=m."Codigo"
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
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1820" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
with read only;         

GO
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Datos" to bcs_desarrollador with grant option;
