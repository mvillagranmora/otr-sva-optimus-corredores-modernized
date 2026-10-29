SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa" AS
SELECT
	 AGOP."IdAgenteOperadorMesa"
	,AGOP."IdMesa"
	,CASE WHEN AGOP."IdMesa" = 0 THEN 'TODOS' ELSE MES."Nombre" END AS "NombreMesa"
	,CASE WHEN AGOP."IdMesa" = 0 THEN 'TODOS' ELSE MES."Codigo" END AS "CodigoMesa"
	,AGOP."IdAgenteOperador"
	,IFNULL(EMP."DocumentoIdentidad",' -') AS "DocumentoIdentidad"
	,IFNULL(EMP."CodigoYNombreEmpleado",' -') AS "CodigoNombreAgOp"
    ,IFNULL(EMP."CodigoTipoCargoEmpleado",' -') AS "CodigoCargoAgOp"
    ,IFNULL(EMP."NombreTipoCargoEmpleado",' -') AS "NombreCargoAgOp"
	,AGOP."IdEstado"
	,CASE AGOP."IdEstado"
		WHEN 'ACT' THEN 'ACTIVO'
		WHEN 'INA' THEN 'INACTIVO'
		ELSE '' END AS "EstadoRelacion"
	,AGOP."IdEmpresa"
	,AGOP."IdUsuarioModificacion"
	,IFNULL(USR."CodigoUsuario",' - ') AS "CodigoUsuario"
	,AGOP."FechaModificacion"
	,TO_CHAR(AGOP."FechaModificacion",'DD-MM-YYYY HH24:MI:SS') AS "FechaModificacionFrmt"
	,AGOP."ModificaOperacionOtroUsuario"
FROM "BSCL.Mesa.BD::MES.AgenteOperadorMesa" AGOP
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
	ON (AGOP."IdMesa" = MES."IdMesa")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP
	ON (AGOP."IdAgenteOperador" = EMP."IdEmpleado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
	ON (AGOP."IdUsuarioModificacion" = USR."IdUsuario" AND USR."IdEstado" = 'ACT' AND AGOP."IdEmpresa" = USR."IdEmpresa")
ORDER BY MES."IdTipoProducto" ASC, MES."Nombre" ASC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
