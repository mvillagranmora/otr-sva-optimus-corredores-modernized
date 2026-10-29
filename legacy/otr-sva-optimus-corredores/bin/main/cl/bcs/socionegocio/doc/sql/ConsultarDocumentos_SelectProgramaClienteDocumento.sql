SELECT PRO."IdPrograma" AS "ProgramaDocumentoEstado"
FROM "BSCL.Plataforma.BD::BAT.Programa" PRO 
JOIN "BSCL.Plataforma.BD::BAT.ParametrosPrograma" PPRO 
ON  PRO."IdPrograma"=PPRO."IdPrograma"
WHERE   PPRO."NombreParametro"='p_IdCliente' 
AND PPRO."Valor" =:p_IdCliente AND (PRO."idEstado"='ASI' OR PRO."idEstado"='PEN')
AND PRO."IdTipoPrograma" LIKE '%GENERAR_DOCUMENTOS_AUTOMATICOS%'