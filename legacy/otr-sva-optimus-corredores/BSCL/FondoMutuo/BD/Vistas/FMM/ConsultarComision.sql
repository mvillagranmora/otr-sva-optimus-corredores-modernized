SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComision";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComision"
AS
SELECT 
 "IdComision"
, "IdSerieFondo"
, "Descripcion"
, "TipoComision"
, "FechaVigenciaDesde"
, "FechaVigenciaHasta"
, "PorcentajeLiberado"
, "Estado"
, "FechaSistema"
, "FechaIngresoRegistro"
, "IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.Comision";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComision" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
