-� REPO2P1 Actualizar Estado SICAM y CODE SICAM
UPDATE "BSCL.Normativa.BD::NOR.DatosREPO2P1" REPO
SET 
    REPO."EstadoSICAM" = :p_EstadoSICAM,
    REPO."SICAM" = :p_CodeSICAM
--FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" REPO
--INNER JOIN "Tabla Diccionario" DIC ON DIC."CodigoRegistro" = :p_CodeSICAM
WHERE REPO."IdDocumento" = (
    SELECT DOC."IdDocumento"
    FROM "BSCL.Normativa.BD::DOC.Documento" DOC
    WHERE DOC."Esquema" = :p_IdEsquema
      AND DOC."Version" = :p_Version
)
AND REPO."ID" IN (SELECT "ID" FROM :p_Registros);