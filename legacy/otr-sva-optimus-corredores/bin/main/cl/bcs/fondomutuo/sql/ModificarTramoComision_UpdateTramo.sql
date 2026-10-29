UPDATE "BSCL.FondoMutuo.BD::FMM.TramoComision"
SET 
	"DiasDesde" = :DiasDesde, 
	"DiasHasta" = :DiasHasta, 
	"PorcentajeComision" = :PorcentajeComision,
	"FechaSistema" = :FechaSistema
WHERE "IdTramo" = :IdTramo;
