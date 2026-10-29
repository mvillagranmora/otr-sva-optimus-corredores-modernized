INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCircular1720"
("IdCuadraturaCircular1720"
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
"BSCL.Custodia.BD.Secuencias::CuadraturaCircular1720".NEXTVAL
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
