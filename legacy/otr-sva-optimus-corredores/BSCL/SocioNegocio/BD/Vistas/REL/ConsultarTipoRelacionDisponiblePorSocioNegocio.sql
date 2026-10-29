SET SCHEMA "BSCL";
 
---Listar Relaciones segun Tipo de Socio Negocio (Persona, Cliente, Contraparte, etc)

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDisponiblePorSocioNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDisponiblePorSocioNegocio" ( "IdPersona",
	 "IdTipoRelacion",
	 "IdEmpresa",
	 "EsPersonaNatural",
	 "IdTipoEntidadOrigen",
	 "NombreTipoRelacion",
	 "EsIndefinida",
	 "Administracion" ,
	 "UsaPorcentajeParticipacion") AS SELECT
	 T."IdPersona" ,
	 T."IdTipoRelacion" ,
	 T."IdEmpresa" ,
	 T."EsPersonaNatural" ,
	 T."IdTipoEntidadOrigen" ,
	 T."NombreTipoRelacion" ,
	 T."EsIndefinida" ,
	 T."Administracion" ,
	 T."UsaPorcentajeParticipacion"
FROM ( SELECT
	 T1."IdPersona",
	 T0."IdTipoRelacion",
	 T0."IdEmpresa",
	 T0."EsPersonaNatural",
	 T0."IdTipoEntidadOrigen",
  	 T0."NombreTipoRelacion" ,
	 T0."EsIndefinida",
	 T0."Administracion" ,
	 T0."UsaPorcentajeParticipacion"
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" as T0,
	 "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T1 
	WHERE T0."IdEmpresa" = T1."IdEmpresa" 
	AND T0."IdEstado"='ACT' 
	AND T0."IdTipoEntidadOrigen" ='Persona' 
	UNION -- CLIENTE
 SELECT
	 T1."IdPersona",
	 T0."IdTipoRelacion",
	 T0."IdEmpresa",
	 T0."EsPersonaNatural",
	 T0."IdTipoEntidadOrigen",
  	 T0."NombreTipoRelacion" ,
	 T0."EsIndefinida",
	 T0."Administracion",
	 T0."UsaPorcentajeParticipacion"
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" as T0,
	 "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T1 
	WHERE T0."IdEmpresa" = T1."IdEmpresa" 
	AND T0."IdEstado"='ACT' 
	AND T0."IdTipoEntidadOrigen" ='Cliente' 
	AND Length(T1."IdCliente")>0 
	UNION -- EMPLEADO
 SELECT
	 T1."IdPersona",
	 T0."IdTipoRelacion",
	 T0."IdEmpresa",
	 T0."EsPersonaNatural",
	 T0."IdTipoEntidadOrigen",
  	 T0."NombreTipoRelacion" ,
	 T0."EsIndefinida",
	 T0."Administracion" ,
	 T0."UsaPorcentajeParticipacion"
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" as T0,
	 "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T1 
	WHERE T0."IdEmpresa" = T1."IdEmpresa" 
	AND T0."IdEstado"='ACT' 
	AND T0."IdTipoEntidadOrigen" ='Empleado' 
	AND Length(T1."IdEmpleado")>0 
	UNION -- OPERADOR
 SELECT
	 T1."IdPersona",
	 T0."IdTipoRelacion",
	 T0."IdEmpresa",
	 T0."EsPersonaNatural",
	 T0."IdTipoEntidadOrigen",
  	 T0."NombreTipoRelacion" ,
	 T0."EsIndefinida",
	 T0."Administracion",
	 T0."UsaPorcentajeParticipacion" 
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" as T0,
	 "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T1 
	WHERE T0."IdEmpresa" = T1."IdEmpresa" 
	AND T0."IdEstado"='ACT' 
	AND T0."IdTipoEntidadOrigen" ='Operador' 
	AND Length(T1."IdOperador")>0 
	UNION -- PROVEEDOR
 SELECT
	 T1."IdPersona",
	 T0."IdTipoRelacion",
	 T0."IdEmpresa",
	 T0."EsPersonaNatural",
	 T0."IdTipoEntidadOrigen",
  	 T0."NombreTipoRelacion" ,
	 T0."EsIndefinida",
	 T0."Administracion" ,
	 T0."UsaPorcentajeParticipacion"
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" as T0,
	 "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as T1 
	WHERE T0."IdEmpresa" = T1."IdEmpresa" 
	AND T0."IdEstado"='ACT' 
	AND T0."IdTipoEntidadOrigen" ='Proveedor' 
	AND Length(T1."IdProveedor")>0 ) as T 
ORDER BY T."IdPersona",
	T."IdTipoEntidadOrigen" WITH READ ONLY;
	
	GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDisponiblePorSocioNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
	
