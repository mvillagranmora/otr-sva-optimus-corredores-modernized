SET SCHEMA "BSCL";
---BSCL.SocioNegocio.BD::SOC.Servicios.ConsultarPersonaPorTipoRelacion
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion" ( "IdTipoRelacion",
	 "IdPersona","IdEstado" ) AS SELECT
	 DISTINCT --T3."RazonSocial",
 --T3."DocumentoIdentidad",
 --T3."NombrePaisNacto",
 T."IdTipoRelacion",
	 T."IdPersona",
	 T."IdEstado"
FROM ( SELECT
	 T1."IdTipoRelacion",
	 T1."IdEstado",
	T0.* 
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T0,
	 "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" as T1 
	WHERE T1."IdTipoEntidad" = 'Persona' 
	and Length(T0."IdPersona")>0 
	UNION SELECT
	 T1."IdTipoRelacion",
	 T1."IdEstado",
	T0.* 
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T0,
	 "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" as T1 
	WHERE T1."IdTipoEntidad" = 'Cliente' 
	and Length(T0."IdCliente")>0 
	UNION SELECT
	 T1."IdTipoRelacion",
	 T1."IdEstado",
	T0.* 
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T0,
	 "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" as T1 
	WHERE T1."IdTipoEntidad" = 'Empleado' 
	and Length(T0."IdEmpleado")>0 
	UNION SELECT
	 T1."IdTipoRelacion",
	 T1."IdEstado",
	T0.* 
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T0,
	 "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" as T1 
	WHERE T1."IdTipoEntidad" = 'Operador' 
	and Length(T0."IdOperador")>0 
	UNION SELECT
	 T1."IdTipoRelacion",
	 T1."IdEstado",
	T0.* 
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T0,
	 "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" as T1 
	WHERE T1."IdTipoEntidad" = 'Proveedor' 
	and Length(T0."IdProveedor")>0 ) as T 
ORDER BY T."IdPersona" WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 