SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasReporte";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasReporte" AS 
SELECT "FechaIngresoRegistro","IdEmpresa","IdOperacionOrigenGarantia","NombreEstado","NombreConcepto","Nemotecnico",SUM("CantidadGarantia") AS "Cantidad",SUM("MontoValorizado") AS "MontoValorizado","IdMonedaInstrumento","IdCliente","IdPortafolio"  FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
Group By "FechaIngresoRegistro","IdEmpresa","IdOperacionOrigenGarantia","NombreEstado","NombreConcepto","Nemotecnico","IdCliente","IdPortafolio","IdMonedaInstrumento";
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasReporte" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
--GO
