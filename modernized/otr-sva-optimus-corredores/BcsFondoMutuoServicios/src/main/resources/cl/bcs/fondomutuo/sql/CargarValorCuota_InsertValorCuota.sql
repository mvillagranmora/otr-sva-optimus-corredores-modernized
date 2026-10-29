INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuota"
(
"IdSerieFondo"
, "FechaCierre"
, "ValorCuota"
, "IdMoneda"
, "Observacion"
, "FechaCarga"
, "FechaIngresoRegistro"
, "IdEmpresa"
)
VALUES(
:IdSerieFondo
,:FechaVigencia
,:ValorCuota
,:IdMoneda
,:Observacion
,:FechaCalculo
,CURRENT_UTCTIMESTAMP
,:IdEmpresa
);