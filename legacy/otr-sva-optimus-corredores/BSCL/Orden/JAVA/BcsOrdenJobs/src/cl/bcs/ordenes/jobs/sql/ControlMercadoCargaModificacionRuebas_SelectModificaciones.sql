SELECT 
    LO.folio as "FolioMod",
    LO.fecha_mod as "FechaMod",
    LO.FECHA AS "Fecha",
    LO.COD_OPE_COM AS "CodComNuevo",
    LO.COD_OPE_VEN AS "CodVenNuevo"
FROM TBRVTRLO LO
WHERE LO.TIPO_MODIF = 'M' 
	AND LO.ESTADO = 'MB'
    AND LO.fecha_mod >= :FechaSistema
    AND (LO.CORREDOR_SOLIC = :CodigoCorredora OR LO.CORREDOR_CONF = :CodigoCorredora)
    AND LO.COND_COD_004 NOT IN ('S','V'); -- excluye simultanea y venta corta
