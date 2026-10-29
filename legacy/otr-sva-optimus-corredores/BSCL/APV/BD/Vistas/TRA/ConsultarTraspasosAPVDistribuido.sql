SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPVDistribuido";

CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPVDistribuido" AS
SELECT CAST(A."IngresoSolicitud" AS NVARCHAR(10)) AS "IngresoSolicitud",
	CAST(A."CierreSolicitud" AS NVARCHAR(10)) AS "CierreSolicitud",
	A."IdEmpresa" , 
	A."IdTraspaso" ,
	A."IdContratoSAAPV" , 
	A."FolioContratoSAAPV" ,
	CAST(A."FechaIngreso" AS NVARCHAR(10)) AS "FechaIngreso",
	CAST(A."FechaEgreso" AS NVARCHAR(10)) AS "FechaEgreso",
    CAST(A."FechaModificacion" AS NVARCHAR(10)) AS "FechaModificacion",
	A."TipoMovimiento" , 
	A."IdPlan" , 
	A."NombrePlan" ,
	A."CodigoRazonRetiro" , 
	A."RazonRetiro" , 
	A."Dias" , 
	A."CodigoTipoTraspaso" , 
	A."IdCliente" , 
	A."IdCategoriaInversionista" , 
	A."Trabajador" , 
	A."DocumentoIdentidadDefecto" , 
	A."IdSocioNegocioCli" ,
	A."IdPersonaCli" , 
	A."NombreCompleto" , 
	A."IdPersona" ,
	A."IdSocioNegocioEMP" , 
	A."RazonSocial" , 
	A."IdEmpleador" , 
	A."CodigoEntidad" , 
	A."CodigoTipoAhorro" , 
	A."NombreEntidad" ,
	A."DocumentoEntidad" , 
	A."Descripcion" , 
	A."IdEstado" ,
	A."EstadoNombre" ,
	A."IdPerfilComercialBaseOrigen" ,
	A."MontoTotalTraspaso" , 
	A."UFTotalTraspaso" , 
	A."PorcentajeTotalTraspaso" ,
	A."MontoPesoDetalle",
	A."MontoUFDetalle",
	A."PorcentajeDetalle" ,
	A."MontoOrdenTraspaso" , 
	A."IdMatrizAgrupador" , 
	A."EsADC",
	CASE WHEN  A."TipoMovimiento"='EGRESO'
		THEN D."NombreFondo" 
		ELSE G."NombreFondo"
	END AS "RazonSocialNemo",
	CASE WHEN  A."TipoMovimiento"='EGRESO'
		THEN C."CodigoSerie" 
		ELSE F."CodigoSerie"
	END	AS "CodigoSerie",
	CASE WHEN  A."TipoMovimiento"='EGRESO'
		THEN C."IdSerieFondo"
	ELSE E."Nemotecnico"
	END AS "Nemotecnico",
	B."MontoTraspaso" AS "MontoPesoDistribucion",
	B."UFTraspaso" AS "MontoUFDistribucion",
	CASE WHEN COALESCE(B."PorcentajeTraspaso",0) = 0 AND A."TipoMovimiento"='EGRESO'
         THEN CASE WHEN COALESCE(B."MontoTraspaso",0) = 0
        	THEN COALESCE(ROUND((B."UFTraspaso" * 100)/NULLIF(A."UFTotalTraspaso",0),4),0)
        	ELSE COALESCE(ROUND((B."MontoTraspaso" * 100)/NULLIF(A."MontoTotalTraspaso",0),4),0)
        END
    	ELSE COALESCE(E."Porcentaje",0) 
    END AS "PorcentajeDistribucion",
	B."CodigoTipoAhorro" AS "TipoAhorro",
	COALESCE(B."Fondo",'') AS "FondoOrigen",
	A."Canal",
	A."DescripcionCanal"
	FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" AS A
	LEFT OUTER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS B
		ON B."IdTraspaso" = A."IdTraspaso"
	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS C
		ON B."Fondo" = C."IdSerieFondo" OR B."IdInstrumento" = C."IdInstrumento"
	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS D
		ON C."IdFondo" = D."IdFondo"
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.DistribucionInversion"  AS E
		ON E."IdContratoSAAPV"= A."IdContratoSAAPV"	
	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS F
	ON E."Nemotecnico" = F."IdSerieFondo" OR E."IdInstrumento" = F."IdInstrumento"
	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS G
	ON F."IdFondo" = G."IdFondo";
GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPVDistribuido" TO  BCS_DESARROLLADOR WITH GRANT OPTION;