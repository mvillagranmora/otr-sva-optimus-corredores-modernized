SELECT  "InterfazAgf"."IdInterfazAgf",
"InterfazAgf"."TipoRegistro",
LPAD("InterfazAgf"."DocumentoIdentidad",11,' ') ||
LPAD("InterfazAgf"."CodigoPortafolio",2,' ') ||
LPAD("InterfazAgf"."FechaLiquidacion",10,' ') ||
LPAD("InterfazAgf"."FechaAceptacion",10,' ') ||
CASE "InterfazAgf"."TipoSolicitud" WHEN 'APO' THEN 'I'
                                   WHEN 'RES' THEN 'R'
                                   ELSE ''
END ||
'I' || --LPAD("InterfazAgf"."IdEstadoSolicitud",1,' ') ||
RPAD("SerieDeFondo"."IdSerieFondo",10,' ') ||
LPAD("SerieDeFondo"."CodigoSerie",3,' ') || --codigo ser
RPAD("InterfazAgf"."IdUsuario",4,' ') ||
LPAD(FLOOR ("InterfazAgf"."NumeroCuotas"),14,'0') ||
LPAD(substring(("InterfazAgf"."NumeroCuotas" - floor("InterfazAgf"."NumeroCuotas")),3,4),4,0) ||
LPAD(FLOOR ("InterfazAgf"."ValorCuota"),14,'0') ||
LPAD(substring(("InterfazAgf"."ValorCuota" - floor("InterfazAgf"."ValorCuota")),3,4),4,0) ||
LPAD(FLOOR ("InterfazAgf"."Monto"),14,'0') ||
LPAD(substring(("InterfazAgf"."Monto" - floor("InterfazAgf"."Monto")),3,4),4,0) ||
LPAD("InterfazAgf"."CalculoPorMontoCuota",1,' ') ||
RPAD(CASE "InterfazAgf"."IdMoneda" WHEN 'CLP' THEN '$' WHEN 'USD' THEN 'US$' WHEN 'EUR' THEN 'EU' ELSE '' END,3,' ') ||
LPAD(CASE WHEN "Solicitud"."IdRegimenTributario" IN ('APV-A','APV-B','APVC') THEN 'APV' 
		  WHEN "Solicitud"."IdRegimenTributario" IN ('CVO') THEN 'CVO'
		  WHEN "Solicitud"."IdRegimenTributario" IN ('DCO') THEN 'DCO'
		  WHEN "Solicitud"."IdRegimenTributario" IN ('57BIS') THEN '57B'
		  WHEN "Solicitud"."IdRegimenTributario" IN ('54BIS') THEN '54B'
		  WHEN "Solicitud"."IdRegimenTributario" IN ('NINGUNO','ART108','ART107') THEN 'FMU'
		  ELSE '' END,3,' ') ||
CASE "InterfazAgf"."CalculoPorMontoCuota" WHEN 'M' THEN 'P'
										  WHEN 'C' THEN 'P'
										  ELSE ''
END ||
LPAD('B',1,' ') || 	 --COM_RTE
LPAD('SIS',3,' ') || --ORIGEN
LPAD("InterfazAgf"."IdSolicitudAporteRescate",8,' ') || -- FOLIO GESTION
LPAD("InterfazAgf"."IdSerieFondoDestino",10,' ') ||
LPAD('',3,' ') || --CODIGO SER DES
LPAD('',8,'0') || --FOLIO GESTION REINVERSION
LPAD('',3,' ') || --TRASPTRANS
LPAD('',3,' ') || --CALIDAD PARTICIPE
RPAD('',3,' ') || --CAMPOS FILLER EN BLANCO PARA HACER CALZAR EL REGISTRO
RPAD('0-0',11,' ') || --RUT AUT
LPAD("InterfazAgf"."IdEstadoSerieFondo",1,' ') ||
LPAD("InterfazAgf"."IndicadorMLT",1,' ') ||
LPAD('',3,' ') || --ORIGEN GES
LPAD('',3,' ') --MONEDA ORIGEN
AS "Registro"
FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS "InterfazAgf"
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "InterfazAgf"."IdSerieFondo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud" ON ("Solicitud"."IdSolicitudAporteRescate" = "InterfazAgf"."IdSolicitudAporteRescate")
WHERE "InterfazAgf"."IdEmpresa" = :IdEmpresa
AND "InterfazAgf"."IdEstado" = 'PEN'
AND "InterfazAgf"."TipoRegistro" = 'AR'
		UNION ALL
SELECT  INTERFAZ."IdInterfazAgf",
INTERFAZ."TipoRegistro",
LPAD(INTERFAZ."DocumentoIdentidad",11,' ') ||
LPAD(INTERFAZ."CodigoPortafolio",2,' ') ||
RPAD(INTERFAZ."NombreCompleto",62,' ') || 
RPAD(COALESCE(DIRECCION_FISICA."NombreLargo",''),50,' ') || 
RPAD('',4,' ') || -- COMPAR
RPAD(COALESCE(TELEFONO."Numero",''),20,' ') ||
RPAD(COALESCE(DIRECCION_FISICA."NombreLargo",''),50,' ') ||
RPAD('',4,' ') || -- COMCOM
RPAD(COALESCE(TELEFONO."Numero",''),20,' ') ||
RPAD('',20,' ') || --CASPOS
RPAD('',20,' ') || --CORPOS
RPAD('',4,' ') || --COMPOS
RPAD(COALESCE(DIRECCION_ELECTRONICA."Direccion",''),50,' ') ||
RPAD(COALESCE(CLIENTE."Tipo",''),1,' ') || --TIPPAR
RPAD(COALESCE(CLIENTE."Sexo",''),1,' ') || --SEXO
RPAD('',1,' ') || --ESTCIV
RPAD('',1,' ') || --REGCON
RPAD('',1,' ') || --ENVCOR
RPAD('',1,' ') || --NACION
RPAD('',4,' ') || --CODUSU
RPAD('',50,' ') || --EMPLEA
RPAD('',10,' ') || --CODBCO
RPAD('',20,' ') || --CTACTE
RPAD(COALESCE(TO_CHAR(CLIENTE."FechaIngreso",'YYYY-MM-DD'),''),10,' ') --FECING
AS "Registro"
FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" INTERFAZ
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = INTERFAZ."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = CLIENTE."IdDireccionFisicaPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELEFONO ON (TELEFONO."IdTelefono" = CLIENTE."IdTelefonoPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIRECCION_ELECTRONICA ON (DIRECCION_ELECTRONICA."IdDireccionElectronica" = CLIENTE."IdDireccionElectronicaPrincipal")
WHERE INTERFAZ."IdEmpresa" = :IdEmpresa
AND INTERFAZ."IdEstado" = 'PEN'
AND INTERFAZ."TipoRegistro" = 'PA';