SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoAnalitica" AS
SELECT "IdTipoMovimientoFondoMutuo",
 	   "Descripcion",
 	   "IdTipoOperacion" 
FROM   "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

