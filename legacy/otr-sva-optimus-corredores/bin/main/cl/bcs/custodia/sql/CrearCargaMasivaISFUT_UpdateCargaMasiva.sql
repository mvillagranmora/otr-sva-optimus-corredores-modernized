UPDATE EVCA
SET
    EVCA."ISFUT"                     = CMDET."ISFUT",
    EVCA."RetencionISFUT"            = CMDET."RetencionISFUT",
    EVCA."MontoRetencionISFUT"		 = IFNULL(TO_NUMBER(CMDET."MontoRetencion"),0)
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVCA
INNER JOIN "BSCL.Custodia.BD::CAR.CargaMasivaDetalleISFUT" AS CMDET ON CMDET."IdEventoCapitalCliente" = EVCA."IdEventoCapitalCliente"
WHERE CMDET."IdCargaMasivaISFUT" in (:IdCargaMasiva);