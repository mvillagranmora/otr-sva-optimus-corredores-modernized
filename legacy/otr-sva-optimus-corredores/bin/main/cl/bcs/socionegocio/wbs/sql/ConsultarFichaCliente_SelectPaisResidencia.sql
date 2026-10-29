SELECT 
"IdPersona" 
,COALESCE( "IdPaisResidencia" , 0) AS "IdPais"
,COALESCE( "PaisResidencia" , '') AS "Pais"
,COALESCE( "TIN" , '') AS "Tin"
,COALESCE( "IdCausaNoTin" , '') AS "NoTin"
,COALESCE( "CausaNoTin" , '') AS "OtraCausa"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatcaPaisResidencia"
WHERE "IdPersona" IN (:p_IdPersonas);