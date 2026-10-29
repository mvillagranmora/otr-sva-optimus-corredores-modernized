SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja"
AS 
SELECT
	  EvCli."IdEventoCapitalCliente",
	  EvCli."IdEventoCapital",
	  EvCli."IdTipoEventoCapital",
	  EvCli."Secuencia",
	  EvCli."NumeroDividendo",
	  EvCli."IdSocioNegocio",
	  EvCli."TipoSocioNegocio",
	  EvCli."IdPortafolio",
	  EvCli."IdTipoDocumentoIdentidad",
	  EvCli."DocumentoIdentidad",
	  EvCli."NombreCliente",
	  EvCli."IdClasificacionTipoInstrumento",
	  EvCli."IdTipoInstrumento",
	  EvCli."IdInstrumento",
	  EvCli."Nemo",
	  EvCli."IdInstrumentoNuevo",
	  EvCli."NemoNuevo",
	  EvCli."FechaCierre",
	  EvCli."FechaPago",
	  EvCli."CantidadDerecho",
	  EvCli."TipoCartera" AS "IdTipoCartera",
	  CASE EvCli."TipoCartera"    
	    WHEN 'CUS'  THEN 'CUSTODIA'
	    WHEN 'GAR'  THEN 'GARANTIAS' 
	    WHEN 'SIM'  THEN 'SIMULTANEAS'
	    WHEN 'EMP'  THEN 'EMPRESTITO'
	    ELSE 'PRESTATARIO'
	  END  AS "TipoCartera",
	  EvCli."CantidadNueva",
	  EvCli."MontoCapital",
	  EvCli."Precio",
	  EvCli."PrecioNuevo",
	  EvCli."MontoInteres",
	  EvCli."MontoReajuste",
	  EvCli."Total",
	  EvCli."IdMoneda",
	  EvCli."SecuenciaCupon",
	  EvCli."CodigoUsuarioAgente",
	  EvCli."NombreAgente",
	  EvCli."CodigoSucursal",
	  EvCli."FechaHoraIngresoRegistro",
	  EvCli."FechaHoraUltimaActualizacion",
	  EvCli."IdUsuario",
	  EvCli."IdEstado",
	  EvCptal."Factor",
	  EvCptal."Valor",
	  EvCptal."Descripcion"  AS "DescripcionEvento",
	  EvCptal."IdEmpresa",
	  EvCli."IdBoveda"   AS "IdBoveda"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  AS EvCli
  INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EvCptal
    ON EvCli."IdEventoCapital" = EvCptal."IdEventoCapital";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja" TO BCS_DESARROLLADOR WITH GRANT OPTION;
