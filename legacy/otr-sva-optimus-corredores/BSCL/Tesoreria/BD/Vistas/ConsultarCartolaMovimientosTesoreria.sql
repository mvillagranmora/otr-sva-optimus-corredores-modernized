SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria" AS
 SELECT  VCFP."IdComprobanteFormaDePago"
		,VCFP."IdComprobante"
		,VCFP."IdTipoMovimiento"
		,VCFP."NombreTipoMovimiento"
		,VCFP."IdConcepto"
		,VCFP."NombreConcepto"	
		,VCFP."IdMedioDePago"
		,VCFP."NombreMedioDePago"
		,VCFP."IdModalidadDePago"
		,VCFP."NombreModalidadDePago"
		,VCFP."IdMoneda"
		,VCFP."NombreMoneda"
		,VCFP."Monto"
		,CASE WHEN VCFP."IdTipoMovimiento" = 'MOVING' THEN VCFP."Monto" ELSE  VCFP."Monto" * -1 END AS "MontoConSigno"
		,CASE WHEN VCFP."IdTipoMovimiento" = 'MOVING' THEN VCFP."Monto" ELSE 0 END AS "MontoIngreso"
		,CASE WHEN VCFP."IdTipoMovimiento" = 'MOVEGR' THEN VCFP."Monto" ELSE 0 END AS "MontoEgreso"
		,CASE WHEN VCFP."IdEstado" = 'ANU' THEN 0 ELSE VCFP."SaldoDisponible" END AS "SaldoDisponible"
		,CASE WHEN VCFP."IdEstado" = 'ANU' THEN 0 ELSE VCFP."SaldoContable" END AS "SaldoContable"
		,VCFP."IdEstado"
		,VCFP."NombreEstado"
		--INFO CORREDORA
		,CORR."IdPersona" 								AS "IdPersonaCorredora"
		,CAST(CORR."NombreCompleto" as NVARCHAR(300))	AS "NombreCorredora"
		,CAST(CORR."TipoPersona" as NVARCHAR(3))		AS "TipoPersonaCorredora"
		,CORR."DocumentoIdentidadDefecto"				AS "DocumentoIdentidadCorredora"					
		,VCFP."IdCuentaBancariaCorredora"
		,VCFP."IdBancoCorredora"
		,VCFP."NombreBancoCorredora"
		,VCFP."NombreTipoCuentaBancariaCorredora"
		,VCFP."NombreCuentaBancariaCorredora"
		,VCFP."NumeroCuentaBancariaCorredora"
		,VCFP."IdChequera"
		,VCFP."NumeroSerieCorredora"
		,VCFP."NumeroDocumentoCorredora"
		,VCFP."FechaDocumentoPagoCorredora"					
		
		--INFO SOCIO DE NEGOCIO
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN  0 ELSE VCFP."IdPersona" END  AS "IdPersonaSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN '' ELSE VCFP."NombrePersona" END AS "NombreSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN '' ELSE CAST(VCFP."NombreTipoSocioDeNegocio" as NVARCHAR(3)) END AS "TipoPersonaSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN '' ELSE VCFP."DocumentoIdentidad" END AS "DocumentoIdentidadSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN  0 ELSE VCFP."IdPersonaTitularPago" END AS "IdPersonaRelacionadoSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN  0 ELSE VCFP."IdPortafolioTitularPago" END AS "IdPortafolioRelacionadoSocio"
		,CASE WHEN VCFP."IdConcepto" IN ('PAREGR','PARING') THEN '' ELSE CAST(VCFP."NombrePortafolioTitularPago" as NVARCHAR(300)) END AS "NombrePortafolioRelacionadoSocio"					
		,VCFP."IdCuentaBancariaSocio"
		,VCFP."IdBancoSocio"
		,VCFP."NombreBancoSocio"
		,VCFP."NombreTipoCuentaBancariaSocio"
		,VCFP."NombreCuentaBancariaSocio"
		,VCFP."NumeroCuentaBancariaSocio"
		,VCFP."NumeroSerieSocio"
		,VCFP."NumeroDocumentoSocio"
		,VCFP."FechaDocumentoPagoSocio"
		
		--INFO ADICIONAL
		,VCFP."FechaMovimiento"
		,VCFP."FechaHoraMovimiento"
		,VCFP."FechaAfectaSaldo"
		,VCFP."FechaHoraAfectaSaldo"
		,VCFP."FechaLiquidacion"
		,VCFP."FechaHoraLiquidacion"
		
		,VCFP."FechaGiro"
		,VCFP."IdComprobantePartida"		
		,EMP."IdEmpresa"
		
   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
   LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as EMP ON EMP."IdEmpresa" = VCFP."IdEmpresa"
   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" as CORR ON CORR."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
  --WHERE COALESCE(VCFP."IdComprobantePartida",0) > 0
  --ORDER BY VCFP."IdCuentaBancariaCorredora", VCFP."FechaContable", VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago" asc
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


