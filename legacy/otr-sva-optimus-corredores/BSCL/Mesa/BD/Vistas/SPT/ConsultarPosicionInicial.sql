SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial"
AS 
SELECT
  "IdPosicionInicial",
  "IdEmpresa",
  "Fecha",
  "IdMoneda",
  "TipoCambioInicial",
  "CierreIngresado",
  "PosicionInicial",
  "TipoInstrumento",
  "IdUsuario",
  "Estado",
  "ResultadoPosicionInicial"
FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa";

GRANT SELECT ON "BSCL.Mesa.BD::SPT.PosicionInicialMesa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
