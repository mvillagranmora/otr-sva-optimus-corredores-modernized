UPDATE "BSCL.Custodia.BD::CUS.EnvioOperacionesPendientes" 
SET "EstadoCclv"=:EstadoCclv ,"EstadoCclvDesc"=:EstadoCclvDesc ,"IdEstado"=:IdEstado  
WHERE
	"IdGestion" = :IdGestion
	AND 
    "FechaEjecucion" = :FechaEjecucion