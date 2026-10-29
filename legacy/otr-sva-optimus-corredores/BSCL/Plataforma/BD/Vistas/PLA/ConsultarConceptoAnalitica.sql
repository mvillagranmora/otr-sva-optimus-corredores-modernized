SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAnalitica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAnalitica" AS
select
	CAST(concepto."IdConcepto" AS NVARCHAR(6)) AS "IdConcepto"  
  	,CAST(concepto."Nombre" AS NVARCHAR(100)) as "NombreConcepto" 
  	,CAST(concepto."TipoConcepto" AS NVARCHAR(10)) AS "TipoConcepto"
  	,CAST(concepto."IdEstado" AS NVARCHAR(3)) AS "IdEstado" 
	,CASE WHEN estado."Nombre" = '' THEN NULL ELSE CAST(estado."Nombre" AS NVARCHAR(100)) END as "NombreEstado"
	,CAST(concepto."ConceptoAbonoReplica" AS NVARCHAR(2)) AS "ConceptoAbonoReplica"
	,CAST(concepto."ConceptoCargoReplica" AS NVARCHAR(2)) AS "ConceptoCargoReplica"
from "BSCL.Plataforma.BD::PLA.Concepto" concepto
inner join "BSCL.Plataforma.BD::PLA.Estado" estado
on estado."IdEstado" = concepto."IdEstado";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

