SELECT 
LPAD(CONCAT(SUBSTRING(COM."Rut",1,LENGTH(COM."Rut")-2),SUBSTRING(COM."Rut",LENGTH(COM."Rut"),1)),10,'0') as "Rut"
FROM "BSCL.Normativa.BD::InterfazSIGListaRut" AS COM
	WHERE COM."IdEmpresa"= :IdEmpresa
	