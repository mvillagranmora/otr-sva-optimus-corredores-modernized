SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarResumenContrato"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarResumenContrato" AS
-- Resumen de Contrato
SELECT C."IdContratoComision"
	  ,C."IdTipoContrato"
	  ,C."Nombre"						AS "NombreContrato"
	  ,C."IdCliente"
	  ,C."IdPortafolio"
	  ,C."IdFormatoCobro"
	  ,C."IdMoneda"
	  ,C."IdEmpresa"
	  ,C."IdEstado"
	  ,C."IdClasificacionTipoInstrumento"
	  ,DC."IdDetalleContratoComision"
	  ,CL."CodigoCobroClasificacion"
	  ,DC."IdCobro"
	  ,DC."IdConcepto"
	  ,DC."EsPorRangos"
	  ,COALESCE(DC."MontoMinimo", 0)	AS "MontoMinimo"
	  ,COALESCE(DC."MontoMaximo", 0)	AS "MontoMaximo"
	  ,COALESCE(SDC."Monto", 0)			AS "MontoRango"
	  ,COALESCE(SDC."Porcentaje", 0)	AS "PorcentajeRango"
	  ,COALESCE(SDC."MontoMaximo", 0)	AS "MontoMaximoRango"
  FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS C
 	LEFT JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS DC ON ( DC."IdContratoComision" = C."IdContratoComision" )
  	LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON ( DC."IdDetalleContratoComision" = SDC."IdDetalleContratoComision" )
 	LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CO ON ( CO."IdCobro" = DC."IdCobro" )  
 	LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CL ON ( CL."IdCobroClasificacion" = CO."IdCobroClasificacion" )
 ORDER BY C."IdContratoComision";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarResumenContrato" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
