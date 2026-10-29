INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCircular1518"
("IdCuadraturaCircular1518"
, "IdCarga"
, "FechaCuadratura"
, "Nemotecnico"
, "IdClasificacionTipoInstrumento"
, "IdTipoInstrumento"
, "Diferencia"
, "TotalCustodioEmisores"
, "CantidadBCS"
, "CantidadBEC"
, "CantidadBOV"
, "CantidadEmisores"
, "NemoExisteEnOptimus"
)
VALUES(
"BSCL.Custodia.BD.Secuencias::CuadraturaCircular1518".NEXTVAL
, :IdCarga
, :FechaCuadratura
, :Nemotecnico
, :IdClasificacionTipoInstrumento
, :IdTipoInstrumento
, :Diferencia
, :TotalCustodioEmisores
, :CantidadBCS
, :CantidadBEC
, :CantidadBOV
, :CantidadEmisores
, :NemoExisteEnOptimus
);
