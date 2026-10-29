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
"BSCL.Mesa.BD.Secuencias::CargaMasivaDetalle".NEXTVAL
, :IdCargaMasiva
, :IdCargaMasivaDetalleCabecera
, 'ING'
, ''
, 'D'
, :FolioAgrupadorCartera
, :DocumentoIdentidad
, :CodigoPortafolio
, :IdPortafolio
, :TipoOperacion
, :CondicionLiquidacion
, :TipoReajuste
, :ValorInicial
, :Mercado
, :SecuenciaCartera
, :Nemotecnico
, :Emisor
, :Boveda
, :ReajusteCartera
, :MonedaLiquidacion
, :FechaVencimiento
, :Cantidad
, :ValorRescate
, :Fungible
, :Monto
, :Tasa
);