INSERT INTO "BSCL.Mesa.BD::CAR.CargaMasivaDetalle"(
"IdCargaMasivaDetalle"
, "IdCargaMasiva"
, "IdCargaMasivaDetalleCabecera"
, "IdEstado"
, "ObservacionValidacion"
, "TipoRegistro"
, "FolioAgrupadorCartera"
, "DocumentoIdentidad"
, "CodigoPortafolio"
, "IdPortafolio"
, "TipoOperacion"
, "CondicionLiquidacion"
, "TipoReajuste"
, "ValorInicial"
, "Mercado"
, "SecuenciaCartera"
, "Nemotecnico"
, "Emisor"
, "Boveda"
, "ReajusteCartera"
, "MonedaLiquidacion"
, "FechaVencimiento"
, "Cantidad"
, "ValorRescate"
, "Fungible"
, "Monto"
, "Tasa"
)
VALUES(
:IdCargaMasivaDetalle
, :IdCargaMasiva
, 0
, 'ING'
, ''
, 'C'
, :FolioAgrupadorCartera
, :DocumentoIdentidad
, :CodigoPortafolio
, :IdPortafolio
, :TipoOperacion
, :CondicionLiquidacion
, :TipoReajuste
, :ValorInicial
, ''
, 0
, ''
, ''
, ''
, ''
, :MonedaLiquidacion
, ''
, 0
, 0
, ''
, 0
, 0
);
