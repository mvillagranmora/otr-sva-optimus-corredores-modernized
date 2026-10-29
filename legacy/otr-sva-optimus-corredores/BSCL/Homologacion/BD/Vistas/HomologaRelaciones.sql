SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaRelaciones";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaRelaciones" as
SELECT
"IdPersonaOrigen" AS per_id_per, -- int] NOT NULL,*************
"IdPersonaDestino" AS per_id_rel, -- int] NOT NULL,*************
CASE WHEN "IdTipoRelacion" = 'REP' then 'TRELALEGAL' ELSE  "IdTipoRelacion" END  AS par_tre_id, -- varchar](10) NOT NULL,*************
'' AS rel_fec_inicio, -- datetime] NOT NULL,
'' AS rel_fec_termino, -- datetime] NOT NULL,
CASE WHEN "IdEstado" = 'ACT' then 'S' ELSE  'N' END AS rel_vigencia, -- char](1) NOT NULL,*************
'' AS rel_fatca, -- varchar](50) NOT NULL,
'' AS rel_ind_benef, -- char](10) NOT NULL,
'' AS tipo_representante, -- varchar](10) NOT NULL,
'' AS rel_fec_modifica -- datetime] NOT NULL,
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaRelaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO