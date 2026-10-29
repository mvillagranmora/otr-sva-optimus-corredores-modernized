UPDATE "BSCL.FondoMutuo.BD::FMM.Comision"
SET 
	"Descripcion" = :Descripcion, 
	"FechaVigenciaDesde" = :FechaVigenciaDesde, 
	"FechaVigenciaHasta" = :FechaVigenciaHasta, 
	"FechaSistema" = :FechaSistema, 
	"PorcentajeLiberado"= :PorcentajeLiberado, 
	"Estado"= :Estado
WHERE "IdComision" = :IdComision;
