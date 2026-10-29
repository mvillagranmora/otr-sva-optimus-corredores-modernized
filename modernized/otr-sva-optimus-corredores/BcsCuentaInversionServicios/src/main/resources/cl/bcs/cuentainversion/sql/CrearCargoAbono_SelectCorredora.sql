SELECT TOP 1 "CCEM"."Nombre", "CCEM"."IdPersona", "CPER"."DocumentoIdentidad" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS "CCEM"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "CPER" ON ("CPER"."IdPersona" = "CCEM"."IdPersona");