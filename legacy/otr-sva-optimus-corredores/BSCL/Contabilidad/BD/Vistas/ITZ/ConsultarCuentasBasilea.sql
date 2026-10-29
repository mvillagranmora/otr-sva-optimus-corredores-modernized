SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasBasilea";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasBasilea" AS 
SELECT
	"Concepto",
	"TipoCuentaContable" ,
	"CuentaContable" ,
	"NombreCuentaContable" ,
	 USR."CodigoUsuario" ,
	"FechaModificacionRegistro" ,
	 EST_PLA."Nombre" AS "Estado",
	"Producto"
FROM
	"BSCL.Contabilidad.BD::ITZ.InterfazCuentasBasilea" ICB
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST_PLA ON ICB."IdEstado"  = EST_PLA."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USR ON USR."IdUsuario"  = ICB."IdUsuarioModifica" 
WHERE ICB."IdEstado" = 'ACT'
ORDER BY "TipoCuentaContable" ;