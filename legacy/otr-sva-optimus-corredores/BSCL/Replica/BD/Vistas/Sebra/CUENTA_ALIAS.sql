SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA_ALIAS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA_ALIAS" AS

select  to_int(trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)))  AS "PER_ID",
TO_INT(COALESCE (POR."CodigoPortafolio" , '0' ))   AS "CUENTA", -- TIPO :int
coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), '')  AS "RUT_CLI", -- TIPO :VARCHAR
LEFT(alias1,COALESCE(conf."Valor",16)) as ALIAS1,
LEFT(alias2,COALESCE(conf."Valor",16)) as ALIAS2,
LEFT(alias3,COALESCE(conf."Valor",16)) as ALIAS3,
LEFT(alias4,COALESCE(conf."Valor",16)) as ALIAS4,
LEFT(alias5,COALESCE(conf."Valor",16)) as ALIAS5
from (
	select "IdPortafolio", max(CASE WHEN r=1 then "ShortName" ELSE '' END) as alias1, 
						max(CASE WHEN r=2 then "ShortName" ELSE '' END) as alias2,
						max(CASE WHEN r=3 then "ShortName" ELSE '' END) as alias3,
						max(CASE WHEN r=4 then "ShortName" ELSE '' END) as alias4,
						max(CASE WHEN r=5 then "ShortName" ELSE '' END) as alias5
	from (
		select "IdPortafolio", rank() over (partition by "IdPortafolio" order by "ShortName") as r, "ShortName" 
		from "BSCL.Orden.BD::ORD.ShortNames"
		)
	group by "IdPortafolio"
) AS ALIAS
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por on ALIAS."IdPortafolio" = por."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdCliente" = por."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" per on per."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" doc on per."IdPersona" = doc."IdPersona"
LEFT JOIN "BSCL.Replica.BD::ConfiguracionReplica" conf on conf. "CodigoParametro" = 'CUENTA_ALIAS_LargoClientId'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CUENTA_ALIAS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

