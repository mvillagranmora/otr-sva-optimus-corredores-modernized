SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" 
AS 

SELECT 
	PPAG.*,
	TIPOP."Nombre" as "NombreTipoOperacion",
	TIPOP."IdTipoProducto" as "IdTipoProducto",
	COALESCE(TIPPR."Nombre",'-') as "NombreTipoProducto",
	TIPOP."RecibePagaPortafolio" as "RecibePagaPortafolio",
	COALESCE(MEDPAG."Nombre",'-') as "NombreMedioDePago",
	COALESCE((( MON."IdMoneda" || ' - ') || MON."Nombre") ,'-') as "NombreMoneda",
	COALESCE(MEDPAG."UsaCuentaBancaria",'-') as "UsaCuentaBancaria",
	COALESCE(MEDPAG."UsaTarjetaBancaria",'-') as "UsaTarjetaBancaria",
	ESTPPAG."Nombre" as "NombreEstado"
FROM 
	"BSCL.Plataforma.BD::PAG.PreferenciaPago" as PPAG
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPOP
		ON (TIPOP."IdTipoOperacion" = PPAG."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TIPPR
		ON (TIPPR."IdTipoProducto" = TIPOP."IdTipoProducto")
	LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" MEDPAG
		ON (MEDPAG."IdMedioDePago" = PPAG."IdMedioDePago")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
		ON (MON."IdMoneda" = PPAG."IdMoneda")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTPPAG
		ON (ESTPPAG."IdEstado" = PPAG."IdEstado") ;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
