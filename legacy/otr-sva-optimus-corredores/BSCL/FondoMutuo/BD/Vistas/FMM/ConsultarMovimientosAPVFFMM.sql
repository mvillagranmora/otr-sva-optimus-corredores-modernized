SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMovimientosAPVFFMM";
GO

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMovimientosAPVFFMM" AS
SELECT 
"IdEmpresa"
,"IdCliente"
,"DocumentoIdentidad"
,"FechaCajaMovimiento"
,"FondoMovimiento"
,"MontoCuotasMovimiento"
,"TipoMoneda"
,"MontoPesosMovimiento"
,"MontoCambio"
,"NumCuotasMovimiento"
,"PeriodoMovimiento"
,"RutEmpleadorMovimiento"
,"TipoCuentaMovimiento"
,"TipoMovimiento"
,"ValCuotaMovimiento"
,"Regimen"
,"SerieFondo"
,"CodigoFondo"
,"TipoCuenta"
,"Estado"
,"Tipo"
,"IdMatrizAgrupador"
,"EsADC"
,"IdSerieFondo"
,"TipoCambioMoneda"
,"Origen"
,"FechaValorCuota"
FROM(
SELECT
    SolicitudAporteRescate."IdEmpresa"					AS "IdEmpresa"
   ,SolicitudAporteRescate."IdCliente"					AS "IdCliente"
   ,DocumentoIdentidadCliente."DocumentoIdentidad"		AS "DocumentoIdentidad"										
   ,TO_NVARCHAR(SolicitudAporteRescate."FechaCertificacion",'YYYY-MM-DD')			AS "FechaCajaMovimiento"
   ,Fondo."NombreFondo"	|| ' ' || SerieDeFondo."CodigoSerie"	AS "FondoMovimiento"
   ,SolicitudAporteRescate."Monto"						AS "MontoCuotasMovimiento"
   --,SolicitudAporteRescate."Monto"						AS "MontoPesosMovimiento"
   ,SolicitudAporteRescate."IdMoneda"					AS "TipoMoneda"
   ,CASE WHEN SolicitudAporteRescate."IdOperacionSpot" > 0 THEN Spot."MontoMonedaSecundaria" ELSE SolicitudAporteRescate."MontoMonedaSistema" END AS "MontoPesosMovimiento"
   ,CASE WHEN SolicitudAporteRescate."IdMoneda" = PARAMETROS."Valor" THEN TO_DECIMAL(1,26,8) 
    	  WHEN COALESCE(Spot."TipoCambioCierre", TO_DECIMAL(0,26,8)) != 0 THEN Spot."TipoCambioCierre"
    	  WHEN COALESCE(Spot."TipoCambioCierre", TO_DECIMAL(0,26,8)) = 0 AND SolicitudAporteRescate."Monto" != 0 THEN ROUND(SolicitudAporteRescate."MontoMonedaSistema"/SolicitudAporteRescate."Monto",4) 
    	  ELSE  TO_DECIMAL(0,26,8) END												AS "MontoCambio"
   ,CASE 
    	WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate" = 'APO' And COALESCE(EstadoTesoreriaSpot."IdEstado", 'ING') = 'ING' THEN (SolicitudAporteRescate."Monto" / (CASE WHEN SolicitudAporteRescate."Estado" != 'CRT' THEN ValorCuota."ValorCuota" ELSE SolicitudAporteRescate."ValorCuota" END))
    	WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate" = 'RES' And SolicitudAporteRescate."CalculoPorMontoCuota" = 'M' And EstadoTesoreriaSpot."IdEstado" = 'ING' THEN (SolicitudAporteRescate."Monto" / (CASE WHEN SolicitudAporteRescate."Estado" != 'CRT' THEN ValorCuota."ValorCuota" ELSE SolicitudAporteRescate."ValorCuota" END))
  		ELSE SolicitudAporteRescate."NumeroCuotas" 
  	END AS "NumCuotasMovimiento"
   ,TO_NVARCHAR(
   				TO_DATE(SolicitudAporteRescate."FechaCertificacion")
   				, 'YYYY-MM')							AS "PeriodoMovimiento"			
   ,DocumentoIdentidad."DocumentoIdentidad"				AS "RutEmpleadorMovimiento"
   ,SUBSTR_BEFORE(RegimenTributario."Codigo",'-')		AS "TipoCuentaMovimiento"
   ,TipoMovimientoFondoMutuo."Descripcion"				AS "TipoMovimiento"				
   ,SolicitudAporteRescate."ValorCuota"					AS "ValCuotaMovimiento"
   ,SolicitudAporteRescate."IdRegimenTributario"		AS "Regimen"
   ,SolicitudAporteRescate."IdSerieFondo"				AS "SerieFondo"
   ,SerieDeFondo."CodigoSerie"							AS "CodigoFondo" 				
   ,SolicitudAporteRescate."IdRegimenTributario"		AS "TipoCuenta"				
   ,CASE WHEN Portafolio."IdMatrizAgrupadorAPV" > 0 THEN
   		'APV'
    ELSE 'NOAPV'
    END 												AS "Origen"	
   ,SolicitudAporteRescate."Estado"						AS "Estado"
   ,CASE WHEN SolicitudAporteRescate."IdAgrupador" > 0 THEN
   		'REV'
    ELSE SolicitudAporteRescate."TipoSolicitud"
    END 												AS "Tipo",
    CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",	
    CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
    ,SolicitudAporteRescate."IdSerieFondo"				AS "IdSerieFondo"
    ,CASE WHEN SolicitudAporteRescate."IdMoneda" = PARAMETROS."Valor" THEN TO_DECIMAL(1,26,8) 
    	  WHEN COALESCE(Spot."TipoCambioCierre", TO_DECIMAL(0,26,8)) != 0 THEN Spot."TipoCambioCierre"
    	  WHEN COALESCE(Spot."TipoCambioCierre", TO_DECIMAL(0,26,8)) = 0 AND SolicitudAporteRescate."Monto" != 0 THEN ROUND(SolicitudAporteRescate."MontoMonedaSistema"/SolicitudAporteRescate."Monto",4) 
    	  ELSE  TO_DECIMAL(0,26,8) END AS "TipoCambioMoneda"
     ,TO_NVARCHAR(SolicitudAporteRescate."FechaValorCuota",'YYYY-MM-DD')			AS "FechaValorCuota"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 	AS SolicitudAporteRescate
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdCliente" = SolicitudAporteRescate."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaCliente ON PersonaCliente."IdPersona" = Cliente."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidadCliente ON DocumentoIdentidadCliente."IdDocumentoIdentidad" = PersonaCliente."IdDocumentoIdentidad" and DocumentoIdentidadCliente."Predeterminado" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = SerieDeFondo."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupador."IdRegimenTributario"
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV ON ContratoSAAPV."IdCliente" = SolicitudAporteRescate."IdCliente" AND ContratoSAAPV."FolioContratoSAAPV" = SolicitudAporteRescate."FolioContratoOperacion" AND SolicitudAporteRescate."TipoContratoOperacion" = 'SAAPV'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdEmpleador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"  and DocumentoIdentidadCliente."Predeterminado" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" PARAMETROS ON PARAMETROS."IdEmpresa" = SolicitudAporteRescate."IdEmpresa" AND PARAMETROS."CodigoParametro" = 'PLA.MonedaSistema'
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" Spot ON Spot."IdOperacionSpotOrigen"= SolicitudAporteRescate."IdOperacionSpot" AND Spot."Estado" != 'MOD'
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot ON (SolicitudAporteRescate."IdIngresoEgreso"=TesoreriaSpot."IdIngresoEgreso")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoTesoreriaSpot ON (EstadoTesoreriaSpot."IdEstado" =  TesoreriaSpot."IdEstado")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS ValorCuota ON ValorCuota."IdSerieFondo" = SerieDeFondo."IdSerieFondo"
WHERE SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" != 'APOREINV' OR (SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'APOREINV' AND SolicitudAporteRescate."IdAgrupador" = 0)
UNION ALL
SELECT
    SolicitudAporteRescate."IdEmpresa"					AS "IdEmpresa"
   ,SolicitudAporteRescate."IdCliente"					AS "IdCliente"
   ,DocumentoIdentidadCliente."DocumentoIdentidad"		AS "DocumentoIdentidad"										
   ,TO_NVARCHAR(SolicitudAporteRescate."FechaCertificacion",'YYYY-MM-DD')			AS "FechaCajaMovimiento"
   ,Fondo."NombreFondo"	|| ' ' || SerieDeFondo."CodigoSerie"	AS "FondoMovimiento"
   ,SolicitudAporteRescate."Monto"						AS "MontoCuotasMovimiento"
   --,SolicitudAporteRescate."Monto"						AS "MontoPesosMovimiento"
   ,SolicitudAporteRescate."IdMoneda"					AS "TipoMoneda"
   ,SolicitudAporteRescate."MontoMonedaSistema"			AS "MontoPesosMovimiento"
   ,CASE WHEN SolicitudAporteRescate."IdMoneda" = PARAMETROS."Valor" THEN TO_DECIMAL(1,26,8) 
    	  WHEN SolicitudAporteRescate."Monto" != 0 THEN ROUND(SolicitudAporteRescate."MontoMonedaSistema"/SolicitudAporteRescate."Monto",4) 
    	  ELSE  TO_DECIMAL(0,26,8) END 					AS "MontoCambio"
   ,SolicitudAporteRescate."NumeroCuotas" AS "NumCuotasMovimiento"
   ,TO_NVARCHAR(
   				TO_DATE(SolicitudAporteRescate."FechaCertificacion")
   				, 'YYYY-MM')							AS "PeriodoMovimiento"			
   ,DocumentoIdentidad."DocumentoIdentidad"				AS "RutEmpleadorMovimiento"
   ,SUBSTR_BEFORE(RegimenTributario."Codigo",'-')		AS "TipoCuentaMovimiento"
   ,TipoMovimientoFondoMutuo."Descripcion"				AS "TipoMovimiento"				
   ,SolicitudAporteRescate."ValorCuota"					AS "ValCuotaMovimiento"
   ,SolicitudAporteRescate."IdRegimenTributario"		AS "Regimen"
   ,SolicitudAporteRescate."IdSerieFondo"				AS "SerieFondo"
   ,SerieDeFondo."CodigoSerie"							AS "CodigoFondo" 				
   ,SolicitudAporteRescate."IdRegimenTributario"		AS "TipoCuenta"				
   ,CASE WHEN Portafolio."IdMatrizAgrupadorAPV" > 0 THEN
   		'APV'
    ELSE 'NOAPV'
    END 												AS "Origen"	
   ,SolicitudAporteRescate."Estado"						AS "Estado"
   ,CASE WHEN SolicitudAporteRescate."IdAgrupador" > 0 THEN
   		'REV'
    ELSE SolicitudAporteRescate."TipoSolicitud"
    END 												AS "Tipo",
    CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",	
    CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
    ,SolicitudAporteRescate."IdSerieFondo"				AS "IdSerieFondo"
    ,CASE WHEN SolicitudAporteRescate."IdMoneda" = PARAMETROS."Valor" THEN TO_DECIMAL(1,26,8) 
    	  WHEN SolicitudAporteRescate."Monto" != 0 THEN ROUND(SolicitudAporteRescate."MontoMonedaSistema"/SolicitudAporteRescate."Monto",4) 
    	  ELSE  TO_DECIMAL(0,26,8) END AS "TipoCambioMoneda"
    ,TO_NVARCHAR(SolicitudAporteRescate."FechaValorCuota",'YYYY-MM-DD')			AS "FechaValorCuota"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupado" 	AS SolicitudAporteRescate
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdCliente" = SolicitudAporteRescate."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersonaCliente ON PersonaCliente."IdPersona" = Cliente."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidadCliente ON DocumentoIdentidadCliente."IdDocumentoIdentidad" = PersonaCliente."IdDocumentoIdentidad" and DocumentoIdentidadCliente."Predeterminado" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = SerieDeFondo."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupador."IdRegimenTributario"
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV ON ContratoSAAPV."IdCliente" = SolicitudAporteRescate."IdCliente" AND ContratoSAAPV."FolioContratoSAAPV" = SolicitudAporteRescate."FolioContratoOperacion" AND SolicitudAporteRescate."TipoContratoOperacion" = 'SAAPV'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdEmpleador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"  and DocumentoIdentidad."Predeterminado" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" PARAMETROS ON PARAMETROS."IdEmpresa" = SolicitudAporteRescate."IdEmpresa" AND PARAMETROS."CodigoParametro" = 'PLA.MonedaSistema'
);
GO

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarMovimientosAPVFFMM" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
