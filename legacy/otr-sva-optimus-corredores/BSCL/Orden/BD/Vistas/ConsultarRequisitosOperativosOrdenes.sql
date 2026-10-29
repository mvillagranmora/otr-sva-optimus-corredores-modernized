SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarRequisitosOperativosOrdenes";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarRequisitosOperativosOrdenes" AS
SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTSIM' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCUSTODIA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoCustodia",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORTBLOQ' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PortafolioBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGENFUT' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoFuturos",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTVENCOR' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoVentaCorta",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTMERCANTIL' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoMercantil",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CNOCRV'  AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CompraNominalRV",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CNOCRF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CompraNominalRFIF",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'VNOCRV' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_VentaNominalRV",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'VNOCRF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_VentaNominalRFIF",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONORDVERESC' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ConfirmacionOrdenVerbalEscrita",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGRALES' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoCondicionesGenerales",
		
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CLIENTEBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ClienteBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PERSONABLOQUEADA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PersonaBloqueada",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERACIONSINFLUJOCUSTODIA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperacionSinFlujoCustodia",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOSINPRODUCTO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoSinProductoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOSINRUTEO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoSinRuteoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOPORTAFOLIOBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoPortafolioBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONTOOPERACIONSUPERAPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoOperacionSuperaPermitidoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COMISIONSUPERAMAXIMOPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ComisionSuperaMaximoPermitido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COMISIONBAJOMINIMOPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ComisionBajoMinimoPermitido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONTOSUPERAMAXIMOCANALDISTRIBUCION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoSuperaMaximoPermitidoCanalDistribucion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ORDENREQUIEREAUTORIZACION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OrdenRequiereAutorizacion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'DESCUENTOCOMISIONSUPERAPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_DescuentoComisionSuperaPermitidoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'FOTOCOPIACARNET' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FotocopiaCarnet",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ANTECEDENTESLEGALES' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_AntecedentesLegales",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'DIFERENCIAPRECIOORDEN' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_DiferenciaPrecioOrden",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" =	'FLUJOCUSTODIAINSUFICIENTE' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FlujoCantidadCustodiaInsuficiente",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COBROMAXPER' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CobroMaxPermitido",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORCENTAJEMAXORDEN' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PorcentajeMaxOrden",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORCENTAJEVISIBLIDAD' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PorcentajeVisibilidad"
		
FROM (

	SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
	FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
	WHERE "IdTipoEtapa" = 'INGRESOORDEN'
) 

GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarRequisitosOperativosOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
