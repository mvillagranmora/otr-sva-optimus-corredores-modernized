INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"(
"IdValorCuotaHistorico"
, "IdSerieFondo"
, "FechaCierre"
, "ValorCuota"
, "IdMoneda"
, "Observacion"
, "FechaCarga"
, "FechaIngresoRegistro"
, "EsUltimaCargaDiaria"
, "IdEmpresa"
)
VALUES(
"BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico".NEXTVAL
,:IdSerieFondo
,:FechaVigencia
,:ValorCuota
,:IdMoneda
,:Observacion
,:FechaCalculo
,CURRENT_UTCTIMESTAMP
,'S'
,:IdEmpresa
);