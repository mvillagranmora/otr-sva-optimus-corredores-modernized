SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionOpcionesCobro";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionOpcionesCobro" AS 	 
SELECT
OPC."IdOpcionCobro"
,OPC."OpcionCobro"
,DI."DetalleIndicador"
,OPC."PortafolioAPV"
,OPC."IdClasificacionTipoInstrumento"
,CASE 
	WHEN OPC."IdClasificacionTipoInstrumento" = 'CI' THEN 'CUENTA INVERSION'
    ELSE CTI."Nombre"
END	
 AS "NombreMercado"
,OPC."PorcentajeCobro"
,OPC."IdEstado"
,EST."Nombre" AS "NombreEstado"
,OPC."FechaVigencia"
,OPC."FechaIngreso"
,OPC."FechaModificacion"
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" OPC	
INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DI 
	ON OPC."OpcionCobro" = DI."IdDetalleIndicador" 
	AND DI."IdTipoIndicador" = (SELECT T_IND."IdTipoIndicador" FROM "BSCL.Plataforma.BD::IND.TipoIndicador" T_IND
		WHERE (T_IND."IdTipoEntidad" IN ('PORTAFOLIO'))  
		AND T_IND."CodigoTipoIndicador" = 'COBROCUST')
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CTI ON OPC."IdClasificacionTipoInstrumento" = CTI."IdClasificacionTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON OPC."IdEstado" = EST."IdEstado";

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionOpcionesCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
