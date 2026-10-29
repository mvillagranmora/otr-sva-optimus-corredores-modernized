SELECT
     TO_VARCHAR(:p_Fecha, 'DD-MM-YYYY')  AS "p_FechaDesde"
    ,"P_OUT_ESQUEMA"                      AS "p_out_Esquema"
    ,"P_OUT_VERSION"                      AS "p_out_Version"
    ,"P_OUT_FECHADATOSDESDE"              AS "p_out_FechaDatosDesde"
    ,"P_OUT_FECHADATOSHASTA"              AS "p_out_FechaDatosHasta"
FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(
    :p_TipoDocumento,
    TO_VARCHAR(:p_Fecha, 'DD-MM-YYYY')
)