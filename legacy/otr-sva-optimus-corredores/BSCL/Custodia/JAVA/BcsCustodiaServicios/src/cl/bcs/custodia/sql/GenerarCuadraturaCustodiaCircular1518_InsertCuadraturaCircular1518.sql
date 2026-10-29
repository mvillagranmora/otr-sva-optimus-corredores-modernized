INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCircular1518"
("IdCuadraturaCircular1518"
, "IdCarga"
, "FechaCuadratura"
, "Nemotecnico"
, "IdClasificacionTipoInstrumento"
, "IdTipoInstrumento"
, "CantidadCustodia"
, "CantidadGarantia"
, "CantidadVentaPlazo"
, "CantidadCompraPlazo"
, "TotalCustodia"
, "TotalCustodioEmisores"
, "CompraEnTransito"
, "VentaEnTransito"
, "TransitoIngreso"
, "TransitoEgreso"
, "Diferencia"
, "Precio"
, "Valorizacion"
, "DiferenciaValorizada"
, "IdInstrumento"
, "ReplicaBaseTotalCustodioEmisores"
, "ReplicaBaseDiferencia"
, "ReplicaBaseDiferenciaValorizada"
, "NemoExisteEnOptimus"
)
VALUES(
"BSCL.Custodia.BD.Secuencias::CuadraturaCircular1518".NEXTVAL
, :IdCarga
, :FechaCuadratura
, :Nemotecnico
, :IdClasificacionTipoInstrumento
, :IdTipoInstrumento
, :CantidadCustodia
, :CantidadGarantia
, :CantidadVentaPlazo
, :CantidadCompraPlazo
, :TotalCustodia
, :TotalCustodioEmisores
, :CompraEnTransito
, :VentaEnTransito
, :TransitoIngreso
, :TransitoEgreso
, :Diferencia
, :Precio
, :Valorizacion
, :DiferenciaValorizada
, :IdInstrumento
, :TotalCustodioEmisores
, :Diferencia
, :DiferenciaValorizada
, 'S'
);
