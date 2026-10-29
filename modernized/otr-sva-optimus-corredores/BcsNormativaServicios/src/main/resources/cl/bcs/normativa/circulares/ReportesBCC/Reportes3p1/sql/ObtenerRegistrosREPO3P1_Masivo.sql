SELECT 
REPO."ID",
REPO."TipoRegistro",
REPO."RUTReportante",
TO_VARCHAR(REPO."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
REPO."HoraOperacion",
REPO."TipoOperacion",
REPO."EventoReporte",
REPO."Identificador",
REPO."RUTContraparte",
REPO."LEIContraparte",
REPO."NombreContraparte",
REPO."PlataformaNegociacion",
REPO."MontoMonedaComprada",
REPO."MontoMonedaVendida",
REPO."TipoCambio"
FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" REPO
WHERE REPO."IdDocumento" = (
    SELECT DOC."IdDocumento"
    FROM "BSCL.Normativa.BD::DOC.Documento" DOC
    WHERE DOC."Esquema" = :p_Esquema
      AND DOC."Version" = :p_Version
)
AND REPO."FechaOperacion" BETWEEN :p_FechaInicio AND :p_FechaFin;
