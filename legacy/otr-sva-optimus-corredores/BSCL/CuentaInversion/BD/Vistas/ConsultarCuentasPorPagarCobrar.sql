SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar";
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" AS




SELECT   CAM."IdCargoAbonoMovimiento"
		,CAM."IdTipoMovimiento"
		,CAM."IdConcepto"
		,CTO."Nombre" AS "NombreConcepto"
		,CAM."IdEjecutivo"
		,EMP."NombreCompleto" AS "NombreEjecutivo"
		,EMP."DocumentoIdentidad" AS "DocumentoIdentidadAgente"
		,CAM."IdSucursal"
		,CAM."IdCuentaInversion"
		,CLI."NombreTipoPersona" AS "TipoCliente"		
		,CI."IdCliente"
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
		,CASE WHEN CLI."Tipo" = 'J' THEN CLI."RazonSocial"
			  WHEN CLI."Tipo" = 'N' THEN CLI."NombreCompleto"
		  END AS "NombreCliente"
		,CLI."RazonSocial"
		,CI."IdPortafolio"
		,POR."CodigoPortafolio"
		,POR."Proposito"
		,(POR."CodigoPortafolio" ||' - '||POR."Proposito") as "CodigoProposito"
		,CI."IdMoneda"
		,CI."CodigoMoneda"
		,CI."NombreMoneda"
		,(CI."CodigoMoneda" ||' - '||CI."NombreMoneda") as "CodigoNombreMoneda"
		,CAM."IdCondicionDeLiquidacion"
		,CAM."FechaEfectivaMovimiento"
		,CAM."Monto"
		,CAM."SaldoLibroInventario"
		,CASE WHEN DAYS_BETWEEN (TO_DATE(CAM."FechaEfectivaMovimiento", 'YYYY-MM-DD'), TO_DATE(CNF."Valor", 'YYYY-MM-DD')) < 0 THEN 0
			  WHEN CAM."SaldoLibroInventario" = 0 THEN 0
		      ELSE ABS(DAYS_BETWEEN (TO_DATE(CAM."FechaEfectivaMovimiento", 'YYYY-MM-DD'), TO_DATE(CNF."Valor", 'YYYY-MM-DD')))
	     END as "AntiguedadDeuda"
	     
	     
	    ,CAM."IdEstado"
	    
	    
	    ,CAM."SaldoPorFechaMovimiento"
	    ,CAM."FechaSistema"
		,CAM."EsCargoAbonoPorTraspaso"
		,CAM."IdModulo"
	    
 FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM
 INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CI    ON CI."IdCuentaInversion" = CAM."IdCuentaInversion"
 INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" as CNF    ON CNF."CodigoParametro" = 'PLA.FechaSistema' AND CNF."IdEmpresa" = CAM."IdEmpresa"
 INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" as CTO  ON CTO."IdConcepto" = CAM."IdConcepto"
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI   ON CLI."IdCliente" = CI."IdCliente"
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR   ON POR."IdPortafolio" = CI."IdPortafolio"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMP  ON EMP."IdEmpleado" = CAM."IdEjecutivo"
 

 
  		 
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
