SELECT * 
FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" REPO
WHERE REPO."IdDocumento" = (
    SELECT DOC."IdDocumento"
    FROM "BSCL.Normativa.BD::DOC.Documento" DOC
    WHERE DOC."Esquema" = :p_IdEsquema
      AND DOC."Version" = :p_Version
)
AND REPO."ID" IN (SELECT "ID" FROM :p_Registros);
