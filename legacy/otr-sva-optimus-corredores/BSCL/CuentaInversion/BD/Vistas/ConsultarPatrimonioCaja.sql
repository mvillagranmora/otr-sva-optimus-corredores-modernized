SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja" AS 	 
SELECT
CAJ."IdPatrimonioCaja"
,CAJ."IdCliente"
,CAJ."IdPortafolio"
,CAJ."FechaPatrimonio"
,CAJ."TipoFechaConsulta"
,TO_BIGINT(0) AS "IdInstrumento"
,'CAJA' AS "Nemotecnico"
,TO_DECIMAL(0,38,16) AS "CantidadPatrimonio"
,CAJ."MontoPatrimonio"
,CAJ."SaldoDisponible"
,CAJ."SaldoEnGarantia"
,CAJ."SaldoRetiro"
,CAJ."SaldoAPlazoSimultanea"
,CAJ."SaldoAPlazoVentaCorta"
,CAJ."SaldoAPlazoCompromiso"
,TO_DECIMAL(0,38,16) AS "PrecioMercadoCustodia"
,TO_DECIMAL(0,38,16) AS "ValorCuota"
,CAJ."IdMonedaSistema"
,CAJ."IdMoneda"
,'NA' AS "IdRegimenTributario"
,DOC."DocumentoIdentidad"
,CASE PER."Tipo" WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno" 
				ELSE PER."RazonSocial" END  as "NombreCompleto"
,CASE WHEN CASE WHEN PORT."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END = 'N' THEN 'NO APV' ELSE 'APV' END "TipoAhorro"
,COALESCE(PORT."IdMatrizAgrupadorAPV",0) AS "IdMatrizAgrupadorAPV"
,PORT."CodigoPortafolio"
,PORT."Proposito"
,COALESCE(MAT."NombrePlan",'NA') AS "PlanAPV"
,COALESCE(MAT."Producto",'NA') AS "CategoriaAPV"
,'NA' AS "Fondo"
,'NA' AS "Serie"
,'NA' AS "IdClasificacionTipoInstrumento"
,CASE WHEN CAJ."IdEstado" = 'ACT' THEN 'V' ELSE 'NV' END AS "Vigencia"
,'0001-01-01' as "FechaFuente"
,CAJ."MontoPatrimonioAfectoComision"
,CAJ."PorcentajeCobroComision"
,CAJ."MontoComision"
,CAJ."IdEstadoComision"
,COALESCE(ESTCOM."Nombre", CAJ."IdEstadoComision") AS "NombreEstadoComision"
FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" CAJ
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = CAJ."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAJ."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" MAT ON PORT."IdMatrizAgrupadorAPV" = MAT."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTCOM ON ESTCOM."IdEstado" = CAJ."IdEstadoComision";	


GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
