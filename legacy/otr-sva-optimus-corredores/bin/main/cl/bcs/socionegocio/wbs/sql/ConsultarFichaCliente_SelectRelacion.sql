SELECT
REL."IdRelacionPersona" AS "IdRelacion"
,REL."IdPersonaOrigen" AS "IdPersona"
,REL."IdPersonaDestino" AS "IdPersonaRelacionada"
,REL."IdTipoRelacion"
,TIPO_RELACION."NombreTipoRelacion"
,PERSONA."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
,PERSONA."DocumentoIdentidad" AS "DocumentoIdentidad"
,PERSONA."NombreCompleto"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
LEFT JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" TIPO_RELACION ON TIPO_RELACION."IdTipoRelacion" = REL."IdTipoRelacion" AND TIPO_RELACION."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON PERSONA."IdPersona" = REL."IdPersonaDestino"
WHERE REL."IdPersonaOrigen" IN (:p_IdPersonas) AND REL."IdTipoRelacion" !='CY'

UNION DISTINCT 

SELECT
COALESCE(REL."IdRelacionPersona",0) AS "IdRelacion"
,PO."IdPersona"
,PO."IdPersonaConyuge" AS "IdPersonaRelacionada"
,'CY' AS "IdTipoRelacion"
,'CONYUGE' AS "NombreTipoRelacion"
,PD."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
,PD."DocumentoIdentidad" AS "DocumentoIdentidad"
,PD."NombreCompleto"
FROM
"BSCL.SocioNegocio.BD::PER.Persona" PO 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PD ON PO."IdPersonaConyuge" = PD."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL ON PO."IdPersonaConyuge" = REL."IdPersonaDestino" AND  REL."IdTipoRelacion" ='CY' AND PO."IdPersona" = REL."IdPersonaOrigen" 
WHERE PO."IdPersonaConyuge" !=0 AND  PO."IdPersona" IN (:p_IdPersonas) 
 
UNION DISTINCT 
 
SELECT  
0 AS "IdRelacion"
,PO."IdPersona"
,PO."IdPersonaEmpleador" AS "IdPersonaRelacionada"
,'PEMDOR' AS "IdTipoRelacion"
,'EMPLEADOR' AS "NombreTipoRelacion"
,PD."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
,PD."DocumentoIdentidad" AS "DocumentoIdentidad"
,PD."NombreCompleto"
FROM
"BSCL.SocioNegocio.BD::PER.Persona" PO 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PD ON PO."IdPersonaEmpleador" = PD."IdPersona"
WHERE PO."IdPersonaEmpleador" !=0  AND  PO."IdPersona" IN (:p_IdPersonas)
 
UNION DISTINCT 
 
SELECT 
0 AS "IdRelacion"
,PO."IdPersona"
,PO."IdPersonaRepresentante" AS "IdPersonaRelacionada"
,'REP' AS "IdTipoRelacion"
,'REPRESENTANTE LEGAL' AS "NombreTipoRelacion"
,PD."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
,PD."DocumentoIdentidad" AS "DocumentoIdentidad"
,PD."NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PO
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PD ON PO."IdPersonaRepresentante" = PD."IdPersona"
WHERE PO."IdPersonaRepresentante" !=0 AND  PO."IdPersona" IN (:p_IdPersonas)


