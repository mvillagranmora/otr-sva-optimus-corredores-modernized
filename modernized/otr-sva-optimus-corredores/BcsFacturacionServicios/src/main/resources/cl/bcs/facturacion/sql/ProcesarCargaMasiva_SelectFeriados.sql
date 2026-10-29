SELECT
	"Fecha" 
FROM
	"BSCL.Plataforma.BD::PLA.Empresa" EMP
INNER JOIN "BSCL.Plataforma.BD::REG.DiaFeriado" FER ON EMP."IdPais"  = FER."IdPais" 
WHERE EMP."IdEmpresa" =  :IdEmpresa
AND "Fecha" IN (:Fechas);