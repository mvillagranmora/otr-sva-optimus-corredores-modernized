SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos" (
    "ID",
    "IdDocumento",
    --"VboUsuario",
    --"Observaciones",
    --"FuenteDatos",
    ----
    "Numero",
    "IdMoneda",
    "RutCliente",
    "IdCliente",
    "ActivoPensionado",
    "RutEmpleador",
    --"IdEmpleador",
    "FechaOperacion",
    "DepositoEmpleador",--uf
    ---Aporte Empleador
    "AporteEmpleador",--uf
    "TraspasoEmpleador",--uf
    "RetiroEmpleadorActualizado",--($)
    "MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    "AhorrosModalidadIndirectaEmpleador",
    "AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    "CotizacionesModalidadIndirectaEmpleador",--uf
    "CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    "CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    "RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    "MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    "NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    "NumeroCertificadoEmpleador",
    "EjecutadoComplementoMontos",
	"EjecutadoComplementoIPC"
) AS

SELECT 
    TO_BIGINT("ID") AS "ID",
    TO_BIGINT("IdDocumento") AS "IdDocumento",
    --MAX("VboUsuario") AS "VboUsuario",
    --MAX("Observaciones") AS "Observaciones",
    --MAX("FuenteDatos") AS "FuenteDatos",
    ----
    ----
    --TO_BIGINT(MAX("Numero")) AS "Numero",CAST("RutCliente" AS NVARCHAR(40)) AS "RutCliente",
    0 AS "Numero", 
    '' AS "IdMoneda",
    "RutCliente",
    TO_BIGINT("IdCliente") AS "IdCliente",
    CAST("ActivoPensionado" AS CHAR(1)) AS "ActivoPensionado",
    COALESCE(CAST("RutEmpleador" AS NVARCHAR(40)),'') AS "RutEmpleador",
    TO_DATE("FechaOperacion") AS "FechaOperacion",
    TO_DECIMAL("DepositoEmpleador",26,8) AS "DepositoEmpleador",
    --AHORRO PREVISIONAL VOLUNTARIO COLECTIVO
    --Aporte Empleador
    TO_DECIMAL("AporteEmpleador",26,8) AS "AporteEmpleador",
    TO_DECIMAL("TraspasoEmpleador",26,8) AS "TraspasoEmpleador",
    TO_DECIMAL("RetiroEmpleadorActualizado",26,8) AS "RetiroEmpleadorActualizado",
    TO_DECIMAL("MontoRetencionActualizado",26,8) AS "MontoRetencionActualizado",
    --Ahorros del Trabajador acogidos al inciso primero del Art. 42 Bis de la LIR en UF
    TO_DECIMAL("AhorrosModalidadIndirectaEmpleador",26,8) AS "AhorrosModalidadIndirectaEmpleador",
    TO_DECIMAL("AhorrosModalidadDirectaTrabajador",26,8) AS "AhorrosModalidadDirectaTrabajador",
    --DEPÓSITOS DE AHORRO PREVISIONAL VOLUNTARIO (DAPV) O COTIZACIONES VOLUNTARIAS TRABAJADORES DEPENDIENTES O INDEPENDIENTES
    --ACOGIDOS AL BENEFICIO TRIBUTARIO DEL INCISO PRIMERO DEL ART. 42 BIS O INCISO TERCERO DEL ART. 50 DE LA LIR
    --DAPV o Cotizaciones Voluntarias enterados en calidad de Trabajador Dependiente del Art. 42° N°1 de la L.I.R. en UF
    TO_DECIMAL("CotizacionesModalidadIndirectaEmpleador",26,8) AS "CotizacionesModalidadIndirectaEmpleador",
    TO_DECIMAL("CotizacionesModalidadDirectaTrabajador",26,8) AS "CotizacionesModalidadDirectaTrabajador",
    --DAPV o Cotizaciones Voluntarias enteradas en calidad de trabajador independiente del Art. 42 N°2 de la L.I.R. en UF
    TO_DECIMAL("CotizacionesTrabajadorIndependiente",26,8) AS "CotizacionesTrabajadorIndependiente",
    --MONTO DE LOS RETIROS EFECTUADOS CON CARGO A LOS AHORROS PREVISIONALES VOLUNTARIOS REALIZADOS
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero del Art. 42 bis de la L.I.R y/o
    --retiros del trabajador efectuados con cargo a los aportes del empleador, actualizados en $
    --TO_DECIMAL("MontoRetiroPorAhorrosIncisoPrimero",26,8) AS "MontoRetiroPorAhorrosIncisoPrimero",
    TO_DECIMAL("MontoRetiroPorAhorrosIncisoPrimero",26,8) AS "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    TO_DECIMAL("RetencionImpuestoSobreRetiros",26,8) AS "RetencionImpuestoSobreRetiros",
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    TO_DECIMAL("MontoRetiroPorAhorrosIncisoSegundo",26,8) AS "MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    TO_BIGINT("NumeroCertificadoTrabajador") AS "NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    TO_BIGINT("NumeroCertificadoEmpleador") AS "NumeroCertificadoEmpleador",
    0 AS "EjecutadoComplementoMontos",
	0 AS "EjecutadoComplementoIPC"
FROM (
    SELECT
        MAX(d."ID") AS "ID",
        d."IdDocumento",
        d."VboUsuario",
        d."Observaciones",
        MAX(d."FuenteDatos") AS "FuenteDatos",
        ----
        --d."Numero",
        --d."IdMoneda",
        d."RutCliente",
        MAX(d."IdCliente") AS "IdCliente",
        MAX(d."ActivoPensionado") AS "ActivoPensionado",
        d."RutEmpleador",
        --d."IdEmpleador",
        MAX(YEAR(d."FechaOperacion")||'-12-31') AS "FechaOperacion",
        COALESCE(SUM(d."DepositoEmpleador"),0) AS "DepositoEmpleador",--uf
        ---Aporte Empleador
        COALESCE(SUM(d."AporteEmpleador"),0) AS "AporteEmpleador",--uf
        COALESCE(SUM(d."TraspasoEmpleador"),0) AS "TraspasoEmpleador",--uf
        COALESCE(SUM(d."RetiroEmpleadorActualizado"),0) AS "RetiroEmpleadorActualizado",--($)
        COALESCE(SUM(d."MontoRetencionActualizado"),0) AS "MontoRetencionActualizado",--15%($)
        --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
        COALESCE(SUM(d."AhorrosModalidadIndirectaEmpleador"),0) AS "AhorrosModalidadIndirectaEmpleador",
        COALESCE(SUM(d."AhorrosModalidadDirectaTrabajador"),0) AS "AhorrosModalidadDirectaTrabajador",
        --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
        --del ART. 42 Nª1 de la LIR en UF
        COALESCE(SUM(d."CotizacionesModalidadIndirectaEmpleador"),0) AS "CotizacionesModalidadIndirectaEmpleador",--uf
        COALESCE(SUM(d."CotizacionesModalidadDirectaTrabajador"),0) AS "CotizacionesModalidadDirectaTrabajador",--uf
        --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
        --del Art.42 Nª2 de la LIR en UF
        COALESCE(SUM(d."CotizacionesTrabajadorIndependiente"),0) AS "CotizacionesTrabajadorIndependiente",--uf
        --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
        --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
        --los aportes del empleador, actualizados en $
        COALESCE(SUM(d."MontoRetiroPorAhorrosIncisoPrimero"),0) AS "MontoRetiroPorAhorrosIncisoPrimero",
        --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
        COALESCE(SUM(d."RetencionImpuestoSobreRetiros"),0) AS "RetencionImpuestoSobreRetiros",--15%($)
        --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
        COALESCE(SUM(d."MontoRetiroPorAhorrosIncisoSegundo"),0) AS "MontoRetiroPorAhorrosIncisoSegundo",
        --N° CERTIFICADO TRABAJADOR
        MAX(d."NumeroCertificadoTrabajador") AS "NumeroCertificadoTrabajador",
        --N° CERTIFICADO EMPLEADO
        MAX(d."NumeroCertificadoEmpleador") "NumeroCertificadoEmpleador"
    FROM (
        SELECT
            dj."ID" AS "ID",
            dj."IdDocumento",
            dj."VboUsuario",
            dj."Observaciones",
            dj."FuenteDatos" AS "FuenteDatos",
            dj."RutCliente",
            dj."IdCliente" AS "IdCliente",
            (SELECT "ActivoPensionado" FROM (SELECT djaux."ActivoPensionado" , ROW_NUMBER () OVER (PARTITION BY djaux."RutCliente" 
                ORDER BY djaux."RutCliente" DESC,djaux."ActivoPensionado" DESC, djaux."FechaOperacion" DESC) as RN 
			      FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" djaux
				  WHERE djaux."RutCliente" = dj."RutCliente"
                  AND  YEAR(djaux."FechaOperacion")=YEAR(dj."FechaOperacion")) 
			  WHERE rn =1) AS "ActivoPensionado",
			 CASE
			 	 WHEN (SELECT "IdEstado" FROM (SELECT CON."IdEstado"
			      FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON 
				  WHERE CON."DocumentoIdentidadCliente" = dj."RutCliente"
				  	AND CON."DocumentoIdentidadEmpleador" = dj."RutEmpleador"
				    AND (SELECT "FolioContratoOperacion" FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL WHERE SOL."IdSolicitudAporteRescate" = dj."Numero") = CON."FolioContratoSAAPV"
					)) = 'VIG' THEN
							  		dj."RutEmpleador"
								ELSE
								    (SELECT "RutEmpleador" FROM (SELECT djaux."RutEmpleador" , ROW_NUMBER () OVER (PARTITION BY djaux."RutCliente" ORDER BY djaux."RutCliente" desc) as RN 
									      FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" djaux 
									      INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOL ON SOL."IdSolicitudAporteRescate" = djaux."Numero"
										  INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON ON CON."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
										  WHERE djaux."RutCliente" = dj."RutCliente"
											AND djaux."IdDocumento" = dj."IdDocumento"
											AND CON."IdEstado" = 'VIG') 
									  WHERE rn =1) 
			END AS "RutEmpleador",
            "FechaOperacion" AS "FechaOperacion",
            COALESCE(dj."DepositoEmpleador",0) AS "DepositoEmpleador",--uf
            COALESCE(dj."AporteEmpleador",0) AS "AporteEmpleador",--uf
            COALESCE(dj."TraspasoEmpleador",0) AS "TraspasoEmpleador",--uf
            COALESCE(dj."RetiroEmpleadorActualizado",0) AS "RetiroEmpleadorActualizado",--($)
            COALESCE(dj."MontoRetencionActualizado",0) AS "MontoRetencionActualizado",--15%($)
            COALESCE(dj."AhorrosModalidadIndirectaEmpleador" ,0) AS "AhorrosModalidadIndirectaEmpleador",
            COALESCE(dj."AhorrosModalidadDirectaTrabajador",0) AS "AhorrosModalidadDirectaTrabajador",
            COALESCE(dj."CotizacionesModalidadIndirectaEmpleador",0) AS "CotizacionesModalidadIndirectaEmpleador",--uf
            COALESCE(dj."CotizacionesModalidadDirectaTrabajador",0) AS "CotizacionesModalidadDirectaTrabajador",--uf
            COALESCE(dj."CotizacionesTrabajadorIndependiente",0) AS "CotizacionesTrabajadorIndependiente",--uf
            COALESCE(dj."MontoRetiroPorAhorrosIncisoPrimero",0) AS "MontoRetiroPorAhorrosIncisoPrimero",
            COALESCE(dj."RetencionImpuestoSobreRetiros",0) AS "RetencionImpuestoSobreRetiros",--15%($)
            COALESCE(dj."MontoRetiroPorAhorrosIncisoSegundo",0) AS "MontoRetiroPorAhorrosIncisoSegundo",
            dj."NumeroCertificadoTrabajador" AS "NumeroCertificadoTrabajador",
            dj."NumeroCertificadoEmpleador" "NumeroCertificadoEmpleador"
        FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos" dj
    ) AS d
    GROUP BY 
        d."IdDocumento",
        d."VboUsuario",
        d."Observaciones",
        d."RutCliente",
        d."ActivoPensionado",
        d."RutEmpleador",
        YEAR(d."FechaOperacion")
   ORDER BY d."RutCliente",d."ActivoPensionado", d."RutEmpleador"
)
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos" to bcs_desarrollador with grant option;