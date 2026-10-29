SELECT 
REPO."ID",
REPO."TipoRegistro",
REPO."RUTEntidadMCF",
TO_VARCHAR(REPO."Fecha", 'YYYY-MM-DD') AS "Fecha",
REPO."SaldoCuentaCambioConversion",
REPO."SaldoMonedaOriginal",
REPO."MonedaSaldo",
REPO."Paridad"
FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" REPO
WHERE REPO."IdDocumento" = (
    SELECT DOC."IdDocumento"
    FROM "BSCL.Normativa.BD::DOC.Documento" DOC
    WHERE DOC."Esquema" = :p_Esquema
      AND DOC."Version" = :p_Version
)
AND REPO."Fecha" BETWEEN :p_FechaInicio AND :p_FechaFin;